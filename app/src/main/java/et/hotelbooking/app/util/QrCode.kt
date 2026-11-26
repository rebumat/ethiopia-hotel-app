package et.hotelbooking.app.util

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

object QrCode {
    const val PREFIX = "EHB-CHECKIN:"
    fun payloadFor(reference: String) = PREFIX + reference
    fun referenceFrom(payload: String): String = payload.removePrefix(PREFIX).trim()

    fun bitmap(content: String, size: Int = 512, dark: Int = Color.BLACK, light: Int = Color.WHITE): Bitmap {
        val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, mapOf(EncodeHintType.MARGIN to 1))
        val pixels = IntArray(size * size)
        for (y in 0 until size) for (x in 0 until size) pixels[y * size + x] = if (matrix[x, y]) dark else light
        return Bitmap.createBitmap(pixels, size, size, Bitmap.Config.ARGB_8888)
    }
}
