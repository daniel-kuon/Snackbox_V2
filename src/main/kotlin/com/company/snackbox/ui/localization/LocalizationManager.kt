package com.company.snackbox.ui.localization

import com.company.snackbox.config.AppConfig
import mu.KotlinLogging
import java.text.MessageFormat
import java.util.*

private val logger = KotlinLogging.logger {}

/**
 * Manages localization resources for the application
 */
class LocalizationManager(private val config: AppConfig) {
    private var currentLocale: Locale = Locale.ENGLISH
    private var resourceBundle: ResourceBundle? = null
    
    init {
        // Set initial locale from config
        setLocale(config.ui.defaultLanguage)
    }
    
    /**
     * Sets the current locale
     *
     * @param language Language code (e.g., "en", "de")
     */
    fun setLocale(language: String) {
        logger.info { "Setting locale to: $language" }
        
        try {
            currentLocale = Locale(language)
            resourceBundle = ResourceBundle.getBundle("i18n.messages", currentLocale)
            logger.info { "Locale set to: $currentLocale" }
        } catch (e: Exception) {
            logger.error(e) { "Error setting locale to: $language, falling back to English" }
            currentLocale = Locale.ENGLISH
            resourceBundle = ResourceBundle.getBundle("i18n.messages", currentLocale)
        }
    }
    
    /**
     * Gets the current locale
     *
     * @return Current locale
     */
    fun getCurrentLocale(): Locale = currentLocale
    
    /**
     * Gets a localized string
     *
     * @param key Resource key
     * @return Localized string, or the key itself if not found
     */
    fun getString(key: String): String {
        return try {
            resourceBundle?.getString(key) ?: key
        } catch (e: MissingResourceException) {
            logger.warn { "Missing resource key: $key" }
            key
        }
    }
    
    /**
     * Gets a formatted localized string with arguments
     *
     * @param key Resource key
     * @param args Arguments to format into the string
     * @return Formatted localized string, or the key itself if not found
     */
    fun getFormattedString(key: String, vararg args: Any): String {
        val pattern = getString(key)
        return try {
            MessageFormat.format(pattern, *args)
        } catch (e: Exception) {
            logger.warn(e) { "Error formatting string: $key" }
            pattern
        }
    }
    
    /**
     * Gets a list of available languages
     *
     * @return Map of language codes to language names
     */
    fun getAvailableLanguages(): Map<String, String> {
        return mapOf(
            "en" to "English",
            "de" to "Deutsch"
        )
    }
    
    companion object {
        // Singleton instance
        private var instance: LocalizationManager? = null
        
        /**
         * Gets the singleton instance
         *
         * @param config Application configuration
         * @return LocalizationManager instance
         */
        fun getInstance(config: AppConfig): LocalizationManager {
            if (instance == null) {
                instance = LocalizationManager(config)
            }
            return instance!!
        }
    }
}
