package anttracker.issues

import anttracker.db.*
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
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
        describe("When the user exits") {
            it("Shows all the options") {
                val t = FakeTerminal(listOf("`"))
                issuesMenu.run(t)
                t.output should
                    haveMenus(mainIssuesMenu)
            }
        }
    })

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
