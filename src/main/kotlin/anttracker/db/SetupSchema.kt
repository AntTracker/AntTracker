// SetupSchema.kt
// Revision History:
// Rev. 1 - 2024/07/02 Original by Eitan
// Rev. 2 - 2024/07/09 by T. Tracey
// Rev. 3 - 2024/07/16 by M. Baker
// -------------------------------------------
// This file contains the schema for the
// database, defining the tables for
// products, contacts, requests, issues,
// and releases. It also contains
// a function which sets up the database.
// ---------------------------------

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

// -----

/** ----
 * This function creates the schema for the database and adds some sample
 * products, releases, and issues.
---- */
fun setupSchema(shouldPopulate: Boolean) {
    transaction {
        SchemaUtils.createMissingTablesAndColumns(Products, Issues, Releases, Requests, Contacts)

        if (shouldPopulate) {
            populate()
        }
    }
}

private val issueIdToStatus =
    arrayOf(
        Status.Created,
        Status.Assessed,
        Status.InProgress,
        Status.Done,
        Status.Cancelled,
    )

private fun genStatus(id: Int): Status = issueIdToStatus[id % 5]

fun populate() {
    (0..5).forEach { productId ->
        val prodId = Products.insert { it[name] = "Product $productId" } get Products.id
        (0..5).forEach { id ->
            val relId =
                Releases.insert {
                    it[product] = prodId
                    it[releaseId] = "$prodId-$id"
                    it[releaseDate] = CurrentDateTime
                } get Releases.id
            (0..20).forEach { issueId ->
                val issId =
                    Issues.insert {
                        it[description] = "Issue $issueId"
                        it[product] = prodId
                        it[status] = genStatus(issueId).toString()
                        it[priority] = 1
                        it[creationDate] = CurrentDateTime
                        it[anticipatedRelease] = relId
                    } get Issues.id
                (0..45).forEach { requestId ->
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
 * Represents the issues table.
--- */
object Issues : IntIdTable() {
    val description = varchar("description", 30)
    val product = reference("product", Products)
    val anticipatedRelease = reference("release", Releases)
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

    var description by Issues.description
    var product by ProductEntity referencedOn Issues.product
    var anticipatedRelease by Release referencedOn Issues.anticipatedRelease
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

    var affectedRelease by Requests.affectedRelease
    var issue by Requests.issue
    var contact by Contact referencedOn Requests.contact
    var requestDate by Requests.requestDate
}
