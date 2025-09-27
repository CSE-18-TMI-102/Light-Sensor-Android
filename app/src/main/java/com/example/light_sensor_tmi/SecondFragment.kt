//package com.example.light_sensor_tmi
//
//import android.os.Bundle
//import androidx.fragment.app.Fragment
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.navigation.fragment.findNavController
//import com.example.light_sensor_tmi.databinding.FragmentSecondBinding
//
///**
// * A simple [Fragment] subclass as the second destination in the navigation.
// */
//class SecondFragment : Fragment() {
//
//    private var _binding: FragmentSecondBinding? = null
//
//    // This property is only valid between onCreateView and
//    // onDestroyView.
//    private val binding get() = _binding!!
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//
//        _binding = FragmentSecondBinding.inflate(inflater, container, false)
//        return binding.root
//
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        binding.buttonSecond.setOnClickListener {
//            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
//        }
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//}

// File: app/src/main/java/com/example/light_sensor_tmi/SecondFragment.kt
package com.example.light_sensor_tmi

import android.Manifest
import android.animation.ObjectAnimator
import android.content.pm.PackageManager
import android.graphics.Typeface
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
        setupBrutalUI()
        startStatusMonitoring()

        binding.testNotificationButton.setOnClickListener {
            notificationManager.sendTestNotification()
        }

        binding.refreshButton.setOnClickListener {
            refreshStatus()
        }
    }

    private fun setupBrutalUI() {
        // Set bold, chunky fonts
        binding.statusText.typeface = Typeface.DEFAULT_BOLD
        binding.statusLabel.typeface = Typeface.DEFAULT_BOLD
        binding.lastUpdateText.typeface = Typeface.MONOSPACE

        // Initial state
        binding.statusText.text = "LOADING..."
        binding.statusLabel.text = "STREET LIGHT STATUS"
        binding.lastUpdateText.text = "INITIALIZING SYSTEM..."
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
                    updateErrorDisplay(error.message ?: "CONNECTION ERROR")
                }
            )
        }
    }

    private fun updateStatusDisplay(status: String, timestamp: String, lightLevel: Int) {
        val timeFormat = SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault())
        val time = try {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).parse(timestamp)?.let {
                timeFormat.format(it)
            } ?: "UNKNOWN TIME"
        } catch (e: Exception) {
            "PARSE ERROR"
        }

        binding.statusText.text = status
        binding.lastUpdateText.text = "LAST UPDATE: $time\nLIGHT LEVEL: $lightLevel"

        // Set neo-brutalist colors and backgrounds
        val (backgroundColor, textColor) = when (status) {
            "OFF" -> Pair(R.drawable.brutal_button_off, ContextCompat.getColor(requireContext(), R.color.brutal_black))
            "ON" -> Pair(R.drawable.brutal_button_on, ContextCompat.getColor(requireContext(), R.color.brutal_black))
            "FLICKER" -> Pair(R.drawable.brutal_button_flicker, ContextCompat.getColor(requireContext(), R.color.brutal_black))
            else -> Pair(R.drawable.brutal_card_background, ContextCompat.getColor(requireContext(), R.color.brutal_black))
        }

        binding.statusCard.setBackgroundResource(backgroundColor)
        binding.statusText.setTextColor(textColor)
        binding.statusLabel.setTextColor(textColor)

        // Flicker animation for FLICKER status
        if (status == "FLICKER" && !isAnimating) {
            startFlickerAnimation()
        }
    }

    private fun updateErrorDisplay(error: String) {
        binding.statusText.text = "ERROR"
        binding.lastUpdateText.text = "CONNECTION FAILED: $error"
        binding.statusCard.setBackgroundResource(R.drawable.brutal_card_background)
        binding.statusText.setTextColor(ContextCompat.getColor(requireContext(), R.color.brutal_red))
    }

    private fun animateStatusChange() {
        isAnimating = true

        // Pulse animation
        val scaleX = ObjectAnimator.ofFloat(binding.statusCard, "scaleX", 1f, 1.1f, 1f)
        val scaleY = ObjectAnimator.ofFloat(binding.statusCard, "scaleY", 1f, 1.1f, 1f)

        scaleX.duration = 600
        scaleY.duration = 600
        scaleX.interpolator = AccelerateDecelerateInterpolator()
        scaleY.interpolator = AccelerateDecelerateInterpolator()

        scaleX.start()
        scaleY.start()

        viewLifecycleOwner.lifecycleScope.launch {
            delay(600)
            isAnimating = false
        }
    }

    private fun startFlickerAnimation() {
        if (isAnimating) return
        isAnimating = true

        viewLifecycleOwner.lifecycleScope.launch {
            repeat(6) { // Flicker 6 times
                binding.statusCard.alpha = 0.3f
                delay(150)
                binding.statusCard.alpha = 1f
                delay(150)
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
