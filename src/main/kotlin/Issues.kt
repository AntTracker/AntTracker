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

import kotlin.time.*

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
value class Product(
    private val name: String,
) {
    init {
        require(name.length in 1..30) {
            "Name length must be between 1 and 30 characters"
        }
    }
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
value class IssueFilter(
    val predicate: (Issue) -> Boolean,
)

// -----------------

/* ---------
This function takes a predicate to be applied on an issue
and returns the issues in the database which satisfy the predicate.
The predicate passed to the function can be a composition of other
predicates. An example usage of the function is demonstrated below.
searchIssues(isDone) will return all the issues in the database
that have done as their status.
--------*/
fun searchIssues(
    filter: IssueFilter, // in
): List<Issue> {
    TODO()
}

@JvmInline
value class IssuePage(
    val page: List<Issue>,
)

data class IssuePages(
    val pages: List<IssuePage>,
    val currentPage: Int,
)

/* -----
This function takes a predicate to apply on an issue and the maximum number of issues to
display at a time and returns all the issues from the database which satisfy the predicate
in pages of size issuesPerPage. For example, listIssues(createdYesterday, 10) will return
all the issues created yesterday partitioned into groups of ten.
 ----- */
fun listIssues(
    filter: IssueFilter, // in
    issuesPerPage: Int, // in
): IssuePages {
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
This function takes an issue id I and prints out all the requests associated with
the issue that has id I.
 ------ */
fun viewRequests(
    issueId: Int, // in
) {
}

/* ------
This function prints out a message asking the user how they would like
to search for an issue.
----- */
fun issuesMenu() {}

/**
 * This function takes the information given and creates a new issue
 * using the passed information. The issue is then added to the
 * database. For example, createIssue("hi", "a", "1", "1.2", 2) will
 * create an issue with the description "hi" belonging to product "a"
 * with an affectedRelease of "1" and a priority of 2 in the database.
 */
fun saveIssue(
    information: IssueInformation, // in
): Issue {
    TODO()
}
