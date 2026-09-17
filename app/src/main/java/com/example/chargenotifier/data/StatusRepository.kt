package com.example.chargenotifier.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object StatusRepository {
    private val _isCharging = MutableStateFlow(false)
    val isCharging: StateFlow<Boolean> = _isCharging

    private val _isOnBody = MutableStateFlow(false)
    val isOnBody: StateFlow<Boolean> = _isOnBody

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val _batteryLevel = MutableStateFlow(100)
    val batteryLevel: StateFlow<Int> = _batteryLevel

    fun setCharging(value: Boolean) {
        _isCharging.value = value
    }

    fun setOnBody(value: Boolean) {
        _isOnBody.value = value
    }

    fun setConnected(value: Boolean) {
        _isConnected.value = value
    }

    fun setBatteryLevel(value: Int) {
        _batteryLevel.value = value
    }
}
