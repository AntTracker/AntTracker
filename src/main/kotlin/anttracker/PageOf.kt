package anttracker
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
    protected var records: MutableList<IntEntity> = mutableListOf() // Container for object instances
    protected var pagenum: Int = 0 // NOTE: Page numbers are 0-indexed
    private var lastPageNum: Int = 0
    protected val queryOffset: Int = 0 // For page calculation in DB query
    protected val queryLimit: Int = 20 // Number of records per page.

    // -------------------------------------------------------------------------------
    // Prints to console a line-by-line display of objects contained in the PageOf instance.
    // How an individual record is printed will be defined by subclass.
    // If there are more pages to go, will indicate to user the no. of records to be seen.
    // ---
    fun display() {
        for (record in records) {
            printRecord(record)
        }
        if (!lastPage()) {
            println("<Enter> for ${countRemainingRecords()} more.")
        }
    }

    // -------------------------------------------------------------------------------
    // Queries to DB and pulls a page of records (max 20) into the records MutableList.
    // ---
    fun loadRecords() {
        records.clear()
        transaction {
            val queryOutput = queryToDB()
            if (queryOutput != null) {
                queryOutput.forEach { record ->
                    records.add(record)
                }
            } else {
                throw Exception("PageOf: Attempted to load from null query output.")
            }
        }
    }

    // -------------------------------------------------------------------------------
    // Returns the size/length of the records MutableList.
    // ---
    fun recordsSize(): Int = records.size

    // -------------------------------------------------------------------------------
    // Getter for elements in the records MutableList.
    // ---
    fun getContentAt(index: Int): IntEntity = records[index]

    // -------------------------------------------------------------------------------
    // Increments page number and loads associated DB records into records MutableList
    // Throws an error if attempting to load beyond the last page.
    // ---
    fun loadNextPage() {
        if (lastPage()) {
            throw Exception("PageOf: Already reached last page.")
        }
        pagenum++
        loadRecords()
    }

    // -------------------------------------------------------------------------------
    // TRUE if current page >= than the last page.
    // ---
    fun lastPage(): Boolean = pagenum >= lastPageNum

    // -------------------------------------------------------------------------------
    // Returns the number of yet-to-be-displayed records after the current page.
    // ---
    fun countRemainingRecords(): Int = getQueryRecordCount() - (queryLimit * (pagenum + 1))

    // -------------------------------------------------------------------------------
    // Used in init/ctor block to calculate the last page number.
    // As we are 0-indexing the page numbers, we subtract 1 at the end.
    // ---
    protected fun initLastPageNum() {
        lastPageNum =
            ceil(
                (getQueryRecordCount().toDouble() / queryLimit.toDouble()),
            ).toInt() - 1
    }

    // -------------------------------------------------------------------------------
    // Returns the total number of records/rows of the query being paginated.
    // Used for calculating the number of pages the query needs.
    // ---
    private fun getQueryRecordCount(): Int {
        var count: Int = -1
        transaction {
            val queryOutput: SizedIterable<IntEntity>? = queryToDB()
            if (queryOutput != null) {
                count = queryOutput.count().toInt()
            }
        }
        return count
    }

    // -------------------------------------------------------------------------------
    // Defines the DAO query to DB used to pull records into memory.
    // Abstract/Virtual as query needs to be defined per PageOf Type
    // ---
    protected abstract fun queryToDB(): SizedIterable<IntEntity>?

    // -------------------------------------------------------------------------------
    // Defines how a single record is printed to console.
    // Abstracted as this changes based on record type (Issue, Contact, Release, etc.)
    // ---
    protected abstract fun printRecord(record: IntEntity)
}
