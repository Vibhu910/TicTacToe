package com.example.tic_tac_toe.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "game_history")
data class GameHistory(
    @PrimaryKey(autoGenerate = true)
    val recordId: Long = 0,
    val timestampValue: Long = System.currentTimeMillis(),
    val victoriousPlayer: String,
    val challengeLevel: String,
    val playMode: String
)
