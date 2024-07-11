/* Revision History:
Rev. 1 - 2024/07/02 Original by A. Kim
Rev. 2 - 2024/07/09 by M. Baker
-------------------------------------------------------------------------------
The Request module contains all exported classes and functions pertaining to
    the creation or selection of request entities.
-------------------------------------------------------------------------------
*/
package anttracker.request

// java imports
import java.time.LocalDate

// contact imports
import anttracker.contact.Name
import anttracker.contact.enterContactInformation
import anttracker.contact.getContactInfo
import anttracker.contact.selectContact

// issue imports
import anttracker.issue.Days
import anttracker.issue.Issue
import anttracker.issue.IssueId
import anttracker.issue.IssueFilter
import anttracker.issue.enterIssueInformation
import anttracker.issue.saveIssue
import anttracker.issue.selectIssue

// product imports
import anttracker.product.selectProduct

// release imports
import anttracker.release.ReleaseId
import anttracker.release.selectRelease

// ----------------------------------------------------------------------------
// Data class for storing the attributes of a given request.
// ---
data class Request(
    val affectedRelease: ReleaseId,
    val issue: Issue,
    val contact: Name,
    val requestDate: LocalDate
)

// any attribute in this may be null - signifying a not being used filtering mechanism
// only non-null attributes of this class may be considered in the filtering process
data class RequestFilter(
    val name: Name?, // by contact name
    val days: Days?, // since n days
    val release: ReleaseId?, // by release
    val issue: IssueId? // by issue
)

// -----------------

// ---- database functions

// saves a given request object into the database
fun saveRequest(
    request: Request // in
) {
    TODO()
}

// gets the next $limit requests from the DB after the $offset according to the $filter
// returns an empty list if there are no more requests to display
// if $filter is null, then retrieve all requests, ordered by the primary key
fun getRequestsInDB(offset: Int, limit: Int, filter: RequestFilter?): List<Request> {
    TODO()
}

// Returns all information about the request identified by the issueID.
// Returns null if not found
fun getRequestInfo(
    requester: String, // in
    issueID: Int, // in
): Request? {
    TODO()
}

// ---- menu functions

// Displays a sub-menu for creating a new request and adding it to the
//     AntTracker database.
// Prompts the user for the various fields for the request, and validates
//     input when necessary, re-prompting where necessary.
// Returns the created request.
fun enterRequestInformation(): Request? {
    // "?:" is a check if selectProduct returns null
    val product = selectProduct() ?: return null
    val release = selectRelease(product) ?: return null

    var contact = selectContact()

    // if user doesn't select a contact, make them enter the contact information
    if (contact == null) {
        contact = enterContactInformation() ?: return null
    }

    // filter available issues with product
    val issueFilter = IssueFilter.ByProduct(product = product)

    // NOTE: implement constant somewhere for "issuesPerPage"
    var issue = selectIssue(issueFilter, 20)

    if (issue == null) {
        val issueInformation = enterIssueInformation() ?: return null

        // if this returns null, then return null -- error occurred
        // TODO: print an error?
        issue = saveIssue(issueInformation) ?: return null
    }

    // construct request object
    val request = Request(
        release.id,
        issue,
        contact.name,
        LocalDate.now()
    )

    // save the request object into the DB
    saveRequest(request)

    return request
}

// Displays a sub-menu for selecting an existing request.
// Implements pagination when necessary.
// Returns the selected request, or null if user chooses to leave the menu.
fun selectRequest(
    filter: RequestFilter? // in
): Request? {
    var offset = 0 // TODO: make this a global constant, for use by multiple modules
    val limit = 20

    while (true) {
        // get next $limit requests from the DB, after the $offset, according to the $filter
        val retrievedRequests = getRequestsInDB(offset, limit, filter)
        var isNoMore = retrievedRequests.size < limit

        // check that there aren't any more requests to display
        if (retrievedRequests.isEmpty()) {
            if (offset != 0) {
                // end of listings reached; print last $limit requests instead.
                offset -= limit

                if (offset < 0) {
                    offset = 0
                }

                isNoMore = true
            } else {
                // no requests stored in the DB yet
                println("There are no requests to select.")
                return null
            }
        }

        // TODO: make columns spaced evenly among listing - could involve pre-construction
        //       of the individual cells as strings, and computing the maximum size per column
        //       to set that column's width in characters - possible implemented in a UI.kt file
        println("AffRel\tRequested\tName\tEmail\tDepartment")

        for (request in retrievedRequests) {
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

        if (isNoMore) {
            println("...")
            println("There are no more requests to be listed.")
        }

        println()
        println("Please select a request. <Enter> to view $limit more requests. ` to abort: ")

        // read input from user
        when (val selection = readln()) {
            "`" -> return null // chose to exit menu
            "" -> {
                if (!isNoMore) {
                    offset += limit
                }

                // continue - re-list the requests.
                // isNoMore should still be true, and the "No more" message will be listed.
            } // next page (enter key - empty string)
            else -> {
                try {
                    // attempt to select a row
                    val row = selection.toInt()

                    if (row < 0 || row >= limit) {
                        println("error")
                        continue
                    }

                    // row was selected - so was the request
                    return retrievedRequests[row]
                } catch (e: NumberFormatException) {
                    println("error")
                    continue
                }
            }
        }
    }
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