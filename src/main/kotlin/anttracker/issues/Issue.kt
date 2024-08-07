/* Issue.kt
Revision History
Rev. 1 - 2024/6/30 Original by Eitan Barylko
Rev. 2 - 2024/7/15 By Eitan Barylko
        - Added next extension function in order to update the offset of the PageWithFilter
Rev. 3 - 2024/7/30 By Eitan Barylko
-------------------------------------------------------------------------------
This file contains the abstraction of the information of an
issue into a data type representing this information.
Furthermore, Specific operations on issues, such as creating, editing
and searching for issues are also present in this file.
-------------------------------------------------------------------------------
 */

package anttracker.issues

import anttracker.db.Issue
import anttracker.db.IssueDescription
import anttracker.db.Priority

// ------

/** ---
 * This value class represents a valid description the user can have for an issue,
 * being 1-30 characters long.
--- */
@JvmInline
value class Description(
    private val description: String,
) {
    init {
        require(description.length in 1..30) {
            "Description length must be between 1 and 30 characters"
        }
    }
}

/**
 * Represents a status an issue can have
 */
sealed class Status {
    data object Created : Status()

    data object Assessed : Status()

    data object InProgress : Status()

    data object Done : Status()

    data object Cancelled : Status()

    companion object {
        /**
         * Generates a list of all the possible statuses an issue can have.
         */
        fun all() = arrayOf(Created, Assessed, InProgress, Done, Cancelled)
    }
}

/** ---
 * This value class represents a valid number of days one can look back in order
 * to find the newly created issues in this time period, being a non-negative
 * number of days.
--- */
@JvmInline
value class NumberOfDays(
    val numOfDays: Int,
) {
    init {
        require(numOfDays >= 0) {
            "The number of days must be non-negative"
        }
    }
}

/** ---
 * This class represents what an issue
 * can be filtered by.
--- */
sealed interface IssueFilter {
    /** ---
     * Represents a filter that uses the description.
     --- */
    data class ByDescription(
        val description: IssueDescription,
    ) : IssueFilter

    /** ---
     * Represents a filter that uses the anticipated release
     --- */
    data class ByAnticipatedRelease(
        val release: String,
    ) : IssueFilter

    /** ---
     * Represents a filter that uses the priority
     --- */
    data class ByPriority(
        val priority: Priority,
    ) : IssueFilter

    /** ---
     * Represents a filter that uses the product.
     --- */
    data class ByProduct(
        val product: String,
    ) : IssueFilter

    /** ---
     * Represents a filter that uses the status
     --- */
    data class ByStatus(
        val statuses: List<Status>,
    ) : IssueFilter

    /** ---
     * Represents a filter that uses the date an
     * issue was created.
     --- */
    data class ByDateCreated(
        val days: NumberOfDays,
    ) : IssueFilter
}

/** ---
 * This class represents a page of items
 * fetched from the database, recording
 * how many records to ignore at the beginning
 * and how many records to keep
--- */
data class PageOf<T>(
    val page: List<T> = emptyList(),
    val offset: Long = 0,
    val limit: Int = 20,
)

/** ---
 * Returns a new page with an updated offset.
--- */
fun <T> PageOf<T>.next() = this.copy(offset = this.offset + this.limit)

/** ---
 * This class represents a page of issues that
 * includes a filter
--- */
data class PageWithFilter(
    val filters: List<IssueFilter> = emptyList(),
    val pageInfo: PageOf<Issue> = PageOf(),
)

/** ---
 * Adds a new filter to the current set of filters.
--- */
fun PageWithFilter.addFilter(
    newFilter: IssueFilter, // in
): PageWithFilter = PageWithFilter(filters = filters + newFilter)

/** ---
 * This function generates the next page, updating the offset.
--- */
fun PageWithFilter.next(): PageWithFilter = this.copy(pageInfo = pageInfo.copy(offset = pageInfo.offset + 20))
