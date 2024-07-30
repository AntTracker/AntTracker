package anttracker.product

import anttracker.PageOf
import anttracker.db.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

@JvmInline
value class ProductName(
    private val id: String,
) {
    init {
        require(id.length in 1..30) {
            "Product name length must be between 1 and 30 characters"
        }
    }

    override fun toString(): String = id
}

/** --------------
 * This function takes the name of a potential product name. Returns TRUE if
 * product name is 1..30 characters, and doesn't yet exist in the database.
 * Returns FALSE if product name already exists in database or exceeds char limits.
----------------- */
fun validateProductName(
    name: String, // in
): Boolean =
    (name.length in 1..30) &&
        transaction {
            ProductEntity.find { Products.name eq name }.empty()
        }

// Pagination class for Product
private class PageOfProducts : PageOf<ProductEntity>(ProductEntity) {
    init {
        initLastPageNum()
    }

    override fun getQuery(): Query =
        Products
            .selectAll()
            .orderBy(
                Products.name to SortOrder.ASC,
            )

    override fun printRecord(record: ProductEntity) {
        println(record.name)
    }
}

// Display pages of products to console and select one
fun selectProduct(): ProductEntity? {
    val productPage = PageOfProducts()
    productPage.loadRecords() // Adjust limit and offset as necessary
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
                try {
                    // Check page contains the line number
                    // If page doesn't, prompt again.
                    val userInputInt = userInput.toInt()
                    if (userInputInt in (1..20) && userInputInt < productPage.recordsSize()) {
                        linenum = userInput.toInt()
                    } else {
                        println("ERROR: Invalid line number.")
                    }
                } catch (e: java.lang.NumberFormatException) {
                    println("ERROR: Please enter a number.")
                }
            }
        }
    }
    return productPage.getContentAt(linenum)
}

// ----------------------------------------------------------------------------
// Displays a sub-menu for creating a new product and adding it to the
//     AntTracker database. To be used by the Main module.
// Prompts the user for the name of the product, and validates input where
//     necessary, ensuring no duplicates in the database.
// Returns when the user wishes to return to the main menu.
// ---
fun menu() {
    println("== NEW PRODUCT==")
    val newProductName = enterProductInformation()
    if (newProductName != null) {
        saveProduct(newProductName)
    }
}

// Displays a sub-menu for entering product information.
fun enterProductInformation(): ProductName? {
    var productNameEntry: ProductName? = null

    while (productNameEntry == null) {
        println("Please enter product name (1-30 characters). ` to abort:")

        try {
            productNameEntry = ProductName(readln())
            if (productNameEntry.toString() == "`") { // User wants to abort
                return null
            }
            if (!validateProductName(productNameEntry.toString())) {
                println("ERROR: Product already exists.")
                productNameEntry = null
            }
        } catch (e: java.lang.IllegalArgumentException) {
            println(e.message)
        }
    }
    return productNameEntry
}

// Displays all products as a string
fun displayProducts() {
    val productPage = PageOfProducts()
    productPage.loadRecords()
    productPage.display()

    while (!productPage.lastPage()) {
        val userInput = readln()
        when (userInput) {
            "`" -> return
            "" -> {
                if (!productPage.lastPage()) {
                    productPage.loadNextPage()
                    productPage.display()
                }
            }
            else -> {} // do nothing on mis-inputs.
        }
    }
}

// Save a product to the database
private fun saveProduct(productName: ProductName): ProductEntity? {
    try {
        val newProduct =
            transaction {
                ProductEntity.new {
                    this.name = productName.toString()
                }
            }
        println("Product ${newProduct.name} created.")
        return newProduct
    } catch (e: Exception) {
        println("ERROR: Failed to create product.")
        return null
    }
}

private fun productExists(productString: String): Boolean =
    transaction {
        !ProductEntity.find { Products.name eq productString }.empty()
    }
