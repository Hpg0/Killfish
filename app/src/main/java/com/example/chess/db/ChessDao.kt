package com.example.chess.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChessDao {
    @Query("SELECT * FROM saved_games ORDER BY date DESC")
    fun getAllGames(): Flow<List<GameEntity>>

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatistics(stats: StatisticEntity)
}
