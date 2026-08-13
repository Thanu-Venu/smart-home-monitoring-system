package com.thanu.smarthome.model

data class SensorReading(
    val timestamp: Long = System.currentTimeMillis(),
    val temperature: Double = 0.0,
    val humidity: Double = 0.0,
    val motionDetected: Boolean = false
)