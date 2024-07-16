/* Revision History:
Rev. 1 - 2024/07/01 Original by M. Baker
Rev. 2 - 2024/07/08 Updated by A. Kim
-------------------------------------------------------------------------------
The Contact module contains all exported classes and functions pertaining to
    the creation or selection of contact entities.
-------------------------------------------------------------------------------
*/


/*
Contact.kt
Contact				; type or class or struct
menu()					; interactive display
enterContactInformation() -> Contact	; interactive display
displayContacts() -> String		; interactive display
getContactInfo(name)	-> Contact	; helper function
saveToDB(Contact)			; helper function
*/
package anttracker.contact

import anttracker.PageOf

// ----------------------------------------------------------------------------
// Displays a sub-menu for creating a new contact and adding it to the
//     AntTracker database. To be used by the Main module.
// Prompts the user for the various fields for the contact, and validates
//     input when necessary, re-prompting where necessary.
// Returns when the user wishes to return to the main menu.
// ---
fun menu() {
    println("== NEW PRODUCT ==")
    enterContactInformation()
    }


// ----------------------------------------------------------------------------
// Data class for storing the attributes of a given contact.
// ---
data class Contact(
    val name: String,
    val phone: String,
    val email: String,
    val department: String? = null // optional
)


//pagination 

//PageOf<T>
open class PageOf<T> {
    var contents: MutableList<T> = mutableListOf()
    var pagenum: Int = 0 // decide on 0 or 1 indexed?
    var lastPageNum: Int = 0 // initialized on construct/init
    var offset: Int = 0
    var limit: Int = 20

    open fun loadContents() {
        // To be overridden in subclasses
    }

    fun loadNextPage() {
        if (lastPage()) {
            throw Exception("Already at the last page")
        }
        pagenum++
        loadContents()
    }

    fun lastPage(): Boolean {
        return pagenum >= lastPageNum
    }

    fun initLastPageNum(totalRecords: Int) {
        lastPageNum = Math.ceil(totalRecords.toDouble() / limit).toInt()
    }
}

// PageOf<Contact>
class PageOfContact : PageOf<Contact>() {
    fun display() {
        for ((index, contactRecord) in contents.withIndex()) {
            println("${index + 1}. ${contactRecord.contactID}")  // add some nice formatting
        }
    }

    override fun loadContents() {
        contents.clear()
        transaction {
            val output = Contact
                .find { Contact.someCriteria eq someValue }
                .limit(limit)
                .offset(pagenum * limit + offset)
            output.forEach {
                contents.add(it)
            }
        }
    }
}

// Display pages of contacts to console and select one
fun selectContact(): Contact? {
    val contactPage = PageOfContact()
    contactPage.loadContents()
    contactPage.display()

    var linenum: Int? = null
    while (linenum == null) {
        println("Please select contact. ` to abort: ")
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
                linenum = userInput.toIntOrNull()
                if (linenum == null || linenum !in 1..contactPage.contents.size) {
                    println("Invalid line number")
                    linenum = null
                }
            }
        }
    }
    return contactPage.contents[linenum - 1]
}





// ----------------------------------------------------------------------------
// Displays a sub-menu for creating a new contact and adding it to the
//     AntTracker database.
// Prompts the user for the various fields for the contact, and validates
//     input when necessary, re-prompting where necessary.
// Returns the created contact.
// ---


fun enterContactInformation(): Contact? {
    while (true) {
        println("Please enter contact name (1-30 characters). ` to abort:")
        val name = readLine()!!
        if (name == "`") return null
        if (name.length !in 1..30) {
            println("ERROR: Invalid contact name length.")
            continue
        }

        println("Please enter contact phone number (10-11 characters). ` to abort:")
        val phone = readLine()!!
        if (phone == "`") return null
        if (phone.length !in 10..11) {
            println("ERROR: Invalid phone number length.")
            continue
        }

        println("Please enter contact email address (1-24 characters). ` to abort:")
        val email = readLine()!!
        if (email == "`") return null
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
        if (email.length !in 1..24 || !email.matches(emailRegex)) {
            println("ERROR: Invalid email. Email must follow the format: name@domain.tld")
            continue
        }

        println("Please enter contact department (1-12 characters). <Enter> to leave blank. ` to abort:")
        val department = readLine()!!
        if (department == "`") return null
        if (department.isNotBlank() && department.length !in 1..12) {
            println("ERROR: Invalid department name.")
            continue
        }

        val newContact = Contact(name, phone, email, if (department.isBlank()) null else department)
        println("Contact ${newContact.name} has been created.")
        return newContact
    }
}

// ----------------------------------------------------------------------------
// Displays a sub-menu for selecting an existing contact.
// Implements pagination when necessary.
// Returns a string indicating the user input that terminated the selection:
//   "`": exit the interface
//   "1"...: selected row
// ---
fun displayContacts(): String {
    while (true) {
        println("Please enter contact name (1-30 characters). ` to abort:")
        val name = readLine()!!
        if (name == "`") return null
        if (name.length !in 1..30) {
            println("ERROR: Invalid contact name length.")
            continue
        }

        println("Please enter contact phone number (10-11 characters). ` to abort:")
        val phone = readLine()!!
        if (phone == "`") return null
        if (phone.length !in 10..11) {
            println("ERROR: Invalid phone number length.")
            continue
        }

        println("Please enter contact email address (1-24 characters). ` to abort:")
        val email = readLine()!!
        if (email == "`") return null
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
        if (email.length !in 1..24 || !email.matches(emailRegex)) {
            println("ERROR: Invalid email. Email must follow the format: name@domain.tld")
            continue
        }

        println("Please enter contact department (1-12 characters). <Enter> to leave blank. ` to abort:")
        val department = readLine()!!
        if (department == "`") return null
        if (department.isNotBlank() && department.length !in 1..12) {
            println("ERROR: Invalid department name.")
            continue
        }

        return Contact(name, phone, email, if (department.isBlank()) null else department)
    }
}


// ----------------------------------------------------------------------------
// Returns all information about the contact identified by a given name.
// ---
fun getContactInfo(name: String): Contact? {
    return contacts.find { it.name.equals(name, ignoreCase = true) }
}

