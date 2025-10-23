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
        println("DEBUG: LaunchedEffect - connectionActive: $connectionActive")
        
        // Validate connection state with BluetoothManager
        val actualConnectionState = connectionManager.validateConnectionState()
        println("DEBUG: Actual connection state: $actualConnectionState")
        
        if (connectionActive && actualConnectionState) {
            // Only show dialog if we haven't assigned symbols yet
            if (assignedSymbol == null) {
                displayFirstMoveDialog = true
                statusMessage = "Connected! Choose who goes first"
                println("DEBUG: Connection active - showing dialog")
            } else {
                println("DEBUG: Connection active but symbols already assigned: $assignedSymbol")
            }
        } else {
            displayFirstMoveDialog = false
            statusMessage = "Waiting for connection..."
            // Reset game state when connection is lost
            matchInstance.initializeMatch()
            currentGridState = matchInstance.duplicateGridState()
            playerTurnActive = false
            assignedSymbol = null
            resultPersisted = false
            println("DEBUG: Connection lost - resetting game state")
        }
    }

    connectionManager.onMessageReceived = { receivedData ->
        asyncScope.launch {
            try {
                println("DEBUG: Message received, processing...")
                val parsedState = GameStateMessage.fromJson(receivedData)
                if (parsedState != null) {
                    // Handle who goes first - improved logic
                    if (parsedState.metadata.miniGame.player1Choice.isNotEmpty() && 
                        parsedState.metadata.miniGame.player2Choice.isEmpty()) {
                        // Opponent chose who goes first, acknowledge it properly
                        val acknowledgement = parsedState.copy(
                            metadata = parsedState.metadata.copy(
                                miniGame = MiniGameData(
                                    player1Choice = parsedState.metadata.miniGame.player1Choice,
                                    player2Choice = localDeviceId  // Fix: Use our device ID, not theirs
                                )
                            )
                        )
                        connectionManager.transmitMessage(acknowledgement.toJson())
                        
                        // Determine our symbol and who moves first
                        println("DEBUG: Received player1Choice: ${parsedState.metadata.miniGame.player1Choice}")
                        println("DEBUG: Our localDeviceId: $localDeviceId")
                        println("DEBUG: Comparison result: ${parsedState.metadata.miniGame.player1Choice == localDeviceId}")
                        println("DEBUG: Message received from opponent - processing symbol assignment")
                        
                        // Update remote device ID from the message
                        if (parsedState.metadata.choices.isNotEmpty()) {
                            val remoteChoice = parsedState.metadata.choices.find { it.name != localDeviceId }
                            if (remoteChoice != null) {
                                remoteDeviceId = remoteChoice.name
                                println("DEBUG: Updated remoteDeviceId to: $remoteDeviceId")
                            }
                        }
                        
                        if (parsedState.metadata.miniGame.player1Choice == "OPPONENT_SHOULD_GO_FIRST") {
                            // Special case: Opponent chose to let us go first
                            assignedSymbol = GameLogic.PLAYER_X
                            matchInstance.setPlayerSymbol(GameLogic.PLAYER_X)
                            playerTurnActive = true
                            statusMessage = "You go first! (You are X)"
                            println("DEBUG: OPPONENT_SHOULD_GO_FIRST - Assigned as PLAYER_X (opponent chose us)")
                            println("DEBUG: OPPONENT_SHOULD_GO_FIRST - playerTurnActive: $playerTurnActive, statusMessage: $statusMessage")
                        } else if (parsedState.metadata.miniGame.player1Choice == localDeviceId) {
                            assignedSymbol = GameLogic.PLAYER_X
                            matchInstance.setPlayerSymbol(GameLogic.PLAYER_X)
                            playerTurnActive = true
                            statusMessage = "You go first! (You are X)"
                            println("DEBUG: Assigned as PLAYER_X")
                        } else {
                            assignedSymbol = GameLogic.PLAYER_O
                            matchInstance.setPlayerSymbol(GameLogic.PLAYER_O)
                            playerTurnActive = false
                            statusMessage = "Opponent goes first (You are O)"
                            println("DEBUG: Assigned as PLAYER_O")
                            println("DEBUG: playerTurnActive = $playerTurnActive")
                            println("DEBUG: statusMessage = $statusMessage")
                            println("DEBUG: assignedSymbol value = '$assignedSymbol'")
                            println("DEBUG: assignedSymbol type = ${assignedSymbol?.javaClass?.simpleName}")
                            // Force UI recomposition
                            currentGridState = matchInstance.duplicateGridState()
                        }
                        displayFirstMoveDialog = false
                    } else if (parsedState.metadata.miniGame.player1Choice.isNotEmpty() && 
                               parsedState.metadata.miniGame.player2Choice.isNotEmpty()) {
                        // Both players agreed on who goes first
                        
                        // Update remote device ID from the message
                        if (parsedState.metadata.choices.isNotEmpty()) {
                            val remoteChoice = parsedState.metadata.choices.find { it.name != localDeviceId }
                            if (remoteChoice != null) {
                                remoteDeviceId = remoteChoice.name
                                println("DEBUG: Acknowledgement - Updated remoteDeviceId to: $remoteDeviceId")
                            }
                        }
                        if (parsedState.metadata.miniGame.player1Choice == localDeviceId) {
                            assignedSymbol = GameLogic.PLAYER_X
                            matchInstance.setPlayerSymbol(GameLogic.PLAYER_X)
                            playerTurnActive = true
                            statusMessage = "You go first! (You are X)"
                        } else {
                            assignedSymbol = GameLogic.PLAYER_O
                            matchInstance.setPlayerSymbol(GameLogic.PLAYER_O)
                            playerTurnActive = false
                            statusMessage = "Opponent goes first (You are O)"
                            println("DEBUG: Acknowledgement - Assigned as PLAYER_O")
                            println("DEBUG: Acknowledgement - playerTurnActive = $playerTurnActive")
                            println("DEBUG: Acknowledgement - statusMessage = $statusMessage")
                            println("DEBUG: Acknowledgement - assignedSymbol value = '$assignedSymbol'")
                            println("DEBUG: Acknowledgement - assignedSymbol type = ${assignedSymbol?.javaClass?.simpleName}")
                            // Force UI recomposition
                            currentGridState = matchInstance.duplicateGridState()
                        }
                        displayFirstMoveDialog = false
                    }
                
                // Handle game state updates
                if (parsedState.gameState.reset) {
                    // Reset the game completely
                    matchInstance.initializeMatch()
                    currentGridState = matchInstance.duplicateGridState()
                    assignedSymbol = null
                    playerTurnActive = false
                    statusMessage = "Choose who goes first"
                    displayFirstMoveDialog = true
                    resultPersisted = false
                    println("DEBUG: Reset message received - resetting game and showing dialog")
                } else {
                    // Update board with proper synchronization
                    val newGridState = GameStateConverter.nestedListToGrid(parsedState.gameState.board)
                    matchInstance.updateGridState(newGridState)
                    
                    // Sync move counter properly - only update if it's higher (prevent rollback)
                    if (parsedState.gameState.turn >= matchInstance.moveCounter) {
                        matchInstance.moveCounter = parsedState.gameState.turn
                    }
                    
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
                            
                            // Only process winner message if we haven't already recorded our own win
                            if (!resultPersisted) {
                                statusMessage = if (victorIdentifier == localDeviceId) {
                                    "You win!"
                                } else {
                                    "Opponent wins!"
                                }
                                
                                val winnerSymbol = if (victorIdentifier == localDeviceId) {
                                    // We won - record our symbol
                                    if (assignedSymbol == GameLogic.PLAYER_X) "X" else "O"
                                } else {
                                    // Opponent won - record opponent's symbol
                                    if (assignedSymbol == GameLogic.PLAYER_X) "O" else "X"
                                }
                                println("DEBUG: Remote winner recording - victorIdentifier: $victorIdentifier, localDeviceId: $localDeviceId, assignedSymbol: $assignedSymbol, winnerSymbol: $winnerSymbol")
                                println("DEBUG: Remote winner recording - We are the winner: ${victorIdentifier == localDeviceId}")
                                println("DEBUG: Remote winner recording - Recording as: ${if (victorIdentifier == localDeviceId) "YOU" else "OPPONENT"}")
                                recordMatchResult(winnerSymbol)
                                resultPersisted = true
                            } else {
                                println("DEBUG: Remote winner message received but result already persisted - ignoring")
                            }
                        }
                    } else {
                        // It's our turn now
                        playerTurnActive = true
                        // Synchronize GameLogic active player with our assigned symbol
                        assignedSymbol?.let { symbol ->
                            matchInstance.setPlayerSymbol(symbol)
                        }
                        // Check if it's actually our turn
                        val isActuallyOurTurn = if (matchInstance.moveCounter == 0) {
                            assignedSymbol == GameLogic.PLAYER_X
                        } else {
                            (matchInstance.moveCounter % 2 == 0 && assignedSymbol == GameLogic.PLAYER_X) ||
                            (matchInstance.moveCounter % 2 == 1 && assignedSymbol == GameLogic.PLAYER_O)
                        }
                        statusMessage = if (isActuallyOurTurn) "Your turn!" else "Opponent's turn"
                        println("DEBUG: Game state update - playerTurnActive = $playerTurnActive, assignedSymbol = $assignedSymbol")
                        println("DEBUG: Game state update - moveCounter = ${matchInstance.moveCounter}, isActuallyOurTurn = $isActuallyOurTurn")
                        println("DEBUG: Game state update - statusMessage = $statusMessage")
                    }
                }
                }
            } catch (e: Exception) {
                // Handle JSON parsing errors gracefully
                println("Error parsing message: ${e.message}")
            }
        }
    }

    fun isOurTurn(): Boolean {
        // Check if it's our turn based on the current game state
        // If it's the first move (moveCounter == 0), check who should go first
        val isTurn = if (matchInstance.moveCounter == 0) {
            // First move: only the player who should go first can move
            assignedSymbol == GameLogic.PLAYER_X
        } else {
            // Subsequent moves: alternate based on move counter
            (matchInstance.moveCounter % 2 == 0 && assignedSymbol == GameLogic.PLAYER_X) ||
            (matchInstance.moveCounter % 2 == 1 && assignedSymbol == GameLogic.PLAYER_O)
        }
        println("DEBUG: isOurTurn() - moveCounter: ${matchInstance.moveCounter}, assignedSymbol: $assignedSymbol, isTurn: $isTurn")
        return isTurn
    }

    fun processCellSelection(rowIdx: Int, colIdx: Int) {
        if (!playerTurnActive || matchInstance.matchEnded || assignedSymbol == null) return
        
        // Additional validation: ensure it's actually our turn
        if (!isOurTurn()) {
            println("DEBUG: Not our turn, ignoring click")
            return
        }
        
        println("DEBUG: processCellSelection - assignedSymbol = $assignedSymbol")
        assignedSymbol?.let { symbol ->
            if (matchInstance.executeMoveWithSymbol(rowIdx, colIdx, symbol)) {
            currentGridState = matchInstance.duplicateGridState()
            playerTurnActive = false
            // Update GameLogic active player to the next player
            matchInstance.setPlayerSymbol(matchInstance.getNextPlayer())
            
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
                        val winnerSymbol = if (matchInstance.victoriousPlayer == assignedSymbol) {
                            // We won - record our symbol
                            if (assignedSymbol == GameLogic.PLAYER_X) "X" else "O"
                        } else {
                            // Opponent won - record opponent's symbol
                            if (assignedSymbol == GameLogic.PLAYER_X) "O" else "X"
                        }
                        println("DEBUG: Local winner recording - victoriousPlayer: ${matchInstance.victoriousPlayer}, assignedSymbol: $assignedSymbol, winnerSymbol: $winnerSymbol")
                        println("DEBUG: Local winner recording - We are the winner: ${matchInstance.victoriousPlayer == assignedSymbol}")
                        println("DEBUG: Local winner recording - Recording as: ${if (matchInstance.victoriousPlayer == assignedSymbol) "YOU" else "OPPONENT"}")
                        recordMatchResult(winnerSymbol)
                        resultPersisted = true
                    } else {
                        println("DEBUG: Local winner recording - Result already persisted, skipping")
                    }
                }
                
                // Send game over state
                val winnerId = if (matchInstance.victoriousPlayer == assignedSymbol) localDeviceId else remoteDeviceId
                println("DEBUG: Sending winner - victoriousPlayer: ${matchInstance.victoriousPlayer}, assignedSymbol: $assignedSymbol, winnerId: $winnerId")
                val finalStateMessage = GameStateMessage(
                    gameState = GameStateData(
                        board = GameStateConverter.gridToNestedList(matchInstance.gridState),
                        turn = matchInstance.moveCounter,
                        winner = winnerId,
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
    }

    fun restartMatch() {
        matchInstance.initializeMatch()
        currentGridState = matchInstance.duplicateGridState()
        resultPersisted = false
        // Reset symbol assignment for new match
        assignedSymbol = null
        playerTurnActive = false
        statusMessage = "Choose who goes first"
        displayFirstMoveDialog = true
        
        // Force connection state reset to ensure both devices are in sync
        println("DEBUG: Restarting match - forcing connection state reset")
        // The connectionActive will be updated by the LaunchedEffect when connection is re-established
        
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
        
        // Debug display
        Text(
            text = "DEBUG: playerTurnActive = $playerTurnActive, assignedSymbol = $assignedSymbol, connectionActive = $connectionActive",
            fontSize = 12.sp,
            color = Color.Gray
        )

        GameBoard(
            board = currentGridState,
            onCellClick = { rowIdx, colIdx -> processCellSelection(rowIdx, colIdx) },
            enabled = playerTurnActive && isOurTurn() && !matchInstance.matchEnded && connectionActive
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
                            println("DEBUG: ME button clicked, localDeviceId: $localDeviceId")
                            assignedSymbol = GameLogic.PLAYER_X
                            playerTurnActive = true
                            // Get remote device ID from message if available, otherwise use placeholder
                            remoteDeviceId = "opponent"
                            statusMessage = "You go first! (You are X)"
                            displayFirstMoveDialog = false
                            println("DEBUG: ME button - assignedSymbol: $assignedSymbol, playerTurnActive: $playerTurnActive, statusMessage: $statusMessage")
                            
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
                                        PlayerChoice("player2", remoteDeviceId)
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
                            println("DEBUG: OPPONENT button clicked, localDeviceId: $localDeviceId")
                            assignedSymbol = GameLogic.PLAYER_O
                            playerTurnActive = false
                            remoteDeviceId = "opponent"
                            statusMessage = "Opponent goes first (You are O)"
                            displayFirstMoveDialog = false
                            println("DEBUG: OPPONENT button - assignedSymbol: $assignedSymbol, playerTurnActive: $playerTurnActive, statusMessage: $statusMessage")
                            
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
                                        PlayerChoice("player2", remoteDeviceId)
                                    ),
                                    miniGame = MiniGameData(
                                        player1Choice = "OPPONENT_SHOULD_GO_FIRST",  // Special marker
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
        println("DEBUG: persistGameRecord - Recording winner: $winnerName")
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            databaseInstance.gameHistoryDao().addGameRecord(
                GameHistory(
                    victoriousPlayer = winnerName,
                    challengeLevel = "N/A",
                    playMode = "Human"
                )
            )
            println("DEBUG: persistGameRecord - Successfully recorded: $winnerName")
        }
    }
}
