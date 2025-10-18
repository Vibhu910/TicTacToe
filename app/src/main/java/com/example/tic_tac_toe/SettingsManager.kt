package com.example.tic_tac_toe

import android.content.Context
import android.content.SharedPreferences

object SettingsManager {
    private const val PREFS = "tictactoe_prefs"
    private const val DIFFICULTY = "difficulty" // easy, medium, or hard

    enum class Difficulty {
        EASY,
        MEDIUM,
        HARD
    }

    private fun prefs(ctx: Context): SharedPreferences =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)


    fun getDifficulty(ctx: Context): Difficulty = when (
        prefs(ctx).getString(DIFFICULTY, "EASY")
    ) {
        "MEDIUM" -> Difficulty.MEDIUM
        "HARD" -> Difficulty.HARD
        else -> Difficulty.EASY
    }

    fun setDifficulty(ctx: Context, diff: Difficulty) {
        val v = when (diff) {
            Difficulty.EASY -> "EASY"
            Difficulty.MEDIUM -> "MEDIUM"
            Difficulty.HARD -> "HARD"
        }
        prefs(ctx).edit().putString(DIFFICULTY, v).apply()
    }


}