package com.kayouarch.musique.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kayouarch.musique.data.ServerConfig
import com.kayouarch.musique.data.SettingsStore
import com.kayouarch.musique.data.SubsonicClient
import com.kayouarch.musique.data.SubsonicException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

data class AlbumUi(
    val id: String,
    val title: String,
    val artist: String,
    val year: Int?,
    val coverUrl: String?,
)

sealed interface UiState {
    data object Starting : UiState
    data class Login(val busy: Boolean = false, val error: String? = null) : UiState
    data class Library(
        val albums: List<AlbumUi> = emptyList(),
        val loading: Boolean = true,
        val error: String? = null,
    ) : UiState
}

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val store = SettingsStore(app)
    private var client: SubsonicClient? = null

    private val _state = MutableStateFlow<UiState>(UiState.Starting)
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val saved = store.config.first()
            if (saved == null) _state.value = UiState.Login() else openLibrary(saved)
        }
    }

    fun login(rawUrl: String, user: String, password: String) {
        var url = rawUrl.trim().trimEnd('/')
        if (!url.startsWith("http://") && !url.startsWith("https://")) url = "http://$url"
        if (url.toHttpUrlOrNull() == null) {
            _state.value = UiState.Login(error = "Adresse du serveur invalide")
            return
        }
        if (user.isBlank() || password.isEmpty()) {
            _state.value = UiState.Login(error = "Identifiant et mot de passe requis")
            return
        }
        _state.value = UiState.Login(busy = true)
        viewModelScope.launch {
            try {
                val config = ServerConfig.fromPassword(url, user.trim(), password)
                SubsonicClient(config).ping()
                store.save(config)
                openLibrary(config)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = UiState.Login(error = e.toMessage())
            }
        }
    }

    fun refresh() {
        val c = client ?: return
        viewModelScope.launch { loadAlbums(c) }
    }

    fun logout() {
        viewModelScope.launch {
            store.clear()
            client = null
            _state.value = UiState.Login()
        }
    }

    private suspend fun openLibrary(config: ServerConfig) {
        val c = SubsonicClient(config)
        client = c
        loadAlbums(c)
    }

    private suspend fun loadAlbums(c: SubsonicClient) {
        val current = (_state.value as? UiState.Library) ?: UiState.Library()
        _state.value = current.copy(loading = true, error = null)
        try {
            val albums = c.allAlbums().map { a ->
                AlbumUi(
                    id = a.id,
                    title = a.name,
                    artist = a.artist ?: "Artiste inconnu",
                    year = a.year,
                    coverUrl = a.coverArt?.let { c.coverUrl(it) },
                )
            }
            _state.value = UiState.Library(albums = albums, loading = false)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _state.value = current.copy(loading = false, error = e.toMessage())
        }
    }

    private fun Exception.toMessage(): String = when (this) {
        is SubsonicException -> message ?: "Erreur du serveur"
        is UnknownHostException -> "Serveur introuvable"
        is ConnectException -> "Connexion refusée : le serveur tourne ?"
        is SocketTimeoutException -> "Le serveur ne répond pas"
        is HttpException -> "Erreur HTTP ${code()}"
        else -> message ?: "Erreur inconnue"
    }
}
