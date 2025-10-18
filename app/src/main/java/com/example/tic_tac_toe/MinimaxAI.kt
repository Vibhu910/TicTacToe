package com.example.tic_tac_toe

import kotlin.random.Random

/**
 * AI implementation using Minimax algorithm with alpha-beta pruning
 * Adapted for Misere Tic-Tac-Toe (getting 3 in a row = losing)
 */
class MinimaxAI {
    
    data class Move(val row: Int, val col: Int, val score: Int)

    /**
     * Get the best move for the AI based on difficulty
     * Easy: Random moves
     * Medium: 50% random, 50% optimal
     * Hard: Always optimal
     */
    fun getBestMove(
        game: GameLogic,
        difficulty: SettingsManager.Difficulty,
        aiPlayer: Char
    ): Pair<Int, Int>? {
        val availableMoves = game.getAvailableMoves()
        if (availableMoves.isEmpty()) return null

        return when (difficulty) {
            SettingsManager.Difficulty.EASY -> {
                // Random move
                availableMoves.random()
            }
            SettingsManager.Difficulty.MEDIUM -> {
                // 50% random, 50% optimal
                if (Random.nextBoolean()) {
                    availableMoves.random()
                } else {
                    getOptimalMove(game, aiPlayer)
                }
            }
            SettingsManager.Difficulty.HARD -> {
                // Always optimal
                getOptimalMove(game, aiPlayer)
            }
        }
    }

    private fun getOptimalMove(game: GameLogic, aiPlayer: Char): Pair<Int, Int>? {
        val opponent = if (aiPlayer == GameLogic.PLAYER_X) GameLogic.PLAYER_O else GameLogic.PLAYER_X
        var bestScore = Int.MIN_VALUE
        var bestMove: Pair<Int, Int>? = null

        val availableMoves = game.getAvailableMoves()
        
        for ((row, col) in availableMoves) {
            // Make temporary move
            val originalBoard = game.copyBoard()
            game.board[row][col] = aiPlayer
            
            val score = minimax(game, 0, false, aiPlayer, opponent, Int.MIN_VALUE, Int.MAX_VALUE)
            
            // Undo move
            game.setBoardState(originalBoard)
            
            if (score > bestScore) {
                bestScore = score
                bestMove = Pair(row, col)
            }
        }

        return bestMove
    }

    private fun minimax(
        game: GameLogic,
        depth: Int,
        isMaximizing: Boolean,
        aiPlayer: Char,
        opponent: Char,
        alpha: Int,
        beta: Int
    ): Int {
        // Check terminal states
        val evaluation = evaluateBoard(game, aiPlayer, opponent)
        if (evaluation != null) {
            return evaluation - depth // Prefer faster wins
        }

        var alphaVar = alpha
        var betaVar = beta

        if (isMaximizing) {
            var maxScore = Int.MIN_VALUE
            val moves = game.getAvailableMoves()
            
            for ((row, col) in moves) {
                val originalBoard = game.copyBoard()
                game.board[row][col] = aiPlayer
                
                val score = minimax(game, depth + 1, false, aiPlayer, opponent, alphaVar, betaVar)
                
                game.setBoardState(originalBoard)
                
                maxScore = maxOf(maxScore, score)
                alphaVar = maxOf(alphaVar, score)
                
                if (betaVar <= alphaVar) {
                    break // Beta cutoff
                }
            }
            return maxScore
        } else {
            var minScore = Int.MAX_VALUE
            val moves = game.getAvailableMoves()
            
            for ((row, col) in moves) {
                val originalBoard = game.copyBoard()
                game.board[row][col] = opponent
                
                val score = minimax(game, depth + 1, true, aiPlayer, opponent, alphaVar, betaVar)
                
                game.setBoardState(originalBoard)
                
                minScore = minOf(minScore, score)
                betaVar = minOf(betaVar, score)
                
                if (betaVar <= alphaVar) {
                    break // Alpha cutoff
                }
            }
            return minScore
        }
    }

    /**
     * Evaluate board state for Misere Tic-Tac-Toe
     * Returns: 10 if AI wins, -10 if AI loses, 0 if draw, null if game continues
     */
    private fun evaluateBoard(game: GameLogic, aiPlayer: Char, opponent: Char): Int? {
        val threeInRow = checkThreeInRow(game.board)
        
        if (threeInRow != GameLogic.EMPTY) {
            // In Misere, making three in a row means you LOSE
            return if (threeInRow == aiPlayer) {
                -10 // AI made three in a row, so AI loses
            } else {
                10 // Opponent made three in a row, so AI wins
            }
        }

        // Check for draw
        if (isBoardFull(game.board)) {
            return 0
        }

        return null // Game continues
    }

    private fun checkThreeInRow(board: Array<CharArray>): Char {
        // Check rows
        for (i in 0..2) {
            if (board[i][0] != GameLogic.EMPTY && 
                board[i][0] == board[i][1] && 
                board[i][1] == board[i][2]) {
                return board[i][0]
            }
        }

        // Check columns
        for (i in 0..2) {
            if (board[0][i] != GameLogic.EMPTY && 
                board[0][i] == board[1][i] && 
                board[1][i] == board[2][i]) {
                return board[0][i]
            }
        }

        // Check diagonals
        if (board[0][0] != GameLogic.EMPTY && 
            board[0][0] == board[1][1] && 
            board[1][1] == board[2][2]) {
            return board[0][0]
        }

        if (board[0][2] != GameLogic.EMPTY && 
            board[0][2] == board[1][1] && 
            board[1][1] == board[2][0]) {
            return board[0][2]
        }

        return GameLogic.EMPTY
    }

    private fun isBoardFull(board: Array<CharArray>): Boolean {
        for (i in 0..2) {
            for (j in 0..2) {
                if (board[i][j] == GameLogic.EMPTY) {
                    return false
                }
            }
        }
        return true
    }
}


