/* Revision History:
Rev. 1 - 2024/07/02 Original by A. Kim
-------------------------------------------------------------------------------
The Request module contains all exported classes and functions pertaining to
    the creation or selection of request entities.
-------------------------------------------------------------------------------
*/
package anttracker.request

import anttracker.contact.Name
import anttracker.issues.Issue
import anttracker.release.ReleaseId

// ----------------------------------------------------------------------------
// Data class for storing the attributes of a given request.
// ---
data class Request(
    val affectedRelease: ReleaseId,
    val issue: Issue,
    val anticipatedRelease: ReleaseId,
    val contact: Name,
)

// ----------------------------------------------------------------------------
// Displays a sub-menu for creating a new request and adding it to the
//     AntTracker database.
// Prompts the user for the various fields for the request, and validates
//     input when necessary, re-prompting where necessary.
// Returns the created request.
// ---
fun enterRequestInformation(): Request {
    TODO()
}

// ----------------------------------------------------------------------------
// Displays a sub-menu for selecting an existing request.
// Implements pagination when necessary.
// Returns a string indicating the user input that terminated the selection:
//   "`": exit the interface
//   "1"...: selected row
// ---
fun displayRequests(): String {
    TODO()
}

// ----------------------------------------------------------------------------
// Returns all information about the request identified by the issueID.
// ---
fun getRequestInfo(
    issue: String, // in
): Request {
    TODO()
}

// ----------------------------------------------------------------------------
// Displays a sub-menu for creating a new request and adding it to the
//     AntTracker database. To be used by the Main module.
// Prompts the user for the various fields for the request, and validates
//     input when necessary, re-prompting where necessary.
// Returns when the user wishes to return to the main menu.
// ---
fun menu() {
    TODO()
}
