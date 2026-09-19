package com.expenser.app.ui.income

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.expenser.app.data.model.EntryType
import com.expenser.app.ui.common.TransactionListScreen

@Composable
fun IncomeScreen(
    onCategoriesClick: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: IncomeViewModel = viewModel(factory = IncomeViewModel.Factory),
) {
    TransactionListScreen(
        title = "Income",
        noun = "income",
        type = EntryType.Income,
        onCategoriesClick = onCategoriesClick,
        onProfileClick = onProfileClick,
        viewModel = viewModel,
    )
}
