package com.example.chargenotifier.wear.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.chargenotifier.shared.CloudSyncManager
import com.example.chargenotifier.wear.data.DataLayerManager
import com.example.chargenotifier.wear.sensors.StatusMonitor
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.combine

class WatchStatusService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var statusMonitor: StatusMonitor
    private lateinit var dataLayerManager: DataLayerManager

    override fun onCreate() {
        super.onCreate()
        statusMonitor = StatusMonitor(this)
        dataLayerManager = DataLayerManager(this)
        
        startForeground(NOTIFICATION_ID, createNotification())
        
        statusMonitor.start()

        serviceScope.launch {
            combine(
                statusMonitor.isCharging,
                statusMonitor.isOnBody,
                statusMonitor.batteryLevel
            ) { charging, onBody, battery ->
                Triple(charging, onBody, battery)
            }.collect { (isCharging, isOnBody, batteryLevel) ->
                // Update Firebase
                CloudSyncManager.updateStatus(isCharging, isOnBody, batteryLevel)
                // Update Local Data Layer
                dataLayerManager.updateChargingStatus(isCharging)
                dataLayerManager.updateOnBodyStatus(isOnBody)
            }
        }
    }

    private fun createNotification(): Notification {
        val channelId = "watch_status_monitor"
        val channelName = "Watch Status Background Monitor"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
        notificationManager.createNotificationChannel(channel)

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("ChargeNotifier")
            .setContentText("Monitoring watch status in background")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        statusMonitor.stop()
        serviceScope.cancel()
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
    }
}
