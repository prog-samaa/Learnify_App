package com.example.learnify.ui.screens

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class Task(
    val id: Int,
    val text: String,
    val isDone: Boolean = false
)

class ToDoViewModel : ViewModel() {

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    private var currentId = 0

    fun addTask(text: String) {
        if (text.isBlank()) return
        val newTask = Task(id = currentId++, text = text)
        _tasks.update { it + newTask }
    }

    fun toggleDone(task: Task, done: Boolean) {
        _tasks.update { list ->
            list.map { if (it.id == task.id) it.copy(isDone = done) else it }
        }
    }

    fun deleteTask(task: Task) {
        _tasks.update { list -> list.filter { it.id != task.id } }
    }
}
