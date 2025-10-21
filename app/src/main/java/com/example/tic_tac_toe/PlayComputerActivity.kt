package com.example.tic_tac_toe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tic_tac_toe.database.AppDatabase
import com.example.tic_tac_toe.database.GameHistory
import com.example.tic_tac_toe.ui.theme.TicTacToeTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ComputerMatchDisplay(
    exitToMenu: () -> Unit,
    recordGameResult: (winner: String, difficulty: String) -> Unit
) {
    val matchInstance = remember { GameLogic() }
    val aiEngine = remember { MinimaxAI() }
    var currentGridState by remember { mutableStateOf(matchInstance.duplicateGridState()) }
    var statusMessage by remember { mutableStateOf("Your turn! (You are X)") }
    var computerThinking by remember { mutableStateOf(false) }
    var playerCanAct by remember { mutableStateOf(true) }
    var selectedDifficulty by remember { mutableStateOf(SettingsManager.Difficulty.MEDIUM) }
    var displayDifficultyPicker by remember { mutableStateOf(false) }
    var resultSaved by remember { mutableStateOf(false) }
    
    val coroutineContext = rememberCoroutineScope()
    val appContext = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        selectedDifficulty = SettingsManager.retrieveDifficulty(appContext)
    }

    fun restartMatch() {
        matchInstance.initializeMatch()
        currentGridState = matchInstance.duplicateGridState()
        statusMessage = "Your turn! (You are X)"
        playerCanAct = true
        computerThinking = false
        resultSaved = false
    }

    fun executeComputerTurn() {
        if (matchInstance.matchEnded) return
        
        coroutineContext.launch {
            computerThinking = true
            playerCanAct = false
            statusMessage = "AI is thinking..."
            
            if (selectedDifficulty == SettingsManager.Difficulty.HARD) {
                delay(500)
            }
            
            val calculatedMove = aiEngine.determineAiMove(matchInstance, selectedDifficulty, GameLogic.PLAYER_O)
            
            if (calculatedMove != null) {
                matchInstance.executeMove(calculatedMove.first, calculatedMove.second)
                currentGridState = matchInstance.duplicateGridState()
                
                if (matchInstance.matchEnded) {
                    if (matchInstance.isStalemate) {
                        statusMessage = "It's a draw!"
                        if (!resultSaved) {
                            recordGameResult("Draw", selectedDifficulty.name)
                            resultSaved = true
                        }
                    } else {
                        statusMessage = if (matchInstance.victoriousPlayer == GameLogic.PLAYER_X) {
                            "You win! (O made three in a row)"
                        } else {
                            "AI wins! (You made three in a row)"
                        }
                        if (!resultSaved) {
                            recordGameResult(
                                if (matchInstance.victoriousPlayer == GameLogic.PLAYER_X) "X" else "O",
                                selectedDifficulty.name
                            )
                            resultSaved = true
                        }
                    }
                } else {
                    statusMessage = "Your turn! (You are X)"
                    playerCanAct = true
                }
            }
            
            computerThinking = false
        }
    }

    fun processCellSelection(rowIdx: Int, colIdx: Int) {
        if (!playerCanAct || computerThinking || matchInstance.matchEnded) return
        
        if (matchInstance.executeMove(rowIdx, colIdx)) {
            currentGridState = matchInstance.duplicateGridState()
            
            if (matchInstance.matchEnded) {
                if (matchInstance.isStalemate) {
                    statusMessage = "It's a draw!"
                    if (!resultSaved) {
                        recordGameResult("Draw", selectedDifficulty.name)
                        resultSaved = true
                    }
                } else {
                    statusMessage = if (matchInstance.victoriousPlayer == GameLogic.PLAYER_X) {
                        "You win! (O made three in a row)"
                    } else {
                        "AI wins! (You made three in a row)"
                    }
                    if (!resultSaved) {
                        recordGameResult(
                            if (matchInstance.victoriousPlayer == GameLogic.PLAYER_X) "X" else "O",
                            selectedDifficulty.name
                        )
                        resultSaved = true
                    }
                }
            } else {
                executeComputerTurn()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Misere Tic-Tac-Toe",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Getting 3 in a row = You LOSE!",
                fontSize = 14.sp,
                color = Color.Red,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Difficulty: ${selectedDifficulty.name}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Text(
            text = statusMessage,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = if (matchInstance.matchEnded) {
                if (matchInstance.isStalemate) Color.Gray
                else if (matchInstance.victoriousPlayer == GameLogic.PLAYER_X) Color(0xFF4CAF50)
                else Color(0xFFF44336)
            } else {
                Color.Black
            }
        )

        GameBoard(
            board = currentGridState,
            onCellClick = { rowIdx, colIdx -> processCellSelection(rowIdx, colIdx) },
            enabled = playerCanAct && !computerThinking && !matchInstance.matchEnded
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { displayDifficultyPicker = true },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(50.dp)
            ) {
                Text("Change Difficulty", fontSize = 16.sp)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = { restartMatch() },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Reset Game", fontSize = 16.sp)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = exitToMenu,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                )
            ) {
                Text("Back to Menu", fontSize = 16.sp)
            }
        }
    }

    if (displayDifficultyPicker) {
        AlertDialog(
            onDismissRequest = { displayDifficultyPicker = false },
            title = { Text("Select Difficulty") },
            text = {
                Column {
                    listOf(
                        SettingsManager.Difficulty.EASY to "Easy (Random moves)",
                        SettingsManager.Difficulty.MEDIUM to "Medium (50% random)",
                        SettingsManager.Difficulty.HARD to "Hard (Optimal play)"
                    ).forEach { (level, info) ->
                        Button(
                            onClick = {
                                selectedDifficulty = level
                                SettingsManager.updateDifficulty(appContext, level)
                                displayDifficultyPicker = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text("$info")
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { displayDifficultyPicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

class PlayComputerActivity : ComponentActivity() {
    private lateinit var databaseInstance: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        databaseInstance = AppDatabase.obtainDatabase(this)

        setContent {
            TicTacToeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ComputerMatchDisplay(
                        exitToMenu = { finish() },
                        recordGameResult = { winnerName, difficultyName ->
                            persistGameRecord(winnerName, difficultyName)
                        }
                    )
                }
            }
        }
    }

    private fun persistGameRecord(winnerName: String, difficultyName: String) {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            databaseInstance.gameHistoryDao().addGameRecord(
                GameHistory(
                    victoriousPlayer = winnerName,
                    challengeLevel = difficultyName,
                    playMode = "Computer"
                )
            )
        }
    }
}
