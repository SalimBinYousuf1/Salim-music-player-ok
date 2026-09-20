package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.SortOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "salim_settings")

enum class ThemeMode {
    SYSTEM,
    DARK,
    LIGHT
}

class PreferencesManager(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val GAPLESS_PLAYBACK = booleanPreferencesKey("gapless_playback")
        val CROSSFADE_SECONDS = intPreferencesKey("crossfade_seconds")
        val SORT_OPTION = stringPreferencesKey("sort_option")
        val LAST_SONG_ID = longPreferencesKey("last_song_id")
        val LAST_POSITION_MS = longPreferencesKey("last_position_ms")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        when (prefs[PreferencesKeys.THEME_MODE]) {
            ThemeMode.DARK.name -> ThemeMode.DARK
            ThemeMode.LIGHT.name -> ThemeMode.LIGHT
            else -> ThemeMode.DARK // Default to true black dark mode as requested
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.THEME_MODE] = mode.name
        }
    }

    val gaplessPlayback: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.GAPLESS_PLAYBACK] ?: true
    }

    suspend fun setGaplessPlayback(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.GAPLESS_PLAYBACK] = enabled
        }
    }

    val crossfadeSeconds: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.CROSSFADE_SECONDS] ?: 0
    }

    suspend fun setCrossfadeSeconds(seconds: Int) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.CROSSFADE_SECONDS] = seconds
        }
    }

    val sortOption: Flow<SortOption> = context.dataStore.data.map { prefs ->
        val raw = prefs[PreferencesKeys.SORT_OPTION] ?: SortOption.TITLE.name
        try {
            SortOption.valueOf(raw)
        } catch (_: Exception) {
            SortOption.TITLE
        }
    }

    suspend fun setSortOption(option: SortOption) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.SORT_OPTION] = option.name
        }
    }

    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.ONBOARDING_COMPLETED] ?: false
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.ONBOARDING_COMPLETED] = completed
        }
    }

    val lastSongId: Flow<Long?> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.LAST_SONG_ID]
    }

    val lastPositionMs: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.LAST_POSITION_MS] ?: 0L
    }

    suspend fun saveLastPlaybackState(songId: Long, positionMs: Long) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.LAST_SONG_ID] = songId
            prefs[PreferencesKeys.LAST_POSITION_MS] = positionMs
        }
    }
}
