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
                    if (userInputInt in (0..19) && userInputInt < productPage.recordsSize()) {
                        linenum = userInput.toInt()
                    } else {
                        println("ERROR: Invalid line number.")
                    }
                } catch (e: java.lang.NumberFormatException) {
                    println(e.message)
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
    val newProduct: ProductEntity? = enterProductInformation()
    if (newProduct != null) {
        // saveProduct(newProduct)
    }
}

// Displays a sub-menu for entering product information.
fun enterProductInformation(): ProductEntity? {
    var productNameEntry: String? = null

    while (productNameEntry == null) {
        println("Please enter product name (1-30 characters). ` to exit:")

        try {
            productNameEntry = ProductName(readln()).toString()
            if (productNameEntry == "`") { // User wants to abort
                return null
            }
            if (productExists(productNameEntry)) {
                println("ERROR: Product already exists.")
                productNameEntry = null
            }
        } catch (e: java.lang.IllegalArgumentException) {
            println(e.message)
        }
    }

    return transaction {
        ProductEntity.new {
            this.name = productNameEntry
        }
    }
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
private fun saveProduct(product: ProductEntity) {
    transaction {
        if (ProductEntity.find { Products.name eq product.name }.empty()) {
            product.flush()
            println("${product.name} has been created.")
        } else {
            println("ERROR: Product already exists.")
        }
    }
    menu()
}

private fun productExists(productString: String): Boolean =
    transaction {
        !ProductEntity.find { Products.name eq productString }.empty()
    }
