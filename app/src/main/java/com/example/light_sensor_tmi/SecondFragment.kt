// File: app/src/main/java/com/example/light_sensor_tmi/SecondFragment.kt
package com.example.light_sensor_tmi

import android.Manifest
import android.animation.ObjectAnimator
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.light_sensor_tmi.databinding.FragmentSecondBinding
import com.example.light_sensor_tmi.notification.SensorNotificationManager
import com.example.light_sensor_tmi.repository.SensorRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    private val binding get() = _binding!!
    private lateinit var repository: SensorRepository
    private lateinit var notificationManager: SensorNotificationManager
    private var currentStatus = "UNKNOWN"
    private var isAnimating = false

    companion object {
        private const val NOTIFICATION_PERMISSION_CODE = 1001
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        repository = SensorRepository()
        notificationManager = SensorNotificationManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestNotificationPermission()
        startStatusMonitoring()

//        binding.testNotificationButton.setOnClickListener {
//            notificationManager.sendTestNotification()
//        }

        binding.refreshButton.setOnClickListener {
            refreshStatus()
        }
    }

    private fun startStatusMonitoring() {
        viewLifecycleOwner.lifecycleScope.launch {
            while (true) {
                refreshStatus()
                delay(15000) // Check every 15 seconds for faster notifications
            }
        }
    }

    private fun refreshStatus() {
        if (isAnimating) return

        viewLifecycleOwner.lifecycleScope.launch {
            repository.getLatestSensorData().fold(
                onSuccess = { feed ->
                    val newStatus = repository.getStatus(feed)
                    val lightLevel = repository.getLightLevel(feed)

                    updateStatusDisplay(newStatus, feed.created_at, lightLevel.toInt())

                    // Check for status change and send notification
                    if (newStatus != currentStatus) {
                        notificationManager.checkAndNotifyStatusChange(newStatus)
                        animateStatusChange()
                    }

                    currentStatus = newStatus
                },
                onFailure = { error ->
                    updateErrorDisplay(error.message ?: "Connection Error")
                }
            )
        }
    }

    private fun updateStatusDisplay(status: String, timestamp: String, lightLevel: Int) {
        val timeFormat = SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault())
        val time = try {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).parse(timestamp)?.let {
                timeFormat.format(it)
            } ?: "Unknown time"
        } catch (e: Exception) {
            "Parse error"
        }

        binding.statusText.text = status
        binding.lastUpdateText.text = "Last updated: $time\nLight level: $lightLevel"

        // Update card background based on status with soft colors
        val (backgroundColor, textColor) = when (status) {
            "OFF" -> Pair(R.drawable.status_off_background, ContextCompat.getColor(requireContext(), R.color.text_on_primary))
            "ON" -> Pair(R.drawable.status_on_background, ContextCompat.getColor(requireContext(), R.color.text_on_primary))
            "FLICKER" -> Pair(R.drawable.status_flicker_background, ContextCompat.getColor(requireContext(), R.color.text_primary))
            else -> Pair(R.drawable.soft_card_background, ContextCompat.getColor(requireContext(), R.color.text_primary))
        }

        binding.statusCard.setBackgroundResource(backgroundColor)
        binding.statusText.setTextColor(textColor)

        // Subtle flicker animation for FLICKER status
        if (status == "FLICKER" && !isAnimating) {
            startFlickerAnimation()
        }
    }

    private fun updateErrorDisplay(error: String) {
        binding.statusText.text = "Error"
        binding.lastUpdateText.text = "Connection failed: $error"
        binding.statusCard.setBackgroundResource(R.drawable.soft_card_background)
        binding.statusText.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_off))
    }

    private fun animateStatusChange() {
        isAnimating = true

        // Gentle pulse animation for soft aesthetic
        val scaleX = ObjectAnimator.ofFloat(binding.statusCard, "scaleX", 1f, 1.05f, 1f)
        val scaleY = ObjectAnimator.ofFloat(binding.statusCard, "scaleY", 1f, 1.05f, 1f)

        scaleX.duration = 800
        scaleY.duration = 800
        scaleX.interpolator = AccelerateDecelerateInterpolator()
        scaleY.interpolator = AccelerateDecelerateInterpolator()

        scaleX.start()
        scaleY.start()

        viewLifecycleOwner.lifecycleScope.launch {
            delay(800)
            isAnimating = false
        }
    }

    private fun startFlickerAnimation() {
        if (isAnimating) return
        isAnimating = true

        viewLifecycleOwner.lifecycleScope.launch {
            repeat(4) { // Gentle flicker 4 times
                binding.statusCard.alpha = 0.7f
                delay(200)
                binding.statusCard.alpha = 1f
                delay(200)
            }
            isAnimating = false
        }
    }

    private fun requestNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_CODE
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
