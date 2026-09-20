package com.expenser.app.ui.income

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.expenser.app.data.model.EntryType
import com.expenser.app.ui.common.TransactionListScreen
import com.expenser.app.ui.common.TransactionListViewModel

@Composable
fun IncomeScreen(
    onCategoriesClick: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: TransactionListViewModel = viewModel(
        factory = remember { TransactionListViewModel.factory(EntryType.Income) },
    ),
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
