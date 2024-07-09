package anttracker

/** ------
This function prints out a message asking the user how they would like
to search for an issue.
----- */
fun mkIssuesMenu(): Screen =
    screenWithMenu {
        title("Issues")
        option("Search by Product") { screenWithMenu { content { t -> t.printLine("There are no products at the moment") } } }
        content { t -> (1..10).forEach { t.printLine("This is issue number $it") } }
        promptMessage("Please select search category. ` to abort:")
    }

val issuesMenu = mkIssuesMenu()
