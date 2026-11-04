package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.ReadingDao
import com.example.data.entity.ReadingProgressEntity
import com.example.data.entity.UserPlanStateEntity

@Database(
    entities = [
        ReadingProgressEntity::class,
        UserPlanStateEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun readingDao(): ReadingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "slowo_na_dzien_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
