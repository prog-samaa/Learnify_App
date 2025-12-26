package com.example.learnify.ui.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.learnify.data.local.TaskDatabase
import com.example.learnify.data.local.TaskEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ToDoViewModel(application: Application) : AndroidViewModel(application) {

    private val taskDao = TaskDatabase.Companion.getDatabase(application).taskDao()

    private val _tasks = MutableStateFlow<List<TaskEntity>>(emptyList())
    val tasks: StateFlow<List<TaskEntity>> = _tasks

    init {
        viewModelScope.launch {
            taskDao.getAllTasks().observeForever { list ->
                _tasks.value = list
            }
        }
    }

    fun addTask(text: String) {
        if (text.isBlank()) return
        val newTask = TaskEntity(text = text)
        viewModelScope.launch {
            taskDao.insertTask(newTask)
        }
    }

    fun toggleDone(task: TaskEntity, done: Boolean) {
        viewModelScope.launch {
            taskDao.updateTask(task.copy(isDone = done))
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            taskDao.deleteTask(task)
        }
    }

    fun clearAllTasks() {
        viewModelScope.launch {
            taskDao.clearAllTasks()
        }
    }
}