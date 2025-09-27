// File: app/src/main/java/com/example/light_sensor_tmi/api/ThingSpeakService.kt
package com.example.light_sensor_tmi.api

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// Data classes for ThingSpeak API responses
data class ThingSpeakResponse(
    val channel: Channel,
    val feeds: List<Feed>
)

data class Channel(
    val id: Int,
    val name: String,
    val description: String?,
    val field1: String?,
    val field2: String?,
    val field3: String?,
    val created_at: String,
    val updated_at: String,
    val last_entry_id: Int
)

data class Feed(
    val created_at: String,
    val entry_id: Int,
    val field1: String?, // Light Level
    val field2: String?, // Standard Deviation
    val field3: String?  // Status (0=OFF, 1=ON, 2=FLICKER)
)

// Retrofit interface
interface ThingSpeakService {
    @GET("channels/{channelId}/feeds.json")
    suspend fun getChannelFeeds(
        @Path("channelId") channelId: String,
        @Query("api_key") apiKey: String? = null,
        @Query("results") results: Int = 20
    ): Response<ThingSpeakResponse>

    @GET("channels/{channelId}/feeds/last.json")
    suspend fun getLastEntry(
        @Path("channelId") channelId: String,
        @Query("api_key") apiKey: String? = null
    ): Response<Feed>
}

// API Client
object ThingSpeakApiClient {
    private const val BASE_URL = "https://api.thingspeak.com/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service: ThingSpeakService = retrofit.create(ThingSpeakService::class.java)
}