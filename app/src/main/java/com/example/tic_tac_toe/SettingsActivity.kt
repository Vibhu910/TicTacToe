package com.example.tic_tac_toe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tic_tac_toe.ui.theme.TicTacToeTheme

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TicTacToeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SettingsScreen(
                        onBack = { finish() }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedDifficulty by remember { 
        mutableStateOf(SettingsManager.getDifficulty(context))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "Settings",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Difficulty Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "AI Difficulty",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                DifficultyOption(
                    difficulty = SettingsManager.Difficulty.EASY,
                    title = "Easy",
                    description = "AI makes random moves",
                    selected = selectedDifficulty == SettingsManager.Difficulty.EASY,
                    onSelect = {
                        selectedDifficulty = SettingsManager.Difficulty.EASY
                        SettingsManager.setDifficulty(context, SettingsManager.Difficulty.EASY)
                    }
                )

                DifficultyOption(
                    difficulty = SettingsManager.Difficulty.MEDIUM,
                    title = "Medium",
                    description = "AI makes 50% random, 50% optimal moves",
                    selected = selectedDifficulty == SettingsManager.Difficulty.MEDIUM,
                    onSelect = {
                        selectedDifficulty = SettingsManager.Difficulty.MEDIUM
                        SettingsManager.setDifficulty(context, SettingsManager.Difficulty.MEDIUM)
                    }
                )

                DifficultyOption(
                    difficulty = SettingsManager.Difficulty.HARD,
                    title = "Hard",
                    description = "AI always makes optimal moves",
                    selected = selectedDifficulty == SettingsManager.Difficulty.HARD,
                    onSelect = {
                        selectedDifficulty = SettingsManager.Difficulty.HARD
                        SettingsManager.setDifficulty(context, SettingsManager.Difficulty.HARD)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Game Rules Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Misere Rules",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "In Misere Tic-Tac-Toe, the objective is INVERTED:",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "• Getting 3 in a row means you LOSE",
                    fontSize = 14.sp,
                    color = Color.Red,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "• Force your opponent to make three in a row to win",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "• Draw if board fills without any three in a row",
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Back button
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
fun DifficultyOption(
    difficulty: SettingsManager.Difficulty,
    title: String,
    description: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onSelect
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onSelect
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}
