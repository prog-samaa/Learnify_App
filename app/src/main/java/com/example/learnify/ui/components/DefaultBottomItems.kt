package com.example.learnify.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timer
import com.example.learnify.data.model.BottomNavItem

fun DefaultBottomItems(): List<BottomNavItem> = listOf(
    BottomNavItem(route = "home", title = "Home", icon = Icons.Default.Home),
    BottomNavItem(route = "pomodoro", title = "Pomodoro", icon = Icons.Default.Timer),
    BottomNavItem(route = "todo", title = "To Do", icon = Icons.Default.Checklist),
    BottomNavItem(route = "you", title = "You", icon = Icons.Default.Person)
)
