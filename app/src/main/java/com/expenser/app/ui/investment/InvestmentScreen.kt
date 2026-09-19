package com.expenser.app.ui.investment

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.expenser.app.data.model.EntryType
import com.expenser.app.ui.common.TransactionListScreen

@Composable
fun InvestmentScreen(
    onCategoriesClick: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: InvestmentViewModel = viewModel(factory = InvestmentViewModel.Factory),
) {
    TransactionListScreen(
        title = "Investments",
        noun = "investment",
        type = EntryType.Investment,
        onCategoriesClick = onCategoriesClick,
        onProfileClick = onProfileClick,
        viewModel = viewModel,
    )
}
