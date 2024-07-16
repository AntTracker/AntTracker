/* Issues.kt
Revision History
Rev 1 - 7/15/2024 Original by Eitan
-------------------------------------------
This file contains the issues menu and all the
submenus contained within the user will interact with.
---------------------------------
 */

package anttracker.issues

import anttracker.db.Issue
import anttracker.db.Release
import anttracker.db.Releases
import org.jetbrains.exposed.dao.with
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.format.DateTimeFormatter

// -------

/**
 * This menu serves as a placeholder for other menus which have
 * not been fully fleshed out.
 */
private val noIssuesMatching =
    screenWithMenu {
        content { t ->
            t.printLine("There are no issues matching the criteria")
        }
    }

/** ----
 * This function displays a screen where the user is presented with the
 * option of saving their issue with an edited description or going back
 * to the previous menu.
----- */
private fun editDescription(
    issue: Issue, // in
): Screen =
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

/** ----
 * This function prints out all the information contained within an issue.
----- */
private fun printIssueSummary(
    t: Terminal, // in
    issue: Issue, // in
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

/** ----
 * This function presents a screen where the user is given a choice of saving their
 * issue with an updated anticipated release or going back to the previous menu.
----- */
private fun confirmNewRelease(
    newRelease: Release, // in
    issue: Issue, // in
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

/** ----
 * This function shows the release versions the user can pick from
 * for the updated value of the anticipated release.
----- */
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

/** ----
 * This function shows all the information present within the passed issue
 * and prompts the user to edit either the description, priority, status,
 * or anticipated release
----- */
private fun viewIssueMenu(
    issue: Issue, // in
): Screen =
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
            // The user needs to choose from the choices that are `, 1, 2, 3, 4...
        }
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

/** ------
 * This function takes an issue and extracts out all the
 * information contained within it.
----- */
private fun toRow(
    anIssue: Issue, // in
): List<Any> {
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
fun mkIssuesMenu(
    page: PageWithFilter, // in
): Screen =
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
