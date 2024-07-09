/* Main.kt
Revision History:
Rev 1 - 2024/07/01 Original by Micah
----------------
entry-point and main-menu of AntTracker.
 */

package anttracker

class Terminal {
    fun printLine() = println()

    fun printLine(text: String) {
        println(text)
    }

    fun prompt(
        message: String,
        choices: List<String>,
    ): String {
        println("$message: ${choices.joinToString(", ")} ")
        val choice = readln()
        if (choices.contains(choice)) {
            return choice
        }
        return prompt(message, choices)
    }

    fun print(message: String) = kotlin.io.print(message)
}

fun main(args: Array<String>) {
    val t = Terminal()
    var currentScreen: Screen? = mainMenu

    while (currentScreen != null) {
        t.printLine("Press 0 to exit or * for the main menu")
        t.printLine()

        currentScreen = currentScreen.run(t)
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
