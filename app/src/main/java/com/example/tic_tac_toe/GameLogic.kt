package com.example.tic_tac_toe

class GameLogic {
    companion object {
        const val EMPTY = ' '
        const val PLAYER_X = 'X'
        const val PLAYER_O = 'O'
    }

    var gridState: Array<CharArray> = Array(3) { CharArray(3) { EMPTY } }
    var activePlayer: Char = PLAYER_X
    var matchEnded: Boolean = false
    var victoriousPlayer: Char = EMPTY
    var isStalemate: Boolean = false
    var moveCounter: Int = 0

    fun duplicateGridState(): Array<CharArray> {
        return Array(3) { idx -> gridState[idx].copyOf() }
    }

    fun updateGridState(newGrid: Array<CharArray>) {
        for (rowIdx in 0..2) {
            for (colIdx in 0..2) {
                gridState[rowIdx][colIdx] = newGrid[rowIdx][colIdx]
            }
        }
    }

    fun collectAvailableMoves(): List<Pair<Int, Int>> {
        val availablePositions = mutableListOf<Pair<Int, Int>>()
        for (rowIdx in 0..2) {
            for (colIdx in 0..2) {
                if (gridState[rowIdx][colIdx] == EMPTY) {
                    availablePositions.add(Pair(rowIdx, colIdx))
                }
            }
        }
        return availablePositions
    }

    private fun detectGridFull(): Boolean {
        for (rowIdx in 0..2) {
            for (colIdx in 0..2) {
                if (gridState[rowIdx][colIdx] == EMPTY) {
                    return false
                }
            }
        }
        return true
    }

    private fun detectThreeConsecutive(): Char {
        for (rowIdx in 0..2) {
            if (gridState[rowIdx][0] != EMPTY && 
                gridState[rowIdx][0] == gridState[rowIdx][1] && 
                gridState[rowIdx][1] == gridState[rowIdx][2]) {
                return gridState[rowIdx][0]
            }
        }

        for (colIdx in 0..2) {
            if (gridState[0][colIdx] != EMPTY && 
                gridState[0][colIdx] == gridState[1][colIdx] && 
                gridState[1][colIdx] == gridState[2][colIdx]) {
                return gridState[0][colIdx]
            }
        }

        if (gridState[0][0] != EMPTY && 
            gridState[0][0] == gridState[1][1] && 
            gridState[1][1] == gridState[2][2]) {
            return gridState[0][0]
        }

        if (gridState[0][2] != EMPTY && 
            gridState[0][2] == gridState[1][1] && 
            gridState[1][1] == gridState[2][0]) {
            return gridState[0][2]
        }

        return EMPTY
    }

    fun evaluateMatchStatus() {
        val losingMarker = detectThreeConsecutive()
        
        if (losingMarker != EMPTY) {
            matchEnded = true
            victoriousPlayer = if (losingMarker == PLAYER_X) PLAYER_O else PLAYER_X
        } else if (detectGridFull()) {
            matchEnded = true
            isStalemate = true
        }
    }

    fun executeMove(rowIdx: Int, colIdx: Int): Boolean {
        if (matchEnded || gridState[rowIdx][colIdx] != EMPTY) {
            return false
        }

        gridState[rowIdx][colIdx] = activePlayer
        moveCounter++
        
        evaluateMatchStatus()
        
        if (!matchEnded) {
            activePlayer = if (activePlayer == PLAYER_X) PLAYER_O else PLAYER_X
        }
        
        return true
    }
    
    fun executeMoveWithSymbol(rowIdx: Int, colIdx: Int, playerSymbol: Char): Boolean {
        if (matchEnded || gridState[rowIdx][colIdx] != EMPTY) {
            return false
        }

        gridState[rowIdx][colIdx] = playerSymbol
        moveCounter++
        
        evaluateMatchStatus()
        
        if (!matchEnded) {
            activePlayer = if (activePlayer == PLAYER_X) PLAYER_O else PLAYER_X
        }
        
        return true
    }

    fun initializeMatch() {
        gridState = Array(3) { CharArray(3) { EMPTY } }
        activePlayer = PLAYER_X
        matchEnded = false
        victoriousPlayer = EMPTY
        isStalemate = false
        moveCounter = 0
    }
    
    fun setPlayerSymbol(playerSymbol: Char) {
        activePlayer = playerSymbol
    }
    
    fun getNextPlayer(): Char {
        return if (activePlayer == PLAYER_X) PLAYER_O else PLAYER_X
    }
}
