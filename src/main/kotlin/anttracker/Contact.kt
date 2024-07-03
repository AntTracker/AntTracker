/* Contact.kt
Revision History:
Rev. 1 - 2024/07/01 Original by M. Baker
-------------------------------------------------------------------------------
The Contact module contains all exported classes and functions pertaining to
    the creation or selection of contact entities.
-------------------------------------------------------------------------------
*/

package anttracker.contact

// ----------------------------------------------------------------------------

@JvmInline
value class Name(
    val name: String,
) {
    init {
        require(name.length in 1..30) {
            "Name must be in between 1 and 30 characters"
        }
    }
}

@JvmInline
value class Email(
    val email: String,
) {
    init {
        require(email.contains("@") && email.length in 5..24) {
            "Email does not contain @ or is not within 1 to 30 characters"
        }
    }
}

@JvmInline
value class PhoneNumber(
    val number: String,
) {
    init {
        require(
            number.all { c -> c.isDigit() } &&
                (
                    (number.length == 11 && number.first() == '1') ||
                        (number.length == 10)
                ),
        ) {
            "The phone number is not callable within BC"
        }
    }
}

@JvmInline
value class Department(
    val name: String,
) {
    init {
        require(name.length in 1..12) {
            "The name of the department does not have 1-12 characters"
        }
    }
}

// Data class for storing the attributes of a given contact.
// ---
data class Contact(
    val name: Name,
    val email: Email,
    val phoneNumber: PhoneNumber,
    val department: Department,
)

// ----------------------------------------------------------------------------
// Displays a sub-menu for creating a new contact and adding it to the
//     AntTracker database.
// Prompts the user for the various fields for the contact, and validates
//     input when necessary, re-prompting where necessary.
// Returns the created contact.
// ---
fun enterContactInformation(): Contact {
    TODO()
}

// ----------------------------------------------------------------------------
// Displays a sub-menu for selecting an existing contact.
// Implements pagination when necessary.
// Returns a string indicating the user input that terminated the selection:
//   "`": exit the interface
//   "1"...: selected row
// ---
fun displayContacts(): String {
    TODO()
}

// ----------------------------------------------------------------------------
// Returns all information about the contact identified by a given name.
// ---
fun getContactInfo(
    name: Name, // in
): Contact {
    TODO()
}

// ----------------------------------------------------------------------------
// Displays a sub-menu for creating a new contact and adding it to the
//     AntTracker database. To be used by the Main module.
// Prompts the user for the various fields for the contact, and validates
//     input when necessary, re-prompting where necessary.
// Returns when the user wishes to return to the main menu.
// ---
fun menu() {
    TODO()
}
