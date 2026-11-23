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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.veil.ui.components.VeilTopAppBar
import com.veil.ui.theme.VeilBlack
import com.veil.ui.theme.VeilCard
import com.veil.ui.theme.VeilEmerald
import com.veil.ui.theme.VeilIndigo
import com.veil.ui.theme.VeilSurfaceVariant
import com.veil.ui.theme.VeilTextPrimary
import com.veil.ui.theme.VeilTextSecondary

data class AlbumDisplay(
    val id: String,
    val name: String,
    val itemCount: Int,
    val icon: ImageVector = Icons.Default.Folder
)

@Composable
fun AlbumsScreen(
    onAlbumClick: (AlbumDisplay) -> Unit,
    onCreateAlbum: (String) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var newAlbumName by remember { mutableStateOf("") }

    val defaultAlbums = listOf(
        AlbumDisplay("fav", "Favorites", 24, Icons.Default.Favorite),
        AlbumDisplay("vid", "Videos", 12, Icons.Default.Videocam),
        AlbumDisplay("cloud", "Encrypted Cloud", 88, Icons.Default.Cloud),
        AlbumDisplay("local", "Local Device", 140, Icons.Default.Folder)
    )

    Scaffold(
        topBar = {
            VeilTopAppBar(title = "Albums")
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = VeilEmerald,
                contentColor = VeilBlack
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Album")
            }
        },
        containerColor = VeilBlack
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(defaultAlbums) { album ->
                AlbumCard(album = album, onClick = { onAlbumClick(album) })
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("New Encrypted Album", color = VeilTextPrimary) },
            text = {
                OutlinedTextField(
                    value = newAlbumName,
                    onValueChange = { newAlbumName = it },
                    placeholder = { Text("Album Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newAlbumName.isNotBlank()) {
                            onCreateAlbum(newAlbumName)
                            newAlbumName = ""
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VeilEmerald, contentColor = VeilBlack)
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = VeilTextSecondary)
                }
            },
            containerColor = VeilCard
        )
    }
}

@Composable
fun AlbumCard(album: AlbumDisplay, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(VeilCard)
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.2f)
                .background(VeilSurfaceVariant, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = album.icon,
                contentDescription = album.name,
                tint = VeilEmerald,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = album.name,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            color = VeilTextPrimary
        )
        Text(
            text = "${album.itemCount} items",
            fontSize = 12.sp,
            color = VeilTextSecondary
        )
    }
}
