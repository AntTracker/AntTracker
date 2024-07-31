/* ScreenWithTable.kt
Revision History
Rev. 1 - 2024/7/30 Original by Eitan Barylko
-------------------------------------------------------------------------------
This file contains all the menus used
when filtering an issue by a collection of
its attributes.
-------------------------------------------------------------------------------
*/

package anttracker.issues

import anttracker.db.IssueDescription
import anttracker.db.Priority
import anttracker.db.Product
import anttracker.db.Release
import anttracker.issues.IssueFilter.*
import org.jetbrains.exposed.sql.transactions.transaction

// -----

/** ---
 * Represents a screen containing a title
--- */
abstract class ScreenWithTitle(
    theTitle: String? = null,
) : Screen {
    private var menuTitle: String? = theTitle

    override fun run(t: Terminal): Screen? {
        menuTitle?.let(t::title)
        return this.displayBody(t)
    }

    abstract fun displayBody(t: Terminal): Screen?
}

/** ---
 * Represents a menu for filtering an issue by one of its attribute.
--- */
class SearchByOrGoBackToIssuesMenu(
    private val page: PageWithFilter,
    private val target: String,
    private val options: List<String>,
    private val prompt: String,
    private val createFilter: (filter: String) -> IssueFilter?,
) : ScreenWithTitle("Search by $target") {
    constructor(
        page: PageWithFilter,
        target: String,
        options: List<String>,
        createFilter: (filter: String) -> IssueFilter?,
    ) : this(page, target, options, "", createFilter)

    /**
     * Shows a menu displaying the options the user has for
     * filtering the issue.
     */
    override fun displayBody(
        t: Terminal, // in
    ): Screen {
        var filter: IssueFilter? = null

        val endOfMessage = "or leave it empty to go back to the issues menu"
        val message =
            when (prompt) {
                "" -> "Please enter a $target to search for $endOfMessage"
                else -> "$prompt $endOfMessage"
            }

        while (filter == null) {
            val input =
                if (options.isNotEmpty()) {
                    t.prompt(message, options + "")
                } else {
                    t.prompt(message, true) { createFilter(it) != null }
                }

            t.printLine()

            if (input.isBlank()) {
                t.printLine("Going back to the issues menu...")
                return mkIssuesMenu(page)
            }

            filter = createFilter(input)
        }

        return displayAllIssuesMenu(page.addFilter(filter))
    }
}

/** ---
 * Generates a label for the issue filter.
--- */
internal fun IssueFilter.toLabel(): String =
    when (this) {
        is ByDescription -> "Description: ${this.description}"
        is ByPriority -> "Priority: ${this.priority}"
        is ByProduct -> "Product: ${this.product}"
        is ByAnticipatedRelease -> "Release: ${this.release}"
        is ByStatus -> "Status: ${this.statuses.joinToString(", ")}"
        is ByDateCreated -> "Date created: within the last ${this.days.numOfDays} days"
    }

/** ----
 * Represents the menu used when filtering an issue by its product
---- */
fun searchByProductMenu(
    page: PageWithFilter, // in
): Screen =
    SearchByOrGoBackToIssuesMenu(
        page,
        "product",
        transaction { Product.all().map { it.name } },
    ) {
        IssueFilter.ByProduct(it)
    }

/** ---
 * Represents the menu used when filtering an issue by its description
--- */
fun searchByDescriptionMenu(
    page: PageWithFilter, // in
) =
    SearchByOrGoBackToIssuesMenu(
        page,
        "description (1-${IssueDescription.MAX_LENGTH} characters)",
        emptyList(),
    ) { IssueDescription.maybeParse(it)?.let(IssueFilter::ByDescription) }

/** ---
 * Represents the menu used when filtering an issue by its anticipated release
--- */
fun searchByAnticipatedReleaseMenu(
    page: PageWithFilter, // in
) =
    SearchByOrGoBackToIssuesMenu(
        page,
        "anticipated release",
        transaction { Release.all().map { it.releaseId } },
        IssueFilter::ByAnticipatedRelease,
    )

/** ---
 * Represents the menu used when filtering an issue by its status
--- */
fun searchByStatusMenu(
    page: PageWithFilter, // in
) =
    SearchByOrGoBackToIssuesMenu(
        page,
        "statuses",
        emptyList(),
        Status
            .all()
            .joinToString(", ")
            .let { "Enter all the statuses to search for separated by commas ($it)" },
    ) { input: String ->
        splitStatuses(input).sequence(::parseStatus)?.takeIf { it.isNotEmpty() }?.let(IssueFilter::ByStatus)
    }

/** ---
 * Returns the transformed collection if all elements are not null
--- */
private fun <T, R> List<T>.sequence(
    f: (T) -> R?, // in
): List<R>? = this.mapNotNull(f).takeIf { it.size == this.size }

/** ---
 * Splits the given potential statuses
--- */
private fun splitStatuses(
    statuses: String, // in
): List<String> = statuses.split(",").map { it.trim() }

/** ---
 * Parses the given status
--- */
private fun parseStatus(
    input: String, // in
): Status? =
    when (input) {
        "Assessed" -> Status.Assessed
        "Created" -> Status.Created
        "Done" -> Status.Done
        "Cancelled" -> Status.Cancelled
        "InProgress" -> Status.InProgress
        else -> null
    }

/**
 * Represents the menu used when filtering an issue by its priority
 */
fun searchByPriorityMenu(
    page: PageWithFilter, // in
) =
    SearchByOrGoBackToIssuesMenu(
        page,
        "priority",
        (1..5).map(Int::toString),
    ) { input -> Priority(input.toInt()).let(IssueFilter::ByPriority) }

/**
 * Represents the menu used when filtering an issue by how recently it was created
 */
fun searchByDaysSinceMenu(
    page: PageWithFilter, // in
) =
    SearchByOrGoBackToIssuesMenu(
        page,
        "created within the last n days",
        emptyList(),
        "Enter how many days back to search for (non-negative number)",
    ) { candidate ->
        candidate
            .takeIf { it.matches(Regex("""\d+""")) }
            ?.toInt()
            ?.let(::NumberOfDays)
            ?.let(IssueFilter::ByDateCreated)
    }
