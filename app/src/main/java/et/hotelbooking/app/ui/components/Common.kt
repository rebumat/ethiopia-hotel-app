package et.hotelbooking.app.ui.components

import android.annotation.SuppressLint
import androidx.annotation.StringRes
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import et.hotelbooking.app.R
import et.hotelbooking.app.data.local.HotelEntity
import et.hotelbooking.app.data.seed.CityInfo
import et.hotelbooking.app.domain.BookingStatus
import et.hotelbooking.app.ui.theme.Brand
import et.hotelbooking.app.ui.theme.LocalExtraColors
import et.hotelbooking.app.util.AppSettings
import et.hotelbooking.app.util.Money

val LocalSettings = staticCompositionLocalOf { AppSettings() }

@SuppressLint("DiscouragedApi")
@Composable
fun drawableId(name: String): Int {
    val ctx = LocalContext.current
    return remember(name) { ctx.resources.getIdentifier(name, "drawable", ctx.packageName).takeIf { it != 0 } ?: R.drawable.hotel_building }
}

/** Decorative strip inspired by Ethiopian "tibeb" woven borders. */
@Composable
fun TibebStrip(modifier: Modifier = Modifier, height: Dp = 8.dp) {
    val green = MaterialTheme.colorScheme.primary; val gold = LocalExtraColors.current.gold; val red = MaterialTheme.colorScheme.tertiary
    Canvas(modifier.fillMaxWidth().height(height)) {
        val h = size.height; val step = h * 2
        var x = 0f; var i = 0
        drawRect(gold.copy(alpha = 0.25f), size = Size(size.width, h))
        while (x < size.width) {
            val c = when (i % 3) { 0 -> green; 1 -> gold; else -> red }
            val p = Path().apply { moveTo(x, h / 2); lineTo(x + step / 2, 0f); lineTo(x + step, h / 2); lineTo(x + step / 2, h); close() }
            drawPath(p, c)
            x += step; i++
        }
    }
}

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier, subtitle: String? = null, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (action != null && onAction != null) TextButton(onClick = onAction) { Text(action) }
    }
}

@Composable
fun Stars(count: Int, size: Dp = 14.dp) {
    Row { repeat(count) { Icon(Icons.Filled.Star, null, tint = LocalExtraColors.current.gold, modifier = Modifier.size(size)) } }
}

