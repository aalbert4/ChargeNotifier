package com.example.chargenotifier.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.chargenotifier.data.SettingsRepository
import com.example.chargenotifier.data.StatusRepository
import com.example.chargenotifier.shared.CloudSyncManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class CloudStatusListenerService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var settingsRepository: SettingsRepository
    private var lastCharging: Boolean? = null
    private var lastOnBody: Boolean? = null

    override fun onCreate() {
        super.onCreate()
        settingsRepository = SettingsRepository(this)
        startForeground(FOREGROUND_ID, createForegroundNotification())
    }

    private fun createForegroundNotification(): android.app.Notification {
        val channelId = "cloud_sync_service"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(channelId, "Watch Sync Service", NotificationManager.IMPORTANCE_LOW)
        notificationManager.createNotificationChannel(channel)

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("ChargeNotifier")
            .setContentText("Syncing with watch in background")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        CloudSyncManager.observeStatus { isCharging, isOnBody, batteryLevel ->
            // Update repository for UI
            StatusRepository.setCharging(isCharging)
            StatusRepository.setOnBody(isOnBody)
            StatusRepository.setBatteryLevel(batteryLevel)

            serviceScope.launch {
                val chargingEnabled = settingsRepository.chargingNotificationsEnabled.first()
                val onBodyEnabled = settingsRepository.onBodyNotificationsEnabled.first()
                val threshold = settingsRepository.lowBatteryThreshold.first()
                val alreadyNotified = settingsRepository.lowBatteryNotified.first()
                val delaySeconds = settingsRepository.dismissDelaySeconds.first()

                // Low Battery Notification Logic - Normal notification (no auto-dismiss)
                if (batteryLevel <= threshold) {
                    if (!alreadyNotified) {
                        showNotification(
                            "Watch Low Battery",
                            "Your watch battery is at $batteryLevel%",
                            2003,
                            null // Pass null to disable auto-dismiss
                        )
                        settingsRepository.setLowBatteryNotified(true)
                    }
                } else if (batteryLevel > threshold + 5 || isCharging) {
                    // Reset when battery is safely above threshold or charging
                    settingsRepository.setLowBatteryNotified(false)
                }

                // Trigger notifications on change if enabled - respects auto-dismiss
                if (lastCharging != null && lastCharging != isCharging && chargingEnabled) {
                    showNotification(
                        "Watch Charging Status",
                        if (isCharging) "Watch is Charging" else "Watch Disconnected from Charger",
                        2001,
                        delaySeconds
                    )
                }
                if (lastOnBody != null && lastOnBody != isOnBody && onBodyEnabled) {
                    showNotification(
                        "Watch On-Body Status",
                        if (isOnBody) "Watch is on wrist" else "Watch removed from wrist",
                        2002,
                        delaySeconds
                    )
                }
                
                lastCharging = isCharging
                lastOnBody = isOnBody
            }
        }
        return START_STICKY
    }

    private fun showNotification(title: String, message: String, notificationId: Int, delaySeconds: Int?) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "watch_status_channel_cloud"
        
        val channel = NotificationChannel(channelId, "Watch Status Alerts (Cloud)", NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)

        // Only self-dismiss if a delay is provided
        if (delaySeconds != null) {
            serviceScope.launch {
                delay(delaySeconds * 1000L)
                notificationManager.cancel(notificationId)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        private const val FOREGROUND_ID = 3001
    }
}
