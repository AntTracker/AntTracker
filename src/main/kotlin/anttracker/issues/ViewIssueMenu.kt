package anttracker.issues

import anttracker.db.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.reflect.KMutableProperty1

/** ----
 * Represents a map between a status and possible transitions.
--- */
private val nextPossibleStates: Map<Status, List<Status>> =
    mapOf(
        Status.Created to listOf(Status.Assessed),
        Status.Assessed to listOf(Status.InProgress, Status.Done, Status.Cancelled),
        Status.InProgress to listOf(Status.Done, Status.Cancelled),
    )

/** ---
 * Edits the status of the issue using the possible transitions for the current status.
--- */
private val editStatus =
    editIssueAttribute(
        Issue::status,
        { issue -> nextPossibleStates[issue.status]?.map { it.toString() } },
    ) { issue ->
        requireNotNull(issue.toStatus())
    }

/** ---
 * Extracts out the information contained in a request.
--- */
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

/** ---
 * Displays all the requests associated to the passed issue.
--- */
private fun viewRequests(
    issue: Issue, // in
    page: PageOf<Request> = PageOf(), // in
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

/** ----
 * This function shows all the information present within the passed issue
 * and prompts the user to edit either the description, priority, status,
 * or anticipated release
----- */
internal fun viewIssueMenu(
    issue: Issue, // in
): Screen =
    screenWithMenu {
        title("Issue #${issue.id}")
        transaction {
            option("Description: ${issue.description}") { editDescription(issue) }
            option("Priority: ${issue.priority}") { editPriority(issue) }
            option("Status: ${issue.status} ${canBeChanged(issue.status)}") { editStatus(issue) }
            option("AntRel: ${issue.anticipatedRelease.releaseId}") { editAnticipatedRelease(issue) }
            option("Created: ${issue.creationDate.format(formatter)} (not editable)") { viewIssueMenu(issue) }
            option("Requests") { viewRequests(issue) }
            option("Print") { noIssuesMatching }
        }
        promptMessage("Enter 1, 2, 3, or 4 to edit the respective fields.")
    }

/** ----
 * Returns a string indicating whether the status can be changed.
--- */
private fun canBeChanged(
    status: Status, // in
): String = if (status == Status.Done || status == Status.Cancelled) "(not editable)" else ""

/** ----
 * This function shows the release versions the user can pick from
 * for the updated value of the anticipated release.
----- */
private val editAnticipatedRelease =
    editIssueAttribute(
        Issue::anticipatedRelease,
        { issue -> transaction { Release.find { Releases.product eq issue.product.id }.map { it.releaseId } } },
    ) { newVal: String ->
        transaction {
            addLogger(StdOutSqlLogger)
            Release.find { Releases.releaseId eq newVal }.first()
        }
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

/** ---
 * Displays a screen for editing the passed issue property.
--- */
private fun <T> editIssueAttribute(
    prop: KMutableProperty1<Issue, T>, // in
    choicesFn: (Issue) -> List<String>?, // in
    parse: (String) -> T, // in
): (Issue) -> Screen =
    { issue ->
        val choices = choicesFn(issue)
        if (choices == null) {
            viewIssueMenu(issue)
        } else {
            editIssueAttribute(prop, choices, parse)(issue)
        }
    }

/**
 * Displays a screen for editing the passed issue property.
 */
private fun <T> editIssueAttribute(
    prop: KMutableProperty1<Issue, T>, // in
    choices: List<String> = emptyList(), // in
    parse: (String) -> T, // in
): (Issue) -> Screen =
    { issue: Issue ->
        screenWithMenu {
            var newVal = ""
            content { t ->
                transaction {
                    t.printLine("Options: ${choices.joinToString(", ")}")
                    newVal = t.prompt("Please enter ${prop.name}", choices)
                    printIssueSummary(t, issue)
                    t.title("Update: ${prop.name}")
                    t.printLine("OLD: ${prop.get(issue)}")
                    t.printLine("NEW: $newVal")
                }
            }
            option("Save") {
                updateIssueAndGoBackToMenu(issue) { prop.set(it, parse(newVal)) }
            }
            option("Back") { viewIssueMenu(issue) }
        }
    }

/** ---
 * Updates the issue using the function passed and returns
 * to the view issues menu.
--- */
private fun updateIssueAndGoBackToMenu(
    issue: Issue, // in
    updateFn: (it: Issue) -> Unit, // in
): Screen {
    val updatedIssue = transaction { Issue.findByIdAndUpdate(issue.id.value, updateFn) }
    require(updatedIssue != null)
    return viewIssueMenu(updatedIssue)
}

/** ---
 * Represents the menu for editing th priority of an issue.
--- */
private val editPriority =
    editIssueAttribute(
        Issue::priority,
        (1..5).map(Int::toString),
    ) { newVal -> newVal.toShort() }

/** ----
 * This function displays a screen where the user is presented with the
 * option of saving their issue with an edited description or going back
 * to the previous menu.
----- */
private val editDescription = editIssueAttribute(Issue::description) { it }
