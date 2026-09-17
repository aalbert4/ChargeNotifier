package com.example.chargenotifier.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.example.chargenotifier.data.StatusRepository
import com.example.chargenotifier.shared.DataLayerConstants
import com.google.android.gms.wearable.*
import kotlinx.coroutines.*

class WatchStatusListenerService : WearableListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onMessageReceived(messageEvent: MessageEvent) {
        val path = messageEvent.path
        val isTrue = String(messageEvent.data) == "true"
        
        android.util.Log.d("WatchStatusListener", "Message received: $path = $isTrue")

        when (path) {
            DataLayerConstants.CHARGING_STATUS_PATH -> {
                StatusRepository.setCharging(isTrue)
                showNotification(
                    "Watch Charging Status",
                    if (isTrue) "Watch is Charging" else "Watch Disconnected from Charger",
                    1001
                )
            }
            DataLayerConstants.ON_BODY_STATUS_PATH -> {
                StatusRepository.setOnBody(isTrue)
                showNotification(
                    "Watch On-Body Status",
                    if (isTrue) "Watch is on wrist" else "Watch removed from wrist",
                    1002
                )
            }
        }
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        android.util.Log.d("WatchStatusListener", "onDataChanged: ${dataEvents.count} events")
        dataEvents.forEach { event ->
            if (event.type == DataEvent.TYPE_CHANGED) {
                val path = event.dataItem.uri.path
                android.util.Log.d("WatchStatusListener", "Event path: $path")
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap

                when (path) {
                    DataLayerConstants.CHARGING_STATUS_PATH -> {
                        val isCharging = dataMap.getBoolean(DataLayerConstants.KEY_CHARGING_STATUS)
                        StatusRepository.setCharging(isCharging)
                        showNotification(
                            "Watch Charging Status",
                            if (isCharging) "Watch is Charging" else "Watch Disconnected from Charger",
                            1001
                        )
                    }
                    DataLayerConstants.ON_BODY_STATUS_PATH -> {
                        val isOnBody = dataMap.getBoolean(DataLayerConstants.KEY_ON_BODY_STATUS)
                        StatusRepository.setOnBody(isOnBody)
                        showNotification(
                            "Watch On-Body Status",
                            if (isOnBody) "Watch is on wrist" else "Watch removed from wrist",
                            1002
                        )
                    }
                }
            }
        }
    }

    private fun showNotification(title: String, message: String, notificationId: Int) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val channelId = "watch_status_channel"
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Watch Status Alerts", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)

        // Self-dismiss after 10 seconds
        serviceScope.launch {
            delay(10000)
            notificationManager.cancel(notificationId)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
