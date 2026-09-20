package com.expenser.app.ui.expense

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.expenser.app.data.model.EntryType
import com.expenser.app.ui.common.TransactionListScreen
import com.expenser.app.ui.common.TransactionListViewModel

@Composable
fun ExpenseScreen(
    onCategoriesClick: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: TransactionListViewModel = viewModel(
        factory = remember { TransactionListViewModel.factory(EntryType.Expense) },
    ),
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
