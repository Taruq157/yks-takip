package com.omerfaruk.ykstakip.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class UserInfo(
    val firstName: String,
    val lastName: String,
    val title: String,
    val major: String,
    val displayName: String,
    val examYear: String,
    val obp: String = "100.0"
)

data class NotificationSettings(
    val isEnabled: Boolean = false,
    val selectedDays: Set<Int> = emptySet(), // 1 (Pazartesi) - 7 (Pazar)
    val hour: Int = 8,
    val minute: Int = 0,
    val displayFormat: String = "Days" // "Days" or "MonthsDays"
)

class PreferenceManager(private val context: Context) {

    companion object {
        val FIRST_NAME = stringPreferencesKey("first_name")
        val LAST_NAME = stringPreferencesKey("last_name")
        val USER_TITLE = stringPreferencesKey("user_title")
        val MAJOR = stringPreferencesKey("major")
        val DISPLAY_NAME = stringPreferencesKey("display_name")
        val EXAM_YEAR = stringPreferencesKey("exam_year")
        val OBP_KEY = stringPreferencesKey("obp")
        val IS_ONBOARDING_COMPLETED = booleanPreferencesKey("is_onboarding_completed")
        
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val FONT_SIZE_MULTIPLIER = floatPreferencesKey("font_size_multiplier")

        // Notification Settings
        val NOTIF_ENABLED = booleanPreferencesKey("notif_enabled")
        val NOTIF_DAYS = stringSetPreferencesKey("notif_days")
        val NOTIF_HOUR = intPreferencesKey("notif_hour")
        val NOTIF_MINUTE = intPreferencesKey("notif_minute")
        val NOTIF_FORMAT = stringPreferencesKey("notif_format")
    }

    val userInfo: Flow<UserInfo?> = context.dataStore.data.map { preferences ->
        val fName = preferences[FIRST_NAME]
        val lName = preferences[LAST_NAME]
        val title = preferences[USER_TITLE]
        val major = preferences[MAJOR]
        val displayName = preferences[DISPLAY_NAME]
        val examYear = preferences[EXAM_YEAR]
        val obp = preferences[OBP_KEY] ?: "100.0"
        
        if (fName != null && lName != null && title != null && major != null && displayName != null && examYear != null) {
            UserInfo(fName, lName, title, major, displayName, examYear, obp)
        } else null
    }

    val notificationSettings: Flow<NotificationSettings> = context.dataStore.data.map { preferences ->
        NotificationSettings(
            isEnabled = preferences[NOTIF_ENABLED] ?: false,
            selectedDays = preferences[NOTIF_DAYS]?.map { it.toInt() }?.toSet() ?: emptySet(),
            hour = preferences[NOTIF_HOUR] ?: 8,
            minute = preferences[NOTIF_MINUTE] ?: 0,
            displayFormat = preferences[NOTIF_FORMAT] ?: "Days"
        )
    }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_ONBOARDING_COMPLETED] ?: false
    }

    val themeMode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[THEME_MODE] ?: "System"
    }

    val fontSizeMultiplier: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[FONT_SIZE_MULTIPLIER] ?: 1.0f
    }

    suspend fun saveUserInfo(firstName: String, lastName: String, title: String, major: String, displayName: String, examYear: String, obp: String) {
        context.dataStore.edit { preferences ->
            preferences[FIRST_NAME] = firstName
            preferences[LAST_NAME] = lastName
            preferences[USER_TITLE] = title
            preferences[MAJOR] = major
            preferences[DISPLAY_NAME] = displayName
            preferences[EXAM_YEAR] = examYear
            preferences[OBP_KEY] = obp
            preferences[IS_ONBOARDING_COMPLETED] = true
        }
    }

    suspend fun updateNotificationSettings(settings: NotificationSettings) {
        context.dataStore.edit { preferences ->
            preferences[NOTIF_ENABLED] = settings.isEnabled
            preferences[NOTIF_DAYS] = settings.selectedDays.map { it.toString() }.toSet()
            preferences[NOTIF_HOUR] = settings.hour
            preferences[NOTIF_MINUTE] = settings.minute
            preferences[NOTIF_FORMAT] = settings.displayFormat
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode
        }
    }

    suspend fun setFontSizeMultiplier(multiplier: Float) {
        context.dataStore.edit { preferences ->
            preferences[FONT_SIZE_MULTIPLIER] = multiplier
        }
    }

    // Auth Tokens
    val accessToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[stringPreferencesKey("access_token")]
    }

    suspend fun saveAuthTokens(access: String, refresh: String) {
        context.dataStore.edit { preferences ->
            preferences[stringPreferencesKey("access_token")] = access
            preferences[stringPreferencesKey("refresh_token")] = refresh
        }
    }

    suspend fun clearAuthTokens() {
        context.dataStore.edit { preferences ->
            preferences.remove(stringPreferencesKey("access_token"))
            preferences.remove(stringPreferencesKey("refresh_token"))
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
