package com.company.snackbox.service

import com.company.snackbox.config.AppConfig
import com.company.snackbox.model.Barcode
import com.company.snackbox.model.BarcodeType
import com.company.snackbox.model.Session
import com.company.snackbox.model.User
import com.company.snackbox.repository.BarcodeRepository
import com.company.snackbox.repository.SessionRepository
import com.company.snackbox.repository.UserRepository
import mu.KotlinLogging
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

private val logger = KotlinLogging.logger {}

/**
 * Service for session operations
 */
class SessionService(
    private val sessionRepository: SessionRepository,
    private val barcodeRepository: BarcodeRepository,
    private val userRepository: UserRepository,
    private val config: AppConfig
) {
    private val sessionTimeoutTasks = ConcurrentHashMap<UUID, ScheduledFuture<*>>()
    private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)
    
    /**
     * Processes a barcode scan
     *
     * @param barcode Barcode that was scanned
     * @return Session if the scan was successful, null otherwise
     */
    fun processBarcodeScan(barcode: Barcode): Session? {
        logger.info { "Processing barcode scan: ${barcode.code}" }
        
        // Get user
        val user = userRepository.findById(barcode.userId) ?: run {
            logger.warn { "User not found for barcode: ${barcode.code}" }
            return null
        }
        
        // Check if there's an active session for this user
        val activeSession = sessionRepository.findActiveSessionForUser(user.id)
        
        return if (activeSession != null) {
            // Add scan to existing session
            addScanToSession(activeSession, barcode)
        } else {
            // Create new session
            createSessionWithScan(user, barcode)
        }
    }
    
    /**
     * Creates a new session with a barcode scan
     *
     * @param user User who scanned the barcode
     * @param barcode Barcode that was scanned
     * @return Created session
     */
    private fun createSessionWithScan(user: User, barcode: Barcode): Session {
        logger.info { "Creating new session for user: ${user.id}" }
        
        // End any active sessions for other users
        endAllActiveSessions()
        
        // Create session
        val session = Session(
            userId = user.id,
            startTime = LocalDateTime.now(),
            endTime = null,
            totalAmount = if (barcode.type == BarcodeType.PAYMENT && barcode.amount != null) barcode.amount else java.math.BigDecimal.ZERO
        )
        
        // Save session
        val createdSession = sessionRepository.create(session)
        
        // Add scan
        val updatedSession = sessionRepository.addScan(createdSession.id, barcode) ?: createdSession
        
        // Schedule session timeout
        scheduleSessionTimeout(updatedSession.id)
        
        return updatedSession
    }
    
    /**
     * Adds a barcode scan to an existing session
     *
     * @param session Session to add the scan to
     * @param barcode Barcode that was scanned
     * @return Updated session
     */
    private fun addScanToSession(session: Session, barcode: Barcode): Session? {
        logger.info { "Adding scan to session: ${session.id}" }
        
        // Cancel existing timeout
        cancelSessionTimeout(session.id)
        
        // Add scan
        val updatedSession = sessionRepository.addScan(session.id, barcode)
        
        // Schedule new timeout
        if (updatedSession != null) {
            scheduleSessionTimeout(updatedSession.id)
        }
        
        return updatedSession
    }
    
    /**
     * Schedules a timeout for a session
     *
     * @param sessionId ID of the session
     */
    private fun scheduleSessionTimeout(sessionId: UUID) {
        logger.debug { "Scheduling timeout for session: $sessionId" }
        
        val timeoutMinutes = config.application.sessionTimeoutMinutes.toLong()
        
        val task = scheduler.schedule({
            logger.info { "Session timeout reached: $sessionId" }
            endSession(sessionId)
        }, timeoutMinutes, TimeUnit.MINUTES)
        
        sessionTimeoutTasks[sessionId] = task
    }
    
    /**
     * Cancels a scheduled timeout for a session
     *
     * @param sessionId ID of the session
     */
    private fun cancelSessionTimeout(sessionId: UUID) {
        logger.debug { "Cancelling timeout for session: $sessionId" }
        
        sessionTimeoutTasks[sessionId]?.cancel(false)
        sessionTimeoutTasks.remove(sessionId)
    }
    
    /**
     * Ends a session
     *
     * @param sessionId ID of the session to end
     * @return true if the session was ended, false otherwise
     */
    fun endSession(sessionId: UUID): Boolean {
        logger.info { "Ending session: $sessionId" }
        
        // Cancel timeout
        cancelSessionTimeout(sessionId)
        
        // End session
        return sessionRepository.endSession(sessionId)
    }
    
    /**
     * Ends all active sessions
     */
    private fun endAllActiveSessions() {
        logger.info { "Ending all active sessions" }
        
        // Get all session IDs with active timeouts
        val sessionIds = sessionTimeoutTasks.keys.toList()
        
        // End each session
        sessionIds.forEach { sessionId ->
            endSession(sessionId)
        }
    }
    
    /**
     * Gets the active session for a user
     *
     * @param userId ID of the user
     * @return Active session if found, null otherwise
     */
    fun getActiveSessionForUser(userId: UUID): Session? {
        logger.debug { "Getting active session for user: $userId" }
        
        return sessionRepository.findActiveSessionForUser(userId)
    }
    
    /**
     * Gets the remaining time for a session in seconds
     *
     * @param sessionId ID of the session
     * @return Remaining time in seconds, or null if the session is not active
     */
    fun getSessionRemainingTime(sessionId: UUID): Long? {
        logger.debug { "Getting remaining time for session: $sessionId" }
        
        val task = sessionTimeoutTasks[sessionId] ?: return null
        
        val timeoutMinutes = config.application.sessionTimeoutMinutes.toLong()
        val timeoutMillis = TimeUnit.MINUTES.toMillis(timeoutMinutes)
        
        // This is an approximation since we don't have direct access to the task's scheduled time
        val remainingMillis = timeoutMillis - (System.currentTimeMillis() % timeoutMillis)
        
        return TimeUnit.MILLISECONDS.toSeconds(remainingMillis)
    }
    
    /**
     * Gets the recent sessions for a user
     *
     * @param userId ID of the user
     * @param limit Maximum number of sessions to return
     * @return List of recent sessions for the user
     */
    fun getRecentSessionsForUser(userId: UUID, limit: Int = 10): List<Session> {
        logger.debug { "Getting recent sessions for user: $userId, limit: $limit" }
        
        return sessionRepository.findByUserId(userId, limit)
    }
    
    /**
     * Cleans up resources when the service is no longer needed
     */
    fun shutdown() {
        logger.info { "Shutting down SessionService" }
        
        // End all active sessions
        endAllActiveSessions()
        
        // Shutdown scheduler
        scheduler.shutdown()
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow()
            }
        } catch (e: InterruptedException) {
            scheduler.shutdownNow()
            Thread.currentThread().interrupt()
        }
    }
}
