package com.expenser.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.expenser.app.data.db.dao.CategoryDao
import com.expenser.app.data.db.dao.TransactionDao
import com.expenser.app.data.db.dao.UserDao
import com.expenser.app.data.db.entity.CategoryEntity
import com.expenser.app.data.db.entity.TransactionEntity
import com.expenser.app.data.db.entity.UserEntity

/** Any schema change needs a version bump and a migration in [MIGRATIONS], or existing installs crash on open. */
@Database(
    entities = [UserEntity::class, CategoryEntity::class, TransactionEntity::class],
    version = 2,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class ExpenserDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        // v1.0.0 databases still have this index; fresh v2.0.0 ones don't, hence IF EXISTS.
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP INDEX IF EXISTS `index_categories_name`")
            }
        }

        val MIGRATIONS: Array<Migration> = arrayOf(MIGRATION_1_2)
    }
}
