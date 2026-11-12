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

/**
 * Schema is exported to `app/schemas` (see the `room.schemaLocation` KSP arg). Room
 * validates the live database against the compiled schema's hash on open, so any change
 * here needs both a version bump and a migration or every existing install fails to
 * start with "Room cannot verify the data integrity". Add the migration to
 * [ExpenserDatabase.MIGRATIONS]; AppContainer registers that list.
 */
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
        /**
         * 1 -> 2: drop `index_categories_name`. Commit da80753 removed `Index("name")` from
         * [CategoryEntity] but left the version at 1, so every install from v1.0.0 crashed
         * on launch after updating to v2.0.0. Two kinds of version-1 database exist: those
         * from v1.0.0, which still have the index, and fresh installs of v2.0.0, which
         * never got it. IF EXISTS makes the one statement right for both.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP INDEX IF EXISTS `index_categories_name`")
            }
        }

        val MIGRATIONS: Array<Migration> = arrayOf(MIGRATION_1_2)
    }
}
