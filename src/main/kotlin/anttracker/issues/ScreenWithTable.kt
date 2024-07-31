/* ScreenWithTable.kt
Revision History
Rev 1 - 7/30/2024 Original by Eitan
-------------------------------------------
This file contains the abstraction of the
configuration for a table and a screen with
a table.
---------------------------------
*/

package anttracker.issues

import org.jetbrains.exposed.sql.transactions.transaction

// -----

/**
 * Represents a screen displaying a table of items.
 */
class ScreenWithTable : ScreenWithMenu() {
    private var table: TableConfiguration = TableConfiguration()

    /** ---
     * Configures the table to be shown on the screen
     --- */
    fun table(
        config: TableConfiguration.() -> Unit, // in
    ) {
        this.table = TableConfiguration()
        config(table)
        option("Next page") { table.nextPage!!.run { this() } }
        option("Print") { screenWithMenu { content { t -> t.printLine("Not currently implemented. In next version") } } }
        content { t ->
            transaction {
                table.fetchRows().let { rows ->
                    if (rows.isEmpty()) {
                        t.printLine(table.emptyMessage)
                    } else {
                        t.displayTable(table.columns, rows)
                    }
                }
            }
        }
    }
}

/**
 * Represents the configuration for a table
 */
class TableConfiguration {
    var fetchRows: () -> List<List<Any?>> = { emptyList() }
    var columns: List<Pair<String, Int>> = emptyList()
    var emptyMessage: String = ""
    var nextPage: ScreenHandler? = null

    /** ---
     * Sets the query to be used
     --- */
    fun query(
        query: () -> List<List<Any?>>, // in
    ) {
        this.fetchRows = query
    }

    /** ---
     * Sets the column formatting to be used
     --- */
    fun columns(
        vararg columns: Pair<String, Int>, // in
    ) {
        this.columns = columns.toList()
    }

    /** ---
     * Sets the message to be displayed when an empty page is shown
     --- */
    fun emptyMessage(
        message: String, // in
    ) {
        this.emptyMessage = message
    }

    /** ---
     * Sets the function which will transition to the next page
     --- */
    fun nextPage(
        nextPage: ScreenHandler, // in
    ) {
        this.nextPage = nextPage
    }
}

/** ---
 * Creates a screen with a table configured using the passed configuration.
--- */
fun screenWithTable(
    config: ScreenWithTable.() -> Unit, // in
): Screen {
    val menu = ScreenWithTable()
    config(menu)
    return menu
}
