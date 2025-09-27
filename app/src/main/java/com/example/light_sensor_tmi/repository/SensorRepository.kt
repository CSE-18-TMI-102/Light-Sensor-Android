// File: app/src/main/java/com/example/light_sensor_tmi/repository/SensorRepository.kt
package com.example.light_sensor_tmi.repository

import com.example.light_sensor_tmi.api.Feed
import com.example.light_sensor_tmi.api.ThingSpeakApiClient
import com.example.light_sensor_tmi.api.ThingSpeakResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SensorRepository {
    private val api = ThingSpeakApiClient.service

    // Replace with your actual channel ID
    private val channelId = "3089109" // Your ThingSpeak channel ID
    private val readApiKey: String? = null // Add read API key if channel is private

    suspend fun getLatestSensorData(): Result<Feed> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getLastEntry(channelId, readApiKey)
                if (response.isSuccessful) {
                    response.body()?.let { feed ->
                        Result.success(feed)
                    } ?: Result.failure(Exception("No data received"))
                } else {
                    Result.failure(Exception("API Error: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getSensorHistory(results: Int = 20): Result<List<Feed>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getChannelFeeds(channelId, readApiKey, results)
                if (response.isSuccessful) {
                    response.body()?.feeds?.let { feeds ->
                        Result.success(feeds.filter { it.field1 != null })
                    } ?: Result.failure(Exception("No data received"))
                } else {
                    Result.failure(Exception("API Error: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // Helper functions to parse data
    fun getLightLevel(feed: Feed): Float {
        return feed.field1?.toFloatOrNull() ?: 0f
    }

    fun getStandardDeviation(feed: Feed): Float {
        return feed.field2?.toFloatOrNull() ?: 0f
    }

    fun getStatus(feed: Feed): String {
        return when (feed.field3?.toIntOrNull()) {
            0 -> "OFF"
            1 -> "ON"
            2 -> "FLICKER"
            else -> "UNKNOWN"
        }
    }

    fun getStatusColor(feed: Feed): Int {
        return when (feed.field3?.toIntOrNull()) {
            0 -> android.graphics.Color.RED
            1 -> android.graphics.Color.GREEN
            2 -> android.graphics.Color.YELLOW
            else -> android.graphics.Color.GRAY
        }
    }
}