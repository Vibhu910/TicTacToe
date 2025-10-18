package com.example.tic_tac_toe.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GameHistoryDao {
    @Query("SELECT * FROM game_history ORDER BY timestamp DESC")
    fun getAllGames(): Flow<List<GameHistory>>

    @Query("SELECT * FROM game_history ORDER BY timestamp DESC")
    suspend fun getAllGamesSync(): List<GameHistory>

    @Insert
    suspend fun insertGame(game: GameHistory)

    @Query("DELETE FROM game_history")
    suspend fun deleteAllGames()

    @Query("SELECT COUNT(*) FROM game_history")
    suspend fun getGameCount(): Int
}


