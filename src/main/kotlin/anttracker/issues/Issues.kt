package anttracker.issues

import anttracker.Issue
import org.jetbrains.exposed.sql.transactions.transaction

private val noIssuesMatching =
    screenWithMenu {
        content { t ->
            t.printLine("There are no issues matching the criteria")
        }
    }

fun displayAllIssuesMenu(page: PageWithFilter): Screen =
    screenWithMenu {
        title("Search Results")
        option("Select filter") { mkIssuesMenu(page) }
        option("Print") { screenWithMenu { content { t -> t.printLine("Not currently implemented. In next version") } } }
        option("Next page") { displayAllIssuesMenu(page.next()) }

        val columns =
            listOf("ID" to 2, "Description" to 30, "Priority" to 9, "Status" to 14, "AntRel" to 8, "Created" to 10)
        content { t ->
            transaction {
                Issue
                    .all()
                    .limit(page.pageInfo.limit, page.pageInfo.offset)
                    .map(::toRow)
                    .let {
                        if (it.isEmpty()) {
                            t.printLine("No more issues")
                        } else {
                            t.displayTable(columns, it)
                        }
                    }
            }
        }
    }

private fun toRow(anIssue: Issue): List<Any> {
    val elements = anIssue.anticipatedRelease.releaseId
    return listOf(
        anIssue.id.value,
        anIssue.description,
        anIssue.priority,
        anIssue.status,
        elements,
        anIssue.creationDate,
    )
}

/** ------
This function prints out a message asking the user how they would like
to search for an issue.
----- */
fun mkIssuesMenu(page: PageWithFilter): Screen =
    screenWithMenu {
        title("VIEW/EDIT ISSUE")
        promptMessage("Please select search category.")
        option("Search by description") { screenWithMenu { content { t -> t.printLine("There are no issues at the moment") } } }
        option("Search by Product") { screenWithMenu { content { t -> t.printLine("There are no products at the moment") } } }
        option("Search by Affected release") { noIssuesMatching }
        option("Search by Anticipated release") { noIssuesMatching }
        option("Search by status") { noIssuesMatching }
        option("Search by priority") { noIssuesMatching }
        option("Display all issues") { displayAllIssuesMenu(page) }
        option("Clear filters") { mkIssuesMenu(page.copy(filter = IssueFilter.NoFilter)) }
    }

internal val issuesMenu = mkIssuesMenu(PageWithFilter())

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
