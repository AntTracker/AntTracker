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
import anttracker.PageOf
import anttracker.Product
import anttracker.Products
import anttracker.Release
import anttracker.Releases
import anttracker.product.selectProduct
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// -------------------------------------------------------------------------------
// Prints a sub-menu of selecting a product to create a release for.
// Use by including in main-menu loop; should be triggered upon user selecting
// New Release from the main menu. Thus, upon function return, system returns to main menu.
// ---
fun menu() {
    println("== NEW RELEASE ==")
    val selectedProduct = selectProduct()
    if (selectedProduct == null) {
        println("Error: NULL product. Aborting to main menu.")
    } else {
        createRelease(selectedProduct.name)
    }
}

// -------------------------------------------------------------------------------
// A read-only display of releases for a product. Returns control upon reaching
//  the final page, or if user aborts with backtick.
// ---
fun displayReleases(
    productName: String, // in
) {
    val relPage = PageOfReleases(productName)
    relPage.loadRecords()
    println("$productName releases:")
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

// -------------------------------------------------------------------------------
// An interactive display of product releases, of which the user will select one by line number.
// This will return a Release entity representing a single record pulled from the database.
// Throws exception on transaction failure.
// The function will return null if the transaction fails, or if user wants to abort the process.
// ---
fun selectRelease(
    productName: String, // in
): Release? {
    val relPage = PageOfReleases(productName)
    relPage.loadRecords()
    relPage.display()

    var linenum: Int? = null
    while (linenum == null) {
        println(promptSelectRel) // "Please select release. ` to abort: "
        val userInput = readln()
        when (userInput) {
            "`" -> return null // User wants to abort
            "" -> { // User wants to see next page of product releases
                if (!relPage.lastPage()) {
                    relPage.loadNextPage()
                    relPage.display()
                }
            }
            else -> { // User has attempted to enter a line number
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

// -------------------------------------------------------------------------------
// Sub-menu for when user wants to create a release.
// Inserts release name defined at runtime into Release relation in database.
// Throws exception on transaction failure (e.g. product doesn't exist in database)
// ---
fun createRelease(
    productName: String, // in
) {
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

// -------------------------------------------------------------------------------
// Implementation of a PageOf Class as PageOfReleases
// Each PageOf class needs to define:
//      - init{} block, which MUST call initLastPageNum(). See below why this is necessary.
//      - display(), to define how the page is displayed to console
//      - queryToDB(), to define the DAO query to DB used to pull records into memory
// ---
class PageOfReleases(
    private val productName: String,
) : PageOf<Release>() {
    // -------------------------------------------------------------------------------
    // Automatically called upon object creation (i.e. a 2nd ctor)
    // Not defined in PageOf superclass as that gets called BEFORE subclass properties
    //      (such as productName) are defined. This can result in the calculation for
    //      lastPageNum being undefined behaviour as the query isn't fully-formed.
    //      To prevent this, the init block is (annoyingly) defined in the subclass
    //      which ensures it is called AFTER the constructor in the class header.
    // ---
    init {
        initLastPageNum()
    }

    // -------------------------------------------------------------------------------
    // Prints a single product release record to console.
    // Should look like:
    // x.x.x.x.    YYYY/MM/DD
    // ---
    override fun printRecord(
        record: Release, // in
    ) {
        val strRelId: String = record.releaseId
        val strRelDate: String = record.releaseDate.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
        println(strRelId.padEnd(12) + strRelDate)
    }

    // -------------------------------------------------------------------------------
    // In SQL this would be:
    //  SELECT * FROM release
    //  WHERE product = productName
    //  ORDER BY product, releaseDate DESC
    //  LIMIT qLimit OFFSET pagenum * qLimit + qOffset
    // ---
    override fun queryToDB(): SizedIterable<Release>? {
        var output: SizedIterable<Release>? = null
        val test = productName
        transaction {
            val product =
                Product.find { Products.name eq productName }.firstOrNull()
                    ?: throw Exception("queryToDB(): Product not found.")

            output =
                Release
                    .find {
                        Releases.product eq product.id
                    }.limit(n = queryLimit, offset = (pagenum * queryLimit + queryOffset).toLong())
                    .orderBy(
                        Releases.product to SortOrder.DESC,
                        Releases.releaseDate to SortOrder.DESC,
                    )
        }
        return output
    }
}

val promptEnterRel = "\nPlease enter new release name. ` to abort:"

val promptSelectRel = "\nPlease select release. ` to abort:"

val promptSelectAffRel = "\nPlease select affected release. ` to abort:"
