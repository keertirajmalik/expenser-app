package com.expenser.app.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import com.expenser.app.ui.common.StatCard
import com.expenser.app.ui.common.entryTypeColor
import com.expenser.app.ui.common.formatMoney
import com.expenser.app.ui.profile.ProfileAvatarAction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onProfileClick: () -> Unit,
    viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory),
) {
    val totalExpense by viewModel.totalExpenseMinor.collectAsStateWithLifecycle()
    val totalInvestment by viewModel.totalInvestmentMinor.collectAsStateWithLifecycle()

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
                actions = { ProfileAvatarAction(onClick = onProfileClick) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatCard(
                title = "Total Expense",
                value = formatMoney(totalExpense),
                icon = Icons.AutoMirrored.Filled.TrendingDown,
                accent = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth(),
            )
            StatCard(
                title = "Total Investment",
                value = formatMoney(totalInvestment),
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                accent = entryTypeColor(EntryType.Investment),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
