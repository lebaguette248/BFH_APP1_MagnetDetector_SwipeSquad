package com.swipesquad

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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
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

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        setContent {
            MagnetSensorTheme {
                // Sensitivity state, default 100µT, range 20–200µT
                var maxField by remember { mutableStateOf(100f) }
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxWidth()
                            .padding(32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        MagneticFieldBar(
                            fieldStrength = fieldStrength,
                            maxField = maxField,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        SensitivitySlider(
                            maxField = maxField,
                            onValueChange = { maxField = it }
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
fun MagneticFieldBar(fieldStrength: Float, maxField: Float, modifier: Modifier = Modifier) {
    val progress = (fieldStrength / maxField).coerceIn(0f, 1f)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Magnetic Field Strength")
        Spacer(modifier = Modifier.height(16.dp))
        FlatProgressBar(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "%.1f µT (%.0f%% of max %.0f µT)".format(fieldStrength, progress * 100, maxField))
    }
}

@Composable
fun FlatProgressBar(progress: Float, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Color.Gray)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress)
                .background(Color(0xFF6200EE)) // Your progress color
        )
    }
}

@Composable
fun SensitivitySlider(
    maxField: Float,
    onValueChange: (Float) -> Unit,
    minField: Float = 20f,
    maxFieldLimit: Float = 5000f
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Sensitivity (max µT for 100%)")
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            value = maxField,
            onValueChange = onValueChange,
            valueRange = minField..maxFieldLimit,
            steps = 20
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Current max: ${maxField.toInt()} µT")
    }
}

@Preview(showBackground = true, name = "MagneticFieldBar - 60% at 60µT of 100µT")
@Composable
fun PreviewMagneticFieldBar() {
    MagnetSensorTheme {
        MagneticFieldBar(
            fieldStrength = 60f,
            maxField = 100f,
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
        )
    }
}

@Preview(showBackground = true, name = "SensitivitySlider - 100µT")
@Composable
fun PreviewSensitivitySlider() {
    MagnetSensorTheme {
        SensitivitySlider(
            maxField = 100f,
            onValueChange = {},
            minField = 20f,
            maxFieldLimit = 200f
        )
    }
}