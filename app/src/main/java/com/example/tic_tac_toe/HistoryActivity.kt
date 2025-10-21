package com.example.tic_tac_toe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import com.example.tic_tac_toe.database.AppDatabase
import com.example.tic_tac_toe.database.GameHistory
import com.example.tic_tac_toe.ui.theme.TicTacToeTheme
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoricalGameItem(gameRecord: GameHistory) {
    val timestampFormatter = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
    val formattedTimestamp = timestampFormatter.format(Date(gameRecord.timestampValue))

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = formattedTimestamp,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = when (gameRecord.victoriousPlayer) {
                        "X" -> "Winner: X (You)"
                        "O" -> "Winner: O (${if (gameRecord.playMode == "Computer") "AI" else "Opponent"})"
                        else -> "Result: Draw"
                    },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (gameRecord.victoriousPlayer) {
                        "X" -> Color(0xFF4CAF50)
                        "O" -> Color(0xFFF44336)
                        else -> Color.Gray
                    }
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Mode: ${gameRecord.playMode} | Difficulty: ${gameRecord.challengeLevel}",
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            }
        }
    }
}

@Composable
fun GameHistoryDisplay(
    dataSource: AppDatabase,
    navigateBack: () -> Unit
) {
    val recordsList by dataSource.gameHistoryDao().retrieveAllRecords().collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Game History",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (recordsList.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No games played yet",
                    fontSize = 18.sp,
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(recordsList) { record ->
                    HistoricalGameItem(record)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = navigateBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Back to Menu", fontSize = 16.sp)
        }
    }
}

class HistoryActivity : ComponentActivity() {
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
                    GameHistoryDisplay(
                        dataSource = databaseInstance,
                        navigateBack = { finish() }
                    )
                }
            }
        }
    }
}
