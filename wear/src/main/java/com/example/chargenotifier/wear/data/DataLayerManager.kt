package com.example.chargenotifier.wear.data

import android.content.Context
import android.util.Log
import com.example.chargenotifier.shared.DataLayerConstants
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

class DataLayerManager(context: Context) {

    private val dataClient = Wearable.getDataClient(context)
    private val messageClient = Wearable.getMessageClient(context)
    private val capabilityClient = Wearable.getCapabilityClient(context)

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private suspend fun sendMessage(path: String, data: Boolean) {
        try {
            // Find nodes that have the "phone_app" capability
            val capabilityInfo = capabilityClient
                .getCapability("phone_app", CapabilityClient.FILTER_REACHABLE)
                .await()
            
            val nodes = capabilityInfo.nodes
            if (nodes.isEmpty()) {
                Log.e("DataLayerManager", "No phone nodes found with capability 'phone_app'")
                return
            }

            val payload = if (data) "true".toByteArray() else "false".toByteArray()
            
            nodes.forEach { node ->
                messageClient.sendMessage(node.id, path, payload).await()
                Log.d("DataLayerManager", "Message sent to ${node.displayName}: $path = $data")
            }
        } catch (e: Exception) {
            Log.e("DataLayerManager", "Error sending message", e)
        }
    }

    suspend fun updateChargingStatus(isCharging: Boolean) {
        sendMessage(DataLayerConstants.CHARGING_STATUS_PATH, isCharging)
        
        // Also keep DataClient as a backup for when the app opens
        try {
            val request = PutDataMapRequest.create(DataLayerConstants.CHARGING_STATUS_PATH).apply {
                dataMap.putBoolean(DataLayerConstants.KEY_CHARGING_STATUS, isCharging)
                dataMap.putLong(DataLayerConstants.KEY_TIMESTAMP, System.currentTimeMillis())
            }.asPutDataRequest().setUrgent()
            dataClient.putDataItem(request).await()
        } catch (e: Exception) {}
    }

    suspend fun updateOnBodyStatus(isOnBody: Boolean) {
        sendMessage(DataLayerConstants.ON_BODY_STATUS_PATH, isOnBody)
        
        try {
            val request = PutDataMapRequest.create(DataLayerConstants.ON_BODY_STATUS_PATH).apply {
                dataMap.putBoolean(DataLayerConstants.KEY_ON_BODY_STATUS, isOnBody)
                dataMap.putLong(DataLayerConstants.KEY_TIMESTAMP, System.currentTimeMillis())
            }.asPutDataRequest().setUrgent()
            dataClient.putDataItem(request).await()
        } catch (e: Exception) {}
    }

    suspend fun checkConnection() {
        try {
            val nodes = Wearable.getNodeClient(dataClient.applicationContext).connectedNodes.await()
            _isConnected.value = nodes.isNotEmpty()
        } catch (e: Exception) {
            Log.e("DataLayerManager", "Error checking connection", e)
            _isConnected.value = false
        }
    }
}
