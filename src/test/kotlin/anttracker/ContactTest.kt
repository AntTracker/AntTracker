/* ContactTest.kt
Revision History:
Rev. 1 - 2024/07/09 Original by Micah Baker
-------------------------------------------------------------------------------
This file contains the tests for the function
enterContactInformation from the Contact module.
-------------------------------------------------------------------------------
 */
import anttracker.contact.enterContactInformation
import anttracker.db.*
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainOnly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * The goal of this test is to confirm that enterContactInformation correctly
 * creates contacts, either for a singularly created contact, or multiple.
 * Any contacts created must be inserted into the database.
 */
class ContactTest :
    DescribeSpec({
        xdescribe("enterContactInformation") {
            describe("when a contact is created") {
                it("should be findable in the database") {
                    // prompts user input to create a contact
                    val contactCreated = enterContactInformation() ?: throw Exception()

                    // find the contact stored in the database identified by the created contact
                    val contactFound =
                        transaction {
                            Contact.find { Contacts.id eq contactCreated.id }.firstOrNull()
                        }

                    contactCreated shouldBe contactFound
                }
            }
            describe("when multiple contacts are created") {
                it("should be findable in the database") {
                    // delete all contacts currently stored in the database
                    transaction {
                        Contact.all().forEach { it.delete() }
                    }

                    // prompts user input to create contacts
                    val contactsCreated =
                        listOf(
                            enterContactInformation(),
                            enterContactInformation(),
                            enterContactInformation(),
                        )

                    // all created contacts should not be null
                    contactsCreated[0] shouldNotBe null
                    contactsCreated[1] shouldNotBe null
                    contactsCreated[2] shouldNotBe null

                    // find the contact stored in the database identified by the created contact
                    val contactsFound =
                        transaction {
                            Contact.all()
                        }

                    contactsFound.shouldContainOnly(contactsCreated)
                }
            }
        }
    })
