package anttracker.issues

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import net.bytebuddy.matcher.ElementMatchers.any

class MainIssuesMenuTest :
    DescribeSpec({

        describe("mainIssuesMenu") {
            describe("when the screen returns another screen") {
                it("runs the next screen") {
                    val allScreens = mutableListOf<String>()
                    val screen = mockk<Screen>()
                    every { screen.run(any()) } answers {
                        allScreens += "first"
                        null
                    }
                    mainIssuesMenu(screen)
                    allScreens shouldBe listOf("first")
                }
            }
        }
    })
