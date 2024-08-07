/* SetupSchema.kt
Revision History:
Rev. 1 - 2024/07/02 Original by Eitan Barylko
Rev. 2 - 2024/07/30 By Tyrus Tracey, Micah Baker
-------------------------------------------------------------------------------
This file contains the schema for the
database, defining the tables for products, contacts, requests, issues,
and releases. It also contains
a function which sets up the database.
-------------------------------------------------------------------------------
*/

package anttracker.db

import anttracker.issues.Status
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate

// -----

/** ----
 * This function creates the schema for the database and adds some sample
 * products, releases, and issues.
---- */
fun setupSchema(
    shouldPopulate: Boolean, // in
) {
    transaction {
        SchemaUtils.createMissingTablesAndColumns(Products, Issues, Releases, Requests, Contacts)

        if (shouldPopulate) {
            populate()
        }
    }
}

/**
 * Generates sample data for the database.
 */
fun populate() {
    (0..5).forEach { productId ->
        val prodId = Products.insert { it[name] = "Product $productId" } get Products.id
        (0..10).forEach { id ->
            val relId =
                Releases.insert {
                    it[product] = prodId
                    it[releaseId] = "p-$prodId-$id"
                    it[releaseDate] = LocalDate.now().plusDays((-40..0L).random()).atStartOfDay()
                } get Releases.id
            (0..10).forEach { issueId ->

                val issId =
                    Issues.insert {
                        it[description] = "Issue $issueId"
                        it[product] = prodId
                        it[status] = Status.all()[issueId % 5].toString()
                        it[priority] = (issueId % 5 + 1).toShort()
                        it[creationDate] = LocalDate.now().plusDays((-40..0L).random()).atStartOfDay()
                        it[anticipatedRelease] = relId.takeUnless { issueId % 3 == 0 }
                    } get Issues.id
                if (issueId % 3 == 0) {
                    (0..25).forEach { requestId ->
                        val contId =
                            Contacts.insert {
                                it[name] = "a-$requestId"
                                it[email] = "a-$requestId@sfu.ca"
                                it[phoneNumber] = "12345678901"
                                it[department] = "Marketing"
                            } get Contacts.id
                        Requests.insert {
                            it[affectedRelease] = relId
                            it[issue] = issId
                            it[requestDate] = CurrentDateTime
                            it[contact] = contId
                        }
                    }
                }
            }
        }
    }
}

/** ---
 * Represents the products table.
--- */
object Products : IntIdTable() {
    val name = varchar("name", 50)
}

/** ---
 * Represents a single row in the products table.
--- */
class ProductEntity(
    id: EntityID<Int>,
) : IntEntity(id) {
    companion object : IntEntityClass<ProductEntity>(Products)

    var name by Products.name
}

class Product(
    id: EntityID<Int>,
) : IntEntity(id) {
    companion object : IntEntityClass<Product>(Products)

    var name by Products.name
}

/** ---
 * Represents the releases table.
--- */
object Releases : IntIdTable() {
    val releaseId = varchar("release_id", 8)
    val product = reference("product", Products)
    val releaseDate = datetime("release_date")
}

/** ---
 * Represents a single row in the releases table.
--- */
class Release(
    id: EntityID<Int>,
) : IntEntity(id) {
    companion object : IntEntityClass<Release>(Releases)

    var releaseId by Releases.releaseId
    var product by ProductEntity referencedOn Releases.product
    var releaseDate by Releases.releaseDate

    override fun toString(): String = releaseId
}

/** ---
 * Represents a valid description for an issue
--- */
@JvmInline
value class IssueDescription private constructor(
    val description: String,
) {
    companion object {
        const val MAX_LENGTH = 30

        /** ---
         * Checks if the description is of the expected length
         --- */
        fun isValid(
            description: String, // in
        ) = description.length in (1..MAX_LENGTH)

        /** ---
         * Parses the candidate description
         --- */
        fun maybeParse(
            candidate: String, // in
        ) = candidate.takeIf(::isValid)?.let(::IssueDescription)
    }

    override fun toString() = this.description
}

/** ---
 * Represents the issues table.
--- */
object Issues : IntIdTable() {
    val description = varchar("description", IssueDescription.MAX_LENGTH)
    val product = reference("product", Products)
    val anticipatedRelease = reference("release", Releases).nullable()
    val creationDate = datetime("creation_date")
    val status = varchar("status", 11)
    val priority = short("priority")
}

/** ---
 * Represents a single row in the issues table.
--- */
class Issue(
    id: EntityID<Int>,
) : IntEntity(id) {
    companion object : IntEntityClass<Issue>(Issues)

    private var _description by Issues.description
    var description: IssueDescription
        set(newDescription) {
            _description = newDescription.description
        }
        get() = requireNotNull(IssueDescription.maybeParse(_description))
    var product by ProductEntity referencedOn Issues.product
    var anticipatedRelease by Release optionalReferencedOn Issues.anticipatedRelease
    var creationDate by Issues.creationDate
    private var _status by Issues.status
    var status: Status
        set(newStatus) {
            println(newStatus.toString())
            _status = newStatus.toString()
        }
        get() = requireNotNull(_status.toStatus())
    var priority by Issues.priority
}

/** ---
 * Returns the corresponding status for the passed string
--- */
fun String.toStatus(): Status? =
    when (this) {
        "Created" -> Status.Created
        "Assessed" -> Status.Assessed
        "InProgress" -> Status.InProgress
        "Done" -> Status.Done
        "Cancelled" -> Status.Cancelled
        else -> null
    }

/** ---
 * Represents the priority an issue can have, being in [1, 5]
--- */
@JvmInline
value class Priority(
    val priority: Int,
) {
    init {
        priority in (1..5)
    }
}

/** ---
 * Represents the contacts table.
--- */
object Contacts : IntIdTable() {
    val name = varchar("name", 30)
    val email = varchar("email", 24)
    val phoneNumber = varchar("phone_number", 11)
    val department = varchar("department", 12)
}

/** ---
 * Represents a single row in the contacts table.
--- */
class Contact(
    id: EntityID<Int>,
) : IntEntity(id) {
    companion object : IntEntityClass<Contact>(Contacts)

    var name by Contacts.name
    var email by Contacts.email
    var phoneNumber by Contacts.phoneNumber
    var department by Contacts.department
}

/** ---
 * Represents the requests table.
--- */
object Requests : IntIdTable() {
    val affectedRelease = reference("release_id", Releases)
    val issue = reference("issue_id", Issues)
    val contact = reference("contact_id", Contacts)
    val requestDate = datetime("request_date")
}

/** ---
 * Represents a single row in the requests table.
--- */
class Request(
    id: EntityID<Int>,
) : IntEntity(id) {
    companion object : IntEntityClass<Request>(Requests)

    var affectedRelease by Release referencedOn Requests.affectedRelease
    var issue by Issue referencedOn Requests.issue
    var contact by Contact referencedOn Requests.contact
    var requestDate by Requests.requestDate
}
