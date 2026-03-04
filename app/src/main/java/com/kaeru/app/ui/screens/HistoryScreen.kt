package com.kaeru.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.LocalShipping
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
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.toShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign.Companion.Center
import com.kaeru.app.tracking.utils.DateUtils
import com.kaeru.app.ui.components.TransitCalendarDialog
import kotlin.math.abs
import androidx.compose.runtime.saveable.rememberSaveable

@Composable
fun HistoryScreen(
    viewModel: TrackingViewModel,
    onNavigateToResult: (String) -> Unit
) {
    val history by viewModel.historyList.collectAsState()
    val backgroundColor = MaterialTheme.colorScheme.background
    val primaryColor = MaterialTheme.colorScheme.primary
    val onBackground = MaterialTheme.colorScheme.onBackground
    var currentFilter by rememberSaveable { mutableStateOf(TrackingFilter.IN_TRANSIT) }
    val filteredHistory = remember(history, currentFilter) {
        when (currentFilter) {
            TrackingFilter.IN_TRANSIT -> history.filter {
                !it.lastStatus.contains("entregue", ignoreCase = true) &&
                        !it.lastStatus.contains("delivered", ignoreCase = true)
            }
            TrackingFilter.DELIVERED -> history.filter {
                it.lastStatus.contains("entregue", ignoreCase = true) ||
                        it.lastStatus.contains("delivered", ignoreCase = true)
            }
            TrackingFilter.ALL -> history
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.packages),
                        color = onBackground,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (filteredHistory.isNotEmpty()) {
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
                                    text = filteredHistory.size.toString(),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = currentFilter == TrackingFilter.IN_TRANSIT,
                        onClick = { currentFilter = TrackingFilter.IN_TRANSIT },
                        label = { Text("Em trânsito") },
                        leadingIcon = if (currentFilter == TrackingFilter.IN_TRANSIT) {
                            { Icon(Icons.Outlined.LocalShipping, null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        shape = CircleShape
                    )

                    FilterChip(
                        selected = currentFilter == TrackingFilter.DELIVERED,
                        onClick = { currentFilter = TrackingFilter.DELIVERED },
                        label = { Text("Entregues") },
                        leadingIcon = if (currentFilter == TrackingFilter.DELIVERED) {
                            { Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        shape = CircleShape
                    )

                    FilterChip(
                        selected = currentFilter == TrackingFilter.ALL,
                        onClick = { currentFilter = TrackingFilter.ALL },
                        label = { Text("Todos") },
                        shape = CircleShape
                    )
                }
            }
        }

        if (history.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val emptyText = when(currentFilter) {
                        TrackingFilter.IN_TRANSIT -> "Nenhuma encomenda em trânsito"
                        TrackingFilter.DELIVERED -> "Nenhuma encomenda entregue"
                        TrackingFilter.ALL -> stringResource(R.string.no_packages_saved)
                    }
                    Text(
                        text = emptyText,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(filteredHistory, key = { it.code }) { item ->
                Box(modifier = Modifier.animateItem()) {
                    HistoryCardNew(
                        item = item,
                        onClick = { onNavigateToResult(item.code) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
    val calculatedDays = remember(item.code, item.lastDate, item.firstDate, item.savedAt, isDelivered) {
        DateUtils.calculateDays(item.lastDate, item.firstDate, item.savedAt, isDelivered)
    }
    val expressiveShapes = remember {
        listOf(
            MaterialShapes.Circle,
            MaterialShapes.Square,
            MaterialShapes.Slanted,
            MaterialShapes.Pill,
            MaterialShapes.Arrow,
            MaterialShapes.Pentagon,
            MaterialShapes.Gem,
            MaterialShapes.Sunny,
            MaterialShapes.Cookie4Sided,
            MaterialShapes.Cookie9Sided,
            MaterialShapes.Clover4Leaf,
            MaterialShapes.Clover8Leaf,
        )
    }
    val shapeIndex = remember(item.code) {
        abs(item.code.hashCode()) % expressiveShapes.size
    }
    val dynamicShape = expressiveShapes[shapeIndex].toShape()
    var showCalendar by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = dynamicShape,
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

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            showCalendar = true
                        }
                        .padding(4.dp)
                ) {
                    Surface(
                        color = primaryColor.copy(alpha = 0.15f),
                        shape = CircleShape,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isDelivered) Icons.Default.CheckCircle else Icons.Outlined.LocalShipping,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isDelivered) stringResource(R.string.delivered) else stringResource(R.string.in_transit),
                                color = primaryColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = "há $calculatedDays dias",
                        color = subTextColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = Center
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
    }
    if (showCalendar) {
        TransitCalendarDialog(
            firstDateStr = item.firstDate,
            lastDateStr = item.lastDate,
            savedAt = item.savedAt,
            isDelivered = isDelivered,
            onDismiss = { showCalendar = false }
        )
    }
}

enum class TrackingFilter {
    IN_TRANSIT, DELIVERED, ALL
}