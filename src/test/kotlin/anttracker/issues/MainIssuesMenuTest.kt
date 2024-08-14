package anttracker.issues

import anttracker.db.*
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

class FakeTerminal(
    private val promptResponse: String = "1",
) : Terminal {
    val output = mutableListOf<String>()

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
        return promptResponse
    }

    override fun prompt(
        message: String,
        allowEmpty: Boolean,
        isValidChoice: (String) -> Boolean,
    ): String {
        output += message
        return promptResponse
    }

    override fun print(message: String) {
        output += message
    }
}

class MainIssuesMenuTest :
    DescribeSpec({
        beforeEach {
            Database.connect("jdbc:h2:mem:anttracker;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
            setupSchema(true)
        }
        afterEach {
            transaction {
                SchemaUtils.drop(Issues, Products, Releases, Contacts, Requests)
            }
        }

        describe("mainIssuesMenu") {
            describe("when the screen returns another screen") {
                it("runs the next screen") {
                    val screen = mockk<Screen>()
                    val t = FakeTerminal()
                    every { screen.run(t) } answers {
                        t.printLine("At the first menu")
                        null
                    }

                    mainIssuesMenu(screen, t)
                    t.output shouldBe listOf("", "/\\".repeat(40), "", "At the first menu")
                }
            }
//            describe("When the user selects to view all the issues") {
//                it("Shows all the issues in the db") {
//                    val t = FakeTerminal("7")
//                    mainIssuesMenu(t)
//                    t.output shouldBe listOf("", "/\\".repeat(40), "")
//                }
//            }
        }
    })
