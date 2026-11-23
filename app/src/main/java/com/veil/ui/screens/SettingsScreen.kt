package com.veil.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.veil.ui.components.VeilTopAppBar
import com.veil.ui.theme.VeilBlack
import com.veil.ui.theme.VeilCard
import com.veil.ui.theme.VeilEmerald
import com.veil.ui.theme.VeilError
import com.veil.ui.theme.VeilTextPrimary
import com.veil.ui.theme.VeilTextSecondary

@Composable
fun SettingsScreen(
    onLockVault: () -> Unit,
    onClearCache: () -> Unit
) {
    var wifiOnly by remember { mutableStateOf(true) }
    var chargingOnly by remember { mutableStateOf(false) }
    var biometricEnabled by remember { mutableStateOf(true) }

    Scaffold(
        topBar = { VeilTopAppBar(title = "Settings") },
        containerColor = VeilBlack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section: Backup Constraints
            Text("AUTOMATIC BACKUP", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VeilTextSecondary)
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = VeilCard)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    SettingToggleRow(
                        title = "Back up on Wi-Fi only",
                        subtitle = "Prevents uploads on mobile cellular data",
                        icon = Icons.Default.Wifi,
                        checked = wifiOnly,
                        onCheckedChange = { wifiOnly = it }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    SettingToggleRow(
                        title = "Back up only while charging",
                        subtitle = "Preserves device battery life",
                        icon = Icons.Default.Security,
                        checked = chargingOnly,
                        onCheckedChange = { chargingOnly = it }
                    )
                }
            }

            // Section: Security
            Text("VAULT SECURITY", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VeilTextSecondary)
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = VeilCard)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    SettingToggleRow(
                        title = "Biometric Unlock",
                        subtitle = "Unlock vault with fingerprint or face",
                        icon = Icons.Default.Fingerprint,
                        checked = biometricEnabled,
                        onCheckedChange = { biometricEnabled = it }
                    )
                }
            }

            // Section: Actions
            Text("DATA & MAINTENANCE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VeilTextSecondary)
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = VeilCard)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onClearCache,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = VeilTextSecondary)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Clear Decrypted Local Cache", color = VeilTextPrimary)
                    }

                    Button(
                        onClick = onLockVault,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VeilError.copy(alpha = 0.85f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = VeilTextPrimary)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Lock Vault Immediately", color = VeilTextPrimary)
                    }
                }
            }

            // Footer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("VEIL v1.0.0", fontSize = 12.sp, color = VeilTextSecondary)
                Text("Private by design. Simple by nature.", fontSize = 11.sp, color = VeilTextSecondary.copy(alpha = 0.6f))
            }
        }
    }
}

@Composable
fun SettingToggleRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = VeilEmerald, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = VeilTextPrimary)
            Text(subtitle, fontSize = 12.sp, color = VeilTextSecondary)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = VeilBlack,
                checkedTrackColor = VeilEmerald
            )
        )
    }
}
