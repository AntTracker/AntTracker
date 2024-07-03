/* Release.kt
Revision History:
Rev. 1 - 2024/07/01 Original by T. Tracey
----------------------------------------------------------
The Release module encapsulates the group of functions related to release creation,
    and printing releases to console.
The module hides validation of release attributes, the interactive menu prompts during
    the release creation process, as well as interfacing with the database.
----------------------------------------------------------
*/

package anttracker.release

import anttracker.product.Product

@JvmInline
value class ReleaseId(
    private val id: String,
) {
    init {
        require(id.length in 1..8) {
            "Release id length must be between 1 and 8 characters"
        }
    }
}

class Release(
    val releaseName: ReleaseId,
    val product: Product,
    var releaseDate: String,
)

// -------------------------------------------------------------------------------
// Prints a sub-menu of selecting a product to create a release for.
// Use by including in main-menu loop; should be triggered upon user selecting
// New Release from the main menu. Thus, upon function return, system returns to main menu.
// ---
fun releaseMenu() {
    TODO()
}

// -------------------------------------------------------------------------------
// Prints to console a paginated list of releases for a product
// Returns a string indicating user input:
//  "`": user exit
//  an int (e.g. "4"): a line number selecting a particular release to query on
// Call as part of any relevant sub-menu
//  e.g. During request creation, call this to find and select the affected release
// ---
fun displayReleases(
    product: String, // in
): String {
    TODO()
}
