/* Main.kt
Revision History:
Rev. 1 - 2024/07/01 Original by Micah Baker
Rev. 2 - 2024/07/16 By Eitan Barylko
Rev. 3 - 2024/07/30 By Eitan Barylko
-------------------------------------------------------------------------------
entry-point and main-menu of AntTracker.
-------------------------------------------------------------------------------
 */

package anttracker

import anttracker.db.setupSchema
import org.jetbrains.exposed.sql.Database
import anttracker.contact.menu as contactMenu
import anttracker.issues.mainIssuesMenu as issuesMenu
import anttracker.product.menu as productMenu
import anttracker.release.menu as releaseMenu
import anttracker.request.menu as requestMenu

sealed class Config(
    val db: String,
    val shouldPopulateDb: Boolean,
) {
    data object InMemory : Config("mem:anttracker", true)

    class PersistentWithSetup(
        db: String,
        shouldPopulate: Boolean = true,
    ) : Config(db, shouldPopulate)
}

fun extractArgs(args: Array<String>): Config =
    args.sorted().fold(Config.PersistentWithSetup("~/anttracker", false) as Config) { current, param ->
        when {
            param.startsWith("--db=") -> Config.PersistentWithSetup(param.removePrefix("--db="), false)
            param.startsWith("--setup") ->
                when (current) {
                    is Config.PersistentWithSetup -> Config.PersistentWithSetup(current.db, true)
                    else -> current
                }

            param.startsWith("--testing") -> Config.InMemory

            else -> current
        }
    }

fun main(args: Array<String>) {
    val config = extractArgs(args)
    Database.connect("jdbc:h2:${config.db};DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")

    setupSchema(config.shouldPopulateDb)

    while (true) {
        val mainMenuText =
            """
            == MAIN MENU ==
            1   View/Edit Issue
            2   New request
            3   New release
            4   New contact
            5   New product
            
            Please select a command. ` to exit program: 
            """.trimIndent()
        print(mainMenuText)

        when (val selection = readln()) {
            "`" -> break
            "1" -> issuesMenu()
            "2" -> requestMenu()
            "3" -> releaseMenu()
            "4" -> contactMenu()
            "5" -> productMenu()
            else -> {
                println("Bad input: $selection.")
            }
        }
    }
}

/**
 *
For all files in this project, the following conventions will be
observed:
 * Each file will have its name at the very top of the file
 * Every major section (revision history, imports, classes, functions, etc..) will be separated by ------------
 *  The revision history will be of the form
```
Revision # - mm/dd/yyyy modified by [editor/s]
- changes made
```
 * The revision history will be in reverse chronological order
 * The revision history will be followed by a paragraph explaining what the file contains
 * Functions and their parameters are in camel case
 * Place all the arguments of a function on a separate line and add a comment indicating if they are in or out parameters
 * Use four spaces for indenting function parameters
 * Classes are in pascal case
 * Constants are in screaming snake case
 * Four spaces are used for indentations
 * For curly braces, put the opening brace at the end of the line where the construct begins, and the closing brace on a separate line aligned horizontally with the opening construct.
 * Put spaces around binary operators, except the range operator
 * Always put a space after ":"
 * If the condition of an if or when statement is multiline, always use curly braces around the body of the statement
 * Indent each subsequent line of the condition by four spaces relative to the statement start
 * Put the closing parentheses of the condition together with the opening curly brace on a separate line:
 * When wrapping chained calls, put the . character or the ?. operator on the next line, with a single indent:
 */
