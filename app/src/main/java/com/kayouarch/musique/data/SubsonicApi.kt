package com.kayouarch.musique.data

import retrofit2.http.GET
import retrofit2.http.Query

interface SubsonicApi {
    @GET("rest/ping.view")
    suspend fun ping(): Envelope

    @GET("rest/getAlbumList2.view")
    suspend fun albumList(
        @Query("type") type: String,
        @Query("size") size: Int,
        @Query("offset") offset: Int,
    ): Envelope
}
