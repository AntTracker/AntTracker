package anttracker

/** ------
This function prints out a message asking the user how they would like
to search for an issue.
----- */
fun mkIssuesMenu(): Screen =
    screenWithMenu {
        title("VIEW/EDIT ISSUE")
        option("Description") { screenWithMenu { content { t -> t.printLine("There are no issues at the moment") } } }
        option("Search by Product") { screenWithMenu { content { t -> t.printLine("There are no products at the moment") } } }
        option("Affected release") {
            screenWithMenu {
                content { t ->
                    t.printLine("There are no releases at the moment for any product")
                }
            }
        }
        option("Anticipated release") {
            screenWithMenu {
                content { t ->
                    t.printLine("There are no releases at the moment for any product")
                }
            }
        }
        option("ID") {
            screenWithMenu {
                content { t ->
                    t.printLine("There are no issues at the moment for any product")
                }
            }
        }

        option("Status") {
            screenWithMenu {
                content { t ->
                    t.printLine("There are no issues at the moment for any product")
                }
            }
        }
        option("Priority") {
            screenWithMenu {
                content { t ->
                    t.printLine("There are no issues at the moment for any product")
                }
            }
        }
        option("All Issues") {
            screenWithMenu {
                content { t ->
                    t.printLine("There are no issues at the moment for any product")
                }
            }
        }
        promptMessage("Please select search category. ` to abort:")
    }

val issuesMenu = mkIssuesMenu()
