package com.example.chargenotifier.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material3.*
import com.example.chargenotifier.wear.data.DataLayerManager
import com.example.chargenotifier.wear.sensors.StatusMonitor
import com.example.chargenotifier.wear.service.WatchStatusService
import com.example.chargenotifier.wear.ui.theme.ChargeNotifierWearTheme

class MainActivity : ComponentActivity() {
    private lateinit var statusMonitor: StatusMonitor
    private lateinit var dataLayerManager: DataLayerManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        statusMonitor = StatusMonitor(this)
        dataLayerManager = DataLayerManager(this)

        // Start background service
        val serviceIntent = android.content.Intent(this, WatchStatusService::class.java)
        startForegroundService(serviceIntent)

        setContent {
            ChargeNotifierWearTheme {
                MainScreen(statusMonitor, dataLayerManager)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        statusMonitor.start()
    }

    override fun onStop() {
        super.onStop()
        statusMonitor.stop()
    }
}

@Composable
fun MainScreen(statusMonitor: StatusMonitor, dataLayerManager: DataLayerManager) {
    val context = LocalContext.current
    val isCharging by statusMonitor.isCharging.collectAsStateWithLifecycle()
    val isOnBody by statusMonitor.isOnBody.collectAsStateWithLifecycle()
    val batteryLevel by statusMonitor.batteryLevel.collectAsStateWithLifecycle()
    val isConnected by dataLayerManager.isConnected.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(android.Manifest.permission.BODY_SENSORS)
        
        while (true) {
            dataLayerManager.checkConnection()
            kotlinx.coroutines.delay(5000)
        }
    }

    AppScaffold {
        ScreenScaffold { _ ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    StatusChip(
                        label = "Battery",
                        value = "$batteryLevel%",
                        icon = Icons.Rounded.BatteryChargingFull,
                        color = if (isCharging) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    )

                    StatusChip(
                        label = "Charging",
                        value = if (isCharging) "Connected" else "Disconnected",
                        icon = if (isCharging) Icons.Rounded.Power else Icons.Rounded.PowerOff,
                        color = if (isCharging) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )

                    StatusChip(
                        label = "On Body",
                        value = if (isOnBody) "Detected" else "Missing",
                        icon = if (isOnBody) Icons.Rounded.Watch else Icons.Rounded.WatchOff,
                        color = if (isOnBody) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )

                    ConnectionIndicator(isConnected)
                }
            }
        }
    }
}

@Composable
fun StatusChip(label: String, value: String, icon: ImageVector, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(20.dp))
            .padding(vertical = 8.dp, horizontal = 12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun ConnectionIndicator(isConnected: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .padding(1.dp)
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(color = if (isConnected) Color.Green else Color.Red)
            }
        }
        Text(
            text = if (isConnected) "Phone Connected" else "Phone Offline",
            style = MaterialTheme.typography.labelMedium,
            color = if (isConnected) Color.Green else Color.Red
        )
    }
}
