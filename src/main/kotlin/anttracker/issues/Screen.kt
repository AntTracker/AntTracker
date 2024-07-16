/* Screen.kt
Revision History
Rev 1 - 7/15/2024 Original by Eitan
-------------------------------------------
This file contains the abstraction of a screen
and a screen with a menu. Helper methods for
prompting the user, validating their input,
displaying the options for them to pick, and
transitioning to the next screen are also present.
---------------------------------
 */

package anttracker.issues

// ------

interface Screen {
    /** ----
     * This function runs the current screen and transitions to the new screen indicated by the
     * user's choice.
     ---- */
    fun run(
        t: Terminal, // in
    ): Screen?
}

/** ----
 * This function creates a configured ScreenWithMenu using the
 * configuration passed.
------ */
fun screenWithMenu(
    config: ScreenWithMenu.() -> Unit, // in
): Screen {
    val menu = ScreenWithMenu()
    config(menu)
    return menu
}

typealias ScreenHandler = () -> Screen

typealias DisplayFn = (t: Terminal) -> Any?

open class ScreenWithMenu : Screen {
    private var menuTitle: String? = null
    private var options: Map<String, ScreenHandler> = mutableMapOf()
    private var displayContent: DisplayFn = { }
    private var promptMessage: String = ""

    /** ----
     * This function sets the title of the menu to the passed
     * title.
     ---- */
    fun title(
        theTitle: String, // in
    ) {
        this.menuTitle = theTitle
    }

    /** ----
     * This function adds a new option to the list
     * of options a user can pick for this menu
     ---- */
    fun option(
        description: String, // in
        handler: () -> Screen, // in
    ) {
        this.options += description to handler
    }

    /** -----
     * This function sets the message to be shown
     * when prompting the user to the given
     * message.
     ------ */
    fun promptMessage(
        message: String, // in
    ) {
        this.promptMessage = message
    }

    override fun run(t: Terminal): Screen? {
        menuTitle?.run { t.printLine("== $menuTitle ==") }
        displayContent(t)
        t.printLine()
        val byIndex = displayMenu(t)

        // Ask the user to enter which menu wants to select
        val mainMenuChoice = "`"
        val backToMainMenuMessage = " Or press ` (backtick) to go back to the main menu:"
        val choices = (1..byIndex.size).map(Integer::toString) + mainMenuChoice
        // The user needs to choose from the choices that are `, 1, 2, 3, 4...
        t.printLine()
        val response = t.prompt(promptMessage + backToMainMenuMessage, choices = choices)

        return when (response) {
            mainMenuChoice -> null
            else -> {
                val index = Integer.parseInt(response)
                byIndex[index - 1].value()
            }
        }
    }

    /** -----
     * This function displays all the options the user can pick in the given menu.
     ----- */
    private fun displayMenu(
        t: Terminal, // in
    ): Array<Map.Entry<String, ScreenHandler>> {
        val byIndex = options.entries.toTypedArray()

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
