package com.kaeru.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import com.kaeru.app.tracking.TrackingViewModel
import com.kaeru.app.ui.components.Material3SettingsGroup
import com.kaeru.app.ui.components.Material3SettingsItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceScreen(
    viewModel: TrackingViewModel,
    onBack: () -> Unit,
    onEditColorsClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Temas e Aparência") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Material3SettingsGroup(
                title = "Customização",
                items = buildList {
                    add(
                        Material3SettingsItem(
                            icon = rememberVectorPainter(Icons.Outlined.ColorLens),
                            title = { Text("Cores do App") },
                            description = { Text("Escolha a cor principal da interface") },
                            onClick = onEditColorsClick
                        )
                    )
                }
            )
        }
    }
}