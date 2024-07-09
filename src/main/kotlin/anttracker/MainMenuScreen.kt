package anttracker

val mainMenu =
    screenWithMenu {
        title("Main menu")
        option("Issues") { issuesMenu }
        option("Requests") { requestsMenu }
        option("Products") { productsMenu }
        option("Contacts") { contactsMenu }
        content { t -> t.printLine("This is the main menu") }
    }

private val requestsMenu =
    screenWithMenu {
        content { t -> t.printLine("We are in the requests menu") }
    }
private val productsMenu =
    screenWithMenu {
        content { t -> t.printLine("We are in the products menu") }
    }
private val contactsMenu =
    screenWithMenu {
        content { t -> t.printLine("We are in the contacts menu") }
    }
