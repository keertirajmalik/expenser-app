package com.expenser.app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.SouthWest
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.expenser.app.data.db.dao.TransactionListItem
import com.expenser.app.data.model.EntryType
import com.expenser.app.ui.common.BentoCard
import com.expenser.app.ui.common.CategoryBadge
import com.expenser.app.ui.common.MonthTitleSelector
import com.expenser.app.ui.common.RowDivider
import com.expenser.app.ui.common.amountTextColor
import com.expenser.app.ui.common.entriesLabel
import com.expenser.app.ui.common.entryTypeTint
import com.expenser.app.ui.common.formatMoneyShort
import com.expenser.app.ui.common.isDarkScheme
import com.expenser.app.ui.common.linkColor
import com.expenser.app.ui.common.shortDayLabel
import com.expenser.app.ui.profile.ScreenTopActions
import java.time.YearMonth
import kotlin.math.abs

// Hero card colours. Fixed rather than scheme slots: the card is deliberately the one
// inverted element on the screen, in both themes.
private val HeroLight = Color(0xFF09090B)
private val HeroDark = Color(0xFF27272A)
private val HeroMuted = Color(0xFFA1A1AA)
private val SplitSpent = Color(0xFFFB7185)
private val SplitInvested = Color(0xFF3B82F6)
private val SplitLeft = Color.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onCategoriesClick: () -> Unit,
    onProfileClick: () -> Unit,
    /** Opens the list tab for a type: the tiles, "All expenses" and "more categories" use it. */
    onOpenType: (EntryType) -> Unit,
    viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory),
) {
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val topCategories by viewModel.topExpenseCategories.collectAsStateWithLifecycle()
    val recent by viewModel.recent.collectAsStateWithLifecycle()
    val month by viewModel.selectedMonth.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { MonthTitleSelector(month = month, onChange = viewModel::selectMonth) },
                actions = { ScreenTopActions(onCategoriesClick, onProfileClick) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "hero") { HeroCard(summary, month) }
            item(key = "tiles") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TypeTile("Income", summary.incomeMinor, summary.incomeCount, EntryType.Income, Icons.Filled.SouthWest, onOpenType)
                    TypeTile("Spent", summary.expenseMinor, summary.expenseCount, EntryType.Expense, Icons.Filled.NorthEast, onOpenType)
                    TypeTile("Invested", summary.investmentMinor, summary.investmentCount, EntryType.Investment, Icons.AutoMirrored.Filled.TrendingUp, onOpenType)
                }
            }
            item(key = "categories") { TopCategoriesCard(topCategories) { onOpenType(EntryType.Expense) } }
            item(key = "recent") { RecentCard(recent, month) }
        }
    }
}

@Composable
private fun HeroCard(summary: MonthSummary, month: YearMonth?) {
    val left = summary.leftMinor
    val overspent = left < 0
    val title = when {
        overspent -> "Over income by"
        month != null -> "Left this month"
        else -> "Left overall"
    }
    val caption = when {
        summary.incomeMinor <= 0L && summary.expenseMinor + summary.investmentMinor <= 0L -> "Nothing recorded yet"
        summary.incomeMinor <= 0L -> "No income recorded"
        overspent -> "Spent and invested more than ${formatMoneyShort(summary.incomeMinor)} income"
        else -> "${summary.leftPercentOfIncome}% of ${formatMoneyShort(summary.incomeMinor)} income"
    }
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isDarkScheme()) HeroDark else HeroLight,
        contentColor = Color.White,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = HeroMuted)
            Text(
                formatMoneyShort(abs(left)),
                fontSize = 36.sp,
                lineHeight = 44.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp).semantics { heading() },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(caption, style = MaterialTheme.typography.bodySmall, color = HeroMuted)
            SplitBar(summary, modifier = Modifier.padding(top = 18.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                LegendItem("Spent", summary.expenseMinor, SplitSpent)
                LegendItem("Invested", summary.investmentMinor, SplitInvested)
                LegendItem("Left", left.coerceAtLeast(0L), SplitLeft)
            }
        }
    }
}

