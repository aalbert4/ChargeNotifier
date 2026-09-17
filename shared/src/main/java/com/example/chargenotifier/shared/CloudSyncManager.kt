package com.example.chargenotifier.shared

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

object CloudSyncManager {
    private val database = Firebase.database.reference.child("watch_status")

    fun updateStatus(isCharging: Boolean, isOnBody: Boolean, batteryLevel: Int) {
        val status = mapOf(
            "isCharging" to isCharging,
            "isOnBody" to isOnBody,
            "batteryLevel" to batteryLevel,
            "timestamp" to System.currentTimeMillis()
        )
        database.setValue(status)
    }

    fun observeStatus(onUpdate: (isCharging: Boolean, isOnBody: Boolean, batteryLevel: Int) -> Unit) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isCharging = snapshot.child("isCharging").getValue(Boolean::class.java) ?: false
                val isOnBody = snapshot.child("isOnBody").getValue(Boolean::class.java) ?: false
                val batteryLevel = snapshot.child("batteryLevel").getValue(Int::class.java) ?: 100
                onUpdate(isCharging, isOnBody, batteryLevel)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
