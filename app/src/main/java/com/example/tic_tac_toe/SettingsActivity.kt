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

@Composable
fun ChallengeLevelOption(
    levelType: SettingsManager.Difficulty,
    displayTitle: String,
    displayDescription: String,
    isChosen: Boolean,
    onChoose: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isChosen,
                onClick = onChoose
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isChosen,
            onClick = onChoose
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = displayTitle,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = displayDescription,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun ConfigurationDisplay(exitAction: () -> Unit) {
    val appContext = androidx.compose.ui.platform.LocalContext.current
    var chosenDifficulty by remember { 
        mutableStateOf(SettingsManager.retrieveDifficulty(appContext))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Settings",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

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

                ChallengeLevelOption(
                    levelType = SettingsManager.Difficulty.EASY,
                    displayTitle = "Easy",
                    displayDescription = "AI makes random moves",
                    isChosen = chosenDifficulty == SettingsManager.Difficulty.EASY,
                    onChoose = {
                        chosenDifficulty = SettingsManager.Difficulty.EASY
                        SettingsManager.updateDifficulty(appContext, SettingsManager.Difficulty.EASY)
                    }
                )

                ChallengeLevelOption(
                    levelType = SettingsManager.Difficulty.MEDIUM,
                    displayTitle = "Medium",
                    displayDescription = "AI makes 50% random, 50% optimal moves",
                    isChosen = chosenDifficulty == SettingsManager.Difficulty.MEDIUM,
                    onChoose = {
                        chosenDifficulty = SettingsManager.Difficulty.MEDIUM
                        SettingsManager.updateDifficulty(appContext, SettingsManager.Difficulty.MEDIUM)
                    }
                )

                ChallengeLevelOption(
                    levelType = SettingsManager.Difficulty.HARD,
                    displayTitle = "Hard",
                    displayDescription = "AI always makes optimal moves",
                    isChosen = chosenDifficulty == SettingsManager.Difficulty.HARD,
                    onChoose = {
                        chosenDifficulty = SettingsManager.Difficulty.HARD
                        SettingsManager.updateDifficulty(appContext, SettingsManager.Difficulty.HARD)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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

        Button(
            onClick = exitAction,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Back to Menu", fontSize = 16.sp)
        }
    }
}

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TicTacToeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ConfigurationDisplay(
                        exitAction = { finish() }
                    )
                }
            }
        }
    }
}
