package com.expenser.app.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Accent color per entry type: expense=rose, income=green, investment=blue. */
@Composable
fun entryTypeColor(type: com.expenser.app.data.model.EntryType): Color = when (type) {
    com.expenser.app.data.model.EntryType.Expense -> MaterialTheme.colorScheme.error
    com.expenser.app.data.model.EntryType.Income -> Color(0xFF16A34A)
    com.expenser.app.data.model.EntryType.Investment -> Color(0xFF2563EB)
}

/** Small rounded label, like the shadcn badge, used for a category's type. */
@Composable
fun TypeBadge(text: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium,
        )
    }
}
