package com.company.snackbox.database

import com.company.snackbox.config.AppConfig
import mu.KotlinLogging
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

private val logger = KotlinLogging.logger {}

/**
 * Manages database connections and schema setup
 */
class DatabaseManager(private val config: AppConfig) {

    /**
     * Initializes the database connection and schema
     */
    fun init() {
        logger.info { "Initializing database connection: ${config.database.url}" }

        // Connect to the database
        Database.connect(
            url = config.database.url,
            driver = config.database.driver,
            user = config.database.username,
            password = config.database.password
        )

        // Create tables if they don't exist
        transaction {
            logger.info { "Creating database schema if not exists" }

            SchemaUtils.create(
                UsersTable,
                BarcodesTable,
                SessionsTable,
                BarcodeScansTable,
                PaymentsTable
            )
        }

        logger.info { "Database initialization completed" }
    }
}
