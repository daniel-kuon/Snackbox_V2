package com.company.snackbox.repository

import com.company.snackbox.database.UsersTable
import com.company.snackbox.model.User
import mu.KotlinLogging
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.util.UUID

private val logger = KotlinLogging.logger {}

/**
 * Repository for User operations
 */
class UserRepository {

    /**
     * Creates a new user in the database
     *
     * @param user User to create
     * @return Created user with ID
     */
    fun create(user: User): User {
        logger.info { "Creating user: ${user.name}" }

        val id = transaction {
            UsersTable.insert {
                it[id] = user.id
                it[name] = user.name
                it[email] = user.email
                it[isAdmin] = user.isAdmin
                it[createdAt] = user.createdAt
                it[updatedAt] = user.updatedAt
            } get UsersTable.id
        }

        return user.copy(id = id.value)
    }

    /**
     * Updates an existing user in the database
     *
     * @param user User to update
     * @return Updated user
     */
    fun update(user: User): User {
        logger.info { "Updating user: ${user.id}" }

        transaction {
            UsersTable.update({ UsersTable.id eq user.id }) {
                it[name] = user.name
                it[email] = user.email
                it[isAdmin] = user.isAdmin
                it[updatedAt] = LocalDateTime.now()
            }
        }

        return user.copy(updatedAt = LocalDateTime.now())
    }

    /**
     * Deletes a user from the database
     *
     * @param id ID of the user to delete
     * @return true if the user was deleted, false otherwise
     */
    fun delete(id: UUID): Boolean {
        logger.info { "Deleting user: $id" }

        val deletedCount = transaction {
            UsersTable.deleteWhere { UsersTable.id eq id }
        }

        return deletedCount > 0
    }

    /**
     * Finds a user by ID
     *
     * @param id ID of the user to find
     * @return User if found, null otherwise
     */
    fun findById(id: UUID): User? {
        logger.debug { "Finding user by ID: $id" }

        return transaction {
            UsersTable.select { UsersTable.id eq id }
                .mapNotNull { it.toUser() }
                .singleOrNull()
        }
    }

    /**
     * Finds a user by email
     *
     * @param email Email of the user to find
     * @return User if found, null otherwise
     */
    fun findByEmail(email: String): User? {
        logger.debug { "Finding user by email: $email" }

        return transaction {
            UsersTable.select { UsersTable.email eq email }
                .mapNotNull { it.toUser() }
                .singleOrNull()
        }
    }

    /**
     * Lists all users
     *
     * @return List of all users
     */
    fun findAll(): List<User> {
        logger.debug { "Finding all users" }

        return transaction {
            UsersTable.selectAll()
                .mapNotNull { it.toUser() }
        }
    }

    /**
     * Lists all admin users
     *
     * @return List of all admin users
     */
    fun findAllAdmins(): List<User> {
        logger.debug { "Finding all admin users" }

        return transaction {
            UsersTable.select { UsersTable.isAdmin eq true }
                .mapNotNull { it.toUser() }
        }
    }

    /**
     * Converts a ResultRow to a User
     */
    private fun ResultRow.toUser(): User {
        return User(
            id = this[UsersTable.id].value,
            name = this[UsersTable.name],
            email = this[UsersTable.email],
            isAdmin = this[UsersTable.isAdmin],
            createdAt = this[UsersTable.createdAt],
            updatedAt = this[UsersTable.updatedAt]
        )
    }
}
