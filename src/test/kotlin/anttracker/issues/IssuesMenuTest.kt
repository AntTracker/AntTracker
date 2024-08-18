package anttracker.issues

import anttracker.db.*
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.instanceOf
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

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
            }
            describe("and the user inputs '1'") {
                describe("and the user then inputs ''") {
                    it("shows all the options and returns to the main issues menu") {
                        val t = FakeTerminal(listOf("1", ""))
                        val actual = issuesMenu.run(t)
                        t.output should haveMenus(mainIssuesMenu, filterByDescriptionMenu())
                        requireNotNull(actual)
                        actual shouldBe instanceOf<Screen>()
                    }
                }
            }
        }
    })

fun filterByDescriptionMenu(toFilterBy: String = ""): Menu =
    listOf(
        "== Search by description (1-30 characters) ==",
        "Please enter a description (1-30 characters) to search for or leave it empty to go back to the issues menu",
    )

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
