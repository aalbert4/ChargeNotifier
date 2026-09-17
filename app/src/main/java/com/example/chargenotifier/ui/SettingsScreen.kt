package com.example.chargenotifier.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.chargenotifier.data.SettingsRepository
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {
    val chargingEnabled = repository.chargingNotificationsEnabled
    val onBodyEnabled = repository.onBodyNotificationsEnabled
    val dismissDelaySeconds = repository.dismissDelaySeconds
    val lowBatteryThreshold = repository.lowBatteryThreshold

    fun setChargingEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setChargingNotificationsEnabled(enabled) }
    }

    fun setOnBodyEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setOnBodyNotificationsEnabled(enabled) }
    }

    fun setDismissDelay(seconds: Int) {
        viewModelScope.launch { repository.setDismissDelaySeconds(seconds) }
    }

    fun setLowBatteryThreshold(threshold: Int) {
        viewModelScope.launch { repository.setLowBatteryThreshold(threshold) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { SettingsRepository(context) }
    val viewModel = remember { SettingsViewModel(repository) }
    
    val chargingEnabled by viewModel.chargingEnabled.collectAsStateWithLifecycle(initialValue = true)
    val onBodyEnabled by viewModel.onBodyEnabled.collectAsStateWithLifecycle(initialValue = true)
    val dismissDelay by viewModel.dismissDelaySeconds.collectAsStateWithLifecycle(initialValue = 10)
    val lowBatteryThreshold by viewModel.lowBatteryThreshold.collectAsStateWithLifecycle(initialValue = 20)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SettingsGroup(title = "Notifications") {
                ToggleSettingItem(
                    title = "Watch Charging",
                    subtitle = "Notify when watch starts/stops charging",
                    icon = Icons.Rounded.BatteryChargingFull,
                    checked = chargingEnabled,
                    onCheckedChange = { viewModel.setChargingEnabled(it) }
                )
                
                ToggleSettingItem(
                    title = "On Wrist Detection",
                    subtitle = "Notify when watch is put on or removed",
                    icon = Icons.Rounded.Watch,
                    checked = onBodyEnabled,
                    onCheckedChange = { viewModel.setOnBodyEnabled(it) }
                )
            }

            SettingsGroup(title = "Low Battery Alert") {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Notify at: $lowBatteryThreshold%",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(Icons.Rounded.BatteryAlert, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    }
                    
                    Slider(
                        value = lowBatteryThreshold.toFloat(),
                        onValueChange = { viewModel.setLowBatteryThreshold(it.toInt()) },
                        valueRange = 5f..50f,
                        steps = 8, // 5% intervals: 5, 10, 15, 20, 25, 30, 35, 40, 45, 50
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            SettingsGroup(title = "Auto-Dismiss Timer") {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Dismiss after: ${formatSeconds(dismissDelay)}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(Icons.Rounded.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                    
                    Slider(
                        value = dismissDelay.toFloat(),
                        onValueChange = { viewModel.setDismissDelay(it.toInt()) },
                        valueRange = 10f..120f,
                        steps = 10, // (120-10)/10 - 1 = 10 steps for 10s intervals
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("10s", style = MaterialTheme.typography.labelSmall)
                        Text("2m", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, bottom = 4.dp),
            fontWeight = FontWeight.Bold
        )
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                content()
            }
        }
    }
}

@Composable
fun ToggleSettingItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

fun formatSeconds(seconds: Int): String {
    return if (seconds < 60) "${seconds}s" else "${seconds / 60}m ${seconds % 60}s".replace(" 0s", "")
}
