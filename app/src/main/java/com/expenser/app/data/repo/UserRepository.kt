package com.expenser.app.data.repo

import com.expenser.app.data.db.dao.UserDao
import com.expenser.app.data.db.entity.UserEntity
import kotlinx.coroutines.flow.Flow

class UserRepository(
    private val userDao: UserDao,
    private val currentUserId: suspend () -> String,
) {
    fun observeUser(): Flow<UserEntity?> = userDao.observeUser()

    /** Make sure the single local user row exists (so the profile screen has data). */
    suspend fun ensureSeeded() { currentUserId() }

    suspend fun updateProfile(name: String, image: String?) {
        userDao.updateProfile(currentUserId(), name.trim(), image)
    }
}
