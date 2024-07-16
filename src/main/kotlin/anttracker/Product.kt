package anttracker.product

import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

// ----------------------------------------------------------------------------
// Data class for storing the attributes of a given product.
// ---
data class Product(
    val productName: String // name of the product (primary key)
)

// Assuming we have a Products table defined for Exposed ORM
object Products : IntIdTable() {
    val productName = varchar("product_name", 50)
}

// Product entity
class ProductEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ProductEntity>(Products)
    var productName by Products.productName
}

// Base class for pagination
abstract class PageOf<T : IntEntity>(private val limit: Int = 10, private val offset: Int = 0) {
    val contents: MutableList<T> = mutableListOf()
    protected abstract fun getQuery(): Query

    fun loadContents() {
        contents.clear()
        transaction {
            val query = getQuery().limit(limit, offset.toLong())
            query.forEach {
                contents.add(it as T)
            }
        }
    }

    fun display() {
        for ((index, record) in contents.withIndex()) {
            println("${index + 1}. ${record.id.value}")  // Customize as needed
        }
    }

    fun lastPage(): Boolean {
        // Implement logic to check if this is the last page
        return false
    }

    fun loadNextPage() {
        // Implement logic to load the next page
        loadContents()
    }
}

// Pagination class for Product
class PageOfProduct : PageOf<ProductEntity>() {
    override fun getQuery(): Query {
        return Products.selectAll().orderBy(Products.productName to SortOrder.ASC)
    }
}

// Display pages of products to console and select one
fun selectProduct(): ProductEntity? {
    val productPage = PageOfProduct()
    productPage.loadContents()  // Adjust limit and offset as necessary
    productPage.display()

    var linenum: Int? = null
    while (linenum == null) {
        println("Please select product. ` to abort: ")
        val userInput = readln()
        when (userInput) {
            "`" -> return null
            "" -> {
                if (!productPage.lastPage()) {
                    productPage.loadNextPage()  // Adjust limit and offset as necessary
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
// Displays a sub-menu for creating a new product and adding it to the
//     AntTracker database. To be used by the Main module.
// Prompts the user for the name of the product, and validates input where
//     necessary, ensuring no duplicates in the database.
// Returns when the user wishes to return to the main menu.
// ---
fun menu() {
    println("== NEW PRODUCT==")
    val newProduct = enterProductInformation()
    if (newProduct != null) {
        saveProduct(newProduct)
    }
}

// Displays a sub-menu for entering product information.
fun enterProductInformation(): ProductEntity? {
    while (true) {
        println("Please enter product name (1-30 characters). ` to exit:")
        val name = readLine()!!
        if (name == "`") return null
        if (name.length !in 1..30) {
            println("ERROR: Invalid product name length.")
            continue
        }
        return transaction {
            ProductEntity.new {
                productName = name
            }
        }
    }
}

// Displays all products as a string
fun displayProducts(): String {
    return transaction {
        ProductEntity.all().joinToString(separator = "\n") { it.productName }
    }
}

// Save a product to the database
private fun saveProduct(product: ProductEntity) {
    transaction {
        if (ProductEntity.find { Products.productName eq product.productName }.empty()) {
            product.flush()
            println("${product.productName} has been created.")
        } else {
            println("ERROR: Product already exists.")
        }
    }
    menu()
}
