package com.example.learnify.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TimerViewModel : ViewModel() {

    var timeLeft by mutableStateOf(25 * 60)
        private set

    var isRunning by mutableStateOf(false)
        private set

    var isBreakTime by mutableStateOf(false)
        private set

    private var timerJob: Job? = null

    fun startTimer() {
        if (isRunning) return
        isRunning = true
        timerJob = CoroutineScope(Dispatchers.Main).launch {
            while (timeLeft > 0 && isActive) {
                delay(1000)
                timeLeft--
            }
            isRunning = false
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        isRunning = false
    }

    fun resetTimer() {
        pauseTimer()
        timeLeft = if (isBreakTime) 5 * 60 else 25 * 60
    }

    fun startWork() {
        pauseTimer()
        isBreakTime = false
        timeLeft = 25 * 60
    }

    fun startBreak() {
        pauseTimer()
        isBreakTime = true
        timeLeft = 5 * 60
    }
}
