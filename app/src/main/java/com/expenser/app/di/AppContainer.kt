package com.expenser.app.di

import android.content.Context
import androidx.room.Room
import com.expenser.app.data.db.ExpenserDatabase
import com.expenser.app.data.db.entity.UserEntity
import com.expenser.app.data.repo.CategoryRepository
import com.expenser.app.data.repo.TransactionRepository
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
}
