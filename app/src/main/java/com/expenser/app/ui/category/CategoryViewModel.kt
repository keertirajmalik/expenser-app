package com.expenser.app.ui.category

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
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
import com.expenser.app.data.repo.CategoryRuleViolation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoryViewModel(private val repository: CategoryRepository) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    val categories: StateFlow<List<CategoryEntity>> =
        combine(repository.observeAll(), _query) { list, query ->
            val q = query.trim()
            if (q.isEmpty()) list
            else list.filter {
                it.name.contains(q, ignoreCase = true) ||
                    (it.description?.contains(q, ignoreCase = true) == true)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setQuery(query: String) { _query.value = query }

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
            } catch (e: Exception) {
                Log.e(TAG, "Couldn't delete category ${category.id}", e)
                _message.value = "Couldn't delete \"${category.name}\"."
            }
        }
    }

    fun consumeMessage() { _message.value = null }

    /**
     * Snackbar text. Only [CategoryRuleViolation] carries a message written for the
     * user; every other throwable is logged and reported generically rather than
     * putting raw SQLite text like "FOREIGN KEY constraint failed (code 787)" on screen.
     */
    private fun Throwable.userMessage(): String = when (this) {
        is CategoryRuleViolation -> message ?: "That change isn't allowed."
        is SQLiteConstraintException -> "A category with that name already exists."
        else -> {
            Log.e(TAG, "Couldn't save category", this)
            "Couldn't save the category."
        }
    }

    companion object {
        private const val TAG = "CategoryViewModel"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as ExpenserApp
                CategoryViewModel(app.container.categoryRepository)
            }
        }
    }
}
