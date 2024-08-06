package anttracker.issues

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class FakeTerminal : Terminal {
    val output = mutableListOf<String>()
    val input = mutableListOf<String>()

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
        return choices.first()
    }

    override fun prompt(
        message: String,
        allowEmpty: Boolean,
        isValidChoice: (String) -> Boolean,
    ): String {
        output += message
        return ""
    }

    override fun print(message: String) {
        output += message
    }
}

class MainIssuesMenuTest :
    DescribeSpec({

        describe("mainIssuesMenu") {
            describe("when the screen returns another screen") {
                it("runs the next screen") {
                    val allScreens = mutableListOf<String>()
                    val screen = mockk<Screen>()
                    val t = FakeTerminal()
                    every { screen.run(t) } answers {
                        allScreens += "first"
                        t.printLine("At the first menu")
                        null
                    }

                    mainIssuesMenu(screen, t)
                    allScreens shouldBe listOf("first")
                    t.output shouldBe listOf("")
                }
            }
        }
    })
