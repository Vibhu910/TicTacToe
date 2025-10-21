package com.example.tic_tac_toe

import android.content.Context
import android.content.SharedPreferences

object SettingsManager {
    private const val PREFERENCES_NAME = "tictactoe_prefs"
    private const val KEY_DIFFICULTY = "difficulty"

    enum class Difficulty {
        EASY,
        MEDIUM,
        HARD
    }

    private fun accessPreferences(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun updateDifficulty(context: Context, level: Difficulty) {
        val storedValue = when (level) {
            Difficulty.EASY -> "EASY"
            Difficulty.MEDIUM -> "MEDIUM"
            Difficulty.HARD -> "HARD"
        }
        accessPreferences(context).edit().putString(KEY_DIFFICULTY, storedValue).apply()
    }

    fun retrieveDifficulty(context: Context): Difficulty = when (
        accessPreferences(context).getString(KEY_DIFFICULTY, "EASY")
    ) {
        "MEDIUM" -> Difficulty.MEDIUM
        "HARD" -> Difficulty.HARD
        else -> Difficulty.EASY
    }
}
