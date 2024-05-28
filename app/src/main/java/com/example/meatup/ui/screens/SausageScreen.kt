package com.example.meatup.ui.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.meatup.R
import kotlinx.coroutines.delay

@Composable
fun SausageScreen() {
    val context = LocalContext.current
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    var xPosition by remember { mutableFloatStateOf(0f) }
    var yPosition by remember { mutableFloatStateOf(0f) }
    var xVelocity by remember { mutableFloatStateOf(0f) }
    var yVelocity by remember { mutableFloatStateOf(0f) }
    var rotationZ by remember { mutableFloatStateOf(0f) }
    val screenWidth = remember { mutableFloatStateOf(0f) }
    val screenHeight = remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        sensorManager.registerListener(object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    // Invert the x and y values to match screen coordinates
                    xVelocity -= event.values[0] * 2
                    yVelocity += event.values[1] * 2

                    // Calculate rotation based on x and y values
                    rotationZ = event.values[0] * 5 // Adjust the multiplier as needed
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }, accelerometer, SensorManager.SENSOR_DELAY_GAME)

        while (true) {
            delay(16L)
            xPosition += xVelocity
            yPosition += yVelocity

            // Apply some friction
            xVelocity *= 0.9f
            yVelocity *= 0.9f

            // Bounce off the edges symmetrically
            if (xPosition < -screenWidth.floatValue / 2 + 150) {
                xPosition = -screenWidth.floatValue / 2 + 150
                xVelocity = -xVelocity
            } else if (xPosition > screenWidth.floatValue / 2 + 150) {
                xPosition = screenWidth.floatValue / 2 + 150
                xVelocity = -xVelocity
            }

            if (yPosition < -screenHeight.floatValue  + 200) {
                yPosition = -screenHeight.floatValue + 200
                yVelocity = -yVelocity
            } else if (yPosition > screenHeight.floatValue  - 200) {
                yPosition = screenHeight.floatValue  - 200
                yVelocity = -yVelocity
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Box(modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                screenWidth.floatValue = this.size.width
                screenHeight.floatValue = this.size.height
                translationX = xPosition
                translationY = yPosition

            }
        ) {
            Image(
                painter = painterResource(id = R.drawable.sausage),
                contentDescription = "Sausage",
                modifier = Modifier.size(250.dp)
            )
        }
    }
}
