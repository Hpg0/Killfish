package com.example.chess.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [GameEntity::class, StatisticEntity::class], version = 1, exportSchema = false)
abstract class ChessDatabase : RoomDatabase() {
    abstract fun chessDao(): ChessDao

    companion object {
        @Volatile
        private var INSTANCE: ChessDatabase? = null

        fun getDatabase(context: Context): ChessDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChessDatabase::class.java,
                    "killfish_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
