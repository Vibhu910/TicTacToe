package com.example.tic_tac_toe

/**
 * Core game logic for Misere Tic-Tac-Toe
 * In Misere variant, getting 3 in a row means you LOSE
 */
class GameLogic {
    companion object {
        const val EMPTY = ' '
        const val PLAYER_X = 'X'
        const val PLAYER_O = 'O'
    }

    var board: Array<CharArray> = Array(3) { CharArray(3) { EMPTY } }
    var currentPlayer: Char = PLAYER_X
    var gameOver: Boolean = false
    var winner: Char = EMPTY
    var isDraw: Boolean = false
    var turnCount: Int = 0

    fun resetGame() {
        board = Array(3) { CharArray(3) { EMPTY } }
        currentPlayer = PLAYER_X
        gameOver = false
        winner = EMPTY
        isDraw = false
        turnCount = 0
    }

    fun makeMove(row: Int, col: Int): Boolean {
        if (gameOver || board[row][col] != EMPTY) {
            return false
        }

        board[row][col] = currentPlayer
        turnCount++
        
        checkGameStatus()
        
        if (!gameOver) {
            currentPlayer = if (currentPlayer == PLAYER_X) PLAYER_O else PLAYER_X
        }
        
        return true
    }

    fun checkGameStatus() {
        // Check for three in a row (which means that player LOSES in Misere)
        val losingPlayer = checkThreeInRow()
        
        if (losingPlayer != EMPTY) {
            gameOver = true
            // In Misere, the player who made three in a row LOSES
            winner = if (losingPlayer == PLAYER_X) PLAYER_O else PLAYER_X
        } else if (isBoardFull()) {
            gameOver = true
            isDraw = true
        }
    }

    private fun checkThreeInRow(): Char {
        // Check rows
        for (i in 0..2) {
            if (board[i][0] != EMPTY && 
                board[i][0] == board[i][1] && 
                board[i][1] == board[i][2]) {
                return board[i][0]
            }
        }

        // Check columns
        for (i in 0..2) {
            if (board[0][i] != EMPTY && 
                board[0][i] == board[1][i] && 
                board[1][i] == board[2][i]) {
                return board[0][i]
            }
        }

        // Check diagonals
        if (board[0][0] != EMPTY && 
            board[0][0] == board[1][1] && 
            board[1][1] == board[2][2]) {
            return board[0][0]
        }

        if (board[0][2] != EMPTY && 
            board[0][2] == board[1][1] && 
            board[1][1] == board[2][0]) {
            return board[0][2]
        }

        return EMPTY
    }

    private fun isBoardFull(): Boolean {
        for (i in 0..2) {
            for (j in 0..2) {
                if (board[i][j] == EMPTY) {
                    return false
                }
            }
        }
        return true
    }

    fun getAvailableMoves(): List<Pair<Int, Int>> {
        val moves = mutableListOf<Pair<Int, Int>>()
        for (i in 0..2) {
            for (j in 0..2) {
                if (board[i][j] == EMPTY) {
                    moves.add(Pair(i, j))
                }
            }
        }
        return moves
    }

    fun copyBoard(): Array<CharArray> {
        return Array(3) { i -> board[i].copyOf() }
    }

    fun setBoardState(newBoard: Array<CharArray>) {
        for (i in 0..2) {
            for (j in 0..2) {
                board[i][j] = newBoard[i][j]
            }
        }
    }
}

