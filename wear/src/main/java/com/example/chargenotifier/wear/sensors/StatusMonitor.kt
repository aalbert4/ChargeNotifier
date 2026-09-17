package com.example.chargenotifier.wear.sensors

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.BatteryManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class StatusMonitor(private val context: Context) {

    private val _isCharging = MutableStateFlow(false)
    val isCharging: StateFlow<Boolean> = _isCharging

    private val _isOnBody = MutableStateFlow(false)
    val isOnBody: StateFlow<Boolean> = _isOnBody

    private val _batteryLevel = MutableStateFlow(100)
    val batteryLevel: StateFlow<Int> = _batteryLevel

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val offBodySensor = sensorManager.getDefaultSensor(34) // TYPE_LOW_LATENCY_OFFBODY_DETECT

    private val chargingReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            _isCharging.value = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL
            
            val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            if (level != -1 && scale != -1) {
                _batteryLevel.value = (level * 100 / scale.toFloat()).toInt()
            }
        }
    }

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event?.sensor?.type == 34) {
                // 1.0 = On-body (on wrist), 0.0 = Off-body
                _isOnBody.value = event.values[0] == 1f
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    fun start() {
        // Initial charging status and battery level
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            context.registerReceiver(null, ifilter)
        }
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        _isCharging.value = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
        
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        if (level != -1 && scale != -1) {
            _batteryLevel.value = (level * 100 / scale.toFloat()).toInt()
        }

        context.registerReceiver(chargingReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        offBodySensor?.let {
            sensorManager.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    fun stop() {
        context.unregisterReceiver(chargingReceiver)
        sensorManager.unregisterListener(sensorListener)
    }
}
