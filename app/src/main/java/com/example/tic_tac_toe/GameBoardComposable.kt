package com.example.tic_tac_toe

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GameCell(
    symbol: Char,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .padding(4.dp)
            .border(3.dp, Color.Black)
            .background(Color.White)
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = when (symbol) {
                GameLogic.PLAYER_X -> "X"
                GameLogic.PLAYER_O -> "O"
                else -> ""
            },
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = when (symbol) {
                GameLogic.PLAYER_X -> Color(0xFF2196F3) // Blue for X
                GameLogic.PLAYER_O -> Color(0xFFF44336) // Red for O
                else -> Color.Transparent
            }
        )
    }
}

@Composable
fun GameBoard(
    board: Array<CharArray>,
    onCellClick: (row: Int, col: Int) -> Unit,
    enabled: Boolean = true
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        for (rowIdx in 0..2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                for (colIdx in 0..2) {
                    GameCell(
                        symbol = board[rowIdx][colIdx],
                        onClick = { if (enabled) onCellClick(rowIdx, colIdx) },
                        enabled = enabled
                    )
                }
            }
        }
    }
}


