package com.veil.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.veil.core.model.MediaItem
import com.veil.core.model.MediaType
import com.veil.core.model.SyncStatus
import com.veil.ui.components.BackupStatusBadge
import com.veil.ui.components.VeilTopAppBar
import com.veil.ui.theme.VeilBlack
import com.veil.ui.theme.VeilCard
import com.veil.ui.theme.VeilEmerald
import com.veil.ui.theme.VeilSurfaceVariant
import com.veil.ui.theme.VeilTextPrimary
import com.veil.ui.theme.VeilTextSecondary

enum class GalleryFilter {
    ALL, PHOTOS, VIDEOS, FAVORITES, BACKED_UP
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    vaultName: String,
    items: List<MediaItem>,
    isSyncing: Boolean = false,
    onItemClick: (MediaItem) -> Unit,
    onLockVault: () -> Unit,
    onTriggerSync: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf(GalleryFilter.ALL) }
    var gridColumns by remember { mutableIntStateOf(3) }

    val filteredItems = remember(items, selectedFilter) {
        when (selectedFilter) {
            GalleryFilter.ALL -> items
            GalleryFilter.PHOTOS -> items.filter { it.mediaType == MediaType.IMAGE }
            GalleryFilter.VIDEOS -> items.filter { it.mediaType == MediaType.VIDEO }
            GalleryFilter.FAVORITES -> items.filter { it.isFavorite }
            GalleryFilter.BACKED_UP -> items.filter { it.syncStatus == SyncStatus.SYNCED }
        }
    }

    Scaffold(
        topBar = {
            VeilTopAppBar(
                title = vaultName,
                navigationIcon = {
                    IconButton(onClick = onLockVault) {
                        Icon(Icons.Default.Lock, contentDescription = "Lock Vault", tint = VeilEmerald)
                    }
                },
                actions = {
                    IconButton(onClick = onTriggerSync) {
                        Icon(
                            Icons.Default.Sync,
                            contentDescription = "Sync",
                            tint = if (isSyncing) VeilEmerald else VeilTextSecondary
                        )
                    }
                    IconButton(onClick = {
                        gridColumns = if (gridColumns == 3) 4 else if (gridColumns == 4) 2 else 3
                    }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Toggle Grid", tint = VeilTextSecondary)
                    }
                }
            )
        },
        containerColor = VeilBlack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filter Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GalleryFilter.values().forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter.name.lowercase().capitalize(), fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VeilEmerald,
                            selectedLabelColor = VeilBlack,
                            containerColor = VeilSurfaceVariant,
                            labelColor = VeilTextSecondary
                        ),
                        border = null
                    )
                }
            }

            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No media found", color = VeilTextSecondary, fontSize = 16.sp)
                        Text("Encrypted items will appear here", color = VeilTextSecondary.copy(alpha = 0.6f), fontSize = 13.sp)
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(gridColumns),
                    contentPadding = PaddingValues(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredItems, key = { it.id }) { item ->
                        MediaThumbnailTile(
                            item = item,
                            onClick = { onItemClick(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MediaThumbnailTile(
    item: MediaItem,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(4.dp))
            .background(VeilCard)
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = item.uri,
            contentDescription = item.fileName,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Video Indicator
        if (item.mediaType == MediaType.VIDEO) {
            Icon(
                imageVector = Icons.Default.PlayCircle,
                contentDescription = "Video",
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(28.dp)
            )
        }

        // Favorite Indicator
        if (item.isFavorite) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Favorite",
                tint = VeilEmerald,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp)
                    .size(16.dp)
            )
        }

        // Backup Status Badge
        BackupStatusBadge(
            status = item.syncStatus,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(4.dp)
        )
    }
}
