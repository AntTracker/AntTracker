/* PageOf.kt
Revision History:
Rev. 1 - 2024/07/15 Original by T. Tracey
----------------------------------------------------------
The PageOf module provides an abstract base class to create paginated queries for records in the database.
Subclasses must define 3 things:
    1. init{} - MUST call initLastPageNum(). Would've been nice to put in the base class, but that would
                 have it trigger BEFORE any subclass init/ctors. If the last page number calculation is
                 dependent on any subclass properties, this would be undefined behaviour.
    2. getQuery() - a DAO query representing the whole record set you want to pull. Other base class
                        functions will handle paginating from this set.
    3. printRecord() - how to print a single record to the console.
----------------------------------------------------------
*/

package anttracker
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.math.ceil

// A PageOf objects contains a max-20 element list of T.
// Provides top-level functionality for:
//  - loading DB records into memory (one page at a time)
//  - accessing records
//  - displaying records as a page
//  - going to the next page and loading its records
abstract class PageOf<T : IntEntity>(
    private val entityClass: IntEntityClass<T>,
) {
    protected var records: List<T> = emptyList() // Container for object instances
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
        var linenumber = 0
        for (record in records) {
            print(linenumber++.toString().padEnd(4))
            printRecord(record)
        }
        if (!lastPage()) {
            println("<Enter> for ${countRemainingRecords()} more.")
        }
    }

    // -------------------------------------------------------------------------------
    // Queries to DB and pulls a page of records (max 20) into the records List.
    // ---
    fun loadRecords() {
        records = emptyList()
        transaction {
            records = fetchPageOfRecords()
        }
    }

    // -------------------------------------------------------------------------------
    // Returns the size/length of the records.
    // ---
    fun recordsSize(): Int = records.size

    // -------------------------------------------------------------------------------
    // Getter for elements in the records.
    // ---
    fun getContentAt(index: Int): T = records[index]

    // -------------------------------------------------------------------------------
    // Increments page number and loads associated DB records into records.
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
    private fun getQueryRecordCount(): Int =
        transaction {
            getQuery().count().toInt()
        }

    // -------------------------------------------------------------------------------
    // Pulls a page of records (max 20) from DB to memory.
    // ---
    protected fun fetchPageOfRecords(): List<T> =
        transaction {
            getQuery()
                .limit(n = queryLimit, offset = (pagenum * queryLimit + queryOffset).toLong())
                .map { entityClass.wrapRow(it) }
        }

    // -------------------------------------------------------------------------------
    // Defines the DAO query to DB used to pull records into memory and for page calculation.
    // Abstract/Virtual as query needs to be defined per PageOf Type
    // ---
    protected abstract fun getQuery(): Query

    // -------------------------------------------------------------------------------
    // Defines how a single record is printed to console.
    // Will be called on each element of records List to print the whole page to console.
    // Abstracted as this changes based on record type (Issue, Contact, Release, etc.)
    // ---
    protected abstract fun printRecord(record: T)
}
