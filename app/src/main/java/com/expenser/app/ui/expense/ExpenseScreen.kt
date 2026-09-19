package com.expenser.app.ui.expense

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.expenser.app.data.model.EntryType
import com.expenser.app.ui.common.TransactionListScreen

@Composable
fun ExpenseScreen(
    onCategoriesClick: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: ExpenseViewModel = viewModel(factory = ExpenseViewModel.Factory),
) {
    TransactionListScreen(
        title = "Expenses",
        noun = "expense",
        type = EntryType.Expense,
        onCategoriesClick = onCategoriesClick,
        onProfileClick = onProfileClick,
        viewModel = viewModel,
    )
}
