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

// a page of issues: performs db queries to load each page into memory, then display
private class PageOfIssues(
    private val productName: String // in
) : PageOf<Issue>(Issue) {

    // ----------------------------------------------------------------

    init {
        // computes how many pages are available
        initLastPageNum()
    }

    // ----------------------------------------------------------------

    override fun printRecord(
        record: Issue, // in
    ) {
        // display issue with leader bar (PageOf prints the line #)
        displayIssue(record, true)
    }

    // ----------------------------------------------------------------

    // in SQL this would be:
    //   SELECT * FROM issue
    //   WHERE product = productName
    //   ORDER BY creationDate DESC;
    // NOTE: PageOf only loads 20 into memory at a time, offsetting per page
    override fun getQuery(): Query =
        (Issues innerJoin Products)
            .select(Issues.columns)
            .where { Products.name eq productName }
            .orderBy(
                Issues.creationDate to SortOrder.DESC
            )
}

// ----------------------------------------------------------------

// display a given issue
private fun displayIssue(
    issue: Issue, // in
    displayLeadingBar: Boolean // in
) {
    val id = "${issue.id}".padEnd(4)
    val desc = issue.description.toString().padEnd(30)
    val priority = "${issue.priority}".padStart(9)
    val status = "${issue.status}".padEnd(14)
    val antrel = issue.anticipatedRelease?.releaseId?.padEnd(8) ?: "".padEnd(8)
    val created = issue.creationDate.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))

    if (displayLeadingBar) {
        println("| $id | $desc | $priority | $status | $antrel | $created")
    } else {
        println("$id | $desc | $priority | $status | $antrel | $created")
    }
}

// ----------------------------------------------------------------

// display the column titles (should precede displayIssue)
private fun displayIssueColumnTitles(
    displayLineNum: Boolean // in
) {
    // create column strings
    val linenum = "##".padEnd(3)
    val id = "ID".padEnd(4)
    val desc = "Description".padEnd(30)
    val priority = "Priority".padStart(9)
    val status = "Status".padEnd(14)
    val antrel = "AntRel".padEnd(8)
    val created = "Created".padEnd(10)

    // print columns
    if (displayLineNum) {
        println("$linenum | $id | $desc | $priority | $status | $antrel | $created")
    } else {
        println("$id | $desc | $priority | $status | $antrel | $created")
    }
}

// ----------------------------------------------------------------

/* display list of issues for a given issue, for selecting
 * issues are ordered by creation date */
private fun selectIssue(
    productName: String // in
): Issue? {
    // prepare page of issues
    val issuePage = PageOfIssues(productName)
    issuePage.loadRecords() // retrieve first page of records

    // selected line number by the user
    var linenum: Int? = null

    // loop until an issue is selected (linenum)
    while (linenum == null) {
        // display page
        displayIssueColumnTitles(true)
        issuePage.display()

        // prompt user for input
        println() // blank line
        print("Please select issue. ` to create an issue instead: ")

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
                    if (issuePage.isValidLineNum(userInputInt)) {
                        linenum = userInputInt
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
    return issuePage.getContentAt(linenum - 1)
}

// ----------------------------------------------------------------

/* This function collects from the user all the information needed to create an issue
 * by prompting them for the description, affectedRelease, and priority.
 * Returns null if the user indicates to abort the procedure.
 * The created issue has creation date set to the time of insertion, and status of created. */
private fun enterIssueInformation(
    product: ProductEntity, // in
): Issue? {
    var desc: String? = null

    // select description of issue; loops until a valid desc is entered
    while (desc == null) {
        // prompt user for input
        println("Issue description (max 30 chars). ` to abort:")

        when (val inputDesc = readln()) {
            // user aborts
            "`" -> return null

            // user input a string
            else -> {
                if (inputDesc.isEmpty() || inputDesc.length > 30) {
                    println("Bad input.")
                    // continue
                } else {
                    desc = inputDesc
                    // loop ends here
                }
            }
        }
    }

    // select anticipated release for this issue
    val release = selectRelease(product.name) ?: return null

    var priority: Short? = null

    // select the priority of this issue; loops until a valid priority is chosen
    while (priority == null) {
        print("Issue priority (1-5). ` to abort: ")

        when (val selection = readln()) {
            // user aborts
            "`" -> return null

            // user input needs validation
            else -> {
                try {
                    // will throw java.lang.NumberFormatException if not int
                    val userInputShort = selection.toShort()

                    // validate selection: priority must be between 1 and 5
                    if (userInputShort in (1..5)) {
                        priority = userInputShort
                        // loop ends here (check loop condition)
                    } else {
                        println("Bad input.")
                        // continue
                    }
                } catch (e: java.lang.NumberFormatException) {
                    println("Bad input.")
                    // continue
                }
            }
        }
    }

    // insert new issue into db
    val issue = transaction {
        Issue.new {
            this.description = IssueDescription.maybeParse(desc)!!
            this.product = product
            this.anticipatedRelease = release
            this.creationDate = LocalDateTime.now()
            this.status = Status.Created
            this.priority = priority
        }
    }

    return issue
}

// ----------------------------------------------------------------

// display a given request to the screen
private fun displayRequester(request: Request) {
    // get full affected release
    val affrelEntity = transaction {
        Release.find { Releases.id eq request.affectedRelease }.firstOrNull()
    }

    // check for integrity of contact and affrel references
    if (affrelEntity == null) {
        println("Error: Anomalies in request $request.id")
        return
    }

    // strings to be printed (with fixed lengths)
    val affrel = affrelEntity.releaseId.padEnd(8)
    val date = request.requestDate.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
    var name: String = ""; var email: String = ""; var dept: String = ""
    // get info on contact
    transaction {
        name = request.contact.name.padEnd(30)
        email = request.contact.email.padEnd(30)
        dept = request.contact.department.padEnd(10)
    }
    // print request to console
    println("$affrel | $date | $name | $email | $dept")
}

// ----------------------------------------------------------------

private fun displayRequestColumnTitles() {
    // column title strings (with fixed lengths)
    val affrel = "AffRel".padEnd(8)
    val date = "Requested".padEnd(10)
    val name = "Name".padEnd(30)
    val email = "Email".padEnd(30)
    val dept = "Dept".padEnd(10)

    // print titles to console
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

    // check if user aborted the select issue menu: offer to create an issue instead
    if (issue == null) {
        // if issue doesn't exist, then enter its information and create it
        issue = enterIssueInformation(product) ?: return null
    }

    // insert request into the db
    val request = transaction {
        Request.new {
            this.affectedRelease = release.id
            this.issue = issue.id
            this.contact = contact
            this.requestDate = LocalDateTime.now()
        }
    }

    // print created request to console
    println() // blank line
    println("Created request:")
    displayRequestColumnTitles()
    displayRequester(request)

    // print issue to console
    println() // blank line
    println("For issue:")
    displayIssueColumnTitles(false)
    displayIssue(issue, false)

    println() // blank line

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
