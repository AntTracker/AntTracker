package anttracker

val requestsMenu =
    object : Screen {
        override fun run(): Screen? {
            TODO("Not yet implemented")
        }
    }

val mainMenu =
    object : Screen {
        val options: Map<String, Screen> = mapOf("Issue" to issuesMenu, "Requests" to requestsMenu)

        override fun run(): Screen? {
            val displayOptions = displayMenu(options, "Main Menu")
            return menuUserInput(displayOptions)
        }
    }
