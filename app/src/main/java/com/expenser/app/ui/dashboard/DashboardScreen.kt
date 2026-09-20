package com.expenser.app.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.expenser.app.data.model.EntryType
import com.expenser.app.ui.common.BarChart
import com.expenser.app.ui.common.DonutChart
import com.expenser.app.ui.common.MonthSelector
import com.expenser.app.ui.common.entryTypeColor
import com.expenser.app.ui.profile.ScreenTopActions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onCategoriesClick: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory),
) {
    val totalExpense by viewModel.totalExpenseMinor.collectAsStateWithLifecycle()
    val totalIncome by viewModel.totalIncomeMinor.collectAsStateWithLifecycle()
    val totalInvestment by viewModel.totalInvestmentMinor.collectAsStateWithLifecycle()
    val netWorthBreakdown by viewModel.netWorthBreakdown.collectAsStateWithLifecycle()

    val incomeByCategory by viewModel.incomeByCategory.collectAsStateWithLifecycle()
    val expenseByCategory by viewModel.expenseByCategory.collectAsStateWithLifecycle()
    val investmentByCategory by viewModel.investmentByCategory.collectAsStateWithLifecycle()
    val month by viewModel.selectedMonth.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Dashboard", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Track and assess your finances",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = { ScreenTopActions(onCategoriesClick, onProfileClick) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                MonthSelector(month = month, onChange = viewModel::selectMonth)
            }
            item {
                ChartCard("Net worth") {
                    DonutChart(
                        data = listOf(
                            netWorthBreakdown.deficitLabel to netWorthBreakdown.deficitMinor,
                            netWorthBreakdown.surplusLabel to netWorthBreakdown.surplusMinor,
                        ),
                        colors = listOf(MaterialTheme.colorScheme.error, entryTypeColor(EntryType.Income)),
                    )
                }
            }
            item {
                ChartCard("Income vs Expense vs Investment") {
                    BarChart(
                        data = listOf(
                            "Income" to totalIncome,
                            "Expense" to totalExpense,
                            "Investment" to totalInvestment,
                        ),
                        colors = listOf(
                            entryTypeColor(EntryType.Income),
                            MaterialTheme.colorScheme.error,
                            entryTypeColor(EntryType.Investment),
                        ),
                    )
                }
            }
            item {
                ChartCard("Expense") {
                    DonutChart(
                        expenseByCategory,
                        baseColor = MaterialTheme.colorScheme.error,
                    )
                }
            }
            item {
                ChartCard("Income") {
                    DonutChart(
                        incomeByCategory,
                        baseColor = entryTypeColor(EntryType.Income),
                    )
                }
            }
            item {
                ChartCard("Investment") {
                    DonutChart(
                        investmentByCategory,
                        baseColor = entryTypeColor(EntryType.Investment),
                    )
                }
            }
        }
    }
}

@Composable
private fun ChartCard(title: String, content: @Composable () -> Unit) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            content()
        }
    }
}
