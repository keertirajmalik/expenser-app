package com.expenser.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.expenser.app.data.db.dao.CategoryDao
import com.expenser.app.data.db.dao.TransactionDao
import com.expenser.app.data.db.dao.UserDao
import com.expenser.app.data.db.entity.CategoryEntity
import com.expenser.app.data.db.entity.TransactionEntity
import com.expenser.app.data.db.entity.UserEntity

@Database(
    entities = [UserEntity::class, CategoryEntity::class, TransactionEntity::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class ExpenserDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
}
