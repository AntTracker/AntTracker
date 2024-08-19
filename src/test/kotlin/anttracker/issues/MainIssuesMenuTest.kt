package anttracker.issues

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot

typealias ScreenOutput = List<String>

class FakeTerminal(
    private vararg val promptResponses: String,
) : Terminal {
    private val _output = mutableListOf<String>()
    val output: ScreenOutput
        get() = _output
    var resIndex = 0

    override fun printLine() {
        _output += ""
    }

    override fun printLine(text: String) {
        _output += text
    }

    override fun prompt(
        message: String,
        choices: List<String>,
    ): String {
        _output += message
        return ""
    }

    override fun prompt(
        message: String,
        allowEmpty: Boolean,
        isValidChoice: (String) -> Boolean,
    ): String {
        _output += message
        return promptResponses[resIndex++]
    }

    override fun print(message: String) {
        _output += message
    }
}

val screenContentGen = Arb.list(Arb.string(), 1..30)

val aBunchOfScreensGen =
    Arb.list(screenContentGen, 1..30).map { messages ->
        messages
            .fold(null) { acc: Screen?, content ->
                fakeScreen(content, acc)
            }.let(::requireNotNull)
            .let { messages.reversed() to it }
    }

fun <T, L : Iterable<T>> List<L>.interleaveWith(separator: L): List<T> = this.flatMap { separator + it }

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
