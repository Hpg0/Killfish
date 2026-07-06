package com.example.chess.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_games")
data class GameEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val date: Long = System.currentTimeMillis(),
    val pgn: String,
    val initialFen: String,
    val movesList: String, // space-separated UCI moves
    val result: String // "1-0", "0-1", "1/2-1/2", "*"
)

@Entity(tableName = "user_statistics")
data class StatisticEntity(
    @PrimaryKey val id: String = "aggregate",
    val wins: Int = 0,
    val losses: Int = 0,
    val draws: Int = 0,
    val gamesPlayed: Int = 0,
    val benchmarkNps: Int = 0
)
