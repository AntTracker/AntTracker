/* Screen.kt
Revision History
Rev. 1 - 2024/7/15 Original by Eitan Barylko
Rev. 2 - 2024/7/30 By Eitan Barylko
-------------------------------------------------------------------------------
This file contains the abstraction of a screen
and a screen with a menu. Helper methods for
prompting the user, validating their input,
displaying the options for them to pick, and
transitioning to the next screen are also present.
-------------------------------------------------------------------------------
 */

package anttracker.issues

// ------

/**
 * Represents a screen which runs in the terminal
 */
interface Screen {
    /** ----
     * This function runs the logic of the screen on the terminal and optionally returns
     * a new screen.
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

/** ---
 * This type represents a function which takes nothing and
 * returns a screen
--- */
typealias ScreenHandler = () -> Screen

/** ---
 * This type represents a function which takes a
 * terminal and returns an optional result.
--- */
typealias DisplayFn = (t: Terminal) -> Any?

/** ---
 * This class represents a screen that shows a menu, asks the user for input,
 * and then transitions to another screen.
--- */
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

    /** -----
     * This function displays the options of the given menu
     * to the user, prompts them for input, and then transitions
     * to the next menu based on their input.
     ----- */
    override fun run(
        t: Terminal, // in
    ): Screen? {
        menuTitle?.run { t.title(this) }
        displayContent(t)
        t.printLine()
        val byIndex = displayMenu(t)

        // Prepare all the choices the user can select
        val mainMenuChoice = "`"
        val backToMainMenuMessage = " Or press ` (backtick) to go back to the main menu:"
        val choices = (1..byIndex.size).map(Integer::toString) + mainMenuChoice

        t.printLine()

        // Prompt the user for their response and then change to the screen corresponding
        // to their response.

        val response = t.prompt(promptMessage + backToMainMenuMessage) { input -> input in choices }

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

    /** ---
     * This function sets the content.
     --- */
    fun content(
        fn: DisplayFn, // in
    ) {
        displayContent = fn
    }
}
