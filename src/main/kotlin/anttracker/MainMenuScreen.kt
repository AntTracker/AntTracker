package anttracker

val mainMenu =
    screenWithMenu {
        title("Main menu")
        option("View/Edit Issues") { issuesMenu }
        option("New Request") { requestsMenu }
        option("New Contact") { contactsMenu }
        option("New Product") { productsMenu }
        content { t -> t.printLine("This is the main menu") }
        promptMessage("Please select a command. ` to exit program:")
    }

private val requestsMenu =
    screenWithMenu {
        title("NEW REQUEST")
        promptMessage(
            "<Enter> to display (20) more.\n" +
                "\n" +
                "Please select affected product. ` to abort:",
        )
    }
private val productsMenu =
    screenWithMenu {
        promptMessage("Please enter product name (1-30 characters). ` to exit: ")
        content { t -> t.printLine("We are in the products menu") }
    }
private val contactsMenu =
    screenWithMenu {
        content { t -> t.printLine("We are in the contacts menu") }
    }
