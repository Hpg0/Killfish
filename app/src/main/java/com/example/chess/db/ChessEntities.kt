package com.example.chess.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val username: String,
    val email: String,
    val passwordHash: String, // hashed password
    val eloRating: Int = 1200,
    val avatarEmoji: String = "👤",
    val wins: Int = 0,
    val losses: Int = 0,
    val draws: Int = 0,
    val gamesPlayed: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_games")
data class GameEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val userId: Long = -1L, // -1L represents Guest
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
    val userId: Long = -1L, // -1L represents Guest/Aggregate
    val wins: Int = 0,
    val losses: Int = 0,
    val draws: Int = 0,
    val gamesPlayed: Int = 0,
    val benchmarkNps: Int = 0
)

@Entity(tableName = "trap_history")
data class TrapHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val userId: Long = -1L,
    val fen: String,
    val trapName: String,
    val description: String,
    val motif: String = "",
    val dangerousMoves: String = "",
    val continuation: String,
    val evaluation: String,
    val timestamp: Long = System.currentTimeMillis()
)
