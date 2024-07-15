package anttracker
import anttracker.issues.Products
import anttracker.issues.Release
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.math.ceil

// A PageOf objects contains a max-20 element list of T.
// Provides top-level functionality for:
//  - loading DB records into memory (one page at a time)
//  - accessing records
//  - displaying records as a page
//  - going to the next page and loading its records
abstract class PageOf<IntEntity> {
    protected var contents: MutableList<IntEntity> = mutableListOf() // Container for object instances
    protected var pagenum: Int = 0 // NOTE: Page numbers are 0-indexed
    private var lastPageNum: Int = 0
    protected val qOffset: Int = 0 // For page calculation in DB query
    protected val qLimit: Int = 20 // Number of records per page.

    // -------------------------------------------------------------------------------
    // Automatically called upon object creation (i.e. a ctor)
    // Inherited in PageOf implementations.
    // ---
    init {
        initLastPageNum()
    }

    // -------------------------------------------------------------------------------
    // Prints to console a line-by-line display of objects contained in the PageOf instance.
    // Abstract/Virtual as issues, releases, contacts will display different things.
    // ---
    abstract fun display()

    // -------------------------------------------------------------------------------
    // Queries to DB and pulls a page of records (max 20) into the contents MutableList.
    // ---
    fun loadContents() {
        contents.clear()
        val queryOutput = queryToDB()
        if (queryOutput != null) {
            queryOutput.forEach { record ->
                contents.add(record)
            }
        } else {
            throw Exception("PageOf: Attempted to load from null query output.")
        }
    }

    // -------------------------------------------------------------------------------
    // Returns the size/length of the contents MutableList.
    // ---
    fun contentsSize(): Int = contents.size

    // -------------------------------------------------------------------------------
    // Getter for elements in the contents MutableList.
    // ---
    fun getContentAt(index: Int): IntEntity = contents[index]

    // -------------------------------------------------------------------------------
    // Increments page number and loads associated DB records into MutableList contents datamember
    // Throws an error if attempting to load beyond the last page.
    // ---
    fun loadNextPage() {
        if (lastPage()) {
            throw Exception("PageOf: Already reached last page.")
        }
        pagenum++
        loadContents()
    }

    // -------------------------------------------------------------------------------
    // TRUE if current page >= than the last page.
    // ---
    fun lastPage(): Boolean = pagenum >= lastPageNum

    // -------------------------------------------------------------------------------
    // Returns the number of yet-to-be-displayed records after the current page.
    // ---
    fun countRemainingRecords(): Int = getQueryRecordCount() - (qLimit * (pagenum + 1))

    // -------------------------------------------------------------------------------
    // Used in init/ctor block to calculate the last page number.
    // As we are 0-indexing the page numbers, we subtract 1 at the end.
    // ---
    private fun initLastPageNum() {
        lastPageNum =
            ceil(
                (getQueryRecordCount().toDouble() / qLimit.toDouble()),
            ).toInt() - 1
    }

    // -------------------------------------------------------------------------------
    // Returns the total number of records/rows of the query being paginated.
    // Used for calculating the number of pages the query needs.
    // ---
    private fun getQueryRecordCount(): Int {
        val numRecords: Int =
            queryToDB()?.count()?.toInt()
                ?: throw IllegalArgumentException("Error: Query returned null")
        return numRecords
    }

    // -------------------------------------------------------------------------------
    // Defines the DAO query to DB used to pull records into memory.
    // Abstract/Virtual as query needs to be defined per PageOf Type
    // ---
    protected abstract fun queryToDB(): SizedIterable<IntEntity>?
}

// -------------------------------------------------------------------------------
// Implementation of a PageOf Class as PageOfReleases
// Each PageOf class needs to define:
//      - display(), to define how the page is displayed to console
//      - queryToDB(), to define the DAO query to DB used to pull records into memory
// ---
class PageOfReleases(
    private val productName: String,
) : PageOf<Release>() {
    override fun display() {
        for (releaseRecord in contents) {
            println(releaseRecord.releaseId)
        }
        if (!lastPage()) {
            println("<Enter> for ${countRemainingRecords()} more.")
        }
    }

    override fun queryToDB(): SizedIterable<Release>? {
        var output: SizedIterable<Release>? = null
        transaction {
            output =
                Release
                    .find { Products.name eq productName }
                    .limit(n = qLimit, offset = (pagenum * qLimit + qOffset).toLong())
        }
        return output
    }
}
