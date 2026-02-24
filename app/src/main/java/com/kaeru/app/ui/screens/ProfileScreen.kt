package com.kaeru.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.kaeru.app.R
import com.kaeru.app.data.utils.GithubRelease
import com.kaeru.app.data.utils.UpdateManager
import com.kaeru.app.tracking.TrackingViewModel
import com.kaeru.app.ui.components.Material3SettingsGroup
import com.kaeru.app.ui.components.Material3SettingsItem

@Composable
fun ProfileScreen(
    viewModel: TrackingViewModel,
    onSettingsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val name by viewModel.userName.collectAsState()
    val bio by viewModel.userBio.collectAsState()
    val imageUri by viewModel.userAvatar.collectAsState()
    val stats by viewModel.stats.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }
    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.updateAvatar(it.toString()) }
    }
    val checkUpdatesEnabled by viewModel.checkUpdatesOnStart.collectAsState()
    var updateRelease by remember { mutableStateOf<GithubRelease?>(null) }
    val updateManager = remember { UpdateManager() }

    LaunchedEffect(Unit) {
        updateRelease = updateManager.checkForUpdate()
    }

    if (showEditDialog) {
        EditProfileDialog(
            currentName = name,
            currentBio = bio,
            onDismiss = { showEditDialog = false },
            onSave = { newName, newBio ->
                viewModel.updateProfile(newName, newBio)
                showEditDialog = false
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .border(4.dp, MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape)
                    .clickable {
                        photoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
            ) {
                if (imageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Box(
                modifier = Modifier
                    .offset(x = 4.dp, y = 4.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable {
                        photoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                    .border(2.dp, MaterialTheme.colorScheme.background, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = bio,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(onClick = { showEditDialog = true }) {
            Text("Editar perfil")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Material3SettingsGroup(
            title = stringResource(R.string.statistics),
            items = buildList {
                add(
                    Material3SettingsItem(
                        icon = rememberVectorPainter(Icons.Outlined.LocalShipping),
                        title = { Text(stringResource(R.string.items_in_transit)) },
                        trailingContent = {
                            Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                                Text(
                                    text = stats.first.toString(),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        },
                        onClick = {}
                    )
                )

                add(
                    Material3SettingsItem(
                        icon = rememberVectorPainter(Icons.Outlined.CheckCircle),
                        title = { Text(stringResource(R.string.delivered_items)) },
                        trailingContent = {
                            Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                                Text(
                                    text = stats.second.toString(),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        },
                        onClick = {}
                    )
                )
                add(
                    Material3SettingsItem(
                        icon = rememberVectorPainter(Icons.Outlined.Settings),
                        title = { Text(stringResource(R.string.settings)) },
                        showBadge = (checkUpdatesEnabled && updateRelease != null),
                        trailingContent = {
                            Icon(
                                Icons.Outlined.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        onClick = onSettingsClick
                    )
                )
            }
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun EditProfileDialog(
    currentName: String,
    currentBio: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var bio by remember { mutableStateOf(currentBio) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_profile)) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.profile_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text(stringResource(R.string.profile_bio)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(name, bio) }) { Text(stringResource(R.string.save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}