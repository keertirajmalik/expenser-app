package com.expenser.app.ui.profile

import android.util.Log
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
import com.expenser.app.ui.common.pruneAvatars
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
    /** Drops every stored avatar but the path passed in; run after a successful save. */
    private val onAvatarPersisted: suspend (String?) -> Unit,
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
                .onSuccess {
                    // Only once the row points at `image` - pruning first would let a
                    // failed save leave the profile referencing a deleted file.
                    onAvatarPersisted(image)
                    _message.value = "Profile updated"
                }
                .onFailure {
                    Log.e(TAG, "Couldn't update profile", it)
                    _message.value = "Couldn't update profile"
                }
        }
    }

    fun consumeMessage() { _message.value = null }

    companion object {
        private const val TAG = "ProfileViewModel"

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
                    onAvatarPersisted = { path -> pruneAvatars(app, path) },
                )
            }
        }
    }
}
