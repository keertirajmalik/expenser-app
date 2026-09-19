package com.expenser.app.data.db

import androidx.room.TypeConverter
import com.expenser.app.data.model.EntryType

class Converters {
    @TypeConverter
    fun entryTypeToString(type: EntryType): String = type.name

    @TypeConverter
    fun stringToEntryType(value: String): EntryType = EntryType.valueOf(value)
}
