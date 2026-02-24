package com.kaeru.app.ui.screens.settings

import android.content.res.Configuration
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kaeru.app.R
import com.kaeru.app.tracking.TrackingViewModel
import com.kaeru.app.ui.theme.KaeruTrackTheme
import com.materialkolor.PaletteStyle
import com.materialkolor.rememberDynamicColorScheme

enum class KaeruThemeMode {
    LIGHT, DARK, SYSTEM
}

data class ThemePalette(val name: String, val seedColor: Color)

val PaletteColors = listOf(
    ThemePalette("Dinâmico", Color.Transparent),
    ThemePalette("Crimson", Color(0xFFEC5464)),
    ThemePalette("Rose", Color(0xFFD81B60)),
    ThemePalette("Purple", Color(0xFF8E24AA)),
    ThemePalette("Deep Purple", Color(0xFF5E35B1)),
    ThemePalette("Indigo", Color(0xFF3949AB)),
    ThemePalette("Blue", Color(0xFF1E88E5)),
    ThemePalette("Light Blue", Color(0xFF039BE5)),
    ThemePalette("Cyan", Color(0xFF00ACC1)),
    ThemePalette("Teal", Color(0xFF00897B)),
    ThemePalette("Green", Color(0xFF43A047)),
    ThemePalette("Light Green", Color(0xFF7CB342)),
    ThemePalette("Lime", Color(0xFFC0CA33)),
    ThemePalette("Yellow", Color(0xFFFDD835)),
    ThemePalette("Amber", Color(0xFFFFB300)),
    ThemePalette("Orange", Color(0xFFFB8C00)),
    ThemePalette("Deep Orange", Color(0xFFF4511E)),
    ThemePalette("Brown", Color(0xFF6D4C41)),
    ThemePalette("Grey", Color(0xFF757575)),
    ThemePalette("Blue Grey", Color(0xFF546E7A)),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeScreen(
    viewModel: TrackingViewModel,
    onBack: () -> Unit
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val isAmoled by viewModel.isAmoled.collectAsState()
    val selectedColorInt by viewModel.currentThemeColor.collectAsState()
    val selectedThemeColor = Color(selectedColorInt)

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_theme)) },
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
        val innerPadding = PaddingValues(
            top = padding.calculateTopPadding(),
            bottom = padding.calculateBottomPadding(),
            start = 0.dp,
            end = 0.dp
        )

        if (isLandscape) {
            LandscapeThemeLayout(
                innerPadding = innerPadding,
                themeMode = themeMode,
                onThemeModeChange = { viewModel.setThemeMode(it) },
                isAmoled = isAmoled,
                onAmoledChange = { viewModel.toggleAmoled(it) },
                selectedThemeColor = selectedThemeColor,
                onColorChange = { viewModel.setThemeColor(it.toArgb()) }
            )
        } else {
            PortraitThemeLayout(
                innerPadding = innerPadding,
                themeMode = themeMode,
                onThemeModeChange = { viewModel.setThemeMode(it) },
                isAmoled = isAmoled,
                onAmoledChange = { viewModel.toggleAmoled(it) },
                selectedThemeColor = selectedThemeColor,
                onColorChange = { viewModel.setThemeColor(it.toArgb()) }
            )
        }
    }
}

@Composable
fun PortraitThemeLayout(
    innerPadding: PaddingValues,
    themeMode: KaeruThemeMode,
    onThemeModeChange: (KaeruThemeMode) -> Unit,
    isAmoled: Boolean,
    onAmoledChange: (Boolean) -> Unit,
    selectedThemeColor: Color,
    onColorChange: (Color) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .width(120.dp)
                .height(240.dp),
            contentAlignment = Alignment.Center
        ) {
            ThemeMockupPortrait(
                themeMode = themeMode,
                pureBlack = isAmoled,
                themeColor = selectedThemeColor
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        ThemeControls(
            themeMode = themeMode,
            onThemeModeChange = onThemeModeChange,
            isAmoled = isAmoled,
            onAmoledChange = onAmoledChange,
            selectedThemeColor = selectedThemeColor,
            onColorChange = onColorChange
        )

        Spacer(modifier = Modifier.height(120.dp))
    }
}

