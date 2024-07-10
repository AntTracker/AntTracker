package anttracker

/** ------
This function prints out a message asking the user how they would like
to search for an issue.
----- */

val searchByDescription =
    object : Screen {
        override fun run() = issuesMenu
    }

val searchByPriority =
    object : Screen {
        override fun run() = issuesMenu
    }

val issuesMenu: Screen =
    object : Screen {
        val options: Map<String, Screen> =
            mapOf("Search by description" to searchByDescription, "Search by priority" to searchByPriority)

        override fun run(): Screen? {
            val displayOptions = displayMenu(options, "Issues Menu")
            return menuUserInput(displayOptions)
        }
    }
