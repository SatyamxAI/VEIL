package com.veil.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.veil.core.model.SyncStatus
import com.veil.ui.theme.VeilEmerald
import com.veil.ui.theme.VeilError
import com.veil.ui.theme.VeilWarning

@Composable
fun BackupStatusBadge(
    status: SyncStatus,
    modifier: Modifier = Modifier
) {
    val (icon, color) = when (status) {
        SyncStatus.SYNCED -> Icons.Default.CloudDone to VeilEmerald
        SyncStatus.SYNCING -> Icons.Default.CloudSync to VeilWarning
        SyncStatus.LOCAL_ONLY -> Icons.Default.CloudQueue to Color.LightGray.copy(alpha = 0.7f)
        SyncStatus.FAILED -> Icons.Default.ErrorOutline to VeilError
    }

    Box(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            .padding(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = status.name,
            tint = color,
            modifier = Modifier.size(14.dp)
        )
    }
}
