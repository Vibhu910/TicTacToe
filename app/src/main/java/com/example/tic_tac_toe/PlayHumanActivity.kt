package com.example.tic_tac_toe

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.example.tic_tac_toe.database.AppDatabase
import com.example.tic_tac_toe.database.GameHistory
import com.example.tic_tac_toe.ui.theme.TicTacToeTheme
import kotlinx.coroutines.launch

class PlayHumanActivity : ComponentActivity() {
    private lateinit var database: AppDatabase
    private lateinit var bluetoothManager: BluetoothManager

    private val requestBluetoothPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Handle permissions result
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = AppDatabase.getDatabase(this)
        bluetoothManager = BluetoothManager(this)

        // Request Bluetooth permissions for Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestBluetoothPermissions.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
                )
            )
        }

        setContent {
            TicTacToeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HumanGameScreen(
                        bluetoothManager = bluetoothManager,
                        onBack = { finish() },
                        onSaveGame = { winner ->
                            saveGameToHistory(winner)
                        }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothManager.closeConnection()
    }

    private fun saveGameToHistory(winner: String) {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            database.gameHistoryDao().insertGame(
                GameHistory(
                    winner = winner,
                    difficultyMode = "N/A",
                    gameMode = "Human"
                )
            )
        }
    }
}

@Composable
fun HumanGameScreen(
    bluetoothManager: BluetoothManager,
    onBack: () -> Unit,
    onSaveGame: (winner: String) -> Unit
) {
    var gameMode by remember { mutableStateOf<GameMode>(GameMode.SelectMode) }
    var isConnected by remember { mutableStateOf(false) }
    var connectionMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        bluetoothManager.onConnectionEstablished = {
            isConnected = true
            connectionMessage = "Connected!"
        }
        bluetoothManager.onConnectionLost = {
            isConnected = false
            connectionMessage = "Connection lost"
            gameMode = GameMode.SelectMode
        }
    }

    when (gameMode) {
        GameMode.SelectMode -> {
            ModeSelectionScreen(
                onLocalPlay = { gameMode = GameMode.LocalPlay },
                onNetworkPlay = { gameMode = GameMode.DeviceSelection },
                onBack = onBack
            )
        }
        GameMode.LocalPlay -> {
            LocalGameScreen(
                onBack = { gameMode = GameMode.SelectMode },
                onSaveGame = onSaveGame
            )
        }
        GameMode.DeviceSelection -> {
            DeviceSelectionScreen(
                bluetoothManager = bluetoothManager,
                onDeviceSelected = {
                    gameMode = GameMode.NetworkPlay
                },
                onStartServer = {
                    bluetoothManager.startServer()
                    gameMode = GameMode.NetworkPlay
                },
                onBack = { gameMode = GameMode.SelectMode }
            )
        }
        GameMode.NetworkPlay -> {
            NetworkGameScreen(
                bluetoothManager = bluetoothManager,
                isConnected = isConnected,
                onBack = {
                    bluetoothManager.closeConnection()
                    gameMode = GameMode.SelectMode
                },
                onSaveGame = onSaveGame
            )
        }
    }
}

enum class GameMode {
    SelectMode,
    LocalPlay,
    DeviceSelection,
    NetworkPlay
}

@Composable
fun ModeSelectionScreen(
    onLocalPlay: () -> Unit,
    onNetworkPlay: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Play vs Human",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onLocalPlay,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(60.dp)
        ) {
            Text("Play on Same Device", fontSize = 18.sp)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onNetworkPlay,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(60.dp)
        ) {
            Text("Play on Different Devices", fontSize = 18.sp)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Text("Back", fontSize = 16.sp)
        }
    }
}

