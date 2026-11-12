package com.expenser.app.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expenser.app.data.model.EntryType

/*
 * Building blocks of the "bento" layout: borderless rounded cards on a grey ground,
 * letter badges per category, pill chips and a segmented control. Colours that the
 * Material scheme has no slot for (category tints, the per-type tiles) live here, each
 * with a light and a dark pair so screens never branch on the theme themselves.
 */

/** True when the current scheme is the dark one. */
@Composable
@ReadOnlyComposable
fun isDarkScheme(): Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f

/** Background + content colour of a tinted element. */
@Immutable
data class Tint(val container: Color, val content: Color)

// Category badge tints: violet, green, orange, blue, yellow, pink, teal, indigo.
private val LightTints = listOf(
    Tint(Color(0xFFEDE9FE), Color(0xFF5B21B6)),
    Tint(Color(0xFFDCFCE7), Color(0xFF166534)),
    Tint(Color(0xFFFFEDD5), Color(0xFF9A3412)),
    Tint(Color(0xFFDBEAFE), Color(0xFF1E40AF)),
    Tint(Color(0xFFFEF9C3), Color(0xFF854D0E)),
    Tint(Color(0xFFFCE7F3), Color(0xFF9D174D)),
    Tint(Color(0xFFCCFBF1), Color(0xFF115E59)),
    Tint(Color(0xFFE0E7FF), Color(0xFF3730A3)),
)
private val DarkTints = listOf(
    Tint(Color(0xFF2E1065), Color(0xFFC4B5FD)),
    Tint(Color(0xFF052E16), Color(0xFF86EFAC)),
    Tint(Color(0xFF431407), Color(0xFFFDBA74)),
    Tint(Color(0xFF172554), Color(0xFF93C5FD)),
    Tint(Color(0xFF422006), Color(0xFFFDE047)),
    Tint(Color(0xFF500724), Color(0xFFF9A8D4)),
    Tint(Color(0xFF042F2E), Color(0xFF5EEAD4)),
    Tint(Color(0xFF1E1B4B), Color(0xFFA5B4FC)),
)

/**
 * A stable tint for a category, derived from its id so it never changes when the
 * category is renamed and needs no stored colour column.
 */
@Composable
@ReadOnlyComposable
fun categoryTint(key: String): Tint {
    val palette = if (isDarkScheme()) DarkTints else LightTints
    return palette[Math.floorMod(key.hashCode(), palette.size)]
}

/** Tile colours for the Income / Spent / Invested summary tiles. */
@Composable
@ReadOnlyComposable
fun entryTypeTint(type: EntryType): Tint = if (isDarkScheme()) {
    when (type) {
        EntryType.Income -> Tint(Color(0xFF052E16), Color(0xFFBBF7D0))
        EntryType.Expense -> Tint(Color(0xFF4C0519), Color(0xFFFECDD3))
        EntryType.Investment -> Tint(Color(0xFF172554), Color(0xFFBFDBFE))
    }
} else {
    when (type) {
        EntryType.Income -> Tint(Color(0xFFDCFCE7), Color(0xFF14532D))
        EntryType.Expense -> Tint(Color(0xFFFFE4E6), Color(0xFF881337))
        EntryType.Investment -> Tint(Color(0xFFDBEAFE), Color(0xFF1E3A8A))
    }
}

/**
 * Text colour for an amount where types are mixed (the dashboard's recent list).
 * Expenses stay neutral; income and investments get a green and a blue picked to pass
 * AA as 14sp text on the card colour of either theme.
 */
@Composable
@ReadOnlyComposable
fun amountTextColor(type: EntryType): Color {
    val dark = isDarkScheme()
    return when (type) {
        EntryType.Expense -> MaterialTheme.colorScheme.onSurface
        EntryType.Income -> if (dark) Color(0xFF4ADE80) else Color(0xFF15803D)
        EntryType.Investment -> if (dark) Color(0xFF60A5FA) else Color(0xFF1D4ED8)
    }
}

/**
 * Rose for text links and text buttons. The filled rose (#E11D48) is 4.1:1 on the dark
 * ground, under AA for body-size text; the lighter #FB7185 is 7.3:1 there. Light mode
 * keeps the brand rose, which is 4.7:1 on white.
 */
@Composable
@ReadOnlyComposable
fun linkColor(): Color = if (isDarkScheme()) Color(0xFFFB7185) else MaterialTheme.colorScheme.primary

/** Rounded square with the first letter of [name], tinted per [key]. */
@Composable
fun CategoryBadge(
    name: String,
    key: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
) {
    val tint = categoryTint(key)
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.3f))
            .background(tint.container),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            name.trim().take(1).uppercase().ifEmpty { "?" },
            color = tint.content,
            fontWeight = FontWeight.SemiBold,
            fontSize = (size.value * 0.38f).sp,
        )
    }
}

/** Borderless card on the grey ground. */
@Composable
fun BentoCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    color: Color = MaterialTheme.colorScheme.surface,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(modifier = modifier.fillMaxWidth(), shape = shape, color = color) {
        Column(content = content)
    }
}

/** Small grey caption above a group of cards, e.g. "Preferences". */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier, trailing: String? = null) {
    Row(
        modifier = modifier.fillMaxWidth().heightIn(min = 36.dp).padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f, fill = false),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (trailing != null) {
            Text(
                trailing,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 12.dp),
            )
        }
    }
}

/** Hairline between rows inside one card. */
@Composable
fun RowDivider() {
    HorizontalDivider(color = MaterialTheme.colorScheme.background, thickness = 1.dp)
}

/**
 * Shape for one row of a card that is split across several LazyColumn items, so each
 * row can be its own item (swipe-to-delete, stable keys) and still read as one card.
 */
fun groupedRowShape(isFirst: Boolean, isLast: Boolean, radius: Dp = 16.dp): Shape =
    RoundedCornerShape(
        topStart = if (isFirst) radius else 0.dp,
        topEnd = if (isFirst) radius else 0.dp,
        bottomStart = if (isLast) radius else 0.dp,
        bottomEnd = if (isLast) radius else 0.dp,
    )

/** Rounded track of equal-width options with one selected, e.g. All / Expense / Income. */
@Composable
fun <T> SegmentedControl(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    leading: (@Composable (T) -> Unit)? = null,
) {
    // Dark: the thumb sits one step above the #27272A / #1C1917 tracks. Light: white.
    val selectedColor = if (isDarkScheme()) Color(0xFF3F3F46) else Color.White
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(trackColor)
            .padding(2.dp),
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) selectedColor else Color.Transparent)
                    .semantics { this.selected = isSelected }
                    .clickable(role = Role.Tab) { onSelect(option) },
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                leading?.invoke(option)
                Text(
                    label(option),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }
    }
}

/** Fully rounded filter chip: dark when selected, plain card colour otherwise. */
@Composable
fun PillChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    unselectedColor: Color = MaterialTheme.colorScheme.surface,
) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(CircleShape)
            .background(if (selected) colors.onSurface else unselectedColor)
            .semantics { this.selected = selected }
            .clickable(role = Role.Checkbox, onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (selected) colors.surface else colors.onSurface,
            maxLines = 1,
        )
    }
}
