package com.expenser.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import com.expenser.app.ExpenserApp
import com.expenser.app.data.db.entity.UserEntity
import com.expenser.app.data.repo.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(private val repository: UserRepository) : ViewModel() {

    init {
        viewModelScope.launch { repository.ensureSeeded() }
    }

    val user: StateFlow<UserEntity?> =
        repository.observeUser()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    fun save(name: String, image: String?) {
        viewModelScope.launch {
            runCatching { repository.updateProfile(name, image) }
                .onSuccess { _message.value = "Profile updated" }
                .onFailure { _message.value = it.message ?: "Couldn't update profile" }
        }
    }

    fun consumeMessage() { _message.value = null }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as ExpenserApp
                ProfileViewModel(app.container.userRepository)
            }
        }
    }
}
