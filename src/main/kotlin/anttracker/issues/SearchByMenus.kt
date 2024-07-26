package anttracker.issues

import anttracker.db.Priority
import anttracker.db.Product
import anttracker.db.Release
import org.jetbrains.exposed.sql.transactions.transaction

abstract class ScreenWithTitle(
    theTitle: String? = null,
) : Screen {
    private var menuTitle: String? = theTitle

    fun title(theTitle: String) {
        this.menuTitle = theTitle
    }

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
    private val createFilter: (filter: String) -> IssueFilter?,
) : ScreenWithTitle("Search by $target") {
    constructor(
        page: PageWithFilter,
        target: String,
        createFilter: (filter: String) -> IssueFilter?,
    ) : this(page, target, emptyList(), createFilter)

    override fun displayBody(t: Terminal): Screen {
        var filter: IssueFilter? = null

        val message = "Please enter a $target to search for or leave it empty to go back to the issues menu"

        val promptIt: (m: String) -> String =
            if (options.isEmpty()) {
                t::prompt
            } else {
                { msg ->
                    t.print("Options: ")
                    t.printLine(options.joinToString(", "))
                    t.printLine()
                    t.prompt(msg, options + "")
                }
            }

        while (filter == null) {
            val input = promptIt(message)

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
        "description",
        IssueFilter::ByDescription,
    )

fun searchByAffectedReleaseMenu(page: PageWithFilter): Screen =
    SearchByOrGoBackToIssuesMenu(
        page,
        "affected release",
        transaction { Release.all().map { it.releaseId } },
        IssueFilter::ByAffectedRelease,
    )

fun searchByAnticipatedReleaseMenu(page: PageWithFilter) =
    SearchByOrGoBackToIssuesMenu(
        page,
        "anticipated release",
        transaction { Release.all().map { it.releaseId } },
        IssueFilter::ByAffectedRelease,
    )

fun searchByStatusMenu(page: PageWithFilter) =
    SearchByOrGoBackToIssuesMenu(
        page,
        "status",
        listOf("assessed", "created", "done", "cancelled", "in progress"),
    ) { input -> parseStatus(input)?.let(IssueFilter::ByStatus) }

private fun parseStatus(input: String): Status? =
    when (input) {
        "assessed" -> Status.Assessed
        "created" -> Status.Created
        "done" -> Status.Done
        "cancelled" -> Status.Cancelled
        "in progress" -> Status.InProgress
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
        "Date created",
        (0..100).map(Int::toString),
    ) { input -> Days(input.toInt()).let(IssueFilter::ByDateCreated) }
