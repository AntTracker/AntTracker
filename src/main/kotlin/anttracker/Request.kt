/* Revision History:
Rev. 1 - 2024/07/02 Original by A. Kim
Rev. 2 - 2024/07/09 by M. Baker
Rev. 3 - 2024/07/16 by M. Baker
-------------------------------------------------------------------------------
The Request module contains all exported classes and functions pertaining to
    the creation or selection of request entities.
-------------------------------------------------------------------------------
*/
package anttracker.request

import anttracker.contact.enterContactInformation
import anttracker.contact.selectContact
import anttracker.db.*
import anttracker.issues.*
import anttracker.product.selectProduct
import anttracker.PageOf
import anttracker.release.selectRelease
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// ----------------------------------------------------------------

// a page of issues
private class PageOfIssues(
    private val productName: String
) : PageOf<Issue>(Issue) {
    init {
        initLastPageNum()
    }

    // ----------------------------------------------------------------

    override fun printRecord(
        record: Issue, // in
    ) {
        displayIssue(record)
    }

    // ----------------------------------------------------------------

    // In SQL this would be:
    //  SELECT * FROM issue
    //  WHERE product = productName
    //  ORDER BY id, creationDate DESC
    override fun getQuery(): Query =
        (Issues innerJoin Products)
            .select(Issues.columns)
            .where { Products.name eq productName }
            .orderBy(
                Issues.id to SortOrder.DESC,
                Issues.creationDate to SortOrder.DESC,
            )
}

// ----------------------------------------------------------------

private fun displayIssue(
    issue: Issue // in
) {
    val id = "${issue.id}".padStart(4)
    val desc = issue.description.padEnd(30)
    val priority = "${issue.priority}".padStart(9)
    val status = "${issue.status}".padEnd(14)
    val antrel = transaction {
        issue.anticipatedRelease.releaseId.padEnd(8)
    }
    val created = issue.creationDate.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
    println("| $id | $desc | $priority | $status | $antrel | $created")
}

// ----------------------------------------------------------------

private fun displayIssueColumnTitles(displayLinenum: Boolean) {
    val linenum = "##".padEnd(3)
    val id = "ID".padStart(4)
    val desc = "Description".padEnd(30)
    val priority = "Priority".padStart(9)
    val status = "Status".padEnd(14)
    val antrel = "AntRel".padEnd(8)
    val created = "Created".padEnd(10)

    if (displayLinenum) {
        println("$linenum | $id | $desc | $priority | $status | $antrel | $created")
    } else {
        println("| $id | $desc | $priority | $status | $antrel | $created")
    }
}

// ----------------------------------------------------------------

/* Selects an issue from the db */
private fun selectIssue(
    productName: String // in
): Issue? {
    // prepare page of issues
    val issuePage = PageOfIssues(productName)
    issuePage.loadRecords() // retrieve first page of records

    // selected line number by the user
    var linenum: Int? = null

    while (linenum == null) {
        // display page
        displayIssueColumnTitles(true)
        issuePage.display()

        // prompt user for input
        print("Please select issue. ` to abort: ")

        // switch on user selection
        when (val selection = readln()) {
            // abort early - don't select an issue
            "`" -> return null

            // go to next page
            "" -> {
                if (!issuePage.lastPage()) {
                    issuePage.loadNextPage()
                } else {
                    println("No more pages to display.")
                }
            }

            // else: check for line # selection
            else -> {
                try {
                    // will throw java.lang.NumberFormatException if not int
                    val userInputInt = selection.toInt()

                    // validate integer selection
                    if (userInputInt in (0..20) && userInputInt < issuePage.recordsSize()) {
                        linenum = userInputInt
                        // loop ends here (check loop condition)
                    }
                } catch (e: java.lang.NumberFormatException) {
                    // print error message, then re-prompt user
                    println(e.message)
                    // continue
                }
            }
        }
    }

    // return selected issue record
    return issuePage.getContentAt(linenum)
}

// ----------------------------------------------------------------

/* This function collects from the user all the information needed to create an issue
 * by prompting them for the description, product, affectedRelease, and priority.
 * Returns null if the user indicates to leave. */
