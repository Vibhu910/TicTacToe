package com.example.tic_tac_toe.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "game_history")
data class GameHistory(
    @PrimaryKey(autoGenerate = true)
    val recordId: Long = 0,
    val timestampValue: Long = System.currentTimeMillis(),
    val victoriousPlayer: String, // "X", "O", or "Draw"
    val challengeLevel: String, // "Easy", "Medium", "Hard", or "Human"
    val playMode: String // "Computer" or "Human"
)


