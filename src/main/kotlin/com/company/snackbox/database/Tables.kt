package com.company.snackbox.database

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.ReferenceOption

/**
 * Database table for users
 */
object UsersTable : UUIDTable("users") {
    val name = varchar("name", 255)
    val email = varchar("email", 255).uniqueIndex()
    val isAdmin = bool("is_admin").default(false)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

/**
 * Database table for barcodes
 */
object BarcodesTable : UUIDTable("barcodes") {
    val code = varchar("code", 50).uniqueIndex()
    val userId = reference("user_id", UsersTable, onDelete = ReferenceOption.CASCADE)
    val type = enumeration("type", BarcodeTypeDB::class)
    val amount = decimal("amount", 10, 2).nullable()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

/**
 * Database table for sessions
 */
object SessionsTable : UUIDTable("sessions") {
    val userId = reference("user_id", UsersTable, onDelete = ReferenceOption.CASCADE)
    val startTime = datetime("start_time")
    val endTime = datetime("end_time").nullable()
    val totalAmount = decimal("total_amount", 10, 2).default(java.math.BigDecimal.ZERO)
}

/**
 * Database table for barcode scans within sessions
 */
object BarcodeScansTable : UUIDTable("barcode_scans") {
    val sessionId = reference("session_id", SessionsTable, onDelete = ReferenceOption.CASCADE)
    val barcodeId = reference("barcode_id", BarcodesTable, onDelete = ReferenceOption.CASCADE)
    val scanTime = datetime("scan_time")
    val amount = decimal("amount", 10, 2).nullable()
}

/**
 * Database table for payments
 */
object PaymentsTable : UUIDTable("payments") {
    val userId = reference("user_id", UsersTable, onDelete = ReferenceOption.CASCADE)
    val amount = decimal("amount", 10, 2)
    val method = enumeration("method", PaymentMethodDB::class)
    val reference = varchar("reference", 255).nullable()
    val notes = text("notes").nullable()
    val timestamp = datetime("timestamp")
    val recordedBy = reference("recorded_by", UsersTable, onDelete = ReferenceOption.SET_NULL).nullable()
}

/**
 * Database representation of barcode types
 */
enum class BarcodeTypeDB {
    PAYMENT, ADMIN
}

/**
 * Database representation of payment methods
 */
enum class PaymentMethodDB {
    PAYPAL, CASH, BANK_TRANSFER, OTHER
}
