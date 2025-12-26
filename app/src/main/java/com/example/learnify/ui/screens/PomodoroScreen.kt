package com.example.learnify.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.learnify.R
import com.example.learnify.ui.viewModels.PomodoroViewModel
import com.example.learnify.ui.theme.Light_Brown
import com.example.learnify.ui.theme.PrimaryColor

@Composable
fun PomodoroScreen(navController: NavController, viewModel: PomodoroViewModel) {
    val timeLeft = viewModel.timeLeft
    val totalTime = viewModel.totalTime
    val isBreak = viewModel.isBreakTime

    val minutes = timeLeft / 60
    val seconds = timeLeft % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    val progress = if (totalTime > 0) timeLeft.toFloat() / totalTime.toFloat() else 0f
    val titleText = if (isBreak) "Break Time" else "Work Time"
    val titleFont = FontFamily(Font(R.font.playwrite))

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.pomodoro_screen),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(320.dp)
            ) {
                PomodoroDial(
                    progress = progress,
                    modifier = Modifier.fillMaxSize()
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(50.dp)
                ) {
                    Text(
                        text = formattedTime,
                        fontSize = 58.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = titleText,
                        fontSize = 16.sp,
                        fontFamily = titleFont,
                        color = Light_Brown
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.pauseTimer() }) {
                            Icon(
                                painter = painterResource(id = R.drawable.pause_icon),
                                contentDescription = "Pause",
                                tint = PrimaryColor,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        val context = LocalContext.current

                        IconButton(onClick = { viewModel.startTimer(context) }) {
                            Icon(
                                painter = painterResource(id = R.drawable.start_icon),
                                contentDescription = "Start",
                                tint = PrimaryColor,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        IconButton(onClick = { viewModel.resetTimer() }) {
                            Icon(
                                painter = painterResource(id = R.drawable.reset_icon),
                                contentDescription = "Reset",
                                tint = PrimaryColor,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { viewModel.startWork() },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                    modifier = Modifier.size(110.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("25 m", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Work", color = Color.White, fontSize = 14.sp)
                    }
                }

                Button(
                    onClick = { viewModel.startBreak() },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                    modifier = Modifier.size(110.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("5 m", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Break", color = Color.White, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun PomodoroDial(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 14.dp.toPx()
        val radius = size.minDimension / 2 - strokeWidth

        drawCircle(
            color = Color.LightGray.copy(alpha = 0.35f),
            radius = radius,
            style = Stroke(width = strokeWidth)
        )

        drawArc(
            color = PrimaryColor,
            startAngle = -90f,
            sweepAngle = 360f * (1f - progress),
            useCenter = false,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round
            )
        )
    }
}
