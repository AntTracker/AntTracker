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

import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

// ----------------------------------------------------------------------------
// Displays a sub-menu for creating a new contact and adding it to the
//     AntTracker database. To be used by the Main module.
// Prompts the user for the various fields for the contact, and validates
//     input when necessary, re-prompting where necessary.
// Returns when the user wishes to return to the main menu.
// ---
fun menu() {
    println("== NEW CONTACT ==")
    val newContact = enterContactInformation()
    if (newContact != null) {
        saveToDB(newContact)
    }
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

// Assuming we have a Contacts table defined for Exposed ORM
object Contacts : IntIdTable() {
    val contactName = varchar("contact_name", 50)
    val email = varchar("email", 50).uniqueIndex()
    val phoneNumber = varchar("phone_number", 20)
    val department = varchar("department", 50).nullable()
}

// Contact entity
class ContactEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ContactEntity>(Contacts)
    var contactName by Contacts.contactName
    var email by Contacts.email
    var phoneNumber by Contacts.phoneNumber
    var department by Contacts.department
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
            println("${index + 1}. ${contactRecord.contactName}")
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
        println("Please enter contact name (1-50 characters). ` to abort:")
        val name = readLine()!!
        if (name == "`") return null
        if (name.length !in 1..50) {
            println("ERROR: Invalid contact name length.")
            continue
        }

        println("Please enter contact phone number (10-20 characters). ` to abort:")
        val phone = readLine()!!
        if (phone == "`") return null
        if (phone.length !in 10..20) {
            println("ERROR: Invalid phone number length.")
            continue
        }

        println("Please enter contact email address (1-50 characters). ` to abort:")
        val email = readLine()!!
        if (email == "`") return null
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
        if (email.length !in 1..50 || !email.matches(emailRegex)) {
            println("ERROR: Invalid email. Email must follow the format: name@domain.tld")
            continue
        }

        println("Please enter contact department (1-50 characters). <Enter> to leave blank. ` to abort:")
        val department = readLine()!!
        if (department == "`") return null
        if (department.isNotBlank() && department.length !in 1..50) {
            println("ERROR: Invalid department name.")
            continue
        }

        val newContact = Contact(name, phone, email, if (department.isBlank()) null else department)
        println("Contact ${newContact.name} has been created.")
        return newContact
    }
}

// Save a contact to the database
private fun saveToDB(contact: Contact) {
    transaction {
        if (ContactEntity.find { Contacts.email eq contact.email }.empty()) {
            ContactEntity.new {
                contactName = contact.name
                email = contact.email
                phoneNumber = contact.phone
                department = contact.department
            }
            println("${contact.name} has been created.")
        } else {
            println("ERROR: Contact with this email already exists.")
        }
    }
}

// ----------------------------------------------------------------------------
// Returns all information about the contact identified by a given name.
// ---
fun getContactInfo(name: String): ContactEntity? {
    return transaction {
        ContactEntity.find { Contacts.contactName eq name }.firstOrNull()
    }
}

