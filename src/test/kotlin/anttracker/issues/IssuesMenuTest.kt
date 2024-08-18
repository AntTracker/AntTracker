package anttracker.issues

import anttracker.db.*
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.instanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import io.kotest.property.checkAll
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

class IssuesMenuTest :
    DescribeSpec({
        beforeSpec {
            Database.connect("jdbc:h2:mem:anttrackertest;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
        }
        beforeEach {
            transaction {
                SchemaUtils.dropSchema()
                SchemaUtils.createMissingTablesAndColumns(Products, Issues, Releases, Requests, Contacts)
            }
        }
        describe("When the menu is displayed") {
            describe("and the user inputs '`'") {
                it("Shows all the options and exits") {
                    val t = FakeTerminal(listOf("`"))
                    val actual = issuesMenu.run(t)
                    t.output should haveMenus(mainIssuesMenu)
                    actual shouldBe null
                }
            }
            describe("and the user inputs '1'") {
                it("Shows all the options and returns the search by description menu") {
                    val t = FakeTerminal(listOf("1"))
                    val actual = issuesMenu.run(t)
                    t.output should haveMenus(mainIssuesMenu)
                    requireNotNull(actual)
                    actual shouldBe instanceOf<SearchByOrGoBackToIssuesMenu>()
                }
                describe("and the user then inputs ''") {
                    it("shows all the options and returns to the main issues menu") {
                        val responses = listOf("1", "")
                        val (actual, output) = enterResponses(issuesMenu, responses)
                        output should haveMenus(mainIssuesMenu, filterByDescriptionMenu())
                        requireNotNull(actual)
                        actual shouldBe instanceOf<Screen>()
                    }
                }
                describe("and the user then inputs a target description matching 1 or more issues") {
                    it("Shows the first 20 issues containing the target description as a substring of the issue description") {
                        checkAll(issueArb) { issues: List<Issue> ->
                            val responses = listOf("1", "Issue", "`")
                            val (actual, output) = enterResponses(issuesMenu, responses)
                            val expectedIssues = issues.filter { it.description.description.contains("Issue") }.take(20)
                            output should
                                haveMenus(
                                    mainIssuesMenu,
                                    filterByDescriptionMenu("Issue"),
                                    issuesMatchingFilterMenu(expectedIssues),
                                )
                            requireNotNull(actual)
                            actual shouldBe instanceOf<Screen>()
                        }
                    }
                }
            }
        }
    })

fun formatIssues(issues: List<Issues>): List<String> = issues.map(toRow)

fun issuesMatchingFilterMenu(issues: List<Issue>): Menu {
    val printedIssues = issues.takeIf { it.isNotEmpty() }?.let { "formatIssues(it) " } ?: "No issues found."
    return listOf("== Search Results ==", printedIssues) +
        generateOptions(
            "Select filter",
            "View issue",
            "Next page",
            "Print",
        )
}

fun createIssue(
    description: String,
    date: LocalDateTime,
    status: Status,
    priority: Int,
    productName: String,
): Issue =
    transaction {
        val prodId =
            Products.insert {
                it[name] = productName
            } get Products.id
        val issueId =
            Issues.insert {
                it[this.description] = description
                it[this.creationDate] = date
                it[this.status] = status.toString()
                it[this.priority] = priority.toShort()
                it[product] = prodId
            } get Issues.id
        Issue.findById(issueId)!!
    }

val issueArb: Arb<Issue> =
    Arb.bind(
        Arb.stringPattern("Issue-.*").filter { it.length <= 30 },
        Arb.localDateTime(0, 9999),
        Arb.element(Status.all()),
        Arb.positiveInt(5),
        Arb.string(50),
    )(::createIssue)

/**
 * Starts at initialScreen and enters each of the messages in responses, returning the end screen and all the messages
 * shown to user
 */
fun enterResponses(
    initialScreen: Screen,
    responses: List<String>,
): Pair<Screen?, List<String>> {
    val terminals = responses.map { FakeTerminal(listOf(it)) }
    return terminals.fold(Pair(initialScreen, emptyList())) { (screen, contentSoFar), terminal ->
        Pair(screen?.run(terminal), contentSoFar + terminal.output)
    }
}

/**
 * Returns the text shown to the user when they
 * search for issues using the given description
 */
fun filterByDescriptionMenu(toFilterBy: String = ""): Menu {
    val searchMessage =
        if (toFilterBy.isEmpty()) "Going back to the issues menu..." else "Searching for issues matching 'Description: $toFilterBy"
    return listOf(
        "== Search by description (1-30 characters) ==",
        "Please enter a description (1-30 characters) to search for or leave it empty to go back to the issues menu",
        "",
        searchMessage,
    )
}

typealias Menu = List<String>

fun haveMenus(vararg expectedMenus: Menu) =
    Matcher { actual: Menu ->
        val expected = expectedMenus.toList().flatten()
        MatcherResult(
            actual == expected,
            { "The given menus \n$actual\n did not match the expected menus \n$expected" },
            { "The given menus \n$actual\n should not have matched \n$expected" },
        )
    }

val mainIssuesMenu =
    listOf(
        "== VIEW/EDIT ISSUE ==",
        "Filters Active: No filters",
        "",
    ) +
        generateOptions(
            "Search by Description",
            "Search by Product",
            "Search by Anticipated release",
            "Search by Status",
            "Search by Priority",
            "Search by Date range",
            "Display all issues",
            "Clear filters",
        ) +
        "" +
        "Please select search category. Or press ` (backtick) to go back to the main menu:"

typealias IssueInfo = List<Any>

fun generateOptions(vararg options: String) = options.flatMapIndexed { idx, option -> listOf(" ${idx + 1}", ". $option") }

fun generateIssues(
    title: String,
    columns: List<String>,
    vararg issuesInfo: IssueInfo,
): List<String> {
    val cols = columns.joinToString(separator = "|", postfix = "|")
    val formattedIssues =
        issuesInfo.mapIndexed { idx, info -> info.joinToString(separator = "|", postfix = "|", prefix = "$idx |") }
    return listOf("== $title ==", cols, *formattedIssues.toTypedArray())
}
