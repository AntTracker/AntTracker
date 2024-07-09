/* Revision History:
Rev. 1 - 2024/07/02 Original by Angela Kim
Rev. 2 - 2024/07/09 by Micah Baker
-------------------------------------------------------------------------------
The Request module contains all exported classes and functions pertaining to
    the creation or selection of request entities.
-------------------------------------------------------------------------------
*/
package anttracker.request

// don't know if these imports are actually right
import anttracker.contact.getContactInfo
import anttracker.contact.Name
import anttracker.contact.Contact
import anttracker.product.selectProduct
import anttracker.release.selectRelease
import anttracker.contact.selectContact
import anttracker.issue.selectIssue
import anttracker.issue.Issue
import anttracker.release.ReleaseId
import anttracker.request.Request

// ----------------------------------------------------------------------------
// Data class for storing the attributes of a given request.
// ---
data class Request(
    val affectedRelease: ReleaseId,
    val issue: Issue,
    val contact: Name,
    val requestDate: Date // TODO: decide on date type
)

// ----------------------------------------------------------------------------
// Displays a sub-menu for creating a new request and adding it to the
//     AntTracker database.
// Prompts the user for the various fields for the request, and validates
//     input when necessary, re-prompting where necessary.
// Returns the created request.
// ---
fun enterRequestInformation(): Request? {
    var product = selectProduct() // Product

    if (product == null) {
        return null
    }

    var release = selectRelease() // ReleaseId

    if (release == null) {
        return null
    }

    // will create a contact if user specifies
    var contact = selectContact() // Contact

    if (contact == null) {
        return null
    }

    // will create an issue if user specifies
    var issue = selectIssue() // Issue

    if (issue == null) {
        return null
    }

    var request = Request(
        release,
        issue,
        issue.information.anticipatedRelease,
        contact.name
    )

    // TODO: save request in DB

    return request
}

// any attribute in this may be null - signifying a not being used filtering mechanism
// only non-null attributes of this class may be considered in the filtering process
data class RequestFilter {
    val name: Name?, // by contact name
    val days: Days?, // since n days
    val release: ReleaseId?, // by release
    val issue: IssueId? // by issue
}

// ----------------------------------------------------------------------------
// Displays a sub-menu for selecting an existing request.
// Implements pagination when necessary.
// Returns the selected request, or null if user chooses to leave the menu.
// ---
fun selectRequest(val filter: RequestFilter?): Request? {
    val offset = 0
    val limit = 20 // TODO: make this a constant

    while (true) {
        val printedRequests = listOf<Request>()

        // TODO: retrieve the next $limit requests from the DB, using $offset
        //       store it in $printedRequests
        // NOTE: if filter != null, use filtering mechanism that way
        //       e.g., filter may be to specify a specific issue, a release, a date, ...

        // TODO: make columns spaced evenly among listing - could involve pre-construction
        //       of the individual cells as strings, and computing the maximum size per column
        //       to set that column's width in characters - possible implemented in a UI.kt file
        println("AffRel\tRequested\tName\tEmail\tDepartment")

        for (request in printedRequests) {
            // get the contact information given their name (for email, dept)
            val contact = getContactInfo(request.contact)

            // print a line for this request
            println(
                "${request.affectedRelease}\t" +
                "${request.requestDate}\t" +
                "${request.contact}\t" +
                "${contact.email}\t" +
                "${contact.department}\t"
            )
        }

        // read input from user
        val selection = readln()

        when (selection) {
            "`" -> return null // chose to exit menu
            "" -> offset += limit // next page (enter key - empty string)
            else -> {
                try {
                    // attempt to select a row
                    val row = toInt(selection)

                    if (row < 0 || row >= limit) {
                        // TODO: print error
                        continue
                    }

                    // row was selected - so was the request
                    return printedRequests[row]
                } catch (e: NumberFormatException) {
                    // TODO: print error
                    continue
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// Returns all information about the request identified by the issueID.
// ---
fun getRequestInfo(
    requester: String, // in
    issueID: Int, // in
): Request? {
    val request: Request

    // TODO: perform lookup in DB with combined primary key requester,issueID

    return request
}

// ----------------------------------------------------------------------------
// Displays a sub-menu for creating a new request and adding it to the
//     AntTracker database. To be used by the Main module.
// Prompts the user for the various fields for the request, and validates
//     input when necessary, re-prompting where necessary.
// Returns when the user wishes to return to the main menu.
// ---
fun menu() {
    println("== NEW REQUEST ==")
    enterRequestInformation()
}
