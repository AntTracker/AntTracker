package anttracker.issues

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.should

class IssuesMenuTest :
    DescribeSpec({
        describe("When the user selects to view all the issues") {
            it("Shows all the issues in the db") {
                val t = FakeTerminal(listOf("7", "`", "`"))
                mainIssuesMenu(issuesMenu, t)
                t.output should
                    haveMenus(
                        listOf(
                            mainIssuesMenu,
                            allIssues,
                            mainIssuesMenu,
                        ),
                    )
            }
        }
    })
