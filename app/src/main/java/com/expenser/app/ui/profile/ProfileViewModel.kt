package com.expenser.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import com.expenser.app.ExpenserApp
import com.expenser.app.data.db.entity.UserEntity
import com.expenser.app.data.model.ThemeMode
import com.expenser.app.data.repo.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Owns the whole "profile" screen: name/photo, and the app-wide theme and currency
 * settings, so the screen only ever talks to one interface. Theme/currency still live
 * on [com.expenser.app.di.AppContainer] (theme preview must apply app-wide, not just
 * within this screen), so they're taken as a [StateFlow] + setter, the same pattern
 * [com.expenser.app.ui.common.TransactionListViewModel] uses for the selected month.
 */
class ProfileViewModel(
    private val repository: UserRepository,
    val themeMode: StateFlow<ThemeMode>,
    val previewTheme: StateFlow<ThemeMode?>,
    private val onPreviewTheme: (ThemeMode?) -> Unit,
    private val onCommitTheme: (ThemeMode) -> Unit,
    val currency: StateFlow<String>,
    private val onSetCurrency: (String) -> Unit,
) : ViewModel() {

    init {
        viewModelScope.launch { repository.ensureSeeded() }
    }

    val user: StateFlow<UserEntity?> =
        repository.observeUser()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    /** Live-preview a theme app-wide without persisting it; pass null to drop the preview. */
    fun previewTheme(mode: ThemeMode?) = onPreviewTheme(mode)

    /** Persists name/photo, and commits the chosen theme and currency. */
    fun save(name: String, image: String?, theme: ThemeMode, currency: String) {
        onCommitTheme(theme)
        onSetCurrency(currency)
        viewModelScope.launch {
            runCatching { repository.updateProfile(name, image) }
                .onSuccess { _message.value = "Profile updated" }
                .onFailure { _message.value = it.message ?: "Couldn't update profile" }
        }
    }

    fun consumeMessage() { _message.value = null }

    /** Drop any live preview left over when leaving the screen without saving. */
    override fun onCleared() {
        onPreviewTheme(null)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as ExpenserApp
                ProfileViewModel(
                    repository = app.container.userRepository,
                    themeMode = app.container.themeMode,
                    previewTheme = app.container.previewTheme,
                    onPreviewTheme = app.container::setPreviewTheme,
                    onCommitTheme = app.container::setThemeMode,
                    currency = app.container.currency,
                    onSetCurrency = app.container::setCurrency,
                )
            }
        }
    }
}
