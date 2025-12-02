package com.example.learnify.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.learnify.data.local.TaskEntity
import com.example.learnify.ui.theme.AppBackgroundColor
import com.example.learnify.ui.theme.Light_Brown

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ToDoScreen(
    viewModel: ToDoViewModel = viewModel(),
    navController: NavController
) {
    var text by remember { mutableStateOf("") }
    val tasks by viewModel.tasks.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackgroundColor)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Enter task") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    viewModel.addTask(text)
                    text = ""
                },
                colors = ButtonDefaults.buttonColors(containerColor = Light_Brown),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.height(56.dp)
            ) {
                Text("Add", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "I'm ready to capture your notes!",
                        fontSize = 20.sp,
                        color = Color.Gray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(Light_Brown, shape = RoundedCornerShape(6.dp))
                    )
                }
            }
        } else {

            AnimatedContent(
                targetState = tasks,
                label = ""
            ) { list ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())   // ← scroll enabled
                ) {
                    list.forEach { task ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + slideInVertically(),
                            exit = fadeOut() + slideOutVertically()
                        ) {
                            TaskItem(
                                task = task,
                                onCheckedChange = { done -> viewModel.toggleDone(task, done) },
                                onDelete = { viewModel.deleteTask(task) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TaskItem(
    task: TaskEntity,
    onCheckedChange: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.Start
        ) {
            Checkbox(
                checked = task.isDone,
                onCheckedChange = onCheckedChange
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = task.text,
                modifier = Modifier
                    .weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Task")
            }
        }
    }
}
