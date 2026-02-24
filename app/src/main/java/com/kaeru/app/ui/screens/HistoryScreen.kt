package com.kaeru.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle // Import necessário
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaeru.app.tracking.database.TrackingEntity
import com.kaeru.app.tracking.TrackingViewModel
import java.util.concurrent.TimeUnit
import com.kaeru.app.R

@Composable
fun HistoryScreen(
    viewModel: TrackingViewModel,
    onNavigateToResult: (String) -> Unit
) {
    // Coleta a lista do banco de dados (Flow)
    val history by viewModel.historyList.collectAsState()

    // Cores Dinâmicas do Material 3
    val backgroundColor = MaterialTheme.colorScheme.background
    val primaryColor = MaterialTheme.colorScheme.primary
    val onBackground = MaterialTheme.colorScheme.onBackground

    // MUDANÇA 1: LazyColumn agora controla a tela toda para o Header scrollar junto
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 80.dp), // Espaço para navbar
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- CABEÇALHO (Como o primeiro item da lista) ---
        item {
            Column {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp), // Ajustei o padding visualmente
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.packages),
                        color = onBackground,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Badge com a quantidade
                    if (history.isNotEmpty()) {
                        Surface(
                            color = primaryColor,
                            shape = CircleShape,
                            modifier = Modifier
                                .height(32.dp)
                                .widthIn(min = 32.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                Text(
                                    text = history.size.toString(),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- LISTA VAZIA ---
        if (history.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp), // Altura fixa para centralizar visualmente
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_packages_saved),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // --- ITENS DA LISTA ---
            items(history) { item ->
                HistoryCardNew(
                    item = item,
                    onClick = { onNavigateToResult(item.code) }
                )
            }
        }
    }
}

@Composable
fun HistoryCardNew(
    item: TrackingEntity,
    onClick: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val cardColor = MaterialTheme.colorScheme.surfaceContainerLow
    val textColor = MaterialTheme.colorScheme.onSurface
    val subTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val isDelivered = item.lastStatus.contains("entregue", ignoreCase = true)
    val daysCount = remember(item.savedAt) {
        val diff = System.currentTimeMillis() - item.savedAt
        TimeUnit.MILLISECONDS.toDays(diff).coerceAtLeast(0)
    }

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {

            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(R.drawable.ic_package_outlined),
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.description.ifBlank { stringResource(R.string.unnamed_package) },
                            color = textColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.code,
                            color = subTextColor,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = item.lastStatus,
                    color = subTextColor,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Surface(
                color = primaryColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(bottomStart = 16.dp), // Canto arredondado apenas na parte inferior esquerda
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isDelivered) {
                        Icon(
                            Icons.Default.CheckCircle,
                            null,
                            tint = primaryColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = if (isDelivered) stringResource(R.string.delivered) else stringResource(R.string.in_transit),
                        color = primaryColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}