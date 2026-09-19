package com.expenser.app.ui.category

import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import com.expenser.app.ExpenserApp
import com.expenser.app.data.db.entity.CategoryEntity
import com.expenser.app.data.model.EntryType
import com.expenser.app.data.repo.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoryViewModel(private val repository: CategoryRepository) : ViewModel() {

    val categories: StateFlow<List<CategoryEntity>> =
        repository.observeAll().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList(),
        )

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    fun save(id: String?, name: String, type: EntryType, description: String?) {
        viewModelScope.launch {
            runCatching { repository.save(id, name, type, description) }
                .onFailure { _message.value = it.userMessage() }
        }
    }

    fun delete(category: CategoryEntity) {
        viewModelScope.launch {
            try {
                repository.delete(category)
            } catch (e: SQLiteConstraintException) {
                _message.value = "\"${category.name}\" is in use and can't be deleted."
            }
        }
    }

    fun consumeMessage() { _message.value = null }

    private fun Throwable.userMessage(): String = when {
        this is SQLiteConstraintException -> "A category with that name already exists."
        else -> message ?: "Something went wrong."
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as ExpenserApp
                CategoryViewModel(app.container.categoryRepository)
            }
        }
    }
}
