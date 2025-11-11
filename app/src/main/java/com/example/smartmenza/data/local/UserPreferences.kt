package com.example.smartmenza.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("user_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        private val KEY_EMAIL = stringPreferencesKey("email")
        private val KEY_NAME = stringPreferencesKey("ime")
        private val KEY_ROLE = stringPreferencesKey("uloga")
        private val KEY_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_LOGGED_IN] ?: false
    }

    val userName: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_NAME]
    }

    suspend fun saveUser(ime: String, email: String, uloga: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_EMAIL] = email
            prefs[KEY_NAME] = ime
            prefs[KEY_ROLE] = uloga
            prefs[KEY_LOGGED_IN] = true
        }
    }

    suspend fun logout() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}