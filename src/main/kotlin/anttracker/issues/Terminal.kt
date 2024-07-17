/* Terminal.kt
Revision History
Rev 1 - 7/15/2024 Original by Eitan
-------------------------------------------
This file contains the abstraction of a Terminal
along with the operations one can do on a
terminal. These include printing, prompting
the user, and displaying a table.
---------------------------------
 */

package anttracker.issues

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// ----

/** ---
 * Represents a terminal.
--- */
class Terminal {
    /** ---
     * Prints an empty line.
     --- */
    fun printLine() = println()

    /** ---
     * Prints the given text.
     --- */
    fun printLine(
        text: String, // in
    ) {
        println(text)
    }

    /** ---
     * prints the given message and returns what the user entered.
     --- */
    fun prompt(
        message: String, // in
    ): String {
        println(message)
        return readln()
    }

    /** ----
     * This function prompts the user for input and only returns their
     * input if it is valid. Otherwise, it prompts the user again.
     ----- */
    fun prompt(
        message: String, // in
        choices: List<String>, // in
    ): String {
        println(message)
        val choice = readln()
        if (choices.contains(choice)) {
            return choice
        }
        return prompt(message, choices)
    }

    /** ---
     * Prints the given message.
     --- */
    fun print(
        message: String, // in
    ) = kotlin.io.print(message)

    /**
     * Represents the wanted format for the date.
     */
    private val formatter = DateTimeFormatter.ofPattern("yyyy/mm/dd")

    /** -----
     * This function displays a table to the screen using the passed columns and rows.
     ----- */
    fun displayTable(
        columns: List<Pair<String, Int>>, // in
        rows: List<List<Any>>, // in
    ) {
        // This aligns the columns according to their format and then prints them out.
        columns
            .joinToString(separator = " | ", postfix = " |", prefix = " | ") { (columnName, length) ->
                columnName.padEnd(length)
            }.let {
                print("##")
                printLine(it)
            }

        // This generates all the rows for the table and prints them out.
        rows.forEachIndexed { index, row ->
            val stringRow =
                row
                    .mapIndexed { colIndex, col ->
                        val length = columns[colIndex].second
                        when {
                            (col is Number) -> col.toString().padStart(length)
                            (col is LocalDateTime) -> col.format(formatter).padEnd(length)
                            (col is LocalDate) -> col.format(formatter).padStart(length)
                            else -> col.toString().padEnd(length)
                        }
                    }.joinToString(separator = " | ", postfix = " |", prefix = " | ")
            printLine("${(index + 1).toString().padStart(2)}$stringRow")
        }
    }

    /** ---
     * Prints the given title.
     --- */
    fun title(
        s: String, // in
    ) {
        printLine("== $s ==")
    }
}
