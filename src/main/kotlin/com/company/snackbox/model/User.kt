package com.company.snackbox.model

import java.time.LocalDateTime
import java.util.UUID

/**
 * Represents a user in the system
 *
 * @property id Unique identifier for the user
 * @property name User's name
 * @property email User's email address
 * @property isAdmin Whether the user has admin privileges
 * @property createdAt When the user was created
 * @property updatedAt When the user was last updated
 */
data class User(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val email: String,
    val isAdmin: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * Validates that the user data is correct
     *
     * @return true if the user data is valid, false otherwise
     */
    fun isValid(): Boolean {
        return name.isNotBlank() && 
               email.isNotBlank() && 
               email.contains("@")
    }
    
    /**
     * Creates a copy of this user with updated timestamp
     *
     * @param name New name (optional)
     * @param email New email (optional)
     * @param isAdmin New admin status (optional)
     * @return Updated user
     */
    fun update(
        name: String = this.name,
        email: String = this.email,
        isAdmin: Boolean = this.isAdmin
    ): User {
        return copy(
            name = name,
            email = email,
            isAdmin = isAdmin,
            updatedAt = LocalDateTime.now()
        )
    }
}
