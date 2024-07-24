package anttracker.issues

import anttracker.db.Request
import org.jetbrains.exposed.sql.transactions.transaction

private fun requestToRow(
    request: Request, // in
): List<Any> =
    listOf(
        request.affectedRelease,
        request.requestDate,
        request.contact.name,
        request.contact.email,
        request.contact.department,
    )

class ScreenWithTable : ScreenWithMenu() {
    private var table: TableConfiguration = TableConfiguration()

    fun table(config: TableConfiguration.() -> Unit) {
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

class TableConfiguration {
    var fetchRows: () -> List<List<Any>> = { emptyList() }
    var columns: List<Pair<String, Int>> = emptyList()
    var emptyMessage: String = ""
    var nextPage: ScreenHandler? = null

    fun query(query: () -> List<List<Any>>/* in */) {
        this.fetchRows = query
    }

    fun columns(vararg columns: Pair<String, Int>/* in */) {
        this.columns = columns.toList()
    }

    fun emptyMessage(message: String/* in */) {
        this.emptyMessage = message
    }

    fun nextPage(nextPage: ScreenHandler/* in */) {
        this.nextPage = nextPage
    }
}

fun screenWithTable(
    config: ScreenWithTable.() -> Unit, // in
): Screen {
    val menu = ScreenWithTable()
    config(menu)
    return menu
}