/** Income split into spent / invested / left as one bar. Empty track when nothing happened. */
@Composable
private fun SplitBar(summary: MonthSummary, modifier: Modifier = Modifier) {
    val split = summary.split
    val pct = { f: Float -> (f * 100).toInt() }
    val description = "Income split: ${pct(split.spent)}% spent, ${pct(split.invested)}% invested, ${pct(split.left)}% left"
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.12f))
            .clearAndSetSemantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        listOf(split.spent to SplitSpent, split.invested to SplitInvested, split.left to SplitLeft)
            .filter { it.first > 0f }
            .forEach { (share, color) ->
                Box(Modifier.weight(share).height(10.dp).background(color))
            }
    }
}

@Composable
private fun RowScope.LegendItem(label: String, amountMinor: Long, dot: Color) {
    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(dot))
            Text(label, style = MaterialTheme.typography.labelMedium, color = HeroMuted)
        }
        Text(
            formatMoneyShort(amountMinor),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun RowScope.TypeTile(
    label: String,
    amountMinor: Long,
    count: Int,
    type: EntryType,
    icon: ImageVector,
    onOpen: (EntryType) -> Unit,
) {
    val tint = entryTypeTint(type)
    Surface(
        onClick = { onOpen(type) },
        shape = RoundedCornerShape(16.dp),
        color = tint.container,
        contentColor = tint.content,
        modifier = Modifier.weight(1f).heightIn(min = 96.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text(
                formatMoneyShort(amountMinor),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                entriesLabel(count),
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
private fun TopCategoriesCard(breakdown: CategoryBreakdown, onOpenExpenses: () -> Unit) {
    BentoCard {
        Column(modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 4.dp, bottom = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Top categories",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.semantics { heading() },
                )
                TextButton(
                    onClick = onOpenExpenses,
                    colors = ButtonDefaults.textButtonColors(contentColor = linkColor()),
                ) { Text("All expenses") }
            }
            if (breakdown.top.isEmpty()) {
                Text(
                    "No expenses in this period.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
                )
                return@Column
            }
            Column(
                modifier = Modifier.padding(end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                breakdown.top.chunked(2).forEach { pair ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        pair.forEach { share -> CategoryTile(share) }
                        if (pair.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
            if (breakdown.otherCount > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, end = 8.dp)
                        .heightIn(min = 44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClickLabel = "Open expenses", onClick = onOpenExpenses),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        if (breakdown.otherCount == 1) "1 more category" else "${breakdown.otherCount} more categories",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    Text(formatMoneyShort(breakdown.otherMinor), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun RowScope.CategoryTile(share: CategoryShare) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CategoryBadge(share.name, share.categoryId, size = 36.dp)
            Text(
                "${share.percent}%",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            share.name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 10.dp),
        )
        Text(
            formatMoneyShort(share.amountMinor),
            fontSize = 20.sp,
            lineHeight = 26.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun RecentCard(recent: List<TransactionListItem>, month: YearMonth?) {
    BentoCard {
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)) {
            Text(
                "Recent",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp).semantics { heading() },
            )
            if (recent.isEmpty()) {
                Text(
                    if (month != null) "Nothing recorded this month yet." else "Nothing recorded yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
            recent.forEachIndexed { index, item ->
                if (index > 0) RowDivider()
                RecentRow(item)
            }
        }
    }
}

@Composable
private fun RecentRow(item: TransactionListItem) {
    val txn = item.transaction
    val sign = when (txn.type) {
        EntryType.Expense -> "−"
        EntryType.Income -> "+"
        EntryType.Investment -> ""
    }
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CategoryBadge(item.categoryName, txn.categoryId)
        Column(modifier = Modifier.weight(1f)) {
            Text(txn.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                "${item.categoryName} · ${shortDayLabel(txn.date)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            sign + formatMoneyShort(txn.amountMinor),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = amountTextColor(txn.type),
        )
    }
}
