package com.company.snackbox.config

import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.PropertySource
import mu.KotlinLogging
import java.io.File

private val logger = KotlinLogging.logger {}

/**
 * Application configuration loaded from application.yaml
 *
 * @property database Database configuration
 * @property application Application settings
 * @property ui UI settings
 */
data class AppConfig(
    val database: DatabaseConfig,
    val application: ApplicationConfig,
    val ui: UiConfig
) {
    companion object {
        /**
         * Loads configuration from the application.yaml file
         *
         * @return AppConfig instance
         */
        fun load(): AppConfig {
            logger.info { "Loading application configuration" }

            // First try to load from external config file
            val externalConfigFile = File("application.yaml")

            return if (externalConfigFile.exists()) {
                logger.info { "Loading configuration from external file: ${externalConfigFile.absolutePath}" }
                ConfigLoader.builder()
                    .addSource(PropertySource.file(externalConfigFile))
                    .build()
                    .loadConfigOrThrow()
            } else {
                logger.info { "Loading configuration from classpath" }
                ConfigLoader.builder()
                    .addSource(PropertySource.resource("/application.yaml"))
                    .build()
                    .loadConfigOrThrow()
            }
        }
    }
}

/**
 * Database configuration
 *
 * @property url JDBC URL for the database
 * @property driver JDBC driver class name
 * @property username Database username
 * @property password Database password
 */
data class DatabaseConfig(
    val url: String,
    val driver: String,
    val username: String = "",
    val password: String = ""
)

/**
 * Application settings
 *
 * @property sessionTimeoutMinutes Session timeout in minutes
 * @property paypalMeAddress PayPal.me address for payments
 * @property currencySymbol Currency symbol
 */
data class ApplicationConfig(
    val sessionTimeoutMinutes: Int,
    val paypalMeAddress: String,
    val currencySymbol: String
)

/**
 * UI settings
 *
 * @property title Window title
 * @property width Window width
 * @property height Window height
 * @property defaultLanguage Default language code
 */
data class UiConfig(
    val title: String,
    val width: Int,
    val height: Int,
    val defaultLanguage: String
)
