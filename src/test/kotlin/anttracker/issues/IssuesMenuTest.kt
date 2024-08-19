package anttracker.issues

import anttracker.db.*
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.*
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
                    val t = FakeTerminal("`")
                    val actual = issuesMenu.run(t)
                    t.output should matchScreenOutput(mainIssuesMenuOutput)
                    actual shouldBe null
                }
            }
            describe("and the user inputs '1'") {
                it("Shows all the options and returns the search by description menu") {
                    val t = FakeTerminal("1")
                    val actual = issuesMenu.run(t)
                    t.output should matchScreenOutput(mainIssuesMenuOutput)
                    requireNotNull(actual)
                    actual shouldBe instanceOf<SearchByOrGoBackToIssuesMenu>()
                }
                describe("and the user then inputs ''") {
                    it("shows all the options and returns to the main issues menu") {
                        val (actual, output) = enterResponses(issuesMenu, "1", "")
                        output should matchScreenOutputs(mainIssuesMenuOutput, filterByDescriptionOutput())
                        requireNotNull(actual)
                        actual shouldBe instanceOf<Screen>()
                    }
                }
                describe("and the user then inputs a target description matching 1 or more issues") {
                    it("Shows the first 20 issues containing the target description as a substring of the issue description") {
                        checkAll(Arb.list(issueArb, 0..30)) { issues: List<Issue> ->
                            val filterByDescription = "1"
                            val matchIssueDescription = { description: String -> description }
                            val goBackToMainMenu = "`"
                            val (actual, outputs) =
                                enterResponses(
                                    issuesMenu,
                                    filterByDescription,
                                    matchIssueDescription("Issue"),
                                    goBackToMainMenu,
                                )
                            val expectedIssues = issues.take(20)
                            outputs should
                                matchScreenOutputs(
                                    mainIssuesMenuOutput,
                                    filterByDescriptionOutput("Issue"),
                                    issuesMatchingFilterOutput(expectedIssues),
                                )
                            actual shouldNotBe null
                        }
                    }
                }
            }
        }
    })

fun formatIssues(issues: List<Issue>): List<String> = issues.map { it.description.description }

fun issuesMatchingFilterOutput(issues: List<Issue>): ScreenOutput {
    val printedIssues = issues.takeIf { it.isNotEmpty() }?.let { formatIssues(it) } ?: listOf("No issues found.")
    return listOf("== Search Results ==") + printedIssues +
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
        Arb.element(*Status.all()),
        Arb.positiveInt(5),
        Arb.string(50),
        ::createIssue,
    )

/**
 * Starts at initialScreen and enters each of the messages in responses, returning the end screen and all the messages
 * shown to user
 */
fun enterResponses(
    initialScreen: Screen?,
    vararg responses: String,
): Pair<Screen?, List<ScreenOutput>> =
    responses.fold(initialScreen to mutableListOf<ScreenOutput>()) { (screen, contentSoFar), response ->
        val terminal = FakeTerminal(response)
        contentSoFar += terminal.output
        screen?.run(terminal) to contentSoFar
    }

/**
 * Returns the text shown to the user when they
 * search for issues using the given description
 */
fun filterByDescriptionOutput(toFilterBy: String = ""): ScreenOutput {
    val searchMessage =
        if (toFilterBy.isEmpty()) "Going back to the issues menu..." else "Searching for issues matching 'Description: $toFilterBy"
    return listOf(
        "== Search by description (1-30 characters) ==",
        "Please enter a description (1-30 characters) to search for or leave it empty to go back to the issues menu",
        "",
        searchMessage,
    )
}

fun matchScreenOutput(expectedOutputs: ScreenOutput) =
    Matcher { actual: ScreenOutput ->
        MatcherResult(
            actual == expectedOutputs,
            { "The given menu \n$actual\n did not match the expected menu \n$expectedOutputs" },
            { "The given menu \n$actual\n should not have matched \n$expectedOutputs" },
        )
    }

fun matchScreenOutputs(vararg expectedOutputs: ScreenOutput) =
    Matcher { actual: List<ScreenOutput> ->
        MatcherResult(
            actual.zip(expectedOutputs).all { (actualOutput, expectedOutput) -> actualOutput == expectedOutput },
            { "The given menus \n$actual\n did not match the expected menus \n${expectedOutputs.map { it.toString() }}" },
            { "The given menus \n$actual\n should not have matched \n$expectedOutputs" },
        )
    }

val mainIssuesMenuOutput =
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
