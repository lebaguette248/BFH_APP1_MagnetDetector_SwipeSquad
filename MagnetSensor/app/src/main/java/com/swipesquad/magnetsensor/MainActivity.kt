package com.swipesquad.magnetsensor

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.swipesquad.magnetsensor.ui.theme.MagnetSensorTheme
import org.json.JSONObject
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

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        setContent {
            MagnetSensorTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                            .padding(32.dp),
                        verticalArrangement = Arrangement.SpaceBetween, // distributes items vertically
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        MagneticFieldBar(
                            fieldStrength = fieldStrength,
                            modifier = Modifier.fillMaxWidth()
                        )

                        LogbookButton(
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
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

fun sendLogbookIntent(context: Context) {
    val log = JSONObject()
    log.put("task", "Metalldetektor")
    log.put("solution", "Test")

    val intent = Intent("ch.apprun.intent.LOG").apply {
        putExtra("ch.apprun.logmessage", log.toString())
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }

    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Log.e("Logger", "LogBook application is not installed on this device.")
    }
}

@Composable
fun LogbookButton(modifier: Modifier) {
    val context = LocalContext.current

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = {
                sendLogbookIntent(context)
            }
        ) {
            Text("Test Intent")
        }
    }
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
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp),
            color = ProgressIndicatorDefaults.linearColor,
            trackColor = ProgressIndicatorDefaults.linearTrackColor,
            strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "%.1f µT (%.0f%%)".format(fieldStrength, progress * 100))
    }
}