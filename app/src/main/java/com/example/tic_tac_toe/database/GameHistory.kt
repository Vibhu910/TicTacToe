package com.example.tic_tac_toe.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "game_history")
data class GameHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val winner: String, // "X", "O", or "Draw"
    val difficultyMode: String, // "Easy", "Medium", "Hard", or "Human"
    val gameMode: String // "Computer" or "Human"
)


