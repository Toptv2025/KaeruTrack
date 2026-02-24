package com.kaeru.app.ui.screens.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kaeru.app.tracking.TrackingViewModel
import com.kaeru.app.ui.components.Material3SettingsGroup
import com.kaeru.app.ui.components.Material3SettingsItem
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.kaeru.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupAndRestore(
    viewModel: TrackingViewModel,
    onBack: () -> Unit
) {
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            viewModel.exportBackup(uri)
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importBackup(uri)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_backup)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {

            Material3SettingsGroup(
                items = listOf(
                    Material3SettingsItem(
                        title = { Text(stringResource(R.string.backup_action)) },
                        icon = painterResource(R.drawable.ic_backup),
                        onClick = {
                            val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm")
                            val fileName = "Kaeru_${LocalDateTime.now().format(formatter)}.json"
                            exportLauncher.launch(fileName)
                        }
                    ),
                    Material3SettingsItem(
                        title = { Text(stringResource(R.string.restore_action)) },
                        icon = painterResource(R.drawable.ic_restore),
                        onClick = {
                            importLauncher.launch(arrayOf("application/json"))
                        }
                    )
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.backup_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}