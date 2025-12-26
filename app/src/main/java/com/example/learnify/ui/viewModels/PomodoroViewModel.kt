package com.example.learnify.ui.viewModels

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learnify.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PomodoroViewModel : ViewModel() {

    var timeLeft by mutableStateOf(25 * 60)
        private set

    var totalTime by mutableStateOf(25 * 60)
        private set

    var isRunning by mutableStateOf(false)
        private set

    var isBreakTime by mutableStateOf(false)
        private set

    private var timerJob: Job? = null

    fun startTimer(context: Context) {
        if (isRunning) return
        isRunning = true
        timerJob = viewModelScope.launch {
            while (timeLeft > 0 && isActive) {
                delay(1000)
                timeLeft--
            }
            isRunning = false
            if (timeLeft == 0) {
                playAlarm(context)
            }
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        isRunning = false
    }

    fun resetTimer() {
        pauseTimer()
        timeLeft = if (isBreakTime) 5 * 60 else 25 * 60
        totalTime = timeLeft
    }

    fun startWork() {
        pauseTimer()
        isBreakTime = false
        timeLeft = 25 * 60
        totalTime = timeLeft
    }

    fun startBreak() {
        pauseTimer()
        isBreakTime = true
        timeLeft = 5 * 60
        totalTime = timeLeft
    }

    fun playAlarm(context: Context) {
        val mediaPlayer = MediaPlayer.create(context, R.raw.pomodoro_sound)
        mediaPlayer.start()
    }
}