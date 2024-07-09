package anttracker

val mainMenu =
    screenWithMenu {
        title("Main menu")
        option("Issues") { issuesMenu }
        option("Requests") { requestsMenu }
        option("Products") { productsMenu }
        option("Contacts") { contactsMenu }
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
        content { t -> t.printLine("We are in the products menu") }
    }
private val contactsMenu =
    screenWithMenu {
        content { t -> t.printLine("We are in the contacts menu") }
    }
