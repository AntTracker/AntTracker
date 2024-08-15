package anttracker.issues

/** ---
 * Generates a label for the issue filter.
--- */
private fun IssueFilter.toLabel(): String =
    when (this) {
        is IssueFilter.ByDescription -> "Description: ${this.description}"
        is IssueFilter.ByPriority -> "Priority: ${this.priority}"
        is IssueFilter.ByProduct -> "Product: ${this.product}"
        is IssueFilter.ByAnticipatedRelease -> "Release: ${this.release}"
        is IssueFilter.ByStatus -> "Status: ${this.statuses.joinToString(", ")}"
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