@Composable
fun LandscapeThemeLayout(
    innerPadding: PaddingValues,
    themeMode: KaeruThemeMode,
    onThemeModeChange: (KaeruThemeMode) -> Unit,
    isAmoled: Boolean,
    onAmoledChange: (Boolean) -> Unit,
    selectedThemeColor: Color,
    onColorChange: (Color) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        Column(
            modifier = Modifier
                .weight(0.4f)
                .fillMaxHeight()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .heightIn(max = 300.dp),
                contentAlignment = Alignment.Center
            ) {
                ThemeMockupPortrait(
                    themeMode = themeMode,
                    pureBlack = isAmoled,
                    themeColor = selectedThemeColor
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(0.6f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(end = 16.dp, top = 16.dp, bottom = 16.dp)
        ) {
            ThemeControls(
                themeMode = themeMode,
                onThemeModeChange = onThemeModeChange,
                isAmoled = isAmoled,
                onAmoledChange = onAmoledChange,
                selectedThemeColor = selectedThemeColor,
                onColorChange = onColorChange
            )
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun ThemeControls(
    themeMode: KaeruThemeMode,
    onThemeModeChange: (KaeruThemeMode) -> Unit,
    isAmoled: Boolean,
    onAmoledChange: (Boolean) -> Unit,
    selectedThemeColor: Color,
    onColorChange: (Color) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.theme_mode),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ModeCircle(
                        isSelected = themeMode == KaeruThemeMode.SYSTEM,
                        isPureBlack = isAmoled, // Passa estado real
                        targetMode = KaeruThemeMode.SYSTEM,
                        targetPureBlack = isAmoled,
                        showIcon = true, // Ícone de sync original
                        onClick = { onThemeModeChange(KaeruThemeMode.SYSTEM) }
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(32.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                    ModeCircle(
                        isSelected = themeMode == KaeruThemeMode.LIGHT,
                        isPureBlack = false,
                        targetMode = KaeruThemeMode.LIGHT,
                        targetPureBlack = false,
                        showIcon = false,
                        onClick = {
                            onThemeModeChange(KaeruThemeMode.LIGHT)
                            onAmoledChange(false)
                        }
                    )
                    ModeCircle(
                        isSelected = themeMode == KaeruThemeMode.DARK && !isAmoled,
                        isPureBlack = false,
                        targetMode = KaeruThemeMode.DARK,
                        targetPureBlack = false,
                        showIcon = false,
                        onClick = {
                            onThemeModeChange(KaeruThemeMode.DARK)
                            onAmoledChange(false)
                        }
                    )
                    ModeCircle(
                        isSelected = themeMode == KaeruThemeMode.DARK && isAmoled,
                        isPureBlack = false,
                        targetMode = KaeruThemeMode.DARK,
                        targetPureBlack = true,
                        showIcon = false,
                        onClick = {
                            onThemeModeChange(KaeruThemeMode.DARK)
                            onAmoledChange(true)
                        }
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.color_palette),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(PaletteColors) { palette ->
                        val isDynamicPalette = palette.seedColor == Color.Transparent
                        val isSelected = if (isDynamicPalette) {
                            selectedThemeColor == Color(0xFF006C4C)
                        } else {
                            selectedThemeColor == palette.seedColor
                        }

                        PaletteItem(
                            palette = palette,
                            isSelected = isSelected,
                            onClick = {
                                val colorToSave = if (isDynamicPalette) Color(0xFF006C4C) else palette.seedColor
                                onColorChange(colorToSave)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModeCircle(
    isSelected: Boolean,
    isPureBlack: Boolean,
    targetMode: KaeruThemeMode,
    targetPureBlack: Boolean,
    showIcon: Boolean,
    onClick: () -> Unit
) {
    val fillColor = when {
        targetPureBlack -> Color.Black
        targetMode == KaeruThemeMode.LIGHT -> Color(0xFFF9F9F9)
        targetMode == KaeruThemeMode.DARK -> Color(0xFF1C1B1F)
        else -> MaterialTheme.colorScheme.surfaceContainer
    }

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "scale"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .graphicsLayer { scaleX = scale; scaleY = scale }
                .clip(CircleShape)
                .background(fillColor)
                .border(
                    width = if (isSelected) 3.dp else 0.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.inversePrimary else Color.Transparent,
                    shape = CircleShape
                )
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            if (showIcon) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                androidx.compose.animation.AnimatedVisibility(
                    visible = isSelected,
                    enter = fadeIn(tween(300)) + scaleIn(initialScale = 0.3f, animationSpec = spring(Spring.DampingRatioMediumBouncy)),
                    exit = fadeOut(tween(150)) + scaleOut(targetScale = 0.3f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = if (targetPureBlack) Color.White else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PaletteItem(
    palette: ThemePalette,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val seed = if (palette.seedColor == Color.Transparent) MaterialTheme.colorScheme.primary else palette.seedColor

    val previewScheme = rememberDynamicColorScheme(
        seedColor = seed,
        isDark = isSystemDark,
        style = PaletteStyle.TonalSpot
    )

    val cornerRadius by animateDpAsState(
        targetValue = if (isSelected) 48.dp * 0.25f else 24.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "corner"
    )

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 3.dp else 0.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "border"
    )

    val dynamicShape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = Modifier
            .size(48.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(dynamicShape)
            .background(if (palette.seedColor == Color.Transparent) MaterialTheme.colorScheme.surfaceVariant else palette.seedColor)
            .border(borderWidth, MaterialTheme.colorScheme.inversePrimary, dynamicShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (palette.seedColor == Color.Transparent) {
            Icon(Icons.Outlined.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        } else if (isSelected) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
        } else {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val h = size.height
                val w = size.width
                drawRect(color = previewScheme.primaryContainer, topLeft = Offset(0f, 0f), size = Size(w, h/2))
                drawRect(color = previewScheme.secondary, topLeft = Offset(0f, h/2), size = Size(w/2, h/2))
                drawRect(color = previewScheme.tertiary, topLeft = Offset(w/2, h/2), size = Size(w/2, h/2))
            }
        }
    }
}

@Composable
fun ThemeMockupPortrait(
    themeMode: KaeruThemeMode,
    pureBlack: Boolean,
    themeColor: Color
) {
    val isSystemDark = isSystemInDarkTheme()
    val useDark = when (themeMode) {
        KaeruThemeMode.SYSTEM -> isSystemDark
        KaeruThemeMode.DARK -> true
        KaeruThemeMode.LIGHT -> false
    }

    KaeruTrackTheme(
        darkTheme = useDark,
        pureBlack = pureBlack,
        seedColor = themeColor
    ) {
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(Modifier.fillMaxSize()) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Box(Modifier
                        .padding(start = 100.dp)
                        .size(8.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape))
                }
                Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(3) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    MaterialTheme.colorScheme.surfaceContainerHigh.copy(
                                        alpha = 0.6f
                                    )
                                )
                                .padding(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(Modifier
                                .size(24.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    CircleShape
                                ))
                        }
                    }
                }

                Spacer(Modifier.weight(1f))

                Box(Modifier
                    .align(Alignment.End)
                    .padding(8.dp)
                    .size(28.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape))
            }
        }
    }
}