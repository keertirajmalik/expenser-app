package com.expenser.app.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.expenser.app.ui.common.formatMoney

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory),
) {
    val totalExpense by viewModel.totalExpenseMinor.collectAsStateWithLifecycle()

    Column(modifier = modifier.padding(16.dp)) {
        TotalCard(label = "Total Expense", amountMinor = totalExpense)
    }
}

@Composable
private fun TotalCard(label: String, amountMinor: Long) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(label, style = MaterialTheme.typography.titleMedium)
                Text(
                    formatMoney(amountMinor),
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.TrendingDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
            )
        }
    }
}
