package com.example.chess.db

import kotlinx.coroutines.flow.Flow

class ChessRepository(private val dao: ChessDao) {
    val allGames: Flow<List<GameEntity>> = dao.getAllGames()
    val statistics: Flow<StatisticEntity?> = dao.getStatistics()

    suspend fun saveGame(game: GameEntity) {
        dao.insertGame(game)
    }

    suspend fun deleteGame(id: Long) {
        dao.deleteGameById(id)
    }

    suspend fun updateStatistics(stats: StatisticEntity) {
        dao.insertStatistics(stats)
    }

    suspend fun clearAllData() {
        dao.deleteAllGames()
        dao.clearStatistics()
    }
}
