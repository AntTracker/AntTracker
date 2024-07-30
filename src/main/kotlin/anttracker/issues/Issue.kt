/* Issue.kt
Revision History
Rev 1 - 6/30/2024 Original by Eitan
Rev 2 - 7/15/2024 by Eitan
        - Added next extension function in order to update the offset of the PageWithFilter
-------------------------------------------
This file contains the abstraction of the information of an
issue into a data type representing this information.
Furthermore, Specific operations on issues, such as creating, editing
and searching for issues are also present in this file.
---------------------------------
 */

package anttracker.issues

import anttracker.db.Issue
import anttracker.db.Priority
import anttracker.db.Request
import anttracker.release.ReleaseId

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

/** ---
 * This class represents the information of an issue before saving
 * it to the DB.
--- */
data class IssueInformation(
    val description: Description,
    val productName: String,
    val affectedRelease: ReleaseId,
    val anticipatedRelease: ReleaseId? = null,
    val priority: Priority,
)

sealed class Status {
    data object Created : Status()

    data object Assessed : Status()

    data object InProgress : Status()

    data object Done : Status()

    data object Cancelled : Status()
}

/** ---
 * This data class represents the valid values an issue id can take on,
 * being between 1-99.
--- */
@JvmInline
value class IssueId(
    private val id: Int,
) {
    init {
        require(id in 1..<100) {
            "Id must be a positive integer within [1, 99]"
        }
    }
}

/** ---
 * This value class represents a valid number of days one can look back in order
 * to find the newly created issues in this time period, being a non-negative
 * number of days.
--- */
@JvmInline
value class Days(
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
sealed class IssueFilter {
    /** ---
     * Represents a filter that uses the description.
     --- */
    data class ByDescription(
        val description: Regex,
    ) : IssueFilter()

    /** ---
     * Represents a filter that uses the product.
     --- */
    data class ByProduct(
        val product: String,
    ) : IssueFilter()

    /** ---
     * Represents the lack of a filter.
     --- */
    data object NoFilter : IssueFilter()
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
 * This class represents a page of issues that
 * includes a filter
--- */
data class PageWithFilter(
    val filter: IssueFilter = IssueFilter.NoFilter,
    val pageInfo: PageOf<Issue> = PageOf(),
)

/** ---
 * This function generates the next page, updating the offset.
--- */
fun PageWithFilter.next(): PageWithFilter = this.copy(pageInfo = pageInfo.copy(offset = pageInfo.offset + 20))

/** ---
 *  Represents a page of requests.
--- */
data class RequestPage(
    val pageInfo: PageOf<Request>,
)

// -----------------

/** ---
 * This function takes in issue information and saves it into the database
 * Returns an Issue object containing the id and createdAt fields populated,
 * according to what was returned by the database
--- */
fun saveIssue(issueInformation: IssueInformation): Issue? {
    TODO()
}

/** ---
 * This function returns the next page of issues to display based on the current page
--- */
fun nextPage(
    oldPage: PageWithFilter, // in
): PageWithFilter {
    TODO()
}

/** -----
//This function takes a predicate to apply on an issue and the maximum number of issues to
//display at a time and returns the first issue from the database which satisfies the predicate
//For example, searchIssues(createdYesterday, 10) will return the first issue from the database
which was created yesterday
// ----- */
fun searchIssues(
    filter: IssueFilter, // in
    issuesPerPage: Int, // in
): PageWithFilter {
    TODO()
}