package com.company.snackbox.service

import com.company.snackbox.model.User
import com.company.snackbox.repository.UserRepository
import mu.KotlinLogging
import java.util.UUID

private val logger = KotlinLogging.logger {}

/**
 * Service for user operations
 */
class UserService(
    private val userRepository: UserRepository
) {
    
    /**
     * Creates a new user
     *
     * @param name User's name
     * @param email User's email address
     * @param isAdmin Whether the user has admin privileges
     * @return Created user
     */
    fun createUser(name: String, email: String, isAdmin: Boolean = false): User {
        logger.info { "Creating user: $name, email: $email, isAdmin: $isAdmin" }
        
        // Check if email is already used
        if (userRepository.findByEmail(email) != null) {
            throw IllegalArgumentException("Email already exists: $email")
        }
        
        // Create user
        val user = User(
            name = name,
            email = email,
            isAdmin = isAdmin
        )
        
        // Validate user
        if (!user.isValid()) {
            throw IllegalArgumentException("Invalid user data")
        }
        
        // Save to database
        return userRepository.create(user)
    }
    
    /**
     * Updates an existing user
     *
     * @param id ID of the user to update
     * @param name New name (optional)
     * @param email New email (optional)
     * @param isAdmin New admin status (optional)
     * @return Updated user
     */
    fun updateUser(id: UUID, name: String? = null, email: String? = null, isAdmin: Boolean? = null): User {
        logger.info { "Updating user: $id" }
        
        // Get existing user
        val existingUser = userRepository.findById(id) ?: throw IllegalArgumentException("User not found: $id")
        
        // Check if email is already used by another user
        if (email != null && email != existingUser.email && userRepository.findByEmail(email) != null) {
            throw IllegalArgumentException("Email already exists: $email")
        }
        
        // Update user
        val updatedUser = existingUser.update(
            name = name ?: existingUser.name,
            email = email ?: existingUser.email,
            isAdmin = isAdmin ?: existingUser.isAdmin
        )
        
        // Validate user
        if (!updatedUser.isValid()) {
            throw IllegalArgumentException("Invalid user data")
        }
        
        // Save to database
        return userRepository.update(updatedUser)
    }
    
    /**
     * Deletes a user
     *
     * @param id ID of the user to delete
     * @return true if the user was deleted, false otherwise
     */
    fun deleteUser(id: UUID): Boolean {
        logger.info { "Deleting user: $id" }
        
        return userRepository.delete(id)
    }
    
    /**
     * Gets a user by ID
     *
     * @param id ID of the user to get
     * @return User if found, null otherwise
     */
    fun getUserById(id: UUID): User? {
        logger.debug { "Getting user by ID: $id" }
        
        return userRepository.findById(id)
    }
    
    /**
     * Gets a user by email
     *
     * @param email Email of the user to get
     * @return User if found, null otherwise
     */
    fun getUserByEmail(email: String): User? {
        logger.debug { "Getting user by email: $email" }
        
        return userRepository.findByEmail(email)
    }
    
    /**
     * Lists all users
     *
     * @return List of all users
     */
    fun getAllUsers(): List<User> {
        logger.debug { "Getting all users" }
        
        return userRepository.findAll()
    }
    
    /**
     * Lists all admin users
     *
     * @return List of all admin users
     */
    fun getAllAdminUsers(): List<User> {
        logger.debug { "Getting all admin users" }
        
        return userRepository.findAllAdmins()
    }
}
