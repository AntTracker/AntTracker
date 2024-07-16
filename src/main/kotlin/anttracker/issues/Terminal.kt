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

class Terminal {
    fun printLine() = println()

    fun printLine(text: String) {
        println(text)
    }

    fun prompt(message: String): String {
        println(message)
        return readln()
    }

    fun prompt(
        message: String,
        choices: List<String>,
    ): String {
        println(message)
        val choice = readln()
        if (choices.contains(choice)) {
            return choice
        }
        return prompt(message, choices)
    }

    fun print(message: String) = kotlin.io.print(message)

    private val formatter = DateTimeFormatter.ofPattern("yyyy/mm/dd")

    fun displayTable(
        columns: List<Pair<String, Int>>,
        rows: List<List<Any>>,
    ) {
        columns
            .joinToString(separator = " | ", postfix = " |", prefix = " | ") { (columnName, length) ->
                columnName.padEnd(length)
            }.let {
                print("##")
                printLine(it)
            }

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

    fun title(s: String) {
        printLine("== $s ==")
    }
}
