package anttracker.db

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

fun setupSchema() {
    transaction {
        SchemaUtils.createMissingTablesAndColumns(Products, Issues, Releases)

        (0..5).forEach { productId ->
            val prodId = Products.insert { it[name] = "Product $productId" } get Products.id
            (0..5).forEach { id ->
                val relId =
                    Releases.insert {
                        it[product] = prodId
                        it[releaseId] = "$id"
                        it[releaseDate] = CurrentDateTime
                    } get Releases.id
                (0..20).forEach { issueId ->

                    Issues.insert {
                        it[description] = "Issue $issueId"
                        it[product] = prodId
                        it[status] = "done"
                        it[priority] = 1
                        it[creationDate] = CurrentDateTime
                        it[anticipatedRelease] = relId
                    }
                }
            }
        }
    }
}

object Products : IntIdTable() {
    val name = varchar("name", 50)
}

class Product(
    id: EntityID<Int>,
) : IntEntity(id) {
    companion object : IntEntityClass<Product>(Products)

    var name by Products.name
}

object Releases : IntIdTable() {
    val releaseId = varchar("release_id", 8)
    val product = reference("product", Products)
    val releaseDate = datetime("release_date")
}

class Release(
    id: EntityID<Int>,
) : IntEntity(id) {
    companion object : IntEntityClass<Release>(Releases)

    var releaseId by Releases.releaseId
    var product by Product referencedOn Releases.product
    var releaseDate by Releases.releaseDate
}

object Issues : IntIdTable() {
    val description = varchar("description", 30)
    val product = reference("product", Products)
    val anticipatedRelease = reference("release", Releases)
    val creationDate = datetime("creation_date")
    val status = varchar("status", 9)
    val priority = short("priority")
}

class Issue(
    id: EntityID<Int>,
) : IntEntity(id) {
    companion object : IntEntityClass<Issue>(Issues)

    var description by Issues.description
    var product by Product referencedOn Issues.product
    var anticipatedRelease by Release referencedOn Issues.anticipatedRelease
    var creationDate by Issues.creationDate
    var status by Issues.status
    var priority by Issues.priority
}

@JvmInline
value class Priority(
    val priority: Int,
) {
    init {
        priority in (1..5)
    }
}

sealed class IssueStatus {
    data object Triage : IssueStatus()

    data object Open : IssueStatus()

    data object Working : IssueStatus()

    data object Review : IssueStatus()

    data object Closed : IssueStatus()
}

@JvmInline
value class ReleaseId(
    val id: UUID,
)

object Contacts : IntIdTable() {
    val name = varchar("name", 30)
    val email = varchar("email", 24)
    val phoneNumber = varchar("phone_number", 11)
    val department = varchar("department", 12)
}

class Contact(
    id: EntityID<Int>,
) : IntEntity(id) {
    companion object : IntEntityClass<Contact>(Contacts)

    var name by Contacts.name
    var email by Contacts.email
    var phoneNumber by Contacts.phoneNumber
    var department by Contacts.department
}

data class Request(
    val id: UUID,
    val foundOn: ReleaseId,
    val fixBy: ReleaseId?,
    val contact: Contact,
)
