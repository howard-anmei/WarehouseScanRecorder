package com.anmei.warehouseputaway.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        PutawayItem::class,
        ScanRecord::class
    ],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun putawayItemDao(): PutawayItemDao

    abstract fun scanRecordDao(): ScanRecordDao
}