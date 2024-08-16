package anttracker.issues

import anttracker.db.*
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import java.time.format.DateTimeFormatter

class FakeTerminal(
    private val promptResponses: List<String> = listOf(""),
) : Terminal {
    val output = mutableListOf<String>()
    var resIndex = 0

    override fun printLine() {
        output += ""
    }

    override fun printLine(text: String) {
        output += text
    }

    override fun prompt(
        message: String,
        choices: List<String>,
    ): String {
        output += message
        return ""
    }

    override fun prompt(
        message: String,
        allowEmpty: Boolean,
        isValidChoice: (String) -> Boolean,
    ): String {
        output += message
        return promptResponses[resIndex++]
    }

    override fun print(message: String) {
        output += message
    }
}

typealias Menu = List<String>

fun haveMenus(expectedMenus: List<Menu>) =
    Matcher { value: Menu ->
        MatcherResult(
            expectedMenus.flatten() == value,
            { "The given menus $value did not match the expected menus $expectedMenus" },
            { "The given menus $value should not have matched $expectedMenus" },
        )
    }

val menuSeparator: Menu = listOf("", "/\\".repeat(40), "")
val mainIssuesMenu =
    menuSeparator +
        listOf(
            "== VIEW/EDIT ISSUE ==",
            "Filters Active: No filters",
            "",
            " 1",
            ". Search by Description",
            " 2",
            ". Search by Product",
            " 3",
            ". Search by Anticipated release",
            " 4",
            ". Search by Status",
            " 5",
            ". Search by Priority",
            " 6",
            ". Search by Date range",
            " 7",
            ". Display all issues",
            " 8",
            ". Clear filters",
            "Please select search category. Or press ` (backtick) to go back to the main menu:",
        )

typealias IssueInfo = List<Any>

fun generateOptions(vararg options: String) = options.flatMapIndexed { idx, option -> listOf(" $idx", ". $option") }

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

private val formatter = DateTimeFormatter.ofPattern("YYYY/MM/dd")

val allIssues: Menu =
    menuSeparator +
        generateIssues(
            "Search Results",
            listOf("##", "ID", "Description", "Priority", "Status", "AntRel", "Created", "Product"),
            listOf(1, "Issue 0", 1, "Created", "p-0-0", "2024/08/14", "Product 0"),
            listOf(2, "Issue 1", 2, "Assessed", "p-0-0", "2024/08/14", "Product 0"),
        ) +
        generateOptions("Select filter", "View issue", "Next page", "Print") +
        "Or press ` (backtick) to go back to the main menu:"

val screenContentGen = Arb.list(Arb.string(), 1..30)

val aBunchOfScreensGen =
    Arb.list(screenContentGen, 1..30).map { messages ->
        messages
            .fold(null) { acc: Screen?, content ->
                fakeScreen(content, acc)
            }.let(::requireNotNull)
            .let { messages.reversed() to it }
    }

// fun <T> List<T>.interleaveWith(separator: T): List<T> = this.flatMap { listOf(separator, it) }

// fun <T> List<T>.interleaveWith(separator: List<T>): List<T> = this.flatMap { separator + it }

fun <T> List<List<T>>.interleaveWith(separator: List<T>): List<T> = this.flatMap { separator + it }

class InterleaveWithTest :
    DescribeSpec({
        describe(
            "When there is a sequence of n elements " +
                "with a separator containing multiple parts",
        ) {
            it("Interleaves them with a separator") {
                val separator = listOf("sep1", "sep2")
                val actual = listOf(listOf("hello", "goodbye"), listOf("hi", "good")).interleaveWith(separator)
                val expected =
                    listOf(
                        "sep1",
                        "sep2",
                        "hello",
                        "goodbye",
                        "sep1",
                        "sep2",
                        "hi",
                        "good",
                    )
                actual shouldBe expected
            }
        }
    })

class MainIssuesMenuTest :
    DescribeSpec({
        val screenSeparator = listOf("", "/\\".repeat(40), "")
        describe("When there is a sequence of n screens") {
            it("Runs all of them to completion") {
                checkAll(aBunchOfScreensGen) { (messages, screen) ->
                    val t = FakeTerminal()
                    mainIssuesMenu(screen, t)

                    val expected = messages.interleaveWith(screenSeparator)
                    t.output shouldBe expected
                }
            }
        }
    })

private fun fakeScreen(
    messages: List<String>,
    nextScreen: Screen? = null,
): Screen {
    val terminal = slot<Terminal>()
    val screen = mockk<Screen>()
    every { screen.run(capture(terminal)) } answers {
        messages.forEach(terminal.captured::printLine)
        nextScreen
    }
    return screen
}
