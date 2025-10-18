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

class HistoryActivity : ComponentActivity() {
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
                    HistoryScreen(
                        database = database,
                        onBack = { finish() }
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryScreen(
    database: AppDatabase,
    onBack: () -> Unit
) {
    val games by database.gameHistoryDao().getAllGames().collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Game History",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (games.isEmpty()) {
            // Empty state
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
            // Game list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(games) { game ->
                    GameHistoryItem(game)
                }
            }
        }

        // Back button
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Back to Menu", fontSize = 16.sp)
        }
    }
}

@Composable
fun GameHistoryItem(game: GameHistory) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
    val dateString = dateFormat.format(Date(game.timestamp))

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
                // Date/Time
                Text(
                    text = dateString,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Winner
                Text(
                    text = when (game.winner) {
                        "X" -> "Winner: X (You)"
                        "O" -> "Winner: O (${if (game.gameMode == "Computer") "AI" else "Opponent"})"
                        else -> "Result: Draw"
                    },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (game.winner) {
                        "X" -> Color(0xFF4CAF50)
                        "O" -> Color(0xFFF44336)
                        else -> Color.Gray
                    }
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Game mode and difficulty
                Text(
                    text = "Mode: ${game.gameMode} | Difficulty: ${game.difficultyMode}",
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            }
        }
    }
}
