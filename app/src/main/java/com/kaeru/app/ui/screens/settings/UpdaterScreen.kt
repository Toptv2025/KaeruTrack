package com.kaeru.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kaeru.app.BuildConfig
import com.kaeru.app.R
import com.kaeru.app.data.utils.GithubRelease
import com.kaeru.app.data.utils.UpdateManager
import com.kaeru.app.ui.components.Material3SettingsGroup
import com.kaeru.app.ui.components.Material3SettingsItem
import com.kaeru.app.tracking.TrackingViewModel
import com.kaeru.app.ui.components.IconSwitch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdaterScreen(
    viewModel: TrackingViewModel,
    onBack: () -> Unit
) {
    val checkOnStartEnabled by viewModel.checkUpdatesOnStart.collectAsState()

    var isChecking by remember { mutableStateOf(false) }
    var updateAvailable by remember { mutableStateOf(false) }
    var latestVersion by remember { mutableStateOf<String?>(null) }
    var showChangelog by remember { mutableStateOf(false) }
    var changelogContent by remember { mutableStateOf<String?>(null) }
    var checkError by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    fun performManualCheck() {
        coroutineScope.launch {
            isChecking = true
            updateAvailable = false
            latestVersion = null
            checkError = null

            try {
                val release = withContext<GithubRelease?>(Dispatchers.IO) {
                    val manager = UpdateManager()
                    manager.checkForUpdate()
                }

                if (release != null) {
                    latestVersion = release.tagName
                    changelogContent = release.body
                    updateAvailable = true
                } else {
                    latestVersion = "App atualizado"
                }
            } catch (e: Exception) {
                checkError = "Erro de conexão"
                updateAvailable = false
            }
            isChecking = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.updates)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Material3SettingsGroup(
                title = stringResource(R.string.settings_current_version),
                items = listOf(
                    Material3SettingsItem(
                        title = { Text(
                            stringResource(
                                R.string.version_name,
                                BuildConfig.VERSION_NAME
                            )) },
                        description = { Text(
                            stringResource(
                                R.string.version_code,
                                BuildConfig.VERSION_CODE
                            )) }
                    )
                )
            )

            Spacer(Modifier.height(16.dp))

            Material3SettingsGroup(
                title = stringResource(R.string.updates_preferences),
                items = listOf(
                    Material3SettingsItem(
                        title = { Text(stringResource(R.string.auto_verify)) },
                        icon = painterResource(R.drawable.ic_update),
                        trailingContent = {
                            IconSwitch(
                                checked = checkOnStartEnabled,
                                onCheckedChange = { isEnabled ->
                                    viewModel.toggleCheckUpdatesOnStart(isEnabled)
                                }
                            )
                        },
                        onClick = { viewModel.toggleCheckUpdatesOnStart(!checkOnStartEnabled) }
                    )
                )
            )

            Spacer(Modifier.height(16.dp))
            Material3SettingsGroup(
                title = stringResource(R.string.check_for_updates),
                items = listOf(
                    Material3SettingsItem(
                        icon = painterResource(R.drawable.ic_refresh),
                        title = {
                            val currentLatest = latestVersion
                            val text = when {
                                isChecking -> stringResource(R.string.checking_for_updates)
                                updateAvailable && currentLatest != null -> {
                                    stringResource(
                                        R.string.new_version_available,
                                        currentLatest
                                    )
                                }

                                latestVersion != null && !updateAvailable -> stringResource(R.string.actual_latest)

                                else -> stringResource(R.string.check_for_updates)
                            }
                            Text(text)
                        },
                        trailingContent = {
                            if (isChecking) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else if (updateAvailable) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_download),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        onClick = { if (!isChecking) performManualCheck() }
                    )
                )
            )

            if (checkError != null) {
                Spacer(Modifier.height(8.dp))
                Text(checkError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
            }

            if (updateAvailable && latestVersion != null) {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { showChangelog = !showChangelog },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (showChangelog) stringResource(R.string.hide_notes) else stringResource(
                        R.string.show_notes
                    ))
                }

                if (showChangelog && changelogContent != null) {
                    Card(
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Text(
                            text = changelogContent!!,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}