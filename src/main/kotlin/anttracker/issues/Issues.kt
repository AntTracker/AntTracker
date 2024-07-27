/* Issues.kt
Revision History
Rev 1 - 7/15/2024 Original by Eitan
-------------------------------------------
This file contains the issues menu and all the
submenus contained within the user will interact with.
---------------------------------
 */

package anttracker.issues

import anttracker.db.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// -------

/** ---
 * This menu serves as a placeholder for other menus which have
 * not been fully fleshed out.
--- */
internal val noIssuesMatching =
    screenWithMenu {
        content { t ->
            t.printLine("There are no issues matching the criteria")
        }
    }

/**
 * Represents how the date will be formatted.
 */
internal val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")

private fun requestToRow(
    request: Request, // in
): List<Any> =
    listOf(
        request.affectedRelease,
        request.requestDate,
        request.contact.name,
        request.contact.email,
        request.contact.department,
    )

private fun viewRequests(
    issue: Issue,
    page: PageOf<Request> = PageOf(),
): Screen =
    screenWithTable {
        table {
            columns(
                "Affected Release" to 17,
                "Date requested" to 14,
                "Name" to 32,
                "Email" to 24,
                "Department" to 12,
            )
            query {
                Request
                    .find { Requests.issue eq issue.id }
                    .limit(page.limit, page.offset)
                    .map(::requestToRow)
            }

            emptyMessage("No requests found.")
            nextPage { viewRequests(issue, page.next()) }
        }
        promptMessage("Press 1 to go to the next page. 2 to print.")
    }

// This data type represents the mapping between a row
// number and the issue corresponding to it
typealias RowToIssuePage = Map<Int, Issue>

/** ----
 * This function presents the issues the user can select along with a choice
 * to return to the main menu.
---- */
private fun selectIssueToViewMenu(
    rows: RowToIssuePage, // in
) =
    object : Screen {
        override fun run(t: Terminal): Screen? {
            t.printLine("== View issue ==")
            val mainMenuChoice = "`"
            val backToMainMenuMessage = " Or press ` (backtick) to go back to the main menu:"
            val response =
                t.prompt(
                    "Enter the row number of the issue you want to view.$backToMainMenuMessage",
                    rows.keys.map { it.toString() } + mainMenuChoice,
                )
            t.printLine()

            return when (response) {
                mainMenuChoice -> null
                else -> {
                    val index = Integer.parseInt(response)
                    return rows[index]?.let(::viewIssueMenu)
                }
            }
        }
    }

private fun fetchPageOfIssuesMatchingFilter(page: PageWithFilter): List<Issue> {
    val condition: Op<Boolean> =
        page.filters.fold(Op.TRUE) { op: Op<Boolean>, filter ->
            op.and(filter.toCondition())
        }

    return Products
        .join(
            Issues leftJoin Releases,
            joinType = JoinType.INNER,
            otherColumn = Issues.product,
            onColumn = Products.id,
        ).selectAll()
        .where { condition }
        .limit(page.pageInfo.limit, page.pageInfo.offset)
        .map { Issue.wrapRow(it) }
}

fun addOffset(numOfDays: Int): LocalDateTime = LocalDate.now().plusDays(numOfDays.toLong()).atStartOfDay()

private fun Status.toStr() =
    when (this) {
        Status.Assessed -> "Assessed"
        Status.Cancelled -> "Cancelled"
        Status.Created -> "Created"
        Status.Done -> "Done"
        Status.InProgress -> "In progress"
    }

private fun IssueFilter.toCondition(): Op<Boolean> =
    when (this) {
        is IssueFilter.ByDescription -> Issues.description like "%$description%"
        is IssueFilter.ByPriority -> Issues.priority eq priority.priority.toShort()
        is IssueFilter.ByAnticipatedRelease -> Releases.releaseId eq release
        is IssueFilter.ByProduct -> Products.name eq product
        is IssueFilter.ByStatus -> Issues.status eq status.toStr()
        is IssueFilter.ByDateCreated -> Issues.creationDate greaterEq addOffset(-days.numOfDays)
    }

