package com.example.tic_tac_toe

import com.google.gson.Gson

/**
 * Data classes for JSON serialization of game state
 */
data class GameStateMessage(
    val gameState: GameStateData,
    val metadata: MetadataData
) {
    fun toJson(): String = Gson().toJson(this)
    
    companion object {
        fun fromJson(json: String): GameStateMessage? {
            return try {
                Gson().fromJson(json, GameStateMessage::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }
}

data class GameStateData(
    val board: List<List<String>>,
    val turn: Int,
    val winner: String,
    val draw: Boolean,
    val connectionEstablished: Boolean,
    val reset: Boolean
)

data class MetadataData(
    val choices: List<PlayerChoice>,
    val miniGame: MiniGameData
)

data class PlayerChoice(
    val id: String,
    val name: String
)

data class MiniGameData(
    val player1Choice: String,
    val player2Choice: String
)

/**
 * Helper functions for game state conversion
 */
object GameStateConverter {
    fun boardToList(board: Array<CharArray>): List<List<String>> {
        return board.map { row ->
            row.map { char ->
                when (char) {
                    GameLogic.PLAYER_X -> "X"
                    GameLogic.PLAYER_O -> "O"
                    else -> " "
                }
            }
        }
    }

    fun listToBoard(list: List<List<String>>): Array<CharArray> {
        return Array(3) { i ->
            CharArray(3) { j ->
                when (list[i][j]) {
                    "X" -> GameLogic.PLAYER_X
                    "O" -> GameLogic.PLAYER_O
                    else -> GameLogic.EMPTY
                }
            }
        }
    }

    fun createInitialGameState(deviceId: String): GameStateMessage {
        return GameStateMessage(
            gameState = GameStateData(
                board = List(3) { List(3) { " " } },
                turn = 0,
                winner = " ",
                draw = false,
                connectionEstablished = true,
                reset = false
            ),
            metadata = MetadataData(
                choices = listOf(
                    PlayerChoice("player1", deviceId),
                    PlayerChoice("player2", "")
                ),
                miniGame = MiniGameData(
                    player1Choice = "",
                    player2Choice = ""
                )
            )
        )
    }
}


