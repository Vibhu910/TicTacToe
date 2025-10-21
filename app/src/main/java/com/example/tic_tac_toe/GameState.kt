package com.example.tic_tac_toe

import com.google.gson.Gson

data class GameStateMessage(
    val gameState: GameStateData,
    val metadata: MetadataData
) {
    companion object {
        fun fromJson(json: String): GameStateMessage? {
            return try {
                Gson().fromJson(json, GameStateMessage::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }
    
    fun toJson(): String = Gson().toJson(this)
}

data class MiniGameData(
    val player1Choice: String,
    val player2Choice: String
)

data class PlayerChoice(
    val id: String,
    val name: String
)

data class MetadataData(
    val choices: List<PlayerChoice>,
    val miniGame: MiniGameData
)

data class GameStateData(
    val board: List<List<String>>,
    val turn: Int,
    val winner: String,
    val draw: Boolean,
    val connectionEstablished: Boolean,
    val reset: Boolean
)

object GameStateConverter {
    fun gridToNestedList(gridState: Array<CharArray>): List<List<String>> {
        return gridState.map { rowArray ->
            rowArray.map { cellChar ->
                when (cellChar) {
                    GameLogic.PLAYER_X -> "X"
                    GameLogic.PLAYER_O -> "O"
                    else -> " "
                }
            }
        }
    }

    fun buildInitialState(deviceIdentifier: String): GameStateMessage {
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
                    PlayerChoice("player1", deviceIdentifier),
                    PlayerChoice("player2", "")
                ),
                miniGame = MiniGameData(
                    player1Choice = "",
                    player2Choice = ""
                )
            )
        )
    }

    fun nestedListToGrid(nestedList: List<List<String>>): Array<CharArray> {
        return Array(3) { rowIdx ->
            CharArray(3) { colIdx ->
                when (nestedList[rowIdx][colIdx]) {
                    "X" -> GameLogic.PLAYER_X
                    "O" -> GameLogic.PLAYER_O
                    else -> GameLogic.EMPTY
                }
            }
        }
    }
}
