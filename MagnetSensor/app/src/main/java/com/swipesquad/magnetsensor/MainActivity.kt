package com.swipesquad.magnetsensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.swipesquad.magnetsensor.ui.theme.MagnetSensorTheme
import kotlin.math.sqrt

class MainActivity : ComponentActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var magneticSensor: Sensor? = null

    private var _fieldStrength by mutableStateOf(0f)
    val fieldStrength: Float
        get() = _fieldStrength

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        setContent {
            MagnetSensorTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MagneticFieldBar(
                        fieldStrength = fieldStrength,
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxWidth()
                            .padding(32.dp)
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        magneticSensor?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        _fieldStrength = sqrt(x * x + y * y + z * z)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

@Composable
fun MagneticFieldBar(fieldStrength: Float, modifier: Modifier = Modifier) {
    // Typical Earth's field is 25-65 µT, so let's map 0–100µT to 0–100%
    val maxField = 100f
    val progress = (fieldStrength / maxField).coerceIn(0f, 1f)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Magnetic Field Strength")
        Spacer(modifier = Modifier.height(16.dp))
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "%.1f µT (%.0f%%)".format(fieldStrength, progress * 100))
    }
}