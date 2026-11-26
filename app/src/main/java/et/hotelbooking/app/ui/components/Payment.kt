package et.hotelbooking.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import et.hotelbooking.app.R
import et.hotelbooking.app.data.repo.DemoPayment
import et.hotelbooking.app.domain.PaymentMethod
import et.hotelbooking.app.domain.UserMode
import et.hotelbooking.app.util.CardUtils
import java.time.LocalDate

/** Card brand marks drawn in code (no third-party logo assets bundled). */
@Composable
fun CardBrandMark(brand: CardUtils.Brand, modifier: Modifier = Modifier) {
    when (brand) {
        CardUtils.Brand.VISA -> Surface(modifier, color = Color(0xFF1A1F71), shape = RoundedCornerShape(4.dp)) {
            Text("VISA", color = Color.White, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
        }
        CardUtils.Brand.MASTERCARD -> Canvas(modifier.size(34.dp, 22.dp)) {
            val r = size.height / 2.2f
            drawCircle(Color(0xFFEB001B), r, Offset(size.width / 2 - r * 0.55f, size.height / 2))
            drawCircle(Color(0xFFF79E1B), r, Offset(size.width / 2 + r * 0.55f, size.height / 2), alpha = 0.9f)
        }
        else -> Icon(Icons.Outlined.CreditCard, null, modifier)
    }
}

@Composable
fun MethodBadge(m: PaymentMethod, modifier: Modifier = Modifier) {
    when (m) {
        PaymentMethod.VISA -> CardBrandMark(CardUtils.Brand.VISA, modifier)
        PaymentMethod.MASTERCARD -> CardBrandMark(CardUtils.Brand.MASTERCARD, modifier)
        PaymentMethod.TELEBIRR -> Surface(modifier, color = Color(0xFF0B7CC1), shape = RoundedCornerShape(4.dp)) { Text("tele", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)) }
        PaymentMethod.CBE_BIRR -> Surface(modifier, color = Color(0xFF6B2C91), shape = RoundedCornerShape(4.dp)) { Text("CBE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)) }
        PaymentMethod.BANK_TRANSFER -> Icon(Icons.Outlined.AccountBalance, null, modifier)
        PaymentMethod.CASH -> Icon(Icons.Outlined.Payments, null, modifier)
        PaymentMethod.PAY_AT_HOTEL -> Icon(Icons.Outlined.Hotel, null, modifier)
    }
}

fun methodsFor(mode: UserMode, includePayAtHotel: Boolean = true, includeCash: Boolean = false): List<PaymentMethod> {
    val local = listOf(PaymentMethod.TELEBIRR, PaymentMethod.CBE_BIRR, PaymentMethod.BANK_TRANSFER)
    val cards = listOf(PaymentMethod.VISA, PaymentMethod.MASTERCARD)
    return (if (mode == UserMode.LOCAL) local + cards else cards + local) +
        (if (includeCash) listOf(PaymentMethod.CASH) else emptyList()) + (if (includePayAtHotel) listOf(PaymentMethod.PAY_AT_HOTEL) else emptyList())
}

class PaymentFormState(initial: PaymentMethod) {
    var method by mutableStateOf(initial)
    var cardNumber by mutableStateOf("")
    var expiry by mutableStateOf("")
    var cvv by mutableStateOf("")
    var holder by mutableStateOf("")
    var phone by mutableStateOf("+251 ")
    val brand get() = CardUtils.detectBrand(cardNumber)
    /** Error string resource or null when the (demo) payment details are valid. */
    fun validate(): Int? {
        val now = LocalDate.now()
        return when {
            method.isCard && !CardUtils.luhnValid(cardNumber) -> R.string.err_card_number
            method.isCard && ((method == PaymentMethod.VISA && brand != CardUtils.Brand.VISA) || (method == PaymentMethod.MASTERCARD && brand != CardUtils.Brand.MASTERCARD)) -> R.string.err_card_brand
            method.isCard && !CardUtils.expiryValid(expiry, now.year, now.monthValue) -> R.string.err_card_expiry
            method.isCard && cvv.length !in 3..4 -> R.string.err_card_cvv
            method.isCard && holder.isBlank() -> R.string.err_card_holder
            (method == PaymentMethod.TELEBIRR || method == PaymentMethod.CBE_BIRR) && !Regex("^(\\+251|0)?[79]\\d{8}$").matches(phone.replace(" ", "")) -> R.string.err_phone
            else -> null
        }
    }
    /** Only brand + last 4 digits leave this form; full number and CVV are discarded. */
    fun toDemoPayment(amount: Double): DemoPayment? = when {
        method == PaymentMethod.PAY_AT_HOTEL -> null
        method.isCard -> DemoPayment(method, amount, brand.name, CardUtils.last4(cardNumber))
        else -> DemoPayment(method, amount, payerPhone = phone.replace(" ", ""))
    }
    fun clearSensitive() { cardNumber = ""; cvv = "" }
}

@Composable
fun PaymentMethodPicker(state: PaymentFormState, methods: List<PaymentMethod>, reference: String? = null) {
    DemoBanner(stringResource(R.string.payment_demo_banner))
    Spacer(Modifier.height(8.dp))
    methods.forEach { m ->
        val sel = state.method == m
        OutlinedCard(onClick = { state.method = m }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            border = BorderStroke(if (sel) 2.dp else 1.dp, if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                RadioButton(sel, { state.method = m })
                Box(Modifier.width(44.dp), contentAlignment = Alignment.Center) { MethodBadge(m) }
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(stringResource(Labels.method(m)), style = MaterialTheme.typography.titleSmall)
                    Text(stringResource(methodHint(m)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        if (sel) Column(Modifier.padding(start = 8.dp, end = 8.dp, bottom = 8.dp)) { MethodForm(state, reference) }
    }
}

private fun methodHint(m: PaymentMethod) = when (m) {
    PaymentMethod.TELEBIRR -> R.string.pm_telebirr_hint; PaymentMethod.CBE_BIRR -> R.string.pm_cbe_hint; PaymentMethod.BANK_TRANSFER -> R.string.pm_bank_hint
    PaymentMethod.VISA, PaymentMethod.MASTERCARD -> R.string.pm_card_hint; PaymentMethod.CASH -> R.string.pm_cash_hint; PaymentMethod.PAY_AT_HOTEL -> R.string.pm_pay_at_hotel_hint
}

@Composable
private fun MethodForm(s: PaymentFormState, reference: String?) {
    when {
        s.method.isCard -> {
            OutlinedTextField(CardUtils.formatGroups(s.cardNumber), { s.cardNumber = CardUtils.digits(it).take(19) }, Modifier.fillMaxWidth(), singleLine = true,
                label = { Text(stringResource(R.string.card_number)) }, trailingIcon = { CardBrandMark(s.brand, Modifier.padding(end = 8.dp)) },
                supportingText = { Text(stringResource(R.string.card_test_hint)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(s.expiry, { v -> val d = CardUtils.digits(v).take(4); s.expiry = if (d.length > 2) d.take(2) + "/" + d.drop(2) else d }, Modifier.weight(1f), singleLine = true,
                    label = { Text(stringResource(R.string.card_expiry)) }, placeholder = { Text("MM/YY") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(s.cvv, { s.cvv = CardUtils.digits(it).take(4) }, Modifier.weight(1f), singleLine = true, label = { Text("CVV") },
                    visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword))
            }
            OutlinedTextField(s.holder, { s.holder = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text(stringResource(R.string.card_holder)) })
            Text(stringResource(R.string.card_never_stored), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        s.method == PaymentMethod.TELEBIRR || s.method == PaymentMethod.CBE_BIRR ->
            OutlinedTextField(s.phone, { s.phone = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text(stringResource(R.string.wallet_phone)) },
                supportingText = { Text(stringResource(R.string.wallet_push_hint)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
        s.method == PaymentMethod.BANK_TRANSFER -> Surface(color = MaterialTheme.colorScheme.surfaceContainer, shape = MaterialTheme.shapes.medium) {
            Text(stringResource(R.string.bank_instructions, reference ?: stringResource(R.string.your_booking_reference)), Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
        }
        else -> {}
    }
}

/** Simulated wallet / card authorization dialog (demo only). */
@Composable
fun DemoProcessingDialog(method: PaymentMethod, onDone: () -> Unit) {
    LaunchedEffect(Unit) { kotlinx.coroutines.delay(1600); onDone() }
    AlertDialog(onDismissRequest = {}, confirmButton = {}, icon = { MethodBadge(method) },
        title = { Text(stringResource(R.string.processing_payment)) },
        text = { Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            CircularProgressIndicator(); Spacer(Modifier.height(12.dp))
            Text(stringResource(if (method.isCard) R.string.processing_card else R.string.processing_wallet), style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(R.string.demo_no_charge), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
        } })
}
