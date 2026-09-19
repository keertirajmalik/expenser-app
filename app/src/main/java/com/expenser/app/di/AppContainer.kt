package com.expenser.app.di

import android.content.Context
import androidx.room.Room
import com.expenser.app.data.db.ExpenserDatabase
import com.expenser.app.data.db.entity.UserEntity
import com.expenser.app.data.model.ThemeMode
import com.expenser.app.data.repo.CategoryRepository
import com.expenser.app.data.repo.TransactionRepository
import com.expenser.app.data.repo.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

/**
 * Manual dependency container. One instance lives on [com.expenser.app.ExpenserApp]
 * and hands repositories to the ViewModels. No Hilt (see CONTEXT.md).
 */
class AppContainer(context: Context) {

    private val db = Room.databaseBuilder(
        context.applicationContext,
        ExpenserDatabase::class.java,
        "expenser.db",
    ).build()

    // Seed-once guard for the single local user.
    private val userMutex = Mutex()
    @Volatile private var cachedUserId: String? = null

    /** Returns the local user's id, creating the single user row on first call. */
    private suspend fun currentUserId(): String {
        cachedUserId?.let { return it }
        return userMutex.withLock {
            cachedUserId?.let { return it }
            val existing = db.userDao().firstUserId()
            val id = existing ?: UUID.randomUUID().toString().also {
                db.userDao().insert(UserEntity(id = it, name = "Me", username = "local"))
            }
            cachedUserId = id
            id
        }
    }

    val categoryRepository = CategoryRepository(db.categoryDao(), ::currentUserId)
    val transactionRepository = TransactionRepository(db.transactionDao(), ::currentUserId)
    val userRepository = UserRepository(db.userDao(), ::currentUserId)

    // Theme preference, persisted in SharedPreferences (no extra dependency).
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    private val _themeMode = MutableStateFlow(
        runCatching { ThemeMode.valueOf(prefs.getString("theme_mode", null) ?: ThemeMode.System.name) }
            .getOrDefault(ThemeMode.System),
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    /** Transient, unsaved theme used for live preview; null means "use the saved one". */
    private val _previewTheme = MutableStateFlow<ThemeMode?>(null)
    val previewTheme: StateFlow<ThemeMode?> = _previewTheme.asStateFlow()

    /** Apply a theme live without persisting; pass null to drop the preview. */
    fun setPreviewTheme(mode: ThemeMode?) {
        _previewTheme.value = mode
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        _previewTheme.value = null
        prefs.edit().putString("theme_mode", mode.name).apply()
    }

    /** Persisted currency code; applied to the formatter by [com.expenser.app.ExpenserApp]. */
    val savedCurrency: String = prefs.getString("currency", "INR") ?: "INR"

    fun persistCurrency(code: String) {
        prefs.edit().putString("currency", code).apply()
    }
}
