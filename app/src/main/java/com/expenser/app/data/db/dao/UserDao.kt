package com.expenser.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.expenser.app.data.db.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT id FROM users LIMIT 1")
    suspend fun firstUserId(): String?

    @Query("SELECT * FROM users LIMIT 1")
    fun observeUser(): Flow<UserEntity?>

    @Insert
    suspend fun insert(user: UserEntity)

    @Query("UPDATE users SET name = :name, image = :image WHERE id = :id")
    suspend fun updateProfile(id: String, name: String, image: String?)
}
