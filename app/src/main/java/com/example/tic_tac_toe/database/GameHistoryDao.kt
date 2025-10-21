package com.example.tic_tac_toe.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GameHistoryDao {
    @Insert
    suspend fun addGameRecord(gameRecord: GameHistory)

    @Query("SELECT COUNT(*) FROM game_history")
    suspend fun countTotalGames(): Int

    @Query("DELETE FROM game_history")
    suspend fun clearAllRecords()

    @Query("SELECT * FROM game_history ORDER BY timestampValue DESC")
    suspend fun retrieveAllRecordsSync(): List<GameHistory>

    @Query("SELECT * FROM game_history ORDER BY timestampValue DESC")
    fun retrieveAllRecords(): Flow<List<GameHistory>>
}


