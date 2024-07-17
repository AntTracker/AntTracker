/* Revision History:
Rev. 1 - 2024/07/01 Original by M. Baker
Rev. 2 - 2024/07/08 Updated by A. Kim
-------------------------------------------------------------------------------
The Contact module contains all exported classes and functions pertaining to
    the creation or selection of contact entities.
-------------------------------------------------------------------------------
*/

package anttracker.contact

import anttracker.db.*
import org.jetbrains.exposed.dao.IntEntity

import org.jetbrains.exposed.sql.transactions.transaction

// ----------------------------------------------------------------------------
// Displays a sub-menu for creating a new contact and adding it to the
//     AntTracker database. To be used by the Main module.
// Prompts the user for the various fields for the contact, and validates
//     input when necessary, re-prompting where necessary.
// Returns when the user wishes to return to the main menu.
// ---
fun menu() {
    println("== NEW CONTACT ==")
    enterContactInformation()
}

// PageOf<T>
open class PageOf<T : IntEntity> {
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

// PageOfContact
class PageOfContact : PageOf<ContactEntity>() {
    fun display() {
        for ((index, contactRecord) in contents.withIndex()) {
            println("${index + 1}. ${contactRecord.name}")
        }
    }

    override fun loadContents() {
        contents.clear()
        transaction {
            val output = ContactEntity
                .all()
                .limit(limit, offset = (pagenum * limit).toLong())
            output.forEach {
                contents.add(it)
            }
        }
    }
}

// Display pages of contacts to console and select one
fun selectContact(): ContactEntity? {
    val contactPage = PageOfContact()
    contactPage.loadContents()
    contactPage.display()

    var linenum: Int? = null
    while (linenum == null) {
        println("Please select contact. ` to abort: ")
        when (val userInput = readln()) {
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
fun enterContactInformation(): ContactEntity? {
    while (true) {
        println("Please enter contact name (1-50 characters). ` to abort:")
        val name = readln()
        if (name == "`") return null
        if (name.length !in 1..50) {
            println("ERROR: Invalid contact name length.")
            continue
        }

        println("Please enter contact phone number (10-20 characters). ` to abort:")
        val phone = readln()
        if (phone == "`") return null
        if (phone.length !in 10..20) {
            println("ERROR: Invalid phone number length.")
            continue
        }

        println("Please enter contact email address (1-50 characters). ` to abort:")
        val email = readln()
        if (email == "`") return null
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
        if (email.length !in 1..50 || !email.matches(emailRegex)) {
            println("ERROR: Invalid email. Email must follow the format: name@domain.tld")
            continue
        }

        println("Please enter contact department (1-50 characters). <Enter> to leave blank. ` to abort:")
        var department = readln()
        if (department == "`") return null
        if (department.isNotBlank() && department.length !in 1..50) {
            println("ERROR: Invalid department name.")
            continue
        }
        if (department.isBlank()) {
            department = ""
        }

        val contact = transaction {
            ContactEntity.new {
                this.name = name
                this.phoneNumber = phone
                this.email = email
                this.department = department
            }
        }

        println("Contact ${contact.name} has been created.")
        return contact
    }
}

// ----------------------------------------------------------------------------
// Returns all information about the contact identified by a given name.
// ---
fun getContactInfo(name: String): ContactEntity? {
    return transaction {
        ContactEntity.find { Contacts.name eq name }.firstOrNull()
    }
}