@Composable
fun RatingBadge(rating: Double, count: Int? = null) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(8.dp)) {
            Text(String.format(java.util.Locale.US, "%.1f", rating), color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
        }
        if (count != null) { Spacer(Modifier.width(6.dp)); Text(pluralStringResource(R.plurals.reviews_count, count, count), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable
fun StatusChip(status: BookingStatus) {
    val ex = LocalExtraColors.current
    val c = when (status) {
        BookingStatus.PENDING -> ex.warning; BookingStatus.CONFIRMED -> ex.info; BookingStatus.CHECKED_IN -> ex.success
        BookingStatus.CHECKED_OUT -> MaterialTheme.colorScheme.onSurfaceVariant; else -> MaterialTheme.colorScheme.error
    }
    Pill(stringResource(Labels.status(status)), c)
}

@Composable
fun Pill(text: String, color: Color, modifier: Modifier = Modifier) {
    Surface(modifier, color = color.copy(alpha = 0.14f), contentColor = color, shape = RoundedCornerShape(50)) {
        Text(text, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp), maxLines = 1)
    }
}

@Composable
fun Price(etb: Double, modifier: Modifier = Modifier, perNight: Boolean = false, big: Boolean = false) {
    val s = LocalSettings.current
    Column(modifier, horizontalAlignment = Alignment.End) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(Money.primary(etb, s.currency), style = if (big) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            if (perNight) Text(" " + stringResource(R.string.per_night), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(Money.secondary(etb, s.currency), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun HotelImage(name: String, modifier: Modifier = Modifier, contentDescription: String? = null) {
    Image(painterResource(drawableId(name)), contentDescription, modifier, contentScale = ContentScale.Crop)
}

@Composable
fun HotelCard(hotel: HotelEntity, fromPrice: Double?, onClick: () -> Unit, modifier: Modifier = Modifier, roomsLeft: Int? = null,
              compareSelected: Boolean = false, onToggleCompare: (() -> Unit)? = null, width: Dp? = null,
              saved: Boolean = false, onToggleSaved: (() -> Unit)? = null) {
    ElevatedCard(onClick = onClick, modifier = modifier.then(if (width != null) Modifier.width(width) else Modifier.fillMaxWidth()),
        shape = MaterialTheme.shapes.large, colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Box {
            HotelImage(hotel.imageList.firstOrNull() ?: "hotel_building", Modifier.fillMaxWidth().height(if (width != null) 140.dp else 180.dp), hotel.name)
            Box(Modifier.matchParentSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.45f)), startY = 200f)))
            Row(Modifier.align(Alignment.TopStart).padding(10.dp)) {
                if (hotel.featured) Pill(stringResource(R.string.featured), Brand.Gold, Modifier.background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(50)))
            }
            if (onToggleCompare != null || onToggleSaved != null) {
                Row(Modifier.align(Alignment.TopEnd).padding(6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (onToggleSaved != null) {
                        FilledTonalIconToggleButton(checked = saved, onCheckedChange = { onToggleSaved() }) {
                            Icon(if (saved) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                stringResource(if (saved) R.string.saved_hotel else R.string.save_hotel))
                        }
                    }
                    if (onToggleCompare != null) {
                        FilledTonalIconToggleButton(checked = compareSelected, onCheckedChange = { onToggleCompare() }) {
                            Icon(if (compareSelected) Icons.Filled.CompareArrows else Icons.Outlined.CompareArrows, stringResource(R.string.compare))
                        }
                    }
                }
            }
            Row(Modifier.align(Alignment.BottomStart).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Stars(hotel.stars)
            }
        }
        Column(Modifier.padding(14.dp)) {
            Text(hotel.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Place, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(stringResource(CityInfo.nameRes(hotel.city)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    if (hotel.reviewCount > 0) RatingBadge(hotel.ratingAvg, hotel.reviewCount)
                    if (roomsLeft != null && roomsLeft in 1..3) Text(pluralStringResource(R.plurals.rooms_left, roomsLeft, roomsLeft), color = MaterialTheme.colorScheme.tertiary, style = MaterialTheme.typography.labelSmall)
                }
                if (fromPrice != null) Column(horizontalAlignment = Alignment.End) {
                    Text(stringResource(R.string.from), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Price(fromPrice, perNight = true)
                }
            }
        }
    }
}

@Composable
fun EmptyState(icon: ImageVector, title: String, message: String, modifier: Modifier = Modifier, action: String? = null, onAction: (() -> Unit)? = null) {
    Column(modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(88.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
            Icon(icon, null, Modifier.size(44.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        if (action != null && onAction != null) { Spacer(Modifier.height(16.dp)); Button(onClick = onAction) { Text(action) } }
    }
}

@Composable
fun LoadingBox(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
}

@Composable
fun ErrorBox(message: String, onRetry: (() -> Unit)? = null) {
    EmptyState(Icons.Outlined.ErrorOutline, stringResource(R.string.error_title), message, action = onRetry?.let { stringResource(R.string.retry) }, onAction = onRetry)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(title: String, onBack: (() -> Unit)? = null, actions: @Composable RowScope.() -> Unit = {}) {
    TopAppBar(title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        navigationIcon = { if (onBack != null) IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) } },
        actions = actions, colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background))
}

@Composable
fun DemoBanner(text: String, modifier: Modifier = Modifier) {
    Surface(modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.secondaryContainer, shape = MaterialTheme.shapes.medium) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Science, null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
            Spacer(Modifier.width(10.dp))
            Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
    }
}

@Composable
fun KeyValue(label: String, value: String, modifier: Modifier = Modifier, bold: Boolean = false) {
    Row(modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = if (bold) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium, fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium, textAlign = TextAlign.End)
    }
}

@Composable
fun StatCard(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier, tint: Color = MaterialTheme.colorScheme.primary, onClick: (() -> Unit)? = null) {
    Card(modifier = modifier.then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier), shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Column(Modifier.padding(14.dp)) {
            Box(Modifier.size(36.dp).clip(CircleShape).background(tint.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp)) }
            Spacer(Modifier.height(10.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
        }
    }
}

@Composable
fun BarChart(data: List<Pair<String, Double>>, modifier: Modifier = Modifier, valueLabel: (Double) -> String = { it.toInt().toString() }, barColor: Color = MaterialTheme.colorScheme.primary) {
    val max = (data.maxOfOrNull { it.second } ?: 0.0).coerceAtLeast(1.0)
    Row(modifier.fillMaxWidth().height(170.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
        data.forEach { (label, v) ->
            Column(Modifier.weight(1f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                Text(valueLabel(v), style = MaterialTheme.typography.labelSmall, maxLines = 1, fontSize = 9.sp)
                Spacer(Modifier.height(2.dp))
                Box(Modifier.fillMaxWidth().fillMaxHeight((v / max).toFloat().coerceIn(0.02f, 0.82f)).clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                    .background(Brush.verticalGradient(listOf(barColor, barColor.copy(alpha = 0.6f)))))
                Spacer(Modifier.height(4.dp))
                Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1)
            }
        }
    }
}

@Composable
fun LineSpark(values: List<Double>, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.primary) {
    Canvas(modifier.fillMaxWidth().height(90.dp)) {
        if (values.size < 2) return@Canvas
        val max = values.max().coerceAtLeast(0.01)
        val stepX = size.width / (values.size - 1)
        val path = Path(); val fill = Path()
        values.forEachIndexed { i, v ->
            val x = i * stepX; val y = size.height - (v / max * size.height * 0.9).toFloat()
            if (i == 0) { path.moveTo(x, y); fill.moveTo(x, size.height); fill.lineTo(x, y) } else { path.lineTo(x, y); fill.lineTo(x, y) }
        }
        fill.lineTo(size.width, size.height); fill.close()
        drawPath(fill, Brush.verticalGradient(listOf(color.copy(alpha = 0.35f), Color.Transparent)))
        drawPath(path, color, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f))
        drawLine(color.copy(alpha = 0.2f), Offset(0f, size.height), Offset(size.width, size.height))
    }
}

@Composable
fun SectionCard(modifier: Modifier = Modifier, title: String? = null, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))) {
        Column(Modifier.padding(16.dp)) {
            if (title != null) { Text(title, style = MaterialTheme.typography.titleMedium); Spacer(Modifier.height(8.dp)) }
            content()
        }
    }
}

@Composable
fun str(@StringRes id: Int): String = stringResource(id)
