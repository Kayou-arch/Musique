package com.kayouarch.musique.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsStore(private val context: Context) {

    private object Keys {
        val URL = stringPreferencesKey("url")
        val USER = stringPreferencesKey("user")
        val SALT = stringPreferencesKey("salt")
        val TOKEN = stringPreferencesKey("token")
    }

    val config: Flow<ServerConfig?> = context.dataStore.data.map { p ->
        val url = p[Keys.URL] ?: return@map null
        val user = p[Keys.USER] ?: return@map null
        val salt = p[Keys.SALT] ?: return@map null
        val token = p[Keys.TOKEN] ?: return@map null
        ServerConfig(url, user, salt, token)
    }

    suspend fun save(config: ServerConfig) {
        context.dataStore.edit { p ->
            p[Keys.URL] = config.url
            p[Keys.USER] = config.user
            p[Keys.SALT] = config.salt
            p[Keys.TOKEN] = config.token
        }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
