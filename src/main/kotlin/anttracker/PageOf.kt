package anttracker
import anttracker.release.Release
import anttracker.release.ReleaseTable
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.math.ceil

// A PageOf objects contains a max-20 element list of T.
// Provides top-level functionality for:
//  - loading DB records into memory (one page at a time)
//  - displaying page contents
//  - going to the next page and loading its records
abstract class PageOf<T> {
    protected var contents: MutableList<T> = mutableListOf() // Container for object instances
    protected var pagenum: Int = 0 // NOTE: Page numbers are 0-indexed
    protected var lastPageNum: Int = 0
    protected val OFFSET: Int = 0 // For page calculation in DB query
    protected val LIMIT: Int = 20 // Number of records per page.

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
    // Queries to DB and pulls a maximum 20 records at a time into the MutableList contents datamember.
    // ---
    fun loadContents() {
        contents.clear()
        val queryOutput = queryToDB()
        queryOutput.forEach {
            contents.add(it[ReleaseTable])
        }
    }

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
    // Used in init/ctor block to calculate the last page number.
    // As we are 0-indexing the page numbers, we subtract 1 at the end.
    // ---
    fun initLastPageNum() {
        lastPageNum =
            ceil(
                (getQueryRowCount().toDouble() / LIMIT.toDouble()),
            ).toInt() - 1
    }

    // -------------------------------------------------------------------------------
    // Returns the total number of rows/records of the query being paginated.
    // Used for calculating the number of pages the query needs.
    // Abstract/Virtual as query needs to be defined per PageOf Type
    // ---
    protected abstract fun getQueryRowCount(): Int

    // -------------------------------------------------------------------------------
    // Defines the DAO query to DB used to pull records into memory.
    // Abstract/Virtual as query needs to be defined per PageOf Type
    // ---
    protected abstract fun queryToDB(): Query
}

// -------------------------------------------------------------------------------
// Implementation of a PageOf Class as PageOfReleases
// Each PageOf class needs to define:
//      - display(), to define how the page is displayed to console
//      - getQueryRowCount(), to calculate the last page number
//      - queryToDB(), to define the DAO query to DB used to pull records into memory
// ---
class PageOfReleases(
    private val productName: String,
) : PageOf<ReleaseTable>() {
    override fun display() {
        for (releaseRecord in contents) {
            println(releaseRecord.releaseID)
        }
    }

    override fun getQueryRowCount(): Int {
        var numRecords = 0
        transaction {
            numRecords = ReleaseTable.count()
        }
        return numRecords
    }

    override fun queryToDB() {
        var output: Query
        transaction {
            output =
                Release
                    .find {
                        ReleaseTable.product eq productName
                    }.limit(LIMIT)
                    .offset(pagenum * LIMIT + OFFSET)
        }
        return output
    }
}
