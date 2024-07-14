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
import anttracker.product.displayProducts
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate


object ProductTable : Table("product") {
    val productName = varchar("productName", 10)
    override val primaryKey = PrimaryKey(productName)
}

object ReleaseTable : Table("release") {
    val releaseID = varchar("releaseID", 8)
    val product =
        varchar("product", 10).references(
            ProductTable.productName,
            onDelete = ReferenceOption.CASCADE,
            onUpdate = ReferenceOption.CASCADE,
        )
    val releaseDate = varchar("releaseDate", 10)
    override val primaryKey = PrimaryKey(arrayOf(releaseID, product))
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

fun testProducts() {
    var linenum = 1
    transaction {
        val output = ProductTable.selectAll()
        println("Product List")
        output.forEach {
            println("$linenum   ${it[ProductTable.productName]}")
            linenum++
        }
    }
}

class Release(
    val releaseName: ReleaseId,
    val product: Product,
    var releaseDate: LocalDate,
)

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
    println("should be some product list below:")
    testProducts()
    println("\nPlease select a product (1). ` to abort:")
    when (val selection = readln()) {
        "`" -> return
        "1" -> createRelease()
        else -> {
            println("Bad input: $selection.")
            return
        }
    }
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
    println("$product releases:")
    transaction {
        val output = ReleaseTable.selectAll().where { ReleaseTable.product eq product }
    }
    return product
}

fun selectRelease(productName: String? = null): Release? {
    var product = productName
    if (product == null) {
        product = selectProduct()
    }

    while (!userinput) {
        displayReleases(product)
    }

    transaction {
//            release = ReleaseTable.selectAll().where {
//                ReleaseTable.product eq productName and
//                ReleaseTable.releaseID eq releaseID
//            }
    }
    return release
}

fun createRelease() {
    displayReleases("GreenTiger")

    var linenum: Int? = null
    while (linenum == null) {
        println(promptSelectRel)
        val selection = readln()
        if (selection == "`") {
            return
        }
        try {
            val userEntry = selection.toInt()
            when (userEntry) {
                in 0..20 -> linenum = userEntry
                else -> {
                    println("Error: please enter a valid line number.")
                }
            }
        } catch (e: java.lang.NumberFormatException) {
            println("Error: please enter a number.")
        }
    }

    // val selectedProduct: String = linenum.toString()
    val selectedProduct: String = displayProducts(selectedProduct)
    displayReleases(selectedProduct)

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
    // if saveToDb()
    println("$selectedProduct $releaseEntry created.\n")
}

/*
NOTE: 95% complete pseudocode

SQL for getting a page of release records from full query output:
select * from Release where
    product = $product
    LIMIT 20 OFFSET (pagenum*limit + offset)

--PageOf<T>--
contents:       MutableList<T>
pagenum:        Int = 0 // decide on 0 or 1 indexed?
lastPageNum:    Int = 0 // initialized on construct/init
offset:         Int = 0
limit:          Int = 20

--PageOf<Release>--
// added data:
product: String

// Prints page contents to terminal
display() {
    for releaseRecord in contents
        println(releaseRecord.releaseID)  // add some nice formatting
}

// Stores page with its associated records in DB


loadNextPage() {
    if (lastPage()) {
        throw exception
    }
    pagenum++;
    loadContents()
}

//EXAMPLE:
//    Total query returns 30 rows = 2 pages (20 + 10)
//    pagelimit is 20, 30/20 = 1.5
//    ceil(1.5) = 2
//    therefore, maxPages = ceil(count(Qtotal)/limit)
initLastPageNum() {

}

// Displays pages of releases to console, prompts user to select one with linenumber.
// Returns user-selected release record from page.
selectRelease(product: String)
    relPage: PageOf<Release>(product)
    relPage.loadContents()
    relPage.display()

    linenum: LineNum? = null
    while(!linenum) {
        println(promptRelSelect) //"Please select release. ` to abort: "
        userInput = readln()
        when (userInput) {
            "`" -> return
            <Enter> -> {
                if (!lastPage()) {
                    loadNextPage()
                }
            }
            else {
                if userInput is valid linenumber (1..20 AND < contents.size())
                    linenum = userInput
                else
                    println(errMsgInvalidLineNum) // While loop continues; user can try again
            }
        }
    }
    return releasePage.contents.get(linenum)
*/
