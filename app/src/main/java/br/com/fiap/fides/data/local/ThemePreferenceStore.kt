package br.com.fiap.fides.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}

private val Context.themeDataStore by preferencesDataStore(name = "fides_preferences")

class ThemePreferenceStore(private val context: Context) {
    private object Keys {
        val themeMode = stringPreferencesKey("theme_mode")
    }

    val mode: Flow<ThemeMode> = context.themeDataStore.data.map { preferences ->
        preferences[Keys.themeMode]
            ?.let { stored -> runCatching { ThemeMode.valueOf(stored) }.getOrNull() }
            ?: ThemeMode.SYSTEM
    }

    suspend fun save(mode: ThemeMode) {
        context.themeDataStore.edit { preferences ->
            preferences[Keys.themeMode] = mode.name
        }
    }
}
