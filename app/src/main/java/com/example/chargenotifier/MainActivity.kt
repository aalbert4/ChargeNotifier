package com.example.chargenotifier

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.chargenotifier.data.StatusRepository
import com.example.chargenotifier.ui.SettingsScreen
import com.example.chargenotifier.ui.theme.ChargeNotifierTheme
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChargeNotifierTheme {
                var currentScreen by remember { mutableStateOf("main") }

                androidx.compose.animation.Crossfade(targetState = currentScreen, label = "screen_transition") { screen ->
                    when (screen) {
                        "main" -> MainScreen(onNavigateToSettings = { currentScreen = "settings" })
                        "settings" -> SettingsScreen(onBack = { currentScreen = "main" })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onNavigateToSettings: () -> Unit) {
    val isCharging by StatusRepository.isCharging.collectAsStateWithLifecycle()
    val isOnBody by StatusRepository.isOnBody.collectAsStateWithLifecycle()
    val isConnected by StatusRepository.isConnected.collectAsStateWithLifecycle()
    val batteryLevel by StatusRepository.batteryLevel.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Start the background cloud listener
        val syncIntent = android.content.Intent(context, com.example.chargenotifier.service.CloudStatusListenerService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(syncIntent)
        } else {
            context.startService(syncIntent)
        }
        
        while (true) {
            checkConnection(context)
            delay(5000)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("ChargeNotifier", fontWeight = FontWeight.Black, letterSpacing = 1.sp) },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Rounded.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ConnectionCard(isConnected)

            StatusCard(
                title = "Watch Battery",
                status = "$batteryLevel%",
                icon = when {
                    isCharging -> Icons.Rounded.BatteryChargingFull
                    batteryLevel <= 20 -> Icons.Rounded.BatteryAlert
                    else -> Icons.Rounded.BatteryStd
                },
                color = when {
                    isCharging -> Color(0xFF00C853)
                    batteryLevel <= 20 -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.primary
                }
            )

            StatusCard(
                title = "Charging Status",
                status = if (isCharging) "Charging" else "Disconnected",
                icon = if (isCharging) Icons.Rounded.Power else Icons.Rounded.PowerOff,
                color = if (isCharging) Color(0xFF00C853) else Color.Gray
            )

            StatusCard(
                title = "On Wrist Detection",
                status = if (isOnBody) "On Wrist" else "Off Body",
                icon = if (isOnBody) Icons.Rounded.Watch else Icons.Rounded.WatchOff,
                color = if (isOnBody) Color(0xFF2979FF) else MaterialTheme.colorScheme.secondary
            )
        }
    }
}

private suspend fun checkConnection(context: Context) {
    try {
        val nodes = Wearable.getNodeClient(context).connectedNodes.await()
        StatusRepository.setConnected(nodes.isNotEmpty())
    } catch (e: Exception) {
        StatusRepository.setConnected(false)
    }
}

@Composable
fun ConnectionCard(isConnected: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isConnected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        if (isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isConnected) Icons.Rounded.BluetoothConnected else Icons.Rounded.BluetoothDisabled,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Column {
                Text(
                    text = if (isConnected) "Watch Connected" else "Watch Disconnected",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = if (isConnected) "Everything is synchronized" else "Please check your watch connection",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun StatusCard(title: String, status: String, icon: ImageVector, color: Color) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = status,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = color
                )
            }

            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(color.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun MainScreenPreview() {
    ChargeNotifierTheme {
        MainScreen(onNavigateToSettings = {})
    }
}
