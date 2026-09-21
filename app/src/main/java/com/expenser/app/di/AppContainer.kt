package com.expenser.app.di

import android.content.Context
import androidx.room.Room
import com.expenser.app.data.db.ExpenserDatabase
import com.expenser.app.data.db.entity.UserEntity
import com.expenser.app.data.model.ThemeMode
import com.expenser.app.data.reminder.ReminderScheduler
import com.expenser.app.data.reminder.ReminderSetting
import com.expenser.app.data.repo.BackupRepository
import com.expenser.app.data.repo.CategoryRepository
import com.expenser.app.data.repo.TransactionRepository
import com.expenser.app.data.repo.UserRepository
import com.expenser.app.ui.common.setCurrencyCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.YearMonth
import java.util.UUID
import androidx.core.content.edit

/**
 * Manual dependency container. One instance lives on [com.expenser.app.ExpenserApp]
 * and hands repositories to the ViewModels. No Hilt (see CONTEXT.md).
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    private val db = Room.databaseBuilder(
        appContext,
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
    val backupRepository = BackupRepository(db, db.categoryDao(), db.transactionDao(), ::currentUserId)

    /** Process-lifetime scope, only for sharing state that outlives any one screen. */
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    /**
     * The single local user - name and avatar.
     *
     * Shared here rather than from a ViewModel because the avatar sits in every main
     * screen's top bar. `viewModel()` inside a NavHost resolves to the current
     * NavBackStackEntry, so a ViewModel-per-avatar meant one instance per tab, each
     * collecting this same row and each holding the profile screen's whole surface -
     * theme preview, currency commit, profile persistence - to draw a 32dp circle.
     */
    val user: StateFlow<UserEntity?> =
        userRepository.observeUser().stateIn(scope, SharingStarted.WhileSubscribed(5_000), null)

    // App-wide time scope shared by the dashboard and every transaction list.
    // null = all time. Defaults to the current month for everyday budgeting.
    private val _selectedMonth = MutableStateFlow<YearMonth?>(YearMonth.now())
    val selectedMonth: StateFlow<YearMonth?> = _selectedMonth.asStateFlow()

    // Whether the scope above is the calendar default or something the user chose. An
    // untouched default has to keep following the calendar: the process can outlive a
    // month boundary, and YearMonth.now() was only ever read once, at construction.
    private var monthChosenByUser = false

    fun setSelectedMonth(month: YearMonth?) {
        monthChosenByUser = true
        _selectedMonth.value = month
    }

    /** Re-point an untouched default at the current month. Called when the app foregrounds. */
    fun refreshDefaultMonth() {
        if (!monthChosenByUser) _selectedMonth.value = YearMonth.now()
    }

    // Theme preference, persisted in SharedPreferences (no extra dependency).
    private val prefs = appContext.getSharedPreferences("settings", Context.MODE_PRIVATE)
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
        prefs.edit { putString("theme_mode", mode.name) }
    }

    // Currency preference, persisted in SharedPreferences and mirrored into the
    // Compose-visible formatter cache in Money.kt so `formatMoney` recomposes on change.
    // `_currency` and that cache must always change together - route every mutation
    // through `setCurrency` (or the init block below) so they can't drift apart.
    private val _currency = MutableStateFlow(prefs.getString("currency", "INR") ?: "INR")
    val currency: StateFlow<String> = _currency.asStateFlow()

    init {
        setCurrencyCode(_currency.value)
        // [user] backs the top-bar avatar, so the row has to exist from launch rather
        // than appearing once the profile screen is first opened.
        scope.launch { runCatching { userRepository.ensureSeeded() } }
    }

    /** The single seam for changing currency: persists it and updates the live formatter. */
    fun setCurrency(code: String) {
        _currency.value = code
        prefs.edit { putString("currency", code) }
        setCurrencyCode(_currency.value)
    }

    // Daily "add your transactions" reminder. Defaults to 9:00 PM, off until enabled.
    private val _reminder = MutableStateFlow(
        ReminderSetting(
            enabled = prefs.getBoolean("reminder_enabled", false),
            hour = prefs.getInt("reminder_hour", 21),
            minute = prefs.getInt("reminder_minute", 0),
        ),
    )
    val reminder: StateFlow<ReminderSetting> = _reminder.asStateFlow()

    fun setReminder(enabled: Boolean, hour: Int, minute: Int) {
        _reminder.value = ReminderSetting(enabled, hour, minute)
        prefs.edit {
            putBoolean("reminder_enabled", enabled)
                .putInt("reminder_hour", hour)
                .putInt("reminder_minute", minute)
        }
        if (enabled) ReminderScheduler.schedule(appContext, hour, minute)
        else ReminderScheduler.cancel(appContext)
    }
}
