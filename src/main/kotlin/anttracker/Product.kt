/* Product.kt
Revision History:
Rev. 1 - 2024/07/01 Original by M. Baker
-------------------------------------------------------------------------------
The Product module contains all exported classes and functions pertaining to
the creation or selection of product entities.
-------------------------------------------------------------------------------
*/

package anttracker.product
import anttracker.Product

// ----------------------------------------------------------------------------
// Class for storing the attributes of a given product.
// ---
@JvmInline
value class ProductName(
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
// Returns the product selected by the user, or null if the user changed their mind.
// ---
fun selectProduct(): Product? {
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

/** --------------
 * This function takes the name of a potential user and checks if it is within 1 and
 * 30 characters and is not in the database. If both of these conditions are met,
 * the function returns true. Returns false otherwise.
 * An example use case would be if 'John' and '123456789123456789123456789123456789'
 * were passed to the function if the database had no previous users. For input 'John',
 * the function would return true. For the second input, the function would return false
 * since the name is longer than 30 characters
----------------- */
fun validateProductName(
    name: String, // in
): Boolean = true
