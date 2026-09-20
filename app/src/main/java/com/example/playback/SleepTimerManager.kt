package com.example.playback

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SleepTimerManager(
    private val scope: CoroutineScope,
    private val onFadeVolume: (Float) -> Unit,
    private val onTimerExpired: () -> Unit
) {
    private var timerJob: Job? = null

    private val _remainingSeconds = MutableStateFlow<Long?>(null)
    val remainingSeconds: StateFlow<Long?> = _remainingSeconds.asStateFlow()

    fun startTimer(minutes: Int) {
        cancelTimer()
        val totalSeconds = (minutes * 60).toLong()
        _remainingSeconds.value = totalSeconds

        timerJob = scope.launch(Dispatchers.Default) {
            var currentSecs = totalSeconds
            val fadeDurationSecs = 30L.coerceAtMost(totalSeconds / 2)

            while (isActive && currentSecs > 0) {
                delay(1000)
                currentSecs--
                _remainingSeconds.value = currentSecs

                // In the final 30 seconds, audibly fade out volume
                if (currentSecs <= fadeDurationSecs && fadeDurationSecs > 0) {
                    val progress = (currentSecs.toFloat() / fadeDurationSecs.toFloat()).coerceIn(0f, 1f)
                    onFadeVolume(progress)
                }
            }

            if (isActive) {
                _remainingSeconds.value = null
                onTimerExpired()
                onFadeVolume(1f)
            }
        }
    }

    fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
        _remainingSeconds.value = null
        onFadeVolume(1f)
    }
}
