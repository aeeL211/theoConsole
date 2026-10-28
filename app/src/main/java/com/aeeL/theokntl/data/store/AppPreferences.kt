package com.aeeL.theokntl.data.store

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.aeeL.theokntl.core.ConsoleConstants
import com.aeeL.theokntl.domain.feature.PurchaseStampStore
import com.aeeL.theokntl.domain.model.AppSettings
import com.aeeL.theokntl.domain.model.ThemeMode
import com.aeeL.theokntl.domain.model.ThemePalette
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val Context.consoleDataStore: DataStore<Preferences> by preferencesDataStore(name = "theo_console")

private val USERNAME = stringPreferencesKey(ConsoleConstants.USERNAME_KEY)
private val THEME_MODE = stringPreferencesKey("theme_mode")
private val THEME_PALETTE = stringPreferencesKey("theme_palette")
private val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
private val DEBUG_DETAILS = booleanPreferencesKey("debug_details")
private val PURCHASE_STATE = stringPreferencesKey(ConsoleConstants.PURCHASE_STATE_KEY)

@Serializable
data class PurchaseEntry(
    val written: Map<String, Long> = emptyMap(),
)

@Serializable
data class PurchaseStateEnvelope(
    val users: Map<String, PurchaseEntry> = emptyMap(),
)

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext context: Context,
) : PurchaseStampStore {
    private val dataStore = context.consoleDataStore
    private val json = Json { ignoreUnknownKeys = true }

    val settings: Flow<AppSettings> = dataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { preferences ->
            AppSettings(
                username = preferences[USERNAME].orEmpty(),
                themeMode = preferences[THEME_MODE]
                    ?.let { stored -> ThemeMode.entries.firstOrNull { it.name == stored } }
                    ?: ThemeMode.SYSTEM,
                palette = preferences[THEME_PALETTE]
                    ?.let { stored -> ThemePalette.entries.firstOrNull { it.name == stored } }
                    ?: ThemePalette.TEAL,
                dynamicColor = preferences[DYNAMIC_COLOR] ?: false,
                debugDetails = preferences[DEBUG_DETAILS] ?: false,
            )
        }

    val username: Flow<String> = settings
        .map { it.username }
        .distinctUntilChanged()

    suspend fun setUsername(value: String) {
        dataStore.edit { preferences ->
            preferences[USERNAME] = value
        }
    }

    suspend fun clearUsername() {
        dataStore.edit { preferences ->
            preferences.remove(USERNAME)
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode.name
        }
    }

    suspend fun setPalette(palette: ThemePalette) {
        dataStore.edit { preferences ->
            preferences[THEME_PALETTE] = palette.name
        }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[DYNAMIC_COLOR] = enabled
        }
    }

    suspend fun setDebugDetails(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[DEBUG_DETAILS] = enabled
        }
    }

    override suspend fun read(userId: Long): Map<String, Long> {
        val preferences = try {
            dataStore.data.first()
        } catch (error: IOException) {
            return emptyMap()
        }
        val envelope = decodeEnvelope(preferences[PURCHASE_STATE])
        return envelope.users[userId.toString()]?.written.orEmpty()
    }

    override suspend fun write(userId: Long, stamps: Map<String, Long>) {
        dataStore.edit { preferences ->
            val envelope = decodeEnvelope(preferences[PURCHASE_STATE])
            val users = envelope.users.toMutableMap()
            users[userId.toString()] = PurchaseEntry(written = stamps.toMap())
            preferences[PURCHASE_STATE] = json.encodeToString(
                PurchaseStateEnvelope.serializer(),
                PurchaseStateEnvelope(users = users),
            )
        }
    }

    private fun decodeEnvelope(raw: String?): PurchaseStateEnvelope {
        if (raw.isNullOrBlank()) return PurchaseStateEnvelope()
        return runCatching {
            json.decodeFromString(PurchaseStateEnvelope.serializer(), raw)
        }.getOrDefault(PurchaseStateEnvelope())
    }
}
