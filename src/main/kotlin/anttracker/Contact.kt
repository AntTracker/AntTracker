/* Revision History:
Rev. 1 - 2024/07/01 Original by M. Baker
Rev. 2 - 2024/07/08 Updated by A. Kim
Rev.3 - 2024/07/30 Updated by A. Kim
-------------------------------------------------------------------------------
The Contact module contains all exported classes and functions pertaining to
    the creation or selection of contact entities.
-------------------------------------------------------------------------------
*/

package anttracker.contact

import anttracker.PageOf
import anttracker.db.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

// ----------------------------------------------------------------------------
// Displays a sub-menu for creating a new contact and adding it to the
// AntTracker database. To be used by the Main module.
// Prompts the user for the various fields for the contact, and validates
// input when necessary, re-prompting where necessary.
// Returns when the user wishes to return to the main menu.
// ---
fun menu() {
    println("== NEW CONTACT ==")
    val newContact = enterContactInformation()
    if (newContact != null) {
        println("Contact ${newContact.name} has been created.")
    }
}

// Displays a sub-menu for entering contact information.
fun enterContactInformation(): Contact? {
    var name: String
    var phone: String
    var email: String
    var department: String

    // Enter and validate name
    while (true) {
        println("Please enter contact name (1-30 characters). ` to abort:")
        name = readln()
        if (name == "`") return null
        if (name.length !in 1..30) {
            println("ERROR: Invalid contact name length.")
            continue
        }
        if (!isUniqueContactName(name)) {
            println("ERROR: Contact name already exists.")
            continue
        }
        break
    }

    // Enter and validate phone number
    while (true) {
        println("Please enter contact phone number (10-11 characters). ` to abort:")
        phone = readln()
        if (phone == "`") return null
        if (phone.length !in 10..11) {
            println("ERROR: Invalid phone number length.")
            continue
        }
        break
    }

    // Enter and validate email
    while (true) {
        println("Please enter contact email address (1-24 characters). ` to abort:")
        email = readln()
        if (email == "`") return null
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
        if (email.length !in 1..24 || !email.matches(emailRegex)) {
            println("ERROR: Invalid email. Email must follow the format: name@domain.tld")
            continue
        }
        break
    }

    // Enter and validate department
    while (true) {
        println("Please enter contact department (1-12 characters). <Enter> to leave blank. ` to abort:")
        department = readln()
        if (department == "`") return null
        if (department.isNotBlank() && department.length !in 1..12) {
            println("ERROR: Invalid department name.")
            continue
        }
        if (department.isBlank()) {
            department = ""
        }
        break
    }

    return transaction {
        Contact.new {
            this.name = name
            this.phoneNumber = phone
            this.email = email
            this.department = department
        }
    }
}

// Checks if the contact name is unique in the database
private fun isUniqueContactName(name: String): Boolean {
    return transaction {
        Contact.find { Contacts.name eq name }.empty()
    }
}

// ----------------------------------------------------------------------------
// A class that represents a page of contacts.
// This class handles pagination and display of contacts from the database.
// ---
private class PageOfContact : PageOf<Contact>(Contact) {
    init {
        initLastPageNum()
    }
    // Query object that defines how to retrieve the contacts from the database.
    //creates a query to select all records from the Contacts table, 
    //ordering them by the name column in ascending order.
    override fun getQuery(): Query =
        Contacts
            .selectAll()
            .orderBy(
                Contacts.name to SortOrder.ASC,
            )
    // prints employee info (name, email, phone number, dept (if internal))
    override fun printRecord(record: Contact) {
        if (record.department.isEmpty()) {
            println("${record.name}, <${record.email}>, ${record.phoneNumber}")
        } else {
            println("${record.name}, <${record.email}>, ${record.phoneNumber}, ${record.department}")
        }
    }
}

// Display pages of contacts to console and select one
fun selectContact(): Contact? {
    val contactPage = PageOfContact()
    contactPage.loadRecords() // Adjust limit and offset as necessary
    contactPage.display()

    var linenum: Int? = null
    while (linenum == null) {
        println("Please select contact. ` to abort:")
        val userInput = readln()
        when (userInput) {
            "`" -> return null
            "" -> {
                if (!contactPage.lastPage()) {
                    contactPage.loadNextPage()
                    contactPage.display()
                }
            }
            else -> {
                try {
                    // Check if the line number is valid and within the current page's range
                    val userInputInt = userInput.toInt()
                    // validate integer selection
                    if (contactPage.isValidLineNum(userInputInt)) {
                        linenum = userInputInt
                    } else {
                        println("ERROR: Invalid line number.")
                    }
                } catch (e: NumberFormatException) {
                    println("ERROR: Please enter a number.")
                }
            }
        }
    }
    return contactPage.getContentAt(linenum - 1)
}