package anttracker.issues

interface Screen {
    fun run(t: Terminal): Screen?
}

fun screenWithMenu(config: ScreenWithMenu.() -> Unit): Screen {
    val menu = ScreenWithMenu()
    config(menu)
    return menu
}

typealias ScreenHandler = () -> Screen

typealias DisplayFn = (t: Terminal) -> Any?

open class ScreenWithMenu : Screen {
    private var menuTitle: String = ""
    private var options: Map<String, ScreenHandler> = mutableMapOf()
    private var displayContent: DisplayFn = { }
    private var promptMessage: String = ""

    fun title(theTitle: String) {
        this.menuTitle = theTitle
    }

    fun option(
        description: String,
        handler: () -> Screen,
    ) {
        this.options += description to handler
    }

    fun promptMessage(message: String) {
        this.promptMessage = message
    }

    override fun run(t: Terminal): Screen? {
        val byIndex = displayMenu(t)

        t.printLine()
        displayContent(t)
        t.printLine()

        // Ask the user to enter which menu wants to select
        val mainMenuChoice = "`"
        val backToMainMenuMessage = " `(backtick) to abort:"
        val choices = (1..byIndex.size).map(Integer::toString) + mainMenuChoice
        // The user needs to choose from the choices that are `, 1, 2, 3, 4...
        val response = t.prompt(promptMessage + backToMainMenuMessage, choices = choices)

        return when (response) {
            mainMenuChoice -> null
            else -> {
                val index = Integer.parseInt(response)
                byIndex[index - 1].value()
            }
        }
    }

    private fun displayMenu(t: Terminal): Array<Map.Entry<String, ScreenHandler>> {
        val byIndex = options.entries.toTypedArray()

        t.printLine("== $menuTitle ==")
        byIndex.forEachIndexed { index, entry ->
            val nbr = "${index + 1}".padStart(2, ' ')
            t.print(nbr)
            t.printLine(". ${entry.key}")
        }

        return byIndex
    }

    fun content(fn: DisplayFn) {
        displayContent = fn
    }
}
