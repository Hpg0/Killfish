package com.example.chess.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ChessDao {
    @Query("SELECT * FROM saved_games ORDER BY date DESC")
    fun getAllGames(): Flow<List<GameEntity>>

    @Query("SELECT * FROM saved_games WHERE userId = :userId ORDER BY date DESC")
    fun getAllGamesForUser(userId: Long): Flow<List<GameEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: GameEntity)

    @Query("DELETE FROM saved_games WHERE id = :id")
    suspend fun deleteGameById(id: Long)

    @Query("DELETE FROM saved_games")
    suspend fun deleteAllGames()

    @Query("DELETE FROM user_statistics")
    suspend fun clearStatistics()

    @Query("SELECT * FROM user_statistics WHERE id = 'aggregate'")
    fun getStatistics(): Flow<StatisticEntity?>

    @Query("SELECT * FROM user_statistics WHERE userId = :userId LIMIT 1")
    fun getStatisticsForUser(userId: Long): Flow<StatisticEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatistics(stats: StatisticEntity)

    // User Operations
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserById(id: Long): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT * FROM users ORDER BY username ASC")
    suspend fun getAllUsers(): List<UserEntity>

    // Trap History Operations
    @Query("SELECT * FROM trap_history ORDER BY timestamp DESC")
    fun getAllTrapHistory(): Flow<List<TrapHistoryEntity>>

    @Query("SELECT * FROM trap_history WHERE userId = :userId ORDER BY timestamp DESC")
    fun getTrapHistoryForUser(userId: Long): Flow<List<TrapHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrapHistory(trap: TrapHistoryEntity)

    @Query("DELETE FROM trap_history WHERE id = :id")
    suspend fun deleteTrapHistoryById(id: Long)

    @Query("DELETE FROM trap_history")
    suspend fun deleteAllTrapHistory()
}
