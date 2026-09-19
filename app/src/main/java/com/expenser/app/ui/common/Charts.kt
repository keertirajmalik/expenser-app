package com.expenser.app.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.expenser.app.ui.theme.ChartDark
import com.expenser.app.ui.theme.ChartLight

@Composable
fun chartPalette(): List<Color> = if (isSystemInDarkTheme()) ChartDark else ChartLight

/** A light→dark ramp of [base], one shade per slice, so a chart varies within its type's hue. */
fun typeShades(base: Color, count: Int): List<Color> {
    if (count <= 1) return listOf(base)
    val light = androidx.compose.ui.graphics.lerp(base, Color.White, 0.45f)
    val dark = androidx.compose.ui.graphics.lerp(base, Color.Black, 0.35f)
    return List(count) { i -> androidx.compose.ui.graphics.lerp(light, dark, i / (count - 1f)) }
}

/** Donut chart with a legend; slices colored as shades of [baseColor] (the entry type's hue). */
@Composable
fun DonutChart(
    data: List<Pair<String, Long>>,
    baseColor: Color,
    modifier: Modifier = Modifier,
) = DonutChart(data, typeShades(baseColor, data.size), modifier)

/** Donut chart with a legend; one explicit color per slice. */
@Composable
fun DonutChart(
    data: List<Pair<String, Long>>,
    colors: List<Color>,
    modifier: Modifier = Modifier,
) {
    val total = data.sumOf { it.second }
    val palette = colors
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    if (total <= 0L) {
        Text(
            "No data yet",
            style = MaterialTheme.typography.bodyMedium,
            color = onSurfaceVariant,
            modifier = modifier.padding(vertical = 24.dp),
        )
        return
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Canvas(modifier = Modifier.size(120.dp)) {
            val stroke = Stroke(width = size.minDimension * 0.18f)
            val inset = stroke.width / 2
            var startAngle = -90f
            data.forEachIndexed { i, (_, value) ->
                val sweep = (value.toFloat() / total) * 360f
                drawArc(
                    color = palette[i % palette.size],
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                    size = androidx.compose.ui.geometry.Size(size.width - stroke.width, size.height - stroke.width),
                    style = stroke,
                )
                startAngle += sweep
            }
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            data.forEachIndexed { i, (label, value) ->
                val pct = (value.toFloat() / total * 100).toInt()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(10.dp)
                            .background(palette[i % palette.size], CircleShape),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        label,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        "${formatMoney(value)}  ${pct}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/** Vertical bar chart; each bar uses its own color and shows its value above the label. */
@Composable
fun BarChart(
    data: List<Pair<String, Long>>,
    colors: List<Color>,
    modifier: Modifier = Modifier,
) {
    val max = data.maxOfOrNull { it.second } ?: 0L
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    if (data.isEmpty() || max <= 0L) {
        Text(
            "No data yet",
            style = MaterialTheme.typography.bodyMedium,
            color = onSurfaceVariant,
            modifier = modifier.padding(vertical = 24.dp),
        )
        return
    }

    Row(
        modifier = modifier.fillMaxWidth().height(180.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        data.forEachIndexed { i, (label, value) ->
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    formatMoney(value),
                    style = MaterialTheme.typography.labelSmall,
                    color = onSurfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
                val fraction = (value.toFloat() / max).coerceIn(0.02f, 1f)
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(fraction)
                            .background(colors[i % colors.size], RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)),
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(label, style = MaterialTheme.typography.labelSmall, color = onSurfaceVariant)
            }
        }
    }
}
