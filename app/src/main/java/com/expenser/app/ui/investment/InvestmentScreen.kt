package com.expenser.app.ui.investment

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.expenser.app.data.model.EntryType
import com.expenser.app.ui.common.TransactionListScreen
import com.expenser.app.ui.common.TransactionListViewModel

@Composable
fun InvestmentScreen(
    onCategoriesClick: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: TransactionListViewModel = viewModel(factory = TransactionListViewModel.factory(EntryType.Investment)),
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
