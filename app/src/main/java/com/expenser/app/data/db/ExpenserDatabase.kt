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

/**
 * Schema is exported to `app/schemas` (see the `room.schemaLocation` KSP arg). Room
 * validates the live database against the compiled schema's hash on open, so the first
 * change here needs both a version bump and a migration or every existing install fails
 * to start with "Room cannot verify the data integrity". The exported JSON is what makes
 * that migration writable and testable, so it is committed.
 */
@Database(
    entities = [UserEntity::class, CategoryEntity::class, TransactionEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class ExpenserDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
}
