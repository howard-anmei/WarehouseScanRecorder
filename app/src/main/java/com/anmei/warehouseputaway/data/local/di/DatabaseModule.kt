package com.anmei.warehouseputaway.data.local.di

import android.content.Context
import androidx.room.Room
import com.anmei.warehouseputaway.data.local.database.AppDatabase
import com.anmei.warehouseputaway.data.local.database.PutawayItemDao
import com.anmei.warehouseputaway.data.local.database.ScanRecordDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext appContext: Context
    ): AppDatabase {

        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "warehouse_putaway.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun providePutawayItemDao(
        appDatabase: AppDatabase
    ): PutawayItemDao {
        return appDatabase.putawayItemDao()
    }

    @Provides
    fun provideScanRecordDao(
        appDatabase: AppDatabase
    ): ScanRecordDao {
        return appDatabase.scanRecordDao()
    }
}