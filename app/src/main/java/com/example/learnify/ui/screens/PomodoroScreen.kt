package com.example.learnify.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.learnify.ui.TimerViewModel
import kotlin.text.format
import androidx.navigation.NavController

@Composable
fun PomodoroScreen( navController: NavController,
                 viewModel: TimerViewModel) {
    val timeLeft = viewModel.timeLeft
    val isBreak = viewModel.isBreakTime

    val minutes = timeLeft / 60
    val seconds = timeLeft % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    val titleText = if (isBreak) "Break Time ☕" else "Work Time 💪"
    val titleColor = if (isBreak) Color(0xFF4CAF50) else Color(0xFF4CAF50)
    val buttonColor = Color(0xFFA96DF3)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 50.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = titleText,
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold,
            color = titleColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = formattedTime,
            fontSize = 80.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    if (viewModel.isRunning) {
                        viewModel.pauseTimer()
                    } else {
                        viewModel.startTimer()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .width(120.dp)
                    .height(50.dp)
            ) {
                Text(
                    if (viewModel.isRunning) "Pause" else "Start",
                    fontSize = 18.sp
                )
            }

            Button(
                onClick = { viewModel.resetTimer() },
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .width(120.dp)
                    .height(50.dp)
            ) {
                Text("Reset", fontSize = 18.sp)
            }
        }



        Spacer(modifier = Modifier.height(20.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { viewModel.startWork() },
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .width(140.dp)
                    .height(50.dp)
            ) {
                Text("25m Work", fontSize = 16.sp)
            }

            Button(
                onClick = { viewModel.startBreak() },
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .width(140.dp)
                    .height(50.dp)
            ) {
                Text("5m Break", fontSize = 16.sp)
            }
        }
    }
}
