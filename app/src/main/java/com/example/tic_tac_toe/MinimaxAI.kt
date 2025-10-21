package com.example.tic_tac_toe

import kotlin.random.Random

/**
 * AI implementation using Minimax algorithm with alpha-beta pruning
 * Adapted for Misere Tic-Tac-Toe (getting 3 in a row = losing)
 */
class MinimaxAI {
    
    data class Move(val row: Int, val col: Int, val score: Int)

    private fun detectGridFull(gridState: Array<CharArray>): Boolean {
        for (rowIdx in 0..2) {
            for (colIdx in 0..2) {
                if (gridState[rowIdx][colIdx] == GameLogic.EMPTY) {
                    return false
                }
            }
        }
        return true
    }

    private fun detectThreeConsecutive(gridState: Array<CharArray>): Char {
        // Check rows
        for (rowIdx in 0..2) {
            if (gridState[rowIdx][0] != GameLogic.EMPTY && 
                gridState[rowIdx][0] == gridState[rowIdx][1] && 
                gridState[rowIdx][1] == gridState[rowIdx][2]) {
                return gridState[rowIdx][0]
            }
        }

        // Check columns
        for (colIdx in 0..2) {
            if (gridState[0][colIdx] != GameLogic.EMPTY && 
                gridState[0][colIdx] == gridState[1][colIdx] && 
                gridState[1][colIdx] == gridState[2][colIdx]) {
                return gridState[0][colIdx]
            }
        }

        // Check diagonals
        if (gridState[0][0] != GameLogic.EMPTY && 
            gridState[0][0] == gridState[1][1] && 
            gridState[1][1] == gridState[2][2]) {
            return gridState[0][0]
        }

        if (gridState[0][2] != GameLogic.EMPTY && 
            gridState[0][2] == gridState[1][1] && 
            gridState[1][1] == gridState[2][0]) {
            return gridState[0][2]
        }

        return GameLogic.EMPTY
    }

    /**
     * Evaluate board state for Misere Tic-Tac-Toe
     * Returns: 10 if AI wins, -10 if AI loses, 0 if draw, null if game continues
     */
    private fun calculateBoardScore(gameInstance: GameLogic, computerMarker: Char, humanMarker: Char): Int? {
        val consecutiveMarker = detectThreeConsecutive(gameInstance.gridState)
        
        if (consecutiveMarker != GameLogic.EMPTY) {
            // In Misere, making three in a row means you LOSE
            return if (consecutiveMarker == computerMarker) {
                -10 // AI made three in a row, so AI loses
            } else {
                10 // Opponent made three in a row, so AI wins
            }
        }

        // Check for draw
        if (detectGridFull(gameInstance.gridState)) {
            return 0
        }

        return null // Game continues
    }

    private fun runMinimaxAlgorithm(
        gameInstance: GameLogic,
        recursionDepth: Int,
        maximizingTurn: Boolean,
        computerMarker: Char,
        humanMarker: Char,
        alphaValue: Int,
        betaValue: Int
    ): Int {
        // Check terminal states
        val scoreEvaluation = calculateBoardScore(gameInstance, computerMarker, humanMarker)
        if (scoreEvaluation != null) {
            return scoreEvaluation - recursionDepth // Prefer faster wins
        }

        var currentAlpha = alphaValue
        var currentBeta = betaValue

        if (maximizingTurn) {
            var maximumScore = Int.MIN_VALUE
            val possibleMoves = gameInstance.collectAvailableMoves()
            
            for ((rowPosition, colPosition) in possibleMoves) {
                val savedGrid = gameInstance.duplicateGridState()
                gameInstance.gridState[rowPosition][colPosition] = computerMarker
                
                val evaluatedScore = runMinimaxAlgorithm(gameInstance, recursionDepth + 1, false, computerMarker, humanMarker, currentAlpha, currentBeta)
                
                gameInstance.updateGridState(savedGrid)
                
                maximumScore = maxOf(maximumScore, evaluatedScore)
                currentAlpha = maxOf(currentAlpha, evaluatedScore)
                
                if (currentBeta <= currentAlpha) {
                    break // Beta cutoff
                }
            }
            return maximumScore
        } else {
            var minimumScore = Int.MAX_VALUE
            val possibleMoves = gameInstance.collectAvailableMoves()
            
            for ((rowPosition, colPosition) in possibleMoves) {
                val savedGrid = gameInstance.duplicateGridState()
                gameInstance.gridState[rowPosition][colPosition] = humanMarker
                
                val evaluatedScore = runMinimaxAlgorithm(gameInstance, recursionDepth + 1, true, computerMarker, humanMarker, currentAlpha, currentBeta)
                
                gameInstance.updateGridState(savedGrid)
                
                minimumScore = minOf(minimumScore, evaluatedScore)
                currentBeta = minOf(currentBeta, evaluatedScore)
                
                if (currentBeta <= currentAlpha) {
                    break // Alpha cutoff
                }
            }
            return minimumScore
        }
    }

    private fun findOptimalMove(gameInstance: GameLogic, computerMarker: Char): Pair<Int, Int>? {
        val humanMarker = if (computerMarker == GameLogic.PLAYER_X) GameLogic.PLAYER_O else GameLogic.PLAYER_X
        var topScore = Int.MIN_VALUE
        var selectedMove: Pair<Int, Int>? = null

        val possibleMoves = gameInstance.collectAvailableMoves()
        
        for ((rowPosition, colPosition) in possibleMoves) {
            // Make temporary move
            val savedGrid = gameInstance.duplicateGridState()
            gameInstance.gridState[rowPosition][colPosition] = computerMarker
            
            val evaluatedScore = runMinimaxAlgorithm(gameInstance, 0, false, computerMarker, humanMarker, Int.MIN_VALUE, Int.MAX_VALUE)
            
            // Undo move
            gameInstance.updateGridState(savedGrid)
            
            if (evaluatedScore > topScore) {
                topScore = evaluatedScore
                selectedMove = Pair(rowPosition, colPosition)
            }
        }

        return selectedMove
    }

    /**
     * Get the best move for the AI based on difficulty
     * Easy: Random moves
     * Medium: 50% random, 50% optimal
     * Hard: Always optimal
     */
    fun determineAiMove(
        gameInstance: GameLogic,
        challengeLevel: SettingsManager.Difficulty,
        computerMarker: Char
    ): Pair<Int, Int>? {
        val possibleMoves = gameInstance.collectAvailableMoves()
        if (possibleMoves.isEmpty()) return null

        return when (challengeLevel) {
            SettingsManager.Difficulty.EASY -> {
                // Random move
                possibleMoves.random()
            }
            SettingsManager.Difficulty.MEDIUM -> {
                // 50% random, 50% optimal
                if (Random.nextBoolean()) {
                    possibleMoves.random()
                } else {
                    findOptimalMove(gameInstance, computerMarker)
                }
            }
            SettingsManager.Difficulty.HARD -> {
                // Always optimal
                findOptimalMove(gameInstance, computerMarker)
            }
        }
    }
}


