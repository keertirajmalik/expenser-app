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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Owns the whole "profile" screen: name/photo, and the app-wide theme and currency
 * settings, so the screen only ever talks to one interface.
 *
 * None of that state is owned here. User, theme and currency all outlive this screen -
 * the avatar is in every top bar, and a theme preview has to apply app-wide - so they
 * live on [com.expenser.app.di.AppContainer] and arrive as a [StateFlow] plus a setter,
 * the pattern [com.expenser.app.ui.common.TransactionListViewModel] already uses for the
 * selected month. What this class adds is the screen's own behaviour: preview-vs-commit,
 * and persisting the profile.
 */
class ProfileViewModel(
    private val repository: UserRepository,
    /** Shared with the top-bar avatar; see [com.expenser.app.di.AppContainer.user]. */
    val user: StateFlow<UserEntity?>,
    val themeMode: StateFlow<ThemeMode>,
    val previewTheme: StateFlow<ThemeMode?>,
    private val onPreviewTheme: (ThemeMode?) -> Unit,
    private val onCommitTheme: (ThemeMode) -> Unit,
    val currency: StateFlow<String>,
    private val onSetCurrency: (String) -> Unit,
    /** Drops every stored avatar but the path passed in; run after a successful save. */
    private val onAvatarPersisted: suspend (String?) -> Unit,
) : ViewModel() {

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
                    user = app.container.user,
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
