package anttracker.issues

import anttracker.db.Issue
import anttracker.db.Release
import anttracker.db.Releases
import anttracker.release.next
import org.jetbrains.exposed.dao.with
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.format.DateTimeFormatter

private val noIssuesMatching =
    screenWithMenu {
        content { t ->
            t.printLine("There are no issues matching the criteria")
        }
    }

private fun editDescription(issue: Issue): Screen =
    screenWithMenu {
        var newDescription = ""
        content { t ->
            newDescription = t.prompt("Please enter description")
            printIssueSummary(t, issue)
            t.title("Update: Description")
            t.printLine("OLD: ${issue.description}")
            t.printLine("NEW: $newDescription")
        }
        option("Save") {
            val updatedIssue =
                transaction {
                    Issue.findByIdAndUpdate(issue.id.value) {
                        it.description = newDescription
                    }
                }
            require(updatedIssue != null)
            viewIssueMenu(updatedIssue)
        }
        option("Back") { viewIssueMenu(issue) }
    }

private fun printIssueSummary(
    t: Terminal,
    issue: Issue,
) {
    transaction {
        t.title("Summary")
        t.printLine("Description: ${issue.description}")
        t.printLine("Priority: ${issue.priority}")
        t.printLine("Status: ${issue.status}")
        t.printLine("AntRel: ${issue.anticipatedRelease.releaseId}")
        t.printLine("Created: ${issue.creationDate.format(formatter)}")
        t.printLine()
    }
}

private fun confirmNewRelease(
    newRelease: Release,
    issue: Issue,
): Screen =
    screenWithMenu {
        content { t ->
            transaction {
                printIssueSummary(t, issue)
                t.title("Update: Release")
                t.printLine("OLD: ${issue.anticipatedRelease.releaseId}")
                t.printLine("NEW: ${newRelease.releaseId}")
            }
        }
        option("Save") {
            val updatedIssue =
                transaction {
                    Issue.findByIdAndUpdate(issue.id.value) {
                        it.anticipatedRelease = newRelease
                    }
                }
            require(updatedIssue != null)
            viewIssueMenu(updatedIssue)
        }
        option("Back") { editAnticipatedRelease(issue) }
    }

private fun editAnticipatedRelease(issue: Issue): Screen =
    screenWithMenu {
        transaction {
            Release
                .find { Releases.product eq issue.product.id }
                .forEach { option(it.releaseId) { confirmNewRelease(it, issue) } }
        }
        promptMessage("Select the line corresponding to the new release id you want.")
    }

private val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")

private fun viewIssueMenu(issue: Issue): Screen =
    screenWithMenu {
        title("Issue #${issue.id}")
        transaction {
            option("Description: ${issue.description}") { editDescription(issue) }
            option("Priority: ${issue.priority}") { noIssuesMatching }
            option("Status: ${issue.status}") { noIssuesMatching }
            option("AntRel: ${issue.anticipatedRelease.releaseId}") { editAnticipatedRelease(issue) }
            option("Created: ${issue.creationDate.format(formatter)} (not editable)") { viewIssueMenu(issue) }
            option("Print") { noIssuesMatching }
        }
        promptMessage("Enter 1, 2, or 3 to edit the respective fields.")
    }

typealias RowToIssuePage = Map<Int, Issue>

private fun selectIssueToViewMenu(rows: RowToIssuePage) =
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
            // The user needs to choose from the choices that are `, 1, 2, 3, 4...
        }
    }

private fun viewIssueMenu(rows: RowToIssuePage) =
    screenWithMenu {
        title("View issue")
        content { t ->
            t.prompt(
                "Enter the row number of the issue you want to view",
                rows.keys.map { it.toString() },
            )
        }
    }

fun displayAllIssuesMenu(page: PageWithFilter): Screen =
    screenWithMenu {
        title("Search Results")
        option("Select filter") { mkIssuesMenu(page) }
        option("Print") { screenWithMenu { content { t -> t.printLine("Not currently implemented. In next version") } } }
        option("Next page") { displayAllIssuesMenu(page.next()) }
        option("View issue") {
            transaction {
                Issue
                    .all()
                    .with(Issue::anticipatedRelease)
                    .limit(page.pageInfo.limit, page.pageInfo.offset)
                    .zip(1..20) { issue, index -> index to issue }
                    .toMap()
            }.let {
                selectIssueToViewMenu(it)
            }
        }
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
