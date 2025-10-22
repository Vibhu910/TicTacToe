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

enum class GameMode {
    SelectMode,
    LocalPlay,
    DeviceSelection,
    NetworkPlay
}

@Composable
fun PlayModeSelector(
    selectLocalMatch: () -> Unit,
    selectNetworkMatch: () -> Unit,
    exitToMenu: () -> Unit
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
            onClick = selectLocalMatch,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(60.dp)
        ) {
            Text("Play on Same Device", fontSize = 18.sp)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = selectNetworkMatch,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(60.dp)
        ) {
            Text("Play on Different Devices", fontSize = 18.sp)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = exitToMenu,
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
fun SameDeviceMatch(
    exitToPrevious: () -> Unit,
    recordMatchResult: (winner: String) -> Unit
) {
    val matchInstance = remember { GameLogic() }
    var currentGridState by remember { mutableStateOf(matchInstance.duplicateGridState()) }
    var statusMessage by remember { mutableStateOf("Player X's turn") }
    var resultPersisted by remember { mutableStateOf(false) }

    fun processCellSelection(rowIdx: Int, colIdx: Int) {
        if (matchInstance.matchEnded) return
        
        if (matchInstance.executeMove(rowIdx, colIdx)) {
            currentGridState = matchInstance.duplicateGridState()
            
            if (matchInstance.matchEnded) {
                if (matchInstance.isStalemate) {
                    statusMessage = "It's a draw!"
                    if (!resultPersisted) {
                        recordMatchResult("Draw")
                        resultPersisted = true
                    }
                } else {
                    statusMessage = "Player ${matchInstance.victoriousPlayer} wins! (Player ${if (matchInstance.victoriousPlayer == GameLogic.PLAYER_X) 'O' else 'X'} made three in a row)"
                    if (!resultPersisted) {
                        recordMatchResult(matchInstance.victoriousPlayer.toString())
                        resultPersisted = true
                    }
                }
            } else {
                statusMessage = "Player ${matchInstance.activePlayer}'s turn"
            }
        }
    }

    fun restartMatch() {
        matchInstance.initializeMatch()
        currentGridState = matchInstance.duplicateGridState()
        statusMessage = "Player X's turn"
        resultPersisted = false
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
            text = statusMessage,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = if (matchInstance.matchEnded) {
                if (matchInstance.isStalemate) Color.Gray else Color(0xFF4CAF50)
            } else Color.Black
        )

        GameBoard(
            board = currentGridState,
            onCellClick = { rowIdx, colIdx -> processCellSelection(rowIdx, colIdx) },
            enabled = !matchInstance.matchEnded
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { restartMatch() },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(50.dp)
            ) {
                Text("Reset Game", fontSize = 16.sp)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = exitToPrevious,
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
fun BluetoothDevicePicker(
    connectionManager: BluetoothManager,
    deviceChosen: () -> Unit,
    serverModeActivated: () -> Unit,
    exitToPrevious: () -> Unit
) {
    val appContext = androidx.compose.ui.platform.LocalContext.current
    var availableDevices by remember { mutableStateOf<List<BluetoothDevice>>(emptyList()) }
    var notificationMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        if (!connectionManager.hasBluetoothCapability()) {
            notificationMessage = "Bluetooth not available on this device"
        } else if (!connectionManager.bluetoothActivated()) {
            notificationMessage = "Please enable Bluetooth"
        } else {
            availableDevices = connectionManager.retrievePairedDeviceList()
            if (availableDevices.isEmpty()) {
                notificationMessage = "No paired devices found. Please pair a device first."
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
                connectionManager.initiateServerMode()
                serverModeActivated()
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

        if (notificationMessage.isNotEmpty()) {
            Text(
                text = notificationMessage,
                fontSize = 14.sp,
                color = Color.Red,
                modifier = Modifier.padding(8.dp)
            )
        }

        if (availableDevices.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(availableDevices) { targetDevice ->
                    Button(
                        onClick = {
                            connectionManager.establishClientConnection(targetDevice)
                            deviceChosen()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            if (ActivityCompat.checkSelfPermission(
                                    appContext,
                                    Manifest.permission.BLUETOOTH_CONNECT
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                Text(targetDevice.name ?: "Unknown Device")
                            } else {
                                Text("Unknown Device")
                            }
                        } else {
                            Text(targetDevice.name ?: "Unknown Device")
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = exitToPrevious,
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
fun NetworkBasedMatch(
    connectionManager: BluetoothManager,
    connectionActive: Boolean,
    exitToPrevious: () -> Unit,
    recordMatchResult: (winner: String) -> Unit
) {
    val matchInstance = remember { GameLogic() }
    var currentGridState by remember { mutableStateOf(matchInstance.duplicateGridState()) }
    var statusMessage by remember { mutableStateOf("Waiting for connection...") }
    var playerTurnActive by remember { mutableStateOf(false) }
    var assignedSymbol by remember { mutableStateOf<Char?>(null) }
    var displayFirstMoveDialog by remember { mutableStateOf(false) }
    var resultPersisted by remember { mutableStateOf(false) }
    var localDeviceId by remember { mutableStateOf("") }
    var remoteDeviceId by remember { mutableStateOf("") }

    val asyncScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        localDeviceId = connectionManager.retrieveDeviceIdentifier()
    }

    LaunchedEffect(connectionActive) {
        if (connectionActive) {
            displayFirstMoveDialog = true
            statusMessage = "Connected! Choose who goes first"
        } else {
            displayFirstMoveDialog = false
            statusMessage = "Waiting for connection..."
            // Reset game state when connection is lost
            matchInstance.initializeMatch()
            currentGridState = matchInstance.duplicateGridState()
            playerTurnActive = false
            assignedSymbol = null
            resultPersisted = false
        }
    }

    connectionManager.onMessageReceived = { receivedData ->
        asyncScope.launch {
            val parsedState = GameStateMessage.fromJson(receivedData)
            if (parsedState != null) {
                // Handle who goes first
                if (parsedState.metadata.miniGame.player1Choice.isNotEmpty() && 
                    parsedState.metadata.miniGame.player2Choice.isEmpty()) {
                    // Opponent chose who goes first, acknowledge it
                    val acknowledgement = parsedState.copy(
                        metadata = parsedState.metadata.copy(
                            miniGame = MiniGameData(
                                player1Choice = parsedState.metadata.miniGame.player1Choice,
                                player2Choice = parsedState.metadata.miniGame.player1Choice
                            )
                        )
                    )
                    connectionManager.transmitMessage(acknowledgement.toJson())
                    
                    // Determine our symbol and who moves first
                    if (parsedState.metadata.miniGame.player1Choice == localDeviceId) {
                        assignedSymbol = GameLogic.PLAYER_X
                        playerTurnActive = true
                        statusMessage = "You go first! (You are X)"
                    } else {
                        assignedSymbol = GameLogic.PLAYER_O
                        playerTurnActive = false
                        statusMessage = "Opponent goes first (You are O)"
                    }
                    displayFirstMoveDialog = false
                } else if (parsedState.metadata.miniGame.player1Choice.isNotEmpty() && 
                           parsedState.metadata.miniGame.player2Choice.isNotEmpty()) {
                    // Both players agreed on who goes first
                    if (parsedState.metadata.miniGame.player1Choice == localDeviceId) {
                        assignedSymbol = GameLogic.PLAYER_X
                        playerTurnActive = true
                        statusMessage = "You go first! (You are X)"
                    } else {
                        assignedSymbol = GameLogic.PLAYER_O
                        playerTurnActive = false
                        statusMessage = "Opponent goes first (You are O)"
                    }
                    displayFirstMoveDialog = false
                }
                
                // Handle game state updates
                if (parsedState.gameState.reset) {
                    matchInstance.initializeMatch()
                    currentGridState = matchInstance.duplicateGridState()
                    statusMessage = if (assignedSymbol == GameLogic.PLAYER_X) "Your turn!" else "Opponent's turn"
                    playerTurnActive = assignedSymbol == GameLogic.PLAYER_X
                    resultPersisted = false
                } else {
                    // Update board
                    matchInstance.updateGridState(GameStateConverter.nestedListToGrid(parsedState.gameState.board))
                    matchInstance.moveCounter = parsedState.gameState.turn
                    currentGridState = matchInstance.duplicateGridState()
                    
                    // Check game status
                    matchInstance.evaluateMatchStatus()
                    
                    if (parsedState.gameState.draw || parsedState.gameState.winner.isNotEmpty()) {
                        matchInstance.matchEnded = true
                        if (parsedState.gameState.draw) {
                            statusMessage = "It's a draw!"
                            if (!resultPersisted) {
                                recordMatchResult("Draw")
                                resultPersisted = true
                            }
                        } else {
                            val victorIdentifier = parsedState.gameState.winner
                            statusMessage = if (victorIdentifier == localDeviceId) {
                                "You win!"
                            } else {
                                "Opponent wins!"
                            }
                            if (!resultPersisted) {
                                val winnerSymbol = if (victorIdentifier == localDeviceId) assignedSymbol.toString() else {
                                    if (assignedSymbol == GameLogic.PLAYER_X) "O" else "X"
                                }
                                recordMatchResult(winnerSymbol)
                                resultPersisted = true
                            }
                        }
                    } else {
                        // It's our turn now
                        playerTurnActive = true
                        statusMessage = "Your turn!"
                    }
                }
            }
        }
    }

    fun processCellSelection(rowIdx: Int, colIdx: Int) {
        if (!playerTurnActive || matchInstance.matchEnded || assignedSymbol == null) return
        
        if (matchInstance.executeMove(rowIdx, colIdx)) {
            currentGridState = matchInstance.duplicateGridState()
            playerTurnActive = false
            
            // Check if game is over
            if (matchInstance.matchEnded) {
                if (matchInstance.isStalemate) {
                    statusMessage = "It's a draw!"
                    if (!resultPersisted) {
                        recordMatchResult("Draw")
                        resultPersisted = true
                    }
                } else {
                    statusMessage = if (matchInstance.victoriousPlayer == assignedSymbol) {
                        "You win! (Opponent made three in a row)"
                    } else {
                        "Opponent wins! (You made three in a row)"
                    }
                    if (!resultPersisted) {
                        recordMatchResult(matchInstance.victoriousPlayer.toString())
                        resultPersisted = true
                    }
                }
                
                // Send game over state
                val finalStateMessage = GameStateMessage(
                    gameState = GameStateData(
                        board = GameStateConverter.gridToNestedList(matchInstance.gridState),
                        turn = matchInstance.moveCounter,
                        winner = if (matchInstance.victoriousPlayer == assignedSymbol) localDeviceId else remoteDeviceId,
                        draw = matchInstance.isStalemate,
                        connectionEstablished = true,
                        reset = false
                    ),
                    metadata = MetadataData(
                        choices = listOf(
                            PlayerChoice("player1", localDeviceId),
                            PlayerChoice("player2", remoteDeviceId)
                        ),
                        miniGame = MiniGameData(
                            player1Choice = if (assignedSymbol == GameLogic.PLAYER_X) localDeviceId else remoteDeviceId,
                            player2Choice = if (assignedSymbol == GameLogic.PLAYER_X) localDeviceId else remoteDeviceId
                        )
                    )
                )
                connectionManager.transmitMessage(finalStateMessage.toJson())
            } else {
                statusMessage = "Opponent's turn"
                
                // Send move to opponent
                val moveStateMessage = GameStateMessage(
                    gameState = GameStateData(
                        board = GameStateConverter.gridToNestedList(matchInstance.gridState),
                        turn = matchInstance.moveCounter,
                        winner = "",
                        draw = false,
                        connectionEstablished = true,
                        reset = false
                    ),
                    metadata = MetadataData(
                        choices = listOf(
                            PlayerChoice("player1", localDeviceId),
                            PlayerChoice("player2", remoteDeviceId)
                        ),
                        miniGame = MiniGameData(
                            player1Choice = if (assignedSymbol == GameLogic.PLAYER_X) localDeviceId else remoteDeviceId,
                            player2Choice = if (assignedSymbol == GameLogic.PLAYER_X) localDeviceId else remoteDeviceId
                        )
                    )
                )
                connectionManager.transmitMessage(moveStateMessage.toJson())
            }
        }
    }

    fun restartMatch() {
        matchInstance.initializeMatch()
        currentGridState = matchInstance.duplicateGridState()
        resultPersisted = false
        playerTurnActive = assignedSymbol == GameLogic.PLAYER_X
        statusMessage = if (playerTurnActive) "Your turn!" else "Opponent's turn"
        
        // Send reset message
        val resetStateMessage = GameStateMessage(
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
                    PlayerChoice("player1", localDeviceId),
                    PlayerChoice("player2", remoteDeviceId)
                ),
                miniGame = MiniGameData(
                    player1Choice = if (assignedSymbol == GameLogic.PLAYER_X) localDeviceId else remoteDeviceId,
                    player2Choice = if (assignedSymbol == GameLogic.PLAYER_X) localDeviceId else remoteDeviceId
                )
            )
        )
        connectionManager.transmitMessage(resetStateMessage.toJson())
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
            if (assignedSymbol != null) {
                Text(
                    text = "You are: $assignedSymbol",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Text(
            text = statusMessage,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = if (matchInstance.matchEnded) Color(0xFF4CAF50) else Color.Black
        )

        GameBoard(
            board = currentGridState,
            onCellClick = { rowIdx, colIdx -> processCellSelection(rowIdx, colIdx) },
            enabled = playerTurnActive && !matchInstance.matchEnded && connectionActive
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { restartMatch() },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(50.dp),
                enabled = connectionActive
            ) {
                Text("Reset Game", fontSize = 16.sp)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = exitToPrevious,
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
    if (displayFirstMoveDialog && connectionActive) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Who Goes First?") },
            text = {
                Column {
                    Text("Choose who makes the first move:")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            assignedSymbol = GameLogic.PLAYER_X
                            playerTurnActive = true
                            remoteDeviceId = "opponent"
                            statusMessage = "You go first! (You are X)"
                            displayFirstMoveDialog = false
                            
                            val initialMessage = GameStateMessage(
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
                                        PlayerChoice("player1", localDeviceId),
                                        PlayerChoice("player2", "opponent")
                                    ),
                                    miniGame = MiniGameData(
                                        player1Choice = localDeviceId,
                                        player2Choice = ""
                                    )
                                )
                            )
                            connectionManager.transmitMessage(initialMessage.toJson())
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("ME")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            assignedSymbol = GameLogic.PLAYER_O
                            playerTurnActive = false
                            remoteDeviceId = "opponent"
                            statusMessage = "Opponent goes first (You are O)"
                            displayFirstMoveDialog = false
                            
                            val initialMessage = GameStateMessage(
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
                                        PlayerChoice("player1", localDeviceId),
                                        PlayerChoice("player2", "opponent")
                                    ),
                                    miniGame = MiniGameData(
                                        player1Choice = "opponent",
                                        player2Choice = ""
                                    )
                                )
                            )
                            connectionManager.transmitMessage(initialMessage.toJson())
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

@Composable
fun HumanGameCoordinator(
    connectionManager: BluetoothManager,
    exitToMenu: () -> Unit,
    recordMatchResult: (winner: String) -> Unit
) {
    var selectedMode by remember { mutableStateOf<GameMode>(GameMode.SelectMode) }
    var networkConnected by remember { mutableStateOf(false) }
    var connectionStatusMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        connectionManager.onConnectionEstablished = {
            networkConnected = true
            connectionStatusMessage = "Connected!"
        }
        connectionManager.onConnectionLost = {
            networkConnected = false
            connectionStatusMessage = "Connection lost"
            // Don't automatically reset to SelectMode - let user decide
        }
    }

    when (selectedMode) {
        GameMode.SelectMode -> {
            PlayModeSelector(
                selectLocalMatch = { selectedMode = GameMode.LocalPlay },
                selectNetworkMatch = { selectedMode = GameMode.DeviceSelection },
                exitToMenu = exitToMenu
            )
        }
        GameMode.LocalPlay -> {
            SameDeviceMatch(
                exitToPrevious = { selectedMode = GameMode.SelectMode },
                recordMatchResult = recordMatchResult
            )
        }
        GameMode.DeviceSelection -> {
            BluetoothDevicePicker(
                connectionManager = connectionManager,
                deviceChosen = {
                    connectionManager.resetConnectionState() // Reset state before connecting
                    selectedMode = GameMode.NetworkPlay
                },
                serverModeActivated = {
                    connectionManager.resetConnectionState() // Reset state before starting server
                    connectionManager.initiateServerMode()
                    selectedMode = GameMode.NetworkPlay
                },
                exitToPrevious = { 
                    connectionManager.terminateConnection()
                    selectedMode = GameMode.SelectMode 
                }
            )
        }
        GameMode.NetworkPlay -> {
            NetworkBasedMatch(
                connectionManager = connectionManager,
                connectionActive = networkConnected,
                exitToPrevious = {
                    connectionManager.terminateConnection()
                    selectedMode = GameMode.SelectMode
                },
                recordMatchResult = recordMatchResult
            )
        }
    }
}

class PlayHumanActivity : ComponentActivity() {
    private lateinit var databaseInstance: AppDatabase
    private lateinit var connectionManager: BluetoothManager

    private val bluetoothPermissionRequester = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Handle permissions result
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        databaseInstance = AppDatabase.obtainDatabase(this)
        connectionManager = BluetoothManager(this)

        // Request Bluetooth permissions for Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            bluetoothPermissionRequester.launch(
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
                    HumanGameCoordinator(
                        connectionManager = connectionManager,
                        exitToMenu = { finish() },
                        recordMatchResult = { winnerName ->
                            persistGameRecord(winnerName)
                        }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        connectionManager.terminateConnection()
    }

    private fun persistGameRecord(winnerName: String) {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            databaseInstance.gameHistoryDao().addGameRecord(
                GameHistory(
                    victoriousPlayer = winnerName,
                    challengeLevel = "N/A",
                    playMode = "Human"
                )
            )
        }
    }
}
