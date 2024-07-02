/*
Issues.kt
-----------------
Revision History
Rev 1 - 6/30/2024 Modified by Eitan
        - Added Issue data type and functions operating on issues

-------------------------------------------
This file contains the abstraction of the information of an
issue into a data type representing this information.
Furthermore, Specific operations on issues, such as creating, editing
and searching for issues are also present in this file.
---------------------------------
 */

// ------
import kotlin.time.TimeSource

// ------

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

@JvmInline
value class Priority(
    private val priority: Int,
) {
    init {
        require(priority in 1..5) {
            "Priority of an issue must be between 1 and 5"
        }
    }
}

data class IssueInformation(
    val description: Description,
    val productName: Product,
    val affectedRelease: ReleaseId,
    val anticipatedRelease: ReleaseId? = null,
    val priority: Priority,
)

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

data class Issue(
    val id: IssueId,
    val information: IssueInformation,
    val createdAt: TimeSource,
)

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

sealed class Status {
    data object Assessed : Status()

    data object Created : Status()

    data object Done : Status()

    data object Cancelled : Status()

    data object InProgress : Status()
}

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
        val product: Product,
    ) : IssueFilter()

    data class Composite(
        val filter: List<IssueFilter>,
    ) : IssueFilter()
}

data class PageOf<T>(
    val page: List<T>,
    val offset: Int,
    val limit: Int,
)

data class IssuePage(
    val filter: IssueFilter,
    val pageInfo: PageOf<Issue>,
)

data class RequestPage(
    val pageInfo: PageOf<Request>,
)

// -----------------

/* ---------
This function takes a predicate to be applied on an issue
and returns the first issue in the database which satisfy the predicate.
 An example usage of the function is demonstrated below.
searchIssues(isDone) will return the first issues in the database
that is marked as done.
--------*/
fun searchIssues(
    filter: IssueFilter, // in
): IssuePage {
    TODO()
}

/**
 * This function returns the next page of issues to display based on the current page
 */
fun nextPage(
    oldPage: IssuePage, // in
): IssuePage {
    TODO()
}

/* -----
//This function takes a predicate to apply on an issue and the maximum number of issues to
//display at a time and returns the first issue from the database which satisfies the predicate
//For example, searchIssues(createdYesterday, 10) will return the first issue from the database
which was created yesterday
// ----- */
fun searchIssues(
    filter: IssueFilter, // in
    issuesPerPage: Int, // in
): IssuePage {
    TODO()
}

/* -----
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

/* -----
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

/* ------
This function prints out a message asking the user how they would like
to search for an issue.
----- */
fun issuesMenu() {
    TODO()
}

/** ---
 * This function collects from the user all the information needed to create an issue.
---- */
fun enterIssueInformation(): IssueInformation {
    TODO()
}

/**
 * This function takes the information given and creates a new issue
 * using the passed information. The issue is then added to the
 * database. For example, saveIssue(IssueInformation("hi", "a", "1", "1.2", 2)) will
 * create an issue with the description "hi" belonging to product "a"
 * with an affectedRelease of "1" and a priority of 2 in the database.
 */
fun saveIssue(
    information: IssueInformation, // in
): Issue {
    TODO()
}
