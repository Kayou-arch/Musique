package com.kayouarch.musique.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Envelope(
    @SerialName("subsonic-response") val response: SubsonicResponse,
)

@Serializable
data class SubsonicResponse(
    val status: String,
    val error: SubsonicError? = null,
    val albumList2: AlbumList? = null,
)

@Serializable
data class SubsonicError(val code: Int, val message: String = "")

@Serializable
data class AlbumList(val album: List<Album> = emptyList())

@Serializable
data class Album(
    val id: String,
    val name: String,
    val artist: String? = null,
    val coverArt: String? = null,
    val year: Int? = null,
    val songCount: Int? = null,
)

class SubsonicException(message: String) : Exception(message)

fun Envelope.orThrow(): SubsonicResponse {
    if (response.status != "ok") {
        throw SubsonicException(response.error?.message?.ifBlank { null } ?: "Erreur du serveur")
    }
    return response
}
