package com.veil.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.veil.core.model.CloudAccount
import com.veil.core.model.CloudProviderType
import com.veil.ui.components.VeilTopAppBar
import com.veil.ui.theme.VeilBlack
import com.veil.ui.theme.VeilBorder
import com.veil.ui.theme.VeilCard
import com.veil.ui.theme.VeilEmerald
import com.veil.ui.theme.VeilSurfaceVariant
import com.veil.ui.theme.VeilTextPrimary
import com.veil.ui.theme.VeilTextSecondary

@Composable
fun CloudAccountsScreen(
    accounts: List<CloudAccount>,
    onAddProvider: () -> Unit
) {
    Scaffold(
        topBar = { VeilTopAppBar(title = "Cloud Destinations") },
        containerColor = VeilBlack
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Storage Summary Card
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(VeilCard)
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Encrypted Vault Usage", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = VeilTextPrimary)
                        Text("1.2 GB / 15 GB", fontSize = 13.sp, color = VeilEmerald)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = 0.08f,
                        color = VeilEmerald,
                        trackColor = VeilSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "All data is encrypted with AES-256-GCM before transport. Your provider cannot see photos or file names.",
                        fontSize = 12.sp,
                        color = VeilTextSecondary
                    )
                }
            }

            item {
                Text(
                    text = "Connected Storage",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = VeilTextSecondary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(accounts) { account ->
                CloudAccountRow(account)
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onAddProvider,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VeilSurfaceVariant,
                        contentColor = VeilTextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = VeilEmerald)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Connect Another Cloud Account")
                }
            }
        }
    }
}

@Composable
fun CloudAccountRow(account: CloudAccount) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(VeilCard)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(VeilSurfaceVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when (account.providerType) {
                    CloudProviderType.LOCAL_SANDBOX -> Icons.Default.Storage
                    else -> Icons.Default.Cloud
                },
                contentDescription = null,
                tint = VeilEmerald,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.size(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(account.accountName, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = VeilTextPrimary)
            Text(
                if (account.isPrimary) "Primary Backup • Ready" else "Secondary Mirror",
                fontSize = 12.sp,
                color = VeilTextSecondary
            )
        }

        Icon(Icons.Default.CloudDone, contentDescription = "Active", tint = VeilEmerald, modifier = Modifier.size(20.dp))
    }
}
