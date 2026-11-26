package com.kayouarch.musique.data

import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class SubsonicClient(private val config: ServerConfig) {

    private val baseUrl = config.url.trimEnd('/') + "/"

    private val json = Json { ignoreUnknownKeys = true }

    private fun HttpUrl.Builder.addAuth(): HttpUrl.Builder = apply {
        addQueryParameter("u", config.user)
        addQueryParameter("t", config.token)
        addQueryParameter("s", config.salt)
        addQueryParameter("v", "1.16.1")
        addQueryParameter("c", "Musique")
        addQueryParameter("f", "json")
    }

    private val http = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request()
            val url = request.url.newBuilder().addAuth().build()
            chain.proceed(request.newBuilder().url(url).build())
        }
        .build()

    private val api: SubsonicApi = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(http)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(SubsonicApi::class.java)

    suspend fun ping() {
        api.ping().orThrow()
    }

    /** Récupère tous les albums, par pages de 500. */
    suspend fun allAlbums(): List<Album> {
        val result = mutableListOf<Album>()
        var offset = 0
        while (true) {
            val page = api.albumList("alphabeticalByName", PAGE, offset)
                .orThrow().albumList2?.album.orEmpty()
            result += page
            if (page.size < PAGE) break
            offset += PAGE
        }
        return result
    }

    /** URL stable (sel fixe) : Coil peut donc la mettre en cache. */
    fun coverUrl(coverId: String, size: Int = 300): String =
        baseUrl.toHttpUrl().newBuilder()
            .addPathSegments("rest/getCoverArt.view")
            .addQueryParameter("id", coverId)
            .addQueryParameter("size", size.toString())
            .addAuth()
            .build()
            .toString()

    private companion object {
        const val PAGE = 500
    }
}
