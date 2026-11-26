package et.hotelbooking.app.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import et.hotelbooking.app.R
import et.hotelbooking.app.data.repo.Invoice
import et.hotelbooking.app.domain.PaymentType
import java.io.File

/** Generates an A4 invoice/receipt PDF in the app cache and opens the Android share sheet. */
object InvoicePdf {
    fun create(ctx: Context, inv: Invoice): File {
        val doc = PdfDocument()
        val page = doc.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
        val c = page.canvas
        val green = Color.rgb(11, 107, 69); val gold = Color.rgb(224, 165, 38)
        val title = Paint().apply { color = green; textSize = 22f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); isAntiAlias = true }
        val h2 = Paint().apply { color = Color.BLACK; textSize = 12f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); isAntiAlias = true }
        val body = Paint().apply { color = Color.DKGRAY; textSize = 10.5f; isAntiAlias = true }
        val right = Paint(body).apply { textAlign = Paint.Align.RIGHT }
        val rightBold = Paint(h2).apply { textAlign = Paint.Align.RIGHT }
        val b = inv.booking
        c.drawRect(0f, 0f, 595f, 8f, Paint().apply { color = green })
        c.drawRect(0f, 8f, 595f, 12f, Paint().apply { color = gold })
        var y = 50f
        c.drawText(inv.hotel.name, 40f, y, title); y += 16
        c.drawText(inv.hotel.address, 40f, y, body); y += 14
        c.drawText(inv.hotel.phone + "  ·  " + inv.hotel.email, 40f, y, body)
        c.drawText(ctx.getString(R.string.invoice_title), 555f, 50f, Paint(title).apply { textAlign = Paint.Align.RIGHT; textSize = 18f })
        c.drawText(ctx.getString(R.string.booking_reference) + ": " + b.reference, 555f, 68f, right)
        c.drawText(Dates.dateTime(System.currentTimeMillis()), 555f, 82f, right)
        y += 30
        c.drawText(ctx.getString(R.string.guest) + ": " + b.guestName, 40f, y, h2); y += 14
        c.drawText(b.guestPhone + "  " + b.guestEmail, 40f, y, body); y += 14
        c.drawText(ctx.getString(R.string.stay) + ": " + Dates.short(b.checkIn) + " → " + Dates.short(b.checkOut) + " (" + b.nights + ")", 40f, y, body); y += 14
        c.drawText(ctx.getString(R.string.room) + ": " + (inv.roomType?.name ?: "") + (inv.roomNumber?.let { " · #$it" } ?: ""), 40f, y, body); y += 26
        fun line(label: String, amount: Double, bold: Boolean = false) {
            c.drawText(label, 40f, y, if (bold) h2 else body); c.drawText(Money.exact(amount), 555f, y, if (bold) rightBold else right); y += 16
        }
        c.drawLine(40f, y - 10, 555f, y - 10, body)
        line(ctx.getString(R.string.room_charges) + " (" + b.nights + ")", b.roomTotalEtb)
        line(ctx.getString(R.string.service_charge), b.serviceChargeEtb)
        line(ctx.getString(R.string.vat), b.vatEtb)
        inv.charges.forEach { line(it.description.ifBlank { it.category.name } + " (" + Dates.dateTime(it.createdAt) + ")", it.amountEtb) }
        c.drawLine(40f, y - 8, 555f, y - 8, body); y += 4
        line(ctx.getString(R.string.grand_total), inv.grandTotal, true)
        line("≈ USD (" + ctx.getString(R.string.sample_rate) + ")", inv.grandTotal / Rates.ETB_PER_USD)
        y += 8
        c.drawText(ctx.getString(R.string.payments), 40f, y, h2); y += 16
        inv.payments.forEach { p -> line((if (p.type == PaymentType.REFUND) "− " else "") + p.method.name + " · " + p.type.name + " · " + p.reference, if (p.type == PaymentType.REFUND) -p.amountEtb else p.amountEtb) }
        line(ctx.getString(R.string.balance_due), inv.balance.coerceAtLeast(0.0), true)
        y += 24
        c.drawText(ctx.getString(R.string.invoice_demo_footer), 40f, y, Paint(body).apply { color = Color.rgb(184, 58, 46) })
        doc.finishPage(page)
        val dir = File(ctx.cacheDir, "invoices").apply { mkdirs() }
        val f = File(dir, "invoice_${b.reference}.pdf")
        f.outputStream().use { doc.writeTo(it) }
        doc.close()
        return f
    }

    fun share(ctx: Context, inv: Invoice) {
        val f = create(ctx, inv)
        val uri = FileProvider.getUriForFile(ctx, ctx.packageName + ".files", f)
        ctx.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("application/pdf").putExtra(Intent.EXTRA_STREAM, uri)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION), ctx.getString(R.string.share_invoice)))
    }
}