private fun enterIssueInformation(
    product: ProductEntity, // in
): Issue? {
    var desc: String? = null

    // select description of issue
    while (desc == null) {
        println("Please describe the issue (max 50 chars). ` to abort:")

        when (val inputDesc = readln()) {
            "`" -> return null
            else -> {
                if (inputDesc.isEmpty() || inputDesc.length > 50) {
                    println("Bad input.")
                } else {
                    desc = inputDesc
                    // loop ends here
                }
            }
        }
    }

    // select anticipated release for this issue
    val release = selectRelease(product.name) ?: return null

    var status: Status? = null

    val statusChoices = listOf(
        Status.Created,
        Status.Assessed,
        Status.InProgress,
        Status.Done,
        Status.Cancelled
    )

    println() // blank line

    // select the status of this issue
    while (status == null) {
        print("""
            0. Created
            1. Assessed
            2. InProgress
            3. Done
            4. Cancelled

            Please select status (0-4). ` to abort: 
        """.trimIndent())

        when (val selection = readln()) {
            "`" -> return null
            else -> {
                try {
                    // will throw java.lang.NumberFormatException if not int
                    val userInputInt = selection.toInt()

                    // validate integer selection
                    if (userInputInt in (0..4)) {
                        status = statusChoices[userInputInt]
                        // loop ends here (check loop condition)
                    }
                } catch (e: java.lang.NumberFormatException) {
                    // print error message, then re-prompt user
                    println(e.message)
                    // continue
                }
            }
        }
    }

    var priority: Short? = null

    // select the priority of this issue
    while (priority == null) {
        print("Please choose priority of this issue (1-5). ` to abort: ")

        when (val selection = readln()) {
            "`" -> return null
            else -> {
                try {
                    // will throw java.lang.NumberFormatException if not int
                    val userInputShort = selection.toShort()

                    // validate short selection
                    if (userInputShort in (1..5)) {
                        priority = userInputShort
                        // loop ends here (check loop condition)
                    }
                } catch (e: java.lang.NumberFormatException) {
                    // print error message, then re-prompt user
                    println(e.message)
                    // continue
                }
            }
        }
    }

    // insert issue into db
    val issue = transaction {
        Issue.new {
            this.description = desc
            this.product = product
            this.anticipatedRelease = release
            this.creationDate = LocalDateTime.now()
            this.status = status
            this.priority = priority
        }
    }

    // print created issue to console
    println() // blank line
    println("Created issue:")
    displayIssueColumnTitles(false)
    displayIssue(issue)

    return issue
}

// ----------------------------------------------------------------

// display a given request to the screen
private fun displayRequester(request: Request?) {
    if (request == null) {
        println("Error: Bad request")
        return // bad request
    }

    // get full information about this contact
    val contactEntity = transaction {
        ContactEntity.find { Contacts.id eq request.contact }.firstOrNull()
    }

    // get full affected release
    val affrelEntity = transaction {
        Release.find { Releases.id eq request.affectedRelease }.firstOrNull()
    }

    // check for integrity of contact and affrel references
    if (contactEntity == null || affrelEntity == null) {
        println("Error: Anomalies in request $request.id")
        return
    }

    val affrel = affrelEntity.releaseId.padEnd(8)
    val date = request.requestDate.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
    val name = contactEntity.name.padEnd(30)
    val email = contactEntity.email.padEnd(30)
    val dept = contactEntity.department.padEnd(10)

    println("$affrel | $date | $name | $email | $dept")
}

// ----------------------------------------------------------------

private fun displayRequestColumnTitles() {
    val affrel = "AffRel".padEnd(8)
    val date = "Requested".padEnd(10)
    val name = "Name".padEnd(30)
    val email = "Email".padEnd(30)
    val dept = "Dept".padEnd(10)
    println("$affrel | $date | $name | $email | $dept")
}

// ----------------------------------------------------------------

// Displays a sub-menu for creating a new request and adding it to the
//     AntTracker database.
// Prompts the user for the various fields for the request, and validates
//     input when necessary, re-prompting where necessary.
// Returns the created request.
fun enterRequestInformation(): Request? {
    // select product for this request
    val product = selectProduct() ?: return null

    // select release for this request
    val release = selectRelease(product.name) ?: return null

    // select contact for this request
    var contact = selectContact()

    // if user doesn't select a contact, make them enter the contact information
    if (contact == null) {
        contact = enterContactInformation() ?: return null
    }

    // select issue for this request
    var issue = selectIssue(product.name)

    if (issue == null) {
        // if issue doesn't exist, then enter its information and create it
        issue = enterIssueInformation(product) ?: return null
    }

    // insert request into the db
    val request = transaction {
        Request.new {
            this.affectedRelease = release.id
            this.issue = issue.id
            this.contact = contact.id
            this.requestDate = LocalDateTime.now()
        }
    }

    println("Created request:")
    displayRequestColumnTitles()
    displayRequester(request)

    return request
}

// ----------------------------------------------------------------

// Displays a sub-menu for creating a new request and adding it to the
//     AntTracker database. To be used by the Main module.
// Prompts the user for the various fields for the request, and validates
//     input when necessary, re-prompting where necessary.
// Returns when the user wishes to return to the main menu.
fun menu() {
    println("== NEW REQUEST ==")
    enterRequestInformation()
}