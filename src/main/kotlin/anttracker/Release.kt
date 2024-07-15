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
import anttracker.PageOfReleases
import anttracker.issues.Product
import anttracker.issues.Products
import anttracker.issues.Release
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

@JvmInline
value class ReleaseId(
    private val id: String,
) {
    init {
        require(id.length in 1..8) {
            "Release id length must be between 1 and 8 characters"
        }
    }

    override fun toString(): String = id
}

val promptEnterRel = "\nPlease enter new release name. ` to abort:"

val promptSelectRel = "\nPlease select release. ` to abort:"

val promptSelectAffRel = "\nPlease select affected release. ` to abort:"

// -------------------------------------------------------------------------------
// Prints a sub-menu of selecting a product to create a release for.
// Use by including in main-menu loop; should be triggered upon user selecting
// New Release from the main menu. Thus, upon function return, system returns to main menu.
// ---
fun menu() {
    println("== NEW RELEASE ==")
    // val selectedProduct = selectProduct()    <- TODO: uncomment and replace when implemented in product module
    val selectedProduct = "Product 1"
    when (val selection = readln()) {
        "`" -> return
        "1" -> createRelease(selectedProduct)
        else -> {
            println("Bad input: $selection.")
            return
        }
    }
}

// -------------------------------------------------------------------------------
// A read-only display of releases for a product. Returns control upon reaching
//  the final page, or if user aborts with backtick.
// ---
fun displayReleases(productName: String) {
    val relPage = PageOfReleases(productName)
    relPage.loadRecords()
    relPage.display()

    while (!relPage.lastPage()) {
        val userInput = readln()
        when (userInput) {
            "`" -> return
            "" -> {
                relPage.loadNextPage()
                relPage.display()
            }
        }
    }
    return
}

fun selectRelease(productName: String): Release? {
    val relPage = PageOfReleases(productName)
    relPage.loadRecords()
    relPage.display()

    var linenum: Int? = null
    while (linenum == null) {
        println(promptSelectRel) // "Please select release. ` to abort: "
        val userInput = readln()
        when (userInput) {
            "`" -> return null
            "" -> {
                if (!relPage.lastPage()) {
                    relPage.loadNextPage()
                    relPage.display()
                }
            }
            else -> {
                try {
                    val userInputInt = userInput.toInt()
                    if (userInputInt in (1..20) && userInputInt < relPage.recordsSize()) {
                        linenum = userInput.toInt()
                    }
                } catch (e: java.lang.NumberFormatException) {
                    println(e.message)
                }
            }
        }
    }
    return relPage.getContentAt(linenum)
}

fun createRelease(productName: String) {
    displayReleases(productName)

    var releaseEntry: String? = null
    while (releaseEntry == null) {
        println(promptEnterRel)
        try {
            releaseEntry = ReleaseId(readln()).toString()
            if (releaseEntry == "`") {
                return
            }
        } catch (e: java.lang.IllegalArgumentException) {
            println(e.message)
        }
    }

    transaction {
        val product =
            Product.find { Products.name eq productName }.firstOrNull()
                ?: throw IllegalArgumentException("Error: Product not found")
        Release.new {
            releaseId = releaseEntry
            this.product = product
            releaseDate = LocalDateTime.now()
        }
    }

    println("$productName $releaseEntry created.\n")
}
