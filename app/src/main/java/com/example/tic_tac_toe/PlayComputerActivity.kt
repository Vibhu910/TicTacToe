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

class PlayComputerActivity : ComponentActivity() {
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = AppDatabase.getDatabase(this)

        setContent {
            TicTacToeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GameScreen(
                        onBack = { finish() },
                        onSaveGame = { winner, difficulty ->
                            saveGameToHistory(winner, difficulty)
                        }
                    )
                }
            }
        }
    }

    private fun saveGameToHistory(winner: String, difficulty: String) {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            database.gameHistoryDao().insertGame(
                GameHistory(
                    winner = winner,
                    difficultyMode = difficulty,
                    gameMode = "Computer"
                )
            )
        }
    }
}

@Composable
fun GameScreen(
    onBack: () -> Unit,
    onSaveGame: (winner: String, difficulty: String) -> Unit
) {
    val game = remember { GameLogic() }
    val ai = remember { MinimaxAI() }
    var boardState by remember { mutableStateOf(game.copyBoard()) }
    var gameMessage by remember { mutableStateOf("Your turn! (You are X)") }
    var isAiThinking by remember { mutableStateOf(false) }
    var canPlayerMove by remember { mutableStateOf(true) }
    var currentDifficulty by remember { mutableStateOf(SettingsManager.Difficulty.MEDIUM) }
    var showDifficultyMenu by remember { mutableStateOf(false) }
    var gameHasBeenSaved by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    // Load difficulty from settings
    LaunchedEffect(Unit) {
        currentDifficulty = SettingsManager.getDifficulty(context)
    }

    fun resetGame() {
        game.resetGame()
        boardState = game.copyBoard()
        gameMessage = "Your turn! (You are X)"
        canPlayerMove = true
        isAiThinking = false
        gameHasBeenSaved = false
    }

    fun makeAiMove() {
        if (game.gameOver) return
        
        scope.launch {
            isAiThinking = true
            canPlayerMove = false
            gameMessage = "AI is thinking..."
            
            // Add delay for Hard mode to show thinking message
            if (currentDifficulty == SettingsManager.Difficulty.HARD) {
                delay(500)
            }
            
            val move = ai.getBestMove(game, currentDifficulty, GameLogic.PLAYER_O)
            
            if (move != null) {
                game.makeMove(move.first, move.second)
                boardState = game.copyBoard()
                
                if (game.gameOver) {
                    if (game.isDraw) {
                        gameMessage = "It's a draw!"
                        if (!gameHasBeenSaved) {
                            onSaveGame("Draw", currentDifficulty.name)
                            gameHasBeenSaved = true
                        }
                    } else {
                        gameMessage = if (game.winner == GameLogic.PLAYER_X) {
                            "You win! (O made three in a row)"
                        } else {
                            "AI wins! (You made three in a row)"
                        }
                        if (!gameHasBeenSaved) {
                            onSaveGame(
                                if (game.winner == GameLogic.PLAYER_X) "X" else "O",
                                currentDifficulty.name
                            )
                            gameHasBeenSaved = true
                        }
                    }
                } else {
                    gameMessage = "Your turn! (You are X)"
                    canPlayerMove = true
                }
            }
            
            isAiThinking = false
        }
    }

    fun handleCellClick(row: Int, col: Int) {
        if (!canPlayerMove || isAiThinking || game.gameOver) return
        
        if (game.makeMove(row, col)) {
            boardState = game.copyBoard()
            
            if (game.gameOver) {
                if (game.isDraw) {
                    gameMessage = "It's a draw!"
                    if (!gameHasBeenSaved) {
                        onSaveGame("Draw", currentDifficulty.name)
                        gameHasBeenSaved = true
                    }
                } else {
                    gameMessage = if (game.winner == GameLogic.PLAYER_X) {
                        "You win! (O made three in a row)"
                    } else {
                        "AI wins! (You made three in a row)"
                    }
                    if (!gameHasBeenSaved) {
                        onSaveGame(
                            if (game.winner == GameLogic.PLAYER_X) "X" else "O",
                            currentDifficulty.name
                        )
                        gameHasBeenSaved = true
                    }
                }
            } else {
                makeAiMove()
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
        // Header
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
                text = "Difficulty: ${currentDifficulty.name}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Game message
        Text(
            text = gameMessage,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = if (game.gameOver) {
                if (game.isDraw) Color.Gray
                else if (game.winner == GameLogic.PLAYER_X) Color(0xFF4CAF50)
                else Color(0xFFF44336)
            } else {
                Color.Black
            }
        )

        // Game board
        GameBoard(
            board = boardState,
            onCellClick = { row, col -> handleCellClick(row, col) },
            enabled = canPlayerMove && !isAiThinking && !game.gameOver
        )

        // Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { showDifficultyMenu = true },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(50.dp)
            ) {
                Text("Change Difficulty", fontSize = 16.sp)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = { resetGame() },
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
                onClick = onBack,
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

    // Difficulty selection dialog
    if (showDifficultyMenu) {
        AlertDialog(
            onDismissRequest = { showDifficultyMenu = false },
            title = { Text("Select Difficulty") },
            text = {
                Column {
                    listOf(
                        SettingsManager.Difficulty.EASY to "Easy (Random moves)",
                        SettingsManager.Difficulty.MEDIUM to "Medium (50% random)",
                        SettingsManager.Difficulty.HARD to "Hard (Optimal play)"
                    ).forEach { (difficulty, description) ->
                        Button(
                            onClick = {
                                currentDifficulty = difficulty
                                SettingsManager.setDifficulty(context, difficulty)
                                showDifficultyMenu = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text("$description")
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showDifficultyMenu = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