@Composable
fun LocalGameScreen(
    onBack: () -> Unit,
    onSaveGame: (winner: String) -> Unit
) {
    val game = remember { GameLogic() }
    var boardState by remember { mutableStateOf(game.copyBoard()) }
    var gameMessage by remember { mutableStateOf("Player X's turn") }
    var gameHasBeenSaved by remember { mutableStateOf(false) }

    fun handleCellClick(row: Int, col: Int) {
        if (game.gameOver) return
        
        if (game.makeMove(row, col)) {
            boardState = game.copyBoard()
            
            if (game.gameOver) {
                if (game.isDraw) {
                    gameMessage = "It's a draw!"
                    if (!gameHasBeenSaved) {
                        onSaveGame("Draw")
                        gameHasBeenSaved = true
                    }
                } else {
                    gameMessage = "Player ${game.winner} wins! (Player ${if (game.winner == GameLogic.PLAYER_X) 'O' else 'X'} made three in a row)"
                    if (!gameHasBeenSaved) {
                        onSaveGame(game.winner.toString())
                        gameHasBeenSaved = true
                    }
                }
            } else {
                gameMessage = "Player ${game.currentPlayer}'s turn"
            }
        }
    }

    fun resetGame() {
        game.resetGame()
        boardState = game.copyBoard()
        gameMessage = "Player X's turn"
        gameHasBeenSaved = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
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
                text = "Local Play (Same Device)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Text(
            text = gameMessage,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = if (game.gameOver) {
                if (game.isDraw) Color.Gray else Color(0xFF4CAF50)
            } else Color.Black
        )

        GameBoard(
            board = boardState,
            onCellClick = { row, col -> handleCellClick(row, col) },
            enabled = !game.gameOver
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { resetGame() },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(50.dp)
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
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Back", fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun DeviceSelectionScreen(
    bluetoothManager: BluetoothManager,
    onDeviceSelected: () -> Unit,
    onStartServer: () -> Unit,
    onBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var devices by remember { mutableStateOf<List<BluetoothDevice>>(emptyList()) }
    var message by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        if (!bluetoothManager.isBluetoothAvailable()) {
            message = "Bluetooth not available on this device"
        } else if (!bluetoothManager.isBluetoothEnabled()) {
            message = "Please enable Bluetooth"
        } else {
            devices = bluetoothManager.getPairedDevices()
            if (devices.isEmpty()) {
                message = "No paired devices found. Please pair a device first."
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Select Device",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                bluetoothManager.startServer()
                onStartServer()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            Text("Wait for Connection (Host)", fontSize = 16.sp)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Or connect to a paired device:",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        if (message.isNotEmpty()) {
            Text(
                text = message,
                fontSize = 14.sp,
                color = Color.Red,
                modifier = Modifier.padding(8.dp)
            )
        }

        if (devices.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(devices) { device ->
                    Button(
                        onClick = {
                            bluetoothManager.connectToDevice(device)
                            onDeviceSelected()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            if (ActivityCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.BLUETOOTH_CONNECT
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                Text(device.name ?: "Unknown Device")
                            } else {
                                Text("Unknown Device")
                            }
                        } else {
                            Text(device.name ?: "Unknown Device")
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Text("Back", fontSize = 16.sp)
        }
    }
}

@Composable
fun NetworkGameScreen(
    bluetoothManager: BluetoothManager,
    isConnected: Boolean,
    onBack: () -> Unit,
    onSaveGame: (winner: String) -> Unit
) {
    val game = remember { GameLogic() }
    var boardState by remember { mutableStateOf(game.copyBoard()) }
    var gameMessage by remember { mutableStateOf("Waiting for connection...") }
    var canMove by remember { mutableStateOf(false) }
    var mySymbol by remember { mutableStateOf<Char?>(null) }
    var showWhoGoesFirstDialog by remember { mutableStateOf(false) }
    var gameHasBeenSaved by remember { mutableStateOf(false) }
    var deviceId by remember { mutableStateOf("") }
    var opponentId by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        deviceId = bluetoothManager.getDeviceId()
    }

    LaunchedEffect(isConnected) {
        if (isConnected) {
            showWhoGoesFirstDialog = true
            gameMessage = "Connected! Choose who goes first"
        }
    }

    bluetoothManager.onMessageReceived = { message ->
        scope.launch {
            val gameState = GameStateMessage.fromJson(message)
            if (gameState != null) {
                // Handle who goes first
                if (gameState.metadata.miniGame.player1Choice.isNotEmpty() && 
                    gameState.metadata.miniGame.player2Choice.isEmpty()) {
                    // Opponent chose who goes first, acknowledge it
                    val response = gameState.copy(
                        metadata = gameState.metadata.copy(
                            miniGame = MiniGameData(
                                player1Choice = gameState.metadata.miniGame.player1Choice,
                                player2Choice = gameState.metadata.miniGame.player1Choice
                            )
                        )
                    )
                    bluetoothManager.sendMessage(response.toJson())
                    
                    // Determine our symbol and who moves first
                    if (gameState.metadata.miniGame.player1Choice == deviceId) {
                        mySymbol = GameLogic.PLAYER_X
                        canMove = true
                        gameMessage = "You go first! (You are X)"
                    } else {
                        mySymbol = GameLogic.PLAYER_O
                        canMove = false
                        gameMessage = "Opponent goes first (You are O)"
                    }
                    showWhoGoesFirstDialog = false
                } else if (gameState.metadata.miniGame.player1Choice.isNotEmpty() && 
                           gameState.metadata.miniGame.player2Choice.isNotEmpty()) {
                    // Both players agreed on who goes first
                    if (gameState.metadata.miniGame.player1Choice == deviceId) {
                        mySymbol = GameLogic.PLAYER_X
                        canMove = true
                        gameMessage = "You go first! (You are X)"
                    } else {
                        mySymbol = GameLogic.PLAYER_O
                        canMove = false
                        gameMessage = "Opponent goes first (You are O)"
                    }
                    showWhoGoesFirstDialog = false
                }
                
                // Handle game state updates
                if (gameState.gameState.reset) {
                    game.resetGame()
                    boardState = game.copyBoard()
                    gameMessage = if (mySymbol == GameLogic.PLAYER_X) "Your turn!" else "Opponent's turn"
                    canMove = mySymbol == GameLogic.PLAYER_X
                    gameHasBeenSaved = false
                } else {
                    // Update board
                    game.setBoardState(GameStateConverter.listToBoard(gameState.gameState.board))
                    game.turnCount = gameState.gameState.turn
                    boardState = game.copyBoard()
                    
                    // Check game status
                    game.checkGameStatus()
                    
                    if (gameState.gameState.draw || gameState.gameState.winner.isNotEmpty()) {
                        game.gameOver = true
                        if (gameState.gameState.draw) {
                            gameMessage = "It's a draw!"
                            if (!gameHasBeenSaved) {
                                onSaveGame("Draw")
                                gameHasBeenSaved = true
                            }
                        } else {
                            val winnerSymbol = gameState.gameState.winner
                            gameMessage = if (winnerSymbol == deviceId) {
                                "You win!"
                            } else {
                                "Opponent wins!"
                            }
                            if (!gameHasBeenSaved) {
                                val winChar = if (winnerSymbol == deviceId) mySymbol.toString() else {
                                    if (mySymbol == GameLogic.PLAYER_X) "O" else "X"
                                }
                                onSaveGame(winChar)
                                gameHasBeenSaved = true
                            }
                        }
                    } else {
                        // It's our turn now
                        canMove = true
                        gameMessage = "Your turn!"
                    }
                }
            }
        }
    }

    fun handleCellClick(row: Int, col: Int) {
        if (!canMove || game.gameOver || mySymbol == null) return
        
        if (game.makeMove(row, col)) {
            boardState = game.copyBoard()
            canMove = false
            
            // Check if game is over
            if (game.gameOver) {
                if (game.isDraw) {
                    gameMessage = "It's a draw!"
                    if (!gameHasBeenSaved) {
                        onSaveGame("Draw")
                        gameHasBeenSaved = true
                    }
                } else {
                    gameMessage = if (game.winner == mySymbol) {
                        "You win! (Opponent made three in a row)"
                    } else {
                        "Opponent wins! (You made three in a row)"
                    }
                    if (!gameHasBeenSaved) {
                        onSaveGame(game.winner.toString())
                        gameHasBeenSaved = true
                    }
                }
                
                // Send game over state
                val message = GameStateMessage(
                    gameState = GameStateData(
                        board = GameStateConverter.boardToList(game.board),
                        turn = game.turnCount,
                        winner = if (game.winner == mySymbol) deviceId else opponentId,
                        draw = game.isDraw,
                        connectionEstablished = true,
                        reset = false
                    ),
                    metadata = MetadataData(
                        choices = listOf(
                            PlayerChoice("player1", deviceId),
                            PlayerChoice("player2", opponentId)
                        ),
                        miniGame = MiniGameData(
                            player1Choice = if (mySymbol == GameLogic.PLAYER_X) deviceId else opponentId,
                            player2Choice = if (mySymbol == GameLogic.PLAYER_X) deviceId else opponentId
                        )
                    )
                )
                bluetoothManager.sendMessage(message.toJson())
            } else {
                gameMessage = "Opponent's turn"
                
                // Send move to opponent
                val message = GameStateMessage(
                    gameState = GameStateData(
                        board = GameStateConverter.boardToList(game.board),
                        turn = game.turnCount,
                        winner = "",
                        draw = false,
                        connectionEstablished = true,
                        reset = false
                    ),
                    metadata = MetadataData(
                        choices = listOf(
                            PlayerChoice("player1", deviceId),
                            PlayerChoice("player2", opponentId)
                        ),
                        miniGame = MiniGameData(
                            player1Choice = if (mySymbol == GameLogic.PLAYER_X) deviceId else opponentId,
                            player2Choice = if (mySymbol == GameLogic.PLAYER_X) deviceId else opponentId
                        )
                    )
                )
                bluetoothManager.sendMessage(message.toJson())
            }
        }
    }

    fun resetGame() {
        game.resetGame()
        boardState = game.copyBoard()
        gameHasBeenSaved = false
        canMove = mySymbol == GameLogic.PLAYER_X
        gameMessage = if (canMove) "Your turn!" else "Opponent's turn"
        
        // Send reset message
        val message = GameStateMessage(
            gameState = GameStateData(
                board = List(3) { List(3) { " " } },
                turn = 0,
                winner = "",
                draw = false,
                connectionEstablished = true,
                reset = true
            ),
            metadata = MetadataData(
                choices = listOf(
                    PlayerChoice("player1", deviceId),
                    PlayerChoice("player2", opponentId)
                ),
                miniGame = MiniGameData(
                    player1Choice = if (mySymbol == GameLogic.PLAYER_X) deviceId else opponentId,
                    player2Choice = if (mySymbol == GameLogic.PLAYER_X) deviceId else opponentId
                )
            )
        )
        bluetoothManager.sendMessage(message.toJson())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
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
            if (mySymbol != null) {
                Text(
                    text = "You are: $mySymbol",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Text(
            text = gameMessage,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = if (game.gameOver) Color(0xFF4CAF50) else Color.Black
        )

        GameBoard(
            board = boardState,
            onCellClick = { row, col -> handleCellClick(row, col) },
            enabled = canMove && !game.gameOver && isConnected
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { resetGame() },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(50.dp),
                enabled = isConnected
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
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Back", fontSize = 16.sp)
            }
        }
    }

    // Who goes first dialog
    if (showWhoGoesFirstDialog && isConnected) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Who Goes First?") },
            text = {
                Column {
                    Text("Choose who makes the first move:")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            mySymbol = GameLogic.PLAYER_X
                            canMove = true
                            opponentId = "opponent"
                            gameMessage = "You go first! (You are X)"
                            showWhoGoesFirstDialog = false
                            
                            val message = GameStateMessage(
                                gameState = GameStateData(
                                    board = List(3) { List(3) { " " } },
                                    turn = 0,
                                    winner = "",
                                    draw = false,
                                    connectionEstablished = true,
                                    reset = false
                                ),
                                metadata = MetadataData(
                                    choices = listOf(
                                        PlayerChoice("player1", deviceId),
                                        PlayerChoice("player2", "opponent")
                                    ),
                                    miniGame = MiniGameData(
                                        player1Choice = deviceId,
                                        player2Choice = ""
                                    )
                                )
                            )
                            bluetoothManager.sendMessage(message.toJson())
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("ME")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            mySymbol = GameLogic.PLAYER_O
                            canMove = false
                            opponentId = "opponent"
                            gameMessage = "Opponent goes first (You are O)"
                            showWhoGoesFirstDialog = false
                            
                            val message = GameStateMessage(
                                gameState = GameStateData(
                                    board = List(3) { List(3) { " " } },
                                    turn = 0,
                                    winner = "",
                                    draw = false,
                                    connectionEstablished = true,
                                    reset = false
                                ),
                                metadata = MetadataData(
                                    choices = listOf(
                                        PlayerChoice("player1", deviceId),
                                        PlayerChoice("player2", "opponent")
                                    ),
                                    miniGame = MiniGameData(
                                        player1Choice = "opponent",
                                        player2Choice = ""
                                    )
                                )
                            )
                            bluetoothManager.sendMessage(message.toJson())
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("OPPONENT")
                    }
                }
            },
            confirmButton = {}
        )
    }
}
