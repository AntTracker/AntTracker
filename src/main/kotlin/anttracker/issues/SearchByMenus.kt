package anttracker.issues

import anttracker.db.IssueDescription
import anttracker.db.Priority
import anttracker.db.Product
import anttracker.db.Release
import org.jetbrains.exposed.sql.transactions.transaction

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

class SearchByOrGoBackToIssuesMenu(
    private val page: PageWithFilter,
    private val target: String,
    private val options: List<String>,
    private val prompt: String,
    private val createFilter: (filter: String) -> IssueFilter?,
    private val isValidChoice: (String) -> Boolean,
) : ScreenWithTitle("Search by $target") {
    constructor(
        page: PageWithFilter,
        target: String,
        createFilter: (filter: String) -> IssueFilter?,
        validationFn: (String) -> Boolean,
    ) : this(page, target, emptyList(), "", createFilter, validationFn)

    constructor(
        page: PageWithFilter,
        target: String,
        options: List<String>,
        createFilter: (filter: String) -> IssueFilter?,
    ) : this(page, target, options, "", createFilter)

    constructor(
        page: PageWithFilter,
        target: String,
        options: List<String>,
        prompt: String,
        createFilter: (filter: String) -> IssueFilter?,
    ) : this(page, target, options, prompt, createFilter, { true })

    override fun displayBody(t: Terminal): Screen {
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
                    t.prompt(message, isValidChoice)
                }

            t.printLine()

            if (input.isBlank()) {
                t.printLine("Going back to the issues menu...")
                return mkIssuesMenu(page)
            }

            filter = createFilter(input)
        }

        t.printLine("Searching for issues matching '$filter'...")
        return displayAllIssuesMenu(page.addFilter(filter))
    }
}

fun searchByProductMenu(page: PageWithFilter): Screen =
    SearchByOrGoBackToIssuesMenu(
        page,
        "product",
        transaction { Product.all().map { it.name } },
    ) {
        IssueFilter.ByProduct(it)
    }

fun searchByDescriptionMenu(page: PageWithFilter) =
    SearchByOrGoBackToIssuesMenu(
        page,
        "description (1-${IssueDescription.MAX_LENGTH} characters)",
        IssueFilter::ByDescription,
        IssueDescription::isValid,
    )

fun searchByAnticipatedReleaseMenu(page: PageWithFilter) =
    SearchByOrGoBackToIssuesMenu(
        page,
        "anticipated release",
        transaction { Release.all().map { it.releaseId } },
        IssueFilter::ByAnticipatedRelease,
    )

fun searchByStatusMenu(page: PageWithFilter) =
    SearchByOrGoBackToIssuesMenu(
        page,
        "statuses",
        emptyList(),
        "Enter all the statuses to search for separated by commas (Assessed, Created, Done, Cancelled, InProgress)",
    ) { input: String ->
        splitStatuses(input).sequence(::parseStatus)?.takeIf { it.isNotEmpty() }?.let(IssueFilter::ByStatus)
    }

private fun <T, R> List<T>.sequence(f: (T) -> R?): List<R>? = this.mapNotNull(f).takeIf { it.size == this.size }

private fun splitStatuses(statuses: String): List<String> = statuses.split(",").map { it.trim() }

private fun parseStatus(input: String): Status? =
    when (input) {
        "Assessed" -> Status.Assessed
        "Created" -> Status.Created
        "Done" -> Status.Done
        "Cancelled" -> Status.Cancelled
        "InProgress" -> Status.InProgress
        else -> null
    }

fun searchByPriorityMenu(page: PageWithFilter) =
    SearchByOrGoBackToIssuesMenu(
        page,
        "priority",
        (1..5).map(Int::toString),
    ) { input -> Priority(input.toInt()).let(IssueFilter::ByPriority) }

fun searchByDaysSinceMenu(page: PageWithFilter) =
    SearchByOrGoBackToIssuesMenu(
        page,
        "created within the last n days",
        (0..100).map(Int::toString),
        "Enter a day to indicate how far back you want to search",
    ) { input -> Days(input.toInt()).let(IssueFilter::ByDateCreated) }
