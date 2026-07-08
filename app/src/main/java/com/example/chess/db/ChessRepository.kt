package com.example.chess.db

import kotlinx.coroutines.flow.Flow

class ChessRepository(private val dao: ChessDao) {
    val allGames: Flow<List<GameEntity>> = dao.getAllGames()
    val statistics: Flow<StatisticEntity?> = dao.getStatistics()

    fun getAllGamesForUser(userId: Long): Flow<List<GameEntity>> {
        return dao.getAllGamesForUser(userId)
    }

    fun getStatisticsForUser(userId: Long): Flow<StatisticEntity?> {
        return dao.getStatisticsForUser(userId)
    }

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

    // User Operations
    suspend fun getUserByEmail(email: String): UserEntity? {
        return dao.getUserByEmail(email)
    }

    fun getUserById(id: Long): Flow<UserEntity?> {
        return dao.getUserById(id)
    }

    suspend fun registerUser(user: UserEntity): Long {
        return dao.insertUser(user)
    }

    suspend fun updateUser(user: UserEntity) {
        dao.updateUser(user)
    }

    suspend fun getAllUsers(): List<UserEntity> {
        return dao.getAllUsers()
    }

    // Trap History operations
    fun getAllTrapHistory(): Flow<List<TrapHistoryEntity>> {
        return dao.getAllTrapHistory()
    }

    fun getTrapHistoryForUser(userId: Long): Flow<List<TrapHistoryEntity>> {
        return dao.getTrapHistoryForUser(userId)
    }

    suspend fun saveTrapHistory(trap: TrapHistoryEntity) {
        dao.insertTrapHistory(trap)
    }

    suspend fun deleteTrapHistoryById(id: Long) {
        dao.deleteTrapHistoryById(id)
    }

    suspend fun clearTrapHistory() {
        dao.deleteAllTrapHistory()
    }
}
