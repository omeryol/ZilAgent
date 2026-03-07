package com.zilagent.app.util

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream
import java.util.zip.GZIPInputStream
import android.util.Base64
import com.google.gson.Gson

object QrUtils {
    private val gson = Gson()
    private const val MAX_QR_BASE64_LENGTH = 8192
    private const val MAX_QR_BINARY_BYTES = 64 * 1024
    private const val MAX_QR_JSON_CHARS = 200 * 1024

    fun generateQrBitmap(content: String, width: Int = 512, height: Int = 512): Bitmap? {
        return try {
            val bitMatrix: BitMatrix = MultiFormatWriter().encode(
                content,
                BarcodeFormat.QR_CODE,
                width,
                height
            )
            val matrixWidth = bitMatrix.width
            val matrixHeight = bitMatrix.height
            val pixels = IntArray(matrixWidth * matrixHeight)
            for (y in 0 until matrixHeight) {
                val offset = y * matrixWidth
                for (x in 0 until matrixWidth) {
                    pixels[offset + x] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
                }
            }
            Bitmap.createBitmap(matrixWidth, matrixHeight, Bitmap.Config.ARGB_8888).apply {
                setPixels(pixels, 0, matrixWidth, 0, 0, matrixWidth, matrixHeight)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun compressAndSerialize(data: Any): String {
        val json = gson.toJson(data)
        if (json.length > MAX_QR_JSON_CHARS) {
            throw IllegalArgumentException("QR payload too large")
        }
        val byteArrayOutputStream = ByteArrayOutputStream()
        GZIPOutputStream(byteArrayOutputStream).use { it.write(json.toByteArray()) }
        val compressed = byteArrayOutputStream.toByteArray()
        if (compressed.size > MAX_QR_BINARY_BYTES) {
            throw IllegalArgumentException("QR compressed payload too large")
        }
        val encoded = Base64.encodeToString(compressed, Base64.NO_WRAP)
        if (encoded.length > MAX_QR_BASE64_LENGTH) {
            throw IllegalArgumentException("QR encoded payload too large")
        }
        return encoded
    }

    fun <T> decompressAndDeserialize(compressedString: String, clazz: Class<T>): T? {
        return try {
            if (compressedString.length > MAX_QR_BASE64_LENGTH) return null
            val decodedBytes = Base64.decode(compressedString, Base64.NO_WRAP)
            if (decodedBytes.size > MAX_QR_BINARY_BYTES) return null
            val byteArrayInputStream = java.io.ByteArrayInputStream(decodedBytes)
            val json = GZIPInputStream(byteArrayInputStream).bufferedReader().use { reader ->
                val sb = StringBuilder()
                val buffer = CharArray(2048)
                while (true) {
                    val read = reader.read(buffer)
                    if (read <= 0) break
                    sb.append(buffer, 0, read)
                    if (sb.length > MAX_QR_JSON_CHARS) return null
                }
                sb.toString()
            }
            gson.fromJson(json, clazz)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