/** -----
 * This function displays at most 20 issues from the DB which
 * passed the issue filter, giving the user four options: view the
 * next page of issues which passed the filter; select a specific
 * issue to view; return to the previous menu to change the filter;
 * return to the main menu
----- */
fun displayAllIssuesMenu(
    page: PageWithFilter, // in
): Screen =
    screenWithTable {
        title("Search Results")
        option("Select filter") { mkIssuesMenu(page) }
        option("View issue") { displayViewIssuesMenu(page) }
        table {
            columns("ID" to 7, "Description" to 30, "Priority" to 9, "Status" to 14, "AntRel" to 8, "Created" to 10)
            query { fetchPageOfIssuesMatchingFilter(page).map(::toRow) }
            emptyMessage("No issues found.")
            nextPage { displayAllIssuesMenu(page.next()) }
        }
    }

private fun displayViewIssuesMenu(page: PageWithFilter) =
    transaction {
        fetchPageOfIssuesMatchingFilter(page)
            .zip(1..20) { issue, index -> index to issue }
            .toMap()
    }.let(::selectIssueToViewMenu)

/** ------
 * This function takes an issue and extracts out all the
 * information contained within it.
----- */
private fun toRow(
    anIssue: Issue, // in
): List<Any?> {
    val elements = anIssue.anticipatedRelease?.releaseId
    return listOf(
        anIssue.id.value,
        anIssue.description,
        anIssue.priority,
        anIssue.status,
        elements,
        anIssue.creationDate,
        anIssue.product.name,
    )
}

val searchByOptions =
    mapOf(
        "Description" to ::searchByDescriptionMenu,
        "Product" to ::searchByProductMenu,
        "Anticipated release" to ::searchByAnticipatedReleaseMenu,
        "Status" to ::searchByStatusMenu,
        "Priority" to ::searchByPriorityMenu,
        "Date range" to ::searchByDaysSinceMenu,
    )

private fun IssueFilter.toLabel(): String =
    when (this) {
        is IssueFilter.ByDescription -> "Description: ${this.description}"
        is IssueFilter.ByPriority -> "Priority: ${this.priority}"
        is IssueFilter.ByProduct -> "Product: ${this.product}"
        is IssueFilter.ByAnticipatedRelease -> "Release: ${this.release}"
        is IssueFilter.ByStatus -> "Status: ${this.status}"
        is IssueFilter.ByDateCreated -> "Date created: within the last ${this.days.numOfDays} days"
    }

/** ------
This function prints out a message asking the user how they would like
to search for an issue.
----- */
fun mkIssuesMenu(
    page: PageWithFilter, // in
): Screen =
    screenWithMenu {
        title("VIEW/EDIT ISSUE")
        promptMessage("Please select search category.")
        searchByOptions.forEach { (column, action) ->
            option("Search by $column") { action(page) }
        }
        option("Display all issues") { displayAllIssuesMenu(page) }
        option("Clear filters") { mkIssuesMenu(PageWithFilter()) }
        content { t ->
            val activeFilters =
                page.filters.map(IssueFilter::toLabel).takeUnless { it.isEmpty() } ?: listOf("No filters")
            t.printLine("Filters Active: ${activeFilters.joinToString(", ")}")
        }
    }

// This represents the default state of the issues menu with
// no filter selected yet by the user
internal val issuesMenu = mkIssuesMenu(PageWithFilter())

/** ----
 * This function displays the issues menu and transitions to the following menu the
 * user selects.
------ */
fun mainIssuesMenu() {
    val t = Terminal()
    var currentScreen: Screen? = issuesMenu

    while (currentScreen != null) {
        t.printLine()
        t.printLine("/\\".repeat(40))
        t.printLine()

        currentScreen = currentScreen.run(t)
    }
}
