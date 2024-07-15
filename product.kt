/* Revision History:
Rev. 1 - 2024/07/01 Original by M. Baker
Rev. 2 - 2024/07/08 Updated by A. Kim
-------------------------------------------------------------------------------
The Product module contains all exported classes and functions pertaining to
    the creation or selection of product entities.
-------------------------------------------------------------------------------
*/

package anttracker.product

import anttracker.product.ProductManager.enterProductInformation
import anttracker.product.ProductManager.products
import anttracker.product.ProductManager.saveProduct
import anttracker.product.ProductManager.showMainMenu

//pagination

// PageOf<Product>
class PageOfProduct : PageOf<Product>() {
    fun display() {
        for ((index, productRecord) in contents.withIndex()) {
            println("${index + 1}. ${productRecord.productID}")  // add some nice formatting
        }
    }

    override fun loadContents() {
        contents.clear()
        transaction {
            val output = Product
                .find { Product.someCriteria eq someValue }
                .limit(limit)
                .offset(pagenum * limit + offset)
            output.forEach {
                contents.add(it)
            }
        }
    }
}

// Display pages of products to console and select one
fun selectProduct(): Product? {
    val productPage = PageOfProduct()
    productPage.loadContents()
    productPage.display()

    var linenum: Int? = null
    while (linenum == null) {
        println("Please select product. ` to abort: ")
        val userInput = readln()
        when (userInput) {
            "`" -> return null
            "" -> {
                if (!productPage.lastPage()) {
                    productPage.loadNextPage()
                    productPage.display()
                }
            }
            else -> {
                linenum = userInput.toIntOrNull()
                if (linenum == null || linenum !in 1..productPage.contents.size) {
                    println("Invalid line number")
                    linenum = null
                }
            }
        }
    }
    return productPage.contents[linenum - 1]
}



// ----------------------------------------------------------------------------
// Data class for storing the attributes of a given product.
// ---
data class Product(
    val productName: String // name of the product (primary key)
)

// ----------------------------------------------------------------------------
// Displays a sub-menu for selecting an existing contact.
// Implements pagination when necessary.
// Returns a string indicating the user input that terminated the selection:
//   "`": exit the interface
//   "1"...: selected row
// ---

fun enterProductInformation(): Product? {
    while (true) {
        println("Please enter product name (1-30 characters). ` to exit:")
        val name = readLine()!!
        if (name == "`") return null
        if (name.length !in 1..30) {
            println("ERROR: Invalid product name length.")
            continue
        }
        return Product(name)
    }
}

fun displayProducts(): String {
    return if (products.isEmpty()) {
        "No products available."
    } else {
        products.joinToString(separator = "\n") { it.toString() }
    }
}

private fun saveProduct(product: Product) {
    if (products.any { it.name.equals(product.name, ignoreCase = true) }) {
        println("ERROR: Product already exists.")
    } else {
        products.add(product)
        println("${product.name} has been created.")
    }
    menu()
}

// ----------------------------------------------------------------------------
// Displays a sub-menu for creating a new product and adding it to the
//     AntTracker database. To be used by the Main module.
// Prompts the user for the name of the product, and validates input where
//     necessary, ensuring no duplicates in the database.
// Returns when the user wishes to return to the main menu.
// ---
fun menu() {
    while (true) {
        println("== Main Menu ==")
        println("1. View/Edit Issue")
        println("2. New request")
        println("3. New release")
        println("4. New contact")
        println("5. New product")
        println("` to exit program")
        when (readLine()!!) {
            "5" -> {
                val product = enterProductInformation()
                if (product != null) {
                    saveProduct(product)
                }
            }
            "`" -> return
            else -> println("Invalid option. Please try again.")
        }
    }
}



