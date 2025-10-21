package com.example.tic_tac_toe.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [GameHistory::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameHistoryDao(): GameHistoryDao

    companion object {
        @Volatile
        private var databaseInstance: AppDatabase? = null

        fun obtainDatabase(appContext: Context): AppDatabase {
            return databaseInstance ?: synchronized(this) {
                val createdInstance = Room.databaseBuilder(
                    appContext.applicationContext,
                    AppDatabase::class.java,
                    "tictactoe_database"
                ).build()
                databaseInstance = createdInstance
                createdInstance
            }
        }
    }
}


