/* Issues.kt
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
import anttracker.product.ProductName
import anttracker.release.ReleaseId
import anttracker.db.Request

// ------

/**
 * This value class represents a valid description the user can have for an issue,
 * being 1-30 characters long.
 */
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

data class IssueInformation(
    val description: Description,
    val productName: ProductName,
    val affectedRelease: ReleaseId,
    val anticipatedRelease: ReleaseId? = null,
    val priority: Priority,
)

/**
 * This data class represents the valid values an issue id can take on,
 * being between 1-99.
 */
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

/**
 * This value class represents a valid number of days one can look back in order
 * to find the newly created issues in this time period, being a non-negative
 * number of days.
 */
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

/**
 * This class represents one of the possible status's an issue can take on.
 */
sealed class Status {
    data object Assessed : Status()

    data object Created : Status()

    data object Done : Status()

    data object Cancelled : Status()

    data object InProgress : Status()
}

/**
 * This
 */
sealed class IssueFilter {
    data class ByDescription(
        val description: Regex,
    ) : IssueFilter()

    data class SinceDaysAgo(
        val days: Days,
    )

    data class ByPriority(
        val priority: Priority,
    ) : IssueFilter()

    data class ByStatus(
        val status: Status,
    ) : IssueFilter()

    data class ById(
        val id: IssueId,
    ) : IssueFilter()

    data class ByProduct(
        val product: ProductName,
    ) : IssueFilter()

    data object NoFilter : IssueFilter()

    data class Composite(
        val filter: List<IssueFilter>,
    ) : IssueFilter()
}

data class PageOf<T>(
    val page: List<T> = emptyList(),
    val offset: Long = 0,
    val limit: Int = 20,
)

data class PageWithFilter(
    val filter: IssueFilter = IssueFilter.NoFilter,
    val pageInfo: PageOf<Issue> = PageOf(),
)

/**
 * This function takes an old PageWith
 */
fun PageWithFilter.next(): PageWithFilter = this.copy(pageInfo = pageInfo.copy(offset = pageInfo.offset + 20))

data class RequestPage(
    val pageInfo: PageOf<Request>,
)

// -----------------

/**
 * This function takes in issue information and saves it into the database
 * Returns an Issue object containing the id and createdAt fields populated,
 * according to what was returned by the database
 */
fun saveIssue(issueInformation: IssueInformation): Issue? {
    TODO()
}

/**
 * This function returns the next page of issues to display based on the current page
 */
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

/** -------
 * This function takes a predicate to apply on an issue, the number of issues to show per
 * page N, and shows the issues from the database which satisfy the predicate in pages of
 * size N.
 * For example, calling displayIssues(hasLowPriority, 3) will show all the issues
 * with a low priority in pages containing only three issues
------- */
fun selectIssue(
    filter: IssueFilter, // in
    issuesPerPage: Int, // in
): Issue? {
    TODO()
}

/** -----
This function takes the edited issue and stores it in the database,
overwriting the old version of the issue.
For example, editIssue(Issue(3, "a", "b", "c", "d", 1, TimeStamp('2024-2-1)))
would go to issue 3 in the database and overwrite it with the content of the
issue passed to it.
------ */
fun editIssue(
    newIssue: Issue, // in
) {
    TODO()
}

/** -----
This function takes an issue id I, the number of requests to show per page, and
returns the first request associated with issue I. For example, listRequests(1, 5)
will obtain all the first request associated with the issue having id 1.
------ */
fun listRequests(
    issueId: Int, // in
    requestsPerPage: Int, // in
): RequestPage {
    TODO()
}

/**
 * This function returns the next page of requests to display based on the current page
 */
fun nextPage(
    oldPage: RequestPage, // in
): RequestPage {
    TODO()
}

/** ------
This function prints out a message asking the user how they would like
to search for an issue.
----- */
fun menu() {
    TODO()
}

/** ---
 * This function collects from the user all the information needed to create an issue
by prompting them for the description, product, affectedRelease, and priority.
 * Returns null if the user (somehow) indicates to leave: optional
---- */
fun enterIssueInformation(): IssueInformation? {
    TODO()
}
