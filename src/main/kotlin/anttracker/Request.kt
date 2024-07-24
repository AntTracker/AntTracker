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
import anttracker.issues.IssueFilter
import anttracker.issues.enterIssueInformation
import anttracker.issues.saveIssue
import anttracker.issues.selectIssue
import anttracker.product.selectProduct

// release imports
import anttracker.release.selectRelease
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

// display a given request to the screen
fun displayRequester(request: Request?) {
    if (request == null) {
        println("Error: Bad request")
        return // bad request
    }

    // get full information about this contact
    val contact =
        transaction {
            Contact.find { Contacts.id eq request.contact }.firstOrNull()
        }

    if (contact == null) {
        println("Error: Bad contact for request")
        return // bad contact
    }

    val affRel = request.affectedRelease
    val date = request.requestDate
    val name = contact.name
    val email = contact.email
    val dept = contact.department

    println("$affRel\t$date\t$name\t$email\t$dept")
}

// Displays a sub-menu for creating a new request and adding it to the
//     AntTracker database.
// Prompts the user for the various fields for the request, and validates
//     input when necessary, re-prompting where necessary.
// Returns the created request.
fun enterRequestInformation(): Request? {
    val product = selectProduct() ?: return null
    val release = selectRelease(product.name) ?: return null

    var contact = selectContact()

    // if user doesn't select a contact, make them enter the contact information
    if (contact == null) {
        contact = enterContactInformation() ?: return null
    }

    // filter available issues with product
    val issueFilter = IssueFilter.ByProduct(product.name)

    // NOTE: implement constant somewhere for "issuesPerPage"
    var issue = selectIssue(issueFilter, 20)

    if (issue == null) {
        val issueInformation = enterIssueInformation() ?: return null

        // if this returns null, then return null -- error occurred
        issue = saveIssue(issueInformation) ?: return null
    }

    var request: Request? = null

    transaction {
        request =
            Request.new {
                this.affectedRelease = release.id
                this.issue = issue.id
                this.contact = contact.id
                this.requestDate = LocalDateTime.now()
            }
    }

    println("Created request:")
    println("AffRel\tRequested\tName\tEmail\tDepartment")
    displayRequester(request)

    return request
}

// Displays a sub-menu for creating a new request and adding it to the
//     AntTracker database. To be used by the Main module.
// Prompts the user for the various fields for the request, and validates
//     input when necessary, re-prompting where necessary.
// Returns when the user wishes to return to the main menu.
fun menu() {
    println("== NEW REQUEST ==")
    enterRequestInformation()
}
