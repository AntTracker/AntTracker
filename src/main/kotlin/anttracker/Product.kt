/* Product.kt
Revision History:
Rev. 1 - 2024/07/01 Original by M. Baker
-------------------------------------------------------------------------------
The Product module contains all exported classes and functions pertaining to
    the creation or selection of product entities.
-------------------------------------------------------------------------------
*/

package anttracker.product

// ----------------------------------------------------------------------------
// Class for storing the attributes of a given product.
// ---
@JvmInline
value class Product(
    private val name: String, // name of the product (primary key)
) {
    init {
        require(name.length in 1..30) {
            "Name length must be between 1 and 30 characters"
        }
    }
}

// ----------------------------------------------------------------------------
// Displays a sub-menu for selecting an existing contact.
// Implements pagination when necessary.
// Returns a string indicating the user input that terminated the selection:
//   "`": exit the interface
//   "1"...: selected row
// ---
fun displayProducts(): String {
    TODO()
}

// ----------------------------------------------------------------------------
// Displays a sub-menu for creating a new product and adding it to the
//     AntTracker database. To be used by the Main module.
// Prompts the user for the name of the product, and validates input where
//     necessary, ensuring no duplicates in the database.
// Returns when the user wishes to return to the main menu.
// ---
fun menu() {
    TODO()
}

fun validateProductName(name: String): Boolean = true
