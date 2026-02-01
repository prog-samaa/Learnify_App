package com.example.learnify.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Task
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.learnify.R
import com.example.learnify.data.local.TaskEntity
import com.example.learnify.ui.theme.*
import com.example.learnify.ui.viewModels.ToDoViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ToDoScreen(
    viewModel: ToDoViewModel = viewModel(),
    navController: NavController
) {
    var text by remember { mutableStateOf("") }
    val tasks by viewModel.tasks.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppBackgroundColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Light_Brown,
                        shape = RoundedCornerShape(
                            bottomStart = 24.dp,
                            bottomEnd = 24.dp
                        )
                    )
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = text,
                        onValueChange = { text = it },
                        placeholder = { Text("Enter tasks...") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Task,
                                contentDescription = "Tasks Icon"
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp)),
                        colors = TextFieldDefaults.colors(
                            disabledContainerColor = SecondaryColor,
                            focusedContainerColor = SecondaryColor,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = PrimaryColor
                        ),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            viewModel.addTask(text)
                            text = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.height(56.dp)
                    ) {
                        Text(
                            text = "Add",
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                if (tasks.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.to_do_background),
                                contentDescription = "No Tasks Image",
                                modifier = Modifier.size(220.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "No tasks yet...",
                                fontSize = 20.sp,
                                color = Color.Gray,
                                fontFamily = FontFamily(Font(R.font.playwrite))
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
                                .verticalScroll(rememberScrollState())
                        ) {
                            list.forEach { task ->
                                AnimatedVisibility(
                                    visible = true,
                                    enter = fadeIn() + slideInVertically(),
                                    exit = fadeOut() + slideOutVertically()
                                ) {
                                    TaskItem(
                                        task = task,
                                        onCheckedChange = { done ->
                                            viewModel.toggleDone(task, done)
                                        },
                                        onDelete = {
                                            viewModel.deleteTask(task)
                                        }
                                    )
                                }
                            }
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackgroundColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Checkbox(
                    checked = task.isDone,
                    onCheckedChange = onCheckedChange
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = task.text,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(40.dp)
                        .offset(x = (-8).dp, y = 6.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.trash_icon),
                        contentDescription = "Delete Task",
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Icon(
            painter = painterResource(id = R.drawable.card_icon),
            contentDescription = "Panda Icon",
            tint = Color.Unspecified,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(36.dp)
                .offset(x = 10.dp, y = (-4).dp)
                .rotate(12f)
        )
    }
}
