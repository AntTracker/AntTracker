/* Release.kt
Revision History:
Rev. 1 - 2024/07/01 Original by Tyrus Tracey
Rev. 2 - 2024/07/15 Full implementation by Tyrus Tracey
Rev. 3 - 2024/07/29 Bug fixing by Tyrus Tracey
----------------------------------------------------------
The Release module encapsulates the group of functions related to release creation,
    release selection, and printing releases to console.
The module hides validation of release attributes, the interactive menu prompts during
    the release creation process, as well as interfacing with the database.
----------------------------------------------------------
*/

package anttracker.release

import anttracker.PageOf
import anttracker.db.*
import anttracker.product.selectProduct
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
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
    if (selectedProduct != null) {
        createRelease(selectedProduct.name)
    }
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

    // Prompt user until system receives valid line number.
    while (linenum == null) {
        println(promptSelectRel) // i.e. "Please select release. ` to abort:"
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
                    // Check page contains the line number
                    // If page doesn't, prompt again.
                    val userInputInt = userInput.toInt()
                    if (relPage.isValidLineNum(userInputInt)) {
                        linenum = userInput.toInt()
                    }
                } catch (e: java.lang.NumberFormatException) {
                    println(e.message)
                }
            }
        }
    }
    // Return selected release record
    return relPage.getContentAt(linenum - 1)
}

// -------------------------------------------------------------------------------
// Sub-menu for when user wants to create a release.
// Inserts release name defined at runtime into Release relation in database.
// Throws exception on transaction failure (e.g. product doesn't exist in database)
// ---
private fun createRelease(
    productName: String, // in
) {
    displayReleases(productName)
    var releaseEntry: String? = null
    val product =
        transaction {
            ProductEntity.find { Products.name eq productName }.firstOrNull()
                ?: throw IllegalArgumentException("Error: Product not found")
        }

    // Prompt user until valid releaseId is entered
    while (releaseEntry == null) {
        println(promptEnterRel)
        try {
            releaseEntry = ReleaseId(readln()).toString()
            if (releaseEntry == "`") { // User wants to abort
                return
            }
            if (releaseExists(product, releaseEntry)) {
                println("ERROR: Release already exists.")
                releaseEntry = null
            }
        } catch (e: java.lang.IllegalArgumentException) {
            println(e.message)
        }
    }

    // Insert release into the database.
    transaction {
        Release.new {
            releaseId = releaseEntry
            this.product = product
            releaseDate = LocalDateTime.now()
        }
    }

    println("$productName $releaseEntry created.\n")
}

// -------------------------------------------------------------------------------
// A read-only display of releases for a product. Returns control upon reaching
//  the final page, or if user aborts with backtick.
// ---
private fun displayReleases(
    productName: String, // in
) {
    val relPage = PageOfReleases(productName)
    relPage.loadRecords()
    println("$productName releases:")
    relPage.display()

    // Loop through pages. Loop terminates at last page, or user abort.
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

// IVC for quick validation of releaseId. Can be used in a try/catch block.
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
//      - getQuery(), to define the query to DB
// ---
private class PageOfReleases(
    private val productName: String,
) : PageOf<Release>(Release) {
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
    // ---
    override fun getQuery(): Query =
        (Releases innerJoin Products)
            .select(Releases.columns)
            .where { Products.name eq productName }
            .orderBy(
                Releases.product to SortOrder.DESC,
                Releases.releaseDate to SortOrder.DESC,
            )
}

// -------------------------------------------------------------------------------
// Returns TRUE if a releaseId of a certain product already exists in the database.
// ---
fun releaseExists(
    product: ProductEntity,
    releaseId: String,
): Boolean =
    !transaction {
        Release
            .find {
                (Releases.product eq product.id) and (Releases.releaseId eq releaseId)
            }.toList()
            .isEmpty()
    }

private val promptEnterRel = "\nPlease enter new release name. ` to abort:"

private val promptSelectRel = "\nPlease select release. ` to abort:"
