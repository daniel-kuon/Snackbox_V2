package com.company.snackbox.ui.util

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import java.io.ByteArrayOutputStream
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.*
import javax.imageio.ImageIO

/**
 * Formats a currency value
 *
 * @param value Value to format
 * @param locale Locale to use for formatting
 * @return Formatted currency string
 */
fun formatCurrency(value: BigDecimal, locale: Locale = Locale.GERMANY): String {
    val formatter = NumberFormat.getCurrencyInstance(locale)
    return formatter.format(value)
}

/**
 * Formats a time in seconds
 *
 * @param seconds Time in seconds
 * @return Formatted time string (mm:ss)
 */
fun formatTime(seconds: Long): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}

/**
 * Generates a QR code for the given text
 *
 * @param text Text to encode in the QR code
 * @param width Width of the QR code
 * @param height Height of the QR code
 * @return QR code as an ImageBitmap, or null if generation failed
 */
fun generateQRCode(text: String, width: Int = 300, height: Int = 300): ImageBitmap? {
    return try {
        val qrCodeWriter = QRCodeWriter()
        val bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height)
        val bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix)
        
        val outputStream = ByteArrayOutputStream()
        ImageIO.write(bufferedImage, "png", outputStream)
        
        org.jetbrains.skia.Image.makeFromEncoded(outputStream.toByteArray()).toComposeImageBitmap()
    } catch (e: Exception) {
        null
    }
}
