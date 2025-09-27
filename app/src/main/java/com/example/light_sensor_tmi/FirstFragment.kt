package com.example.light_sensor_tmi

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.light_sensor_tmi.databinding.FragmentFirstBinding
import com.example.light_sensor_tmi.repository.SensorRepository
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.graphics.toColorInt

class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!
    private lateinit var repository: SensorRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        repository = SensorRepository()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBrutalUI()
        setupChart()
        startDataRefresh()

        binding.buttonFirst.setOnClickListener {
            refreshData()
        }
    }

    private fun setupBrutalUI() {
        // Apply neo-brutalist styling
        binding.textviewFirst.typeface = Typeface.MONOSPACE
        binding.infoText.typeface = Typeface.MONOSPACE
    }

    private fun setupChart() {
        binding.lineChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.text_muted))

            // Neo-brutalist chart styling
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(true)
                gridColor = ContextCompat.getColor(requireContext(), R.color.text_primary)
                gridLineWidth = 2f
                axisLineColor = ContextCompat.getColor(requireContext(), R.color.text_primary)
                axisLineWidth = 3f
                textColor = ContextCompat.getColor(requireContext(), R.color.text_primary)
                textSize = 10f
                typeface = Typeface.MONOSPACE
            }

            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = ContextCompat.getColor(requireContext(), R.color.text_primary)
                gridLineWidth = 2f
                axisLineColor = ContextCompat.getColor(requireContext(), R.color.text_primary)
                axisLineWidth = 3f
                textColor = ContextCompat.getColor(requireContext(), R.color.text_primary)
                textSize = 10f
                typeface = Typeface.MONOSPACE
            }

            axisRight.isEnabled = false
            legend.apply {
                textColor = ContextCompat.getColor(requireContext(), R.color.text_primary)
                textSize = 12f
                typeface = Typeface.MONOSPACE
            }
        }
    }

    private fun startDataRefresh() {
        viewLifecycleOwner.lifecycleScope.launch {
            while (true) {
                refreshData()
                delay(30000) // Refresh every 30 seconds
            }
        }
    }

    private fun refreshData() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Get latest data
            repository.getLatestSensorData().fold(
                onSuccess = { feed ->
                    updateCurrentStatus(feed)
                },
                onFailure = { error ->
                    binding.textviewFirst.text = "ERROR: ${error.message?.uppercase()}"
                }
            )

            // Get historical data for chart
            repository.getSensorHistory(50).fold(
                onSuccess = { feeds ->
                    updateChart(feeds)
                },
                onFailure = { error ->
                    binding.textviewFirst.append("\nCHART ERROR: ${error.message?.uppercase()}")
                }
            )
        }
    }

    private fun updateCurrentStatus(feed: com.example.light_sensor_tmi.api.Feed) {
        val lightLevel = repository.getLightLevel(feed)
        val status = repository.getStatus(feed)
        val stdDev = repository.getStandardDeviation(feed)
        val timeFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        val time = try {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).parse(feed.created_at)?.let {
                timeFormat.format(it)
            } ?: "UNKNOWN TIME"
        } catch (e: Exception) {
            "PARSE ERROR"
        }

        val statusText = """
            STREET LIGHT STATUS: $status
            LIGHT LEVEL: ${lightLevel.toInt()}
            LAST UPDATE: $time
            
            STATUS DEFINITIONS:
            • OFF: LIGHT IS OFF (LOW + STABLE)
            • ON: LIGHT IS ON (HIGH + STABLE)  
            • FLICKER: LIGHT IS FLICKERING (UNSTABLE)
        """.trimIndent()

        binding.textviewFirst.text = statusText

        // Neo-brutalist color coding
        val color = when (status) {
            "OFF" -> ContextCompat.getColor(requireContext(), R.color.status_off)
            "ON" -> ContextCompat.getColor(requireContext(), R.color.status_on)
            "FLICKER" -> ContextCompat.getColor(requireContext(), R.color.status_flicker)
            else -> ContextCompat.getColor(requireContext(), R.color.text_primary)
        }
        binding.textviewFirst.setTextColor(color)

        binding.textviewFirst.setTextColor(color)
    }

    private fun updateChart(feeds: List<com.example.light_sensor_tmi.api.Feed>) {
        val lightEntries = mutableListOf<Entry>()
        val statusEntries = mutableListOf<Entry>()
        val labels = mutableListOf<String>()

        feeds.forEachIndexed { index, feed ->
            val lightLevel = repository.getLightLevel(feed)
            val status = feed.field3?.toFloatOrNull() ?: 0f

            lightEntries.add(Entry(index.toFloat(), lightLevel))
            statusEntries.add(Entry(index.toFloat(), status * 1000)) // Scale status for visibility

            // Format time for x-axis
            val timeLabel = try {
                val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).parse(feed.created_at)
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
            } catch (e: Exception) {
                "${index}"
            }
            labels.add(timeLabel)
        }

        // Neo-brutalist chart styling
        val lightDataSet = LineDataSet(lightEntries, "LIGHT LEVEL").apply {
            color = "#FF5733".toColorInt()
            setCircleColor(ContextCompat.getColor(requireContext(), R.color.blue_muted))
            lineWidth = 4f // Thicker lines
            circleRadius = 5f
            setDrawCircleHole(false)
            valueTextSize = 10f
            valueTextColor = ContextCompat.getColor(requireContext(), R.color.text_primary)
            valueTypeface = Typeface.MONOSPACE
        }

        val statusDataSet = LineDataSet(statusEntries, "STATUS (X1000)").apply {
            color = ContextCompat.getColor(requireContext(), R.color.status_off)
            setCircleColor(ContextCompat.getColor(requireContext(), R.color.status_off))
            lineWidth = 4f // Thicker lines
            circleRadius = 5f
            setDrawCircleHole(false)
            valueTextSize = 10f
            valueTextColor = ContextCompat.getColor(requireContext(), R.color.text_primary)
            valueTypeface = Typeface.MONOSPACE
        }

        val lineData = LineData(lightDataSet, statusDataSet)

        binding.lineChart.apply {
            data = lineData
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.granularity = 1f
            invalidate() // Refresh chart
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
