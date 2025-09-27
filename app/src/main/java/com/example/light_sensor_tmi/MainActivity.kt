package com.example.light_sensor_tmi

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.example.light_sensor_tmi.repository.SensorRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var repository: SensorRepository
    private lateinit var toolbarStatus: TextView
    private lateinit var btnAnalytics: Button
    private lateinit var btnStatus: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        repository = SensorRepository()

        // Initialize views
        toolbarStatus = findViewById(R.id.toolbar_status)
        btnAnalytics = findViewById(R.id.btn_analytics)
        btnStatus = findViewById(R.id.btn_status)

        // Set up navigation
        setupNavigation()

        // Start data fetching
        startDataFetching()
    }

    private fun setupNavigation() {
        btnAnalytics.setOnClickListener {
            try {
                findNavController(R.id.nav_host_fragment).navigate(R.id.FirstFragment)
                updateButtonStates(true)
            } catch (e: Exception) {
                // Handle navigation error
            }
        }

        btnStatus.setOnClickListener {
            try {
                findNavController(R.id.nav_host_fragment).navigate(R.id.SecondFragment)
                updateButtonStates(false)
            } catch (e: Exception) {
                // Handle navigation error
            }
        }
    }

    private fun updateButtonStates(analyticsSelected: Boolean) {
        if (analyticsSelected) {
            btnAnalytics.setBackgroundColor(getColor(android.R.color.holo_blue_bright))
            btnStatus.setBackgroundColor(getColor(android.R.color.holo_orange_dark))
        } else {
            btnAnalytics.setBackgroundColor(getColor(android.R.color.holo_blue_dark))
            btnStatus.setBackgroundColor(getColor(android.R.color.holo_orange_light))
        }
    }

    private fun startDataFetching() {
        lifecycleScope.launch {
            while (true) {
                fetchSensorData()
                delay(30000) // Fetch every 30 seconds
            }
        }
    }

    private suspend fun fetchSensorData() {
        repository.getLatestSensorData().fold(
            onSuccess = { feed ->
                val status = repository.getStatus(feed)
                val lightLevel = repository.getLightLevel(feed)

                runOnUiThread {
                    toolbarStatus.text = "$status | ${lightLevel.toInt()}"

                    // Update status color
                    val color = when (status) {
                        "OFF" -> android.R.color.holo_red_dark
                        "ON" -> android.R.color.holo_green_dark
                        "FLICKER" -> android.R.color.holo_orange_dark
                        else -> android.R.color.darker_gray
                    }
                    toolbarStatus.setBackgroundColor(getColor(color))
                }
            },
            onFailure = { error ->
                runOnUiThread {
                    toolbarStatus.text = "ERROR"
                    toolbarStatus.setBackgroundColor(getColor(android.R.color.holo_red_dark))
                }
            }
        )
    }
}
