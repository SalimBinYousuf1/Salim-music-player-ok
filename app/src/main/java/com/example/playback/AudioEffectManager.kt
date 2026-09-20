package com.example.playback

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class EqualizerBand(
    val index: Short,
    val centerFreqHz: Int,
    val levelMilliBels: Short,
    val minMilliBels: Short,
    val maxMilliBels: Short
)

data class EqualizerState(
    val isEnabled: Boolean = false,
    val bands: List<EqualizerBand> = emptyList(),
    val bassBoostStrength: Short = 0,
    val currentPreset: String = "Custom"
)

class AudioEffectManager {

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var currentSessionId: Int = 0

    private val _state = MutableStateFlow(EqualizerState())
    val state: StateFlow<EqualizerState> = _state.asStateFlow()

    fun bindAudioSession(sessionId: Int) {
        if (sessionId <= 0 || sessionId == currentSessionId) return
        currentSessionId = sessionId

        release()

        try {
            val eq = Equalizer(0, sessionId).apply {
                enabled = true
            }
            equalizer = eq

            val bb = BassBoost(0, sessionId).apply {
                enabled = true
            }
            bassBoost = bb

            val minLevel = eq.bandLevelRange[0]
            val maxLevel = eq.bandLevelRange[1]
            val numBands = eq.numberOfBands

            val bandList = (0 until numBands).map { i ->
                val shortIdx = i.toShort()
                val freq = eq.getCenterFreq(shortIdx) / 1000 // mHz to Hz
                val level = eq.getBandLevel(shortIdx)
                EqualizerBand(
                    index = shortIdx,
                    centerFreqHz = freq,
                    levelMilliBels = level,
                    minMilliBels = minLevel,
                    maxMilliBels = maxLevel
                )
            }

            val bbStrength = if (bb.strengthSupported) bb.roundedStrength else 0.toShort()

            _state.value = EqualizerState(
                isEnabled = true,
                bands = bandList,
                bassBoostStrength = bbStrength,
                currentPreset = "Flat"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback default state if device equalizer fails to attach
            val fallbackBands = listOf(
                EqualizerBand(0, 60, 0, -1500, 1500),
                EqualizerBand(1, 230, 0, -1500, 1500),
                EqualizerBand(2, 910, 0, -1500, 1500),
                EqualizerBand(3, 3600, 0, -1500, 1500),
                EqualizerBand(4, 14000, 0, -1500, 1500)
            )
            _state.value = EqualizerState(
                isEnabled = true,
                bands = fallbackBands,
                bassBoostStrength = 0,
                currentPreset = "Flat"
            )
        }
    }

    fun setBandLevel(bandIndex: Short, levelMilliBels: Short) {
        try {
            equalizer?.setBandLevel(bandIndex, levelMilliBels)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val updated = _state.value.bands.map { band ->
            if (band.index == bandIndex) band.copy(levelMilliBels = levelMilliBels) else band
        }
        _state.value = _state.value.copy(bands = updated, currentPreset = "Custom")
    }

    fun setBassBoost(strength: Short) {
        try {
            bassBoost?.let {
                if (it.strengthSupported) {
                    it.setStrength(strength)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _state.value = _state.value.copy(bassBoostStrength = strength)
    }

    fun applyPreset(presetName: String) {
        val bands = _state.value.bands
        if (bands.isEmpty()) return

        val gains = when (presetName) {
            "Bass Boost" -> listOf(600, 400, 100, 0, 0)
            "Rock" -> listOf(500, 300, -100, 200, 400)
            "Vocal" -> listOf(-200, 100, 500, 300, -100)
            "Jazz" -> listOf(300, 100, -200, 200, 300)
            "Electronic" -> listOf(500, 300, 0, 300, 500)
            else -> listOf(0, 0, 0, 0, 0) // Flat
        }

        bands.forEachIndexed { i, band ->
            val gain = (gains.getOrNull(i) ?: 0).toShort()
            val clamped = gain.coerceIn(band.minMilliBels, band.maxMilliBels)
            try {
                equalizer?.setBandLevel(band.index, clamped)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val updatedBands = bands.mapIndexed { i, band ->
            val gain = (gains.getOrNull(i) ?: 0).toShort().coerceIn(band.minMilliBels, band.maxMilliBels)
            band.copy(levelMilliBels = gain)
        }

        _state.value = _state.value.copy(bands = updatedBands, currentPreset = presetName)
    }

    fun toggleEnabled() {
        val next = !_state.value.isEnabled
        try {
            equalizer?.enabled = next
            bassBoost?.enabled = next
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _state.value = _state.value.copy(isEnabled = next)
    }

    fun release() {
        try {
            equalizer?.release()
        } catch (_: Exception) {}
        try {
            bassBoost?.release()
        } catch (_: Exception) {}
        equalizer = null
        bassBoost = null
    }
}
