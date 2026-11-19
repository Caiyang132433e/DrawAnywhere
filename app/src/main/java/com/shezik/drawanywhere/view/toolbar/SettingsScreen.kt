@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class,
)

package com.shezik.drawanywhere.view.toolbar

import android.app.Activity
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shezik.drawanywhere.BuildConfig
import com.shezik.drawanywhere.DrawViewModel
import com.shezik.drawanywhere.R
import com.shezik.drawanywhere.model.PRESET_COLORS
import com.shezik.drawanywhere.model.StylusButtonAction
import com.shezik.drawanywhere.model.StylusButtonScheme

private val pressureThresholdOptions = listOf(0.65f, 0.75f, 0.85f, 0.95f)

@Composable
fun SettingsScreen(
    viewModel: DrawViewModel,
    modifier: Modifier = Modifier,
    onClose: (() -> Unit)? = null,
    onChooseSaveLocation: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val activity = LocalContext.current as? Activity
    val close: () -> Unit = onClose ?: { activity?.finish() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = close) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = paddingValues.calculateTopPadding() + 8.dp,
                end = 16.dp,
                bottom = paddingValues.calculateBottomPadding() + 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                SettingsSection(title = stringResource(R.string.settings_toolbar_section)) {
                    SettingsSegmentedRow(
                        title = stringResource(R.string.toolbar_orientation_mode),
                        options = listOf(
                            stringResource(R.string.horizontal),
                            stringResource(R.string.vertical),
                        ),
                        selectedIndex = toolbarOrientationValues.indexOf(uiState.toolbarOrientationMode)
                            .coerceAtLeast(0),
                        onSelected = { index ->
                            viewModel.setToolbarOrientationMode(toolbarOrientationValues[index])
                        },
                    )
                    HorizontalDivider()
                    SettingsSwitchRow(
                        title = stringResource(R.string.canvas_visible_on_start),
                        checked = uiState.visibleOnStart,
                        onCheckedChange = viewModel::setVisibleOnStart,
                    )
                    HorizontalDivider()
                    SettingsSwitchRow(
                        title = stringResource(R.string.clear_on_hiding_canvas),
                        checked = uiState.autoClearCanvas,
                        onCheckedChange = viewModel::setAutoClearCanvas,
                    )
                    HorizontalDivider()
                    SettingsSwitchRow(
                        title = stringResource(R.string.monet_color),
                        subtitle = stringResource(R.string.monet_color_desc),
                        checked = uiState.dynamicColor,
                        onCheckedChange = viewModel::setDynamicColor,
                    )
                    HorizontalDivider()
                    SaveLocationRow(
                        exportTreeUri = uiState.exportTreeUri,
                        onChooseSaveLocation = onChooseSaveLocation,
                        onReset = viewModel::resetExportLocation,
                    )
                }
            }

            item {
                SettingsSection(title = stringResource(R.string.settings_toolbar_buttons)) {
                    Text(
                        text = stringResource(R.string.settings_toolbar_buttons_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    )
                    HorizontalDivider()
                    ToolbarButtonsSettings(
                        uiState = uiState,
                        onToggle = viewModel::setFirstLayerButton,
                        onReset = viewModel::resetFirstLayerButtons,
                    )
                }
            }

            item {
                SettingsSection(title = stringResource(R.string.settings_drawing_section)) {
                    SettingsSwitchRow(
                        title = stringResource(R.string.finger_drawing),
                        checked = uiState.fingerDrawingEnabled,
                        onCheckedChange = viewModel::setFingerDrawingEnabled,
                    )
                }
            }

            item {
                SettingsSection(title = stringResource(R.string.stylus_settings)) {
                    SettingsDropdownRow(
                        title = stringResource(R.string.stylus_button_scheme),
                        items = stylusSchemeLabels(),
                        selectedIndex = StylusButtonScheme.entries.indexOf(uiState.stylusButtonScheme),
                        onSelected = { index ->
                            viewModel.setStylusButtonScheme(StylusButtonScheme.entries[index])
                        },
                    )
                    if (uiState.stylusButtonScheme != StylusButtonScheme.Disabled) {
                        HorizontalDivider()
                        SettingsDropdownRow(
                            title = stringResource(R.string.stylus_primary_button_action),
                            items = stylusActionLabels(),
                            selectedIndex = StylusButtonAction.entries.indexOf(uiState.stylusPrimaryButtonAction),
                            onSelected = { index ->
                                viewModel.setStylusPrimaryButtonAction(StylusButtonAction.entries[index])
                            },
                        )
                        HorizontalDivider()
                        SettingsDropdownRow(
                            title = stringResource(R.string.stylus_secondary_button_action),
                            items = stylusActionLabels(),
                            selectedIndex = StylusButtonAction.entries.indexOf(uiState.stylusSecondaryButtonAction),
                            onSelected = { index ->
                                viewModel.setStylusSecondaryButtonAction(StylusButtonAction.entries[index])
                            },
                        )
                    }
                    HorizontalDivider()
                    SettingsSwitchRow(
                        title = stringResource(R.string.pressure_eraser),
                        checked = uiState.pressureEraserEnabled,
                        onCheckedChange = viewModel::setPressureEraserEnabled,
                    )
                    if (uiState.pressureEraserEnabled) {
                        HorizontalDivider()
                        SettingsDropdownRow(
                            title = stringResource(R.string.pressure_eraser_threshold),
                            items = pressureThresholdLabels(),
                            selectedIndex = pressureThresholdOptions.indexOf(
                                pressureThresholdOption(uiState.pressureEraserThreshold)
                            ),
                            onSelected = { index ->
                                viewModel.setPressureEraserThreshold(pressureThresholdOptions[index])
                            },
                        )
                    }
                    HorizontalDivider()
                    StylusCycleColorEditor(
                        colors = uiState.stylusCycleColors,
                        onAddColor = viewModel::addStylusCycleColor,
                        onRemoveColor = viewModel::removeStylusCycleColor,
                        onReset = viewModel::resetStylusCycleColors,
                    )
                }
            }

            item {
                SettingsSection(title = stringResource(R.string.settings_app_section)) {
                    AboutContent()
                }
            }
        }
    }
}

@Composable
fun FloatingSettingsWindow(
    viewModel: DrawViewModel,
    onChooseSaveLocation: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val configuration = LocalConfiguration.current
    val windowWidth = minOf(configuration.screenWidthDp.dp * 0.92f, 720.dp)
    val windowHeight = minOf(configuration.screenHeightDp.dp * 0.88f, 900.dp)

    Box(
        modifier = modifier.padding(12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = windowWidth)
                .height(windowHeight)
                .clip(RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 12.dp,
        ) {
            SettingsScreen(
                viewModel = viewModel,
                onClose = onClose,
                onChooseSaveLocation = onChooseSaveLocation,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp),
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
            ),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    subtitle: String? = null,
    icon: ImageVector? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Switch(checked = checked, onCheckedChange = null)
    }
}

@Composable
private fun SettingsDropdownRow(
    title: String,
    items: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = items.getOrElse(selectedIndex) { "" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            items.forEachIndexed { index, label ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        expanded = false
                        onSelected(index)
                    },
                )
            }
        }
    }
}

@Composable
private fun SettingsSegmentedRow(
    title: String,
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        options.forEachIndexed { index, label ->
            FilterChip(
                selected = index == selectedIndex,
                onClick = { onSelected(index) },
                label = { Text(label) },
            )
        }
    }
}

@Composable
private fun SaveLocationRow(
    exportTreeUri: String?,
    onChooseSaveLocation: () -> Unit,
    onReset: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onChooseSaveLocation() }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.save_location),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = exportTreeUri?.toSaveLocationSummary()
                        ?: stringResource(R.string.save_location_default_summary),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextButton(onClick = onChooseSaveLocation, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.choose_folder))
            }
            TextButton(onClick = onReset, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.restore_default))
            }
        }
    }
}

@Composable
private fun ToolbarButtonsSettings(
    uiState: com.shezik.drawanywhere.UiState,
    onToggle: (String, Boolean) -> Unit,
    onReset: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        toolbarButtonCatalog.forEachIndexed { index, meta ->
            SettingsSwitchRow(
                title = stringResource(meta.labelRes),
                checked = meta.id in uiState.firstDrawerButtons,
                onCheckedChange = { checked -> onToggle(meta.id, checked) },
                icon = meta.icon,
            )
            if (index != toolbarButtonCatalog.lastIndex) {
                HorizontalDivider(modifier = Modifier.padding(start = 52.dp))
            }
        }
        HorizontalDivider()
        TextButton(
            onClick = onReset,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        ) {
            Text(stringResource(R.string.restore_default))
        }
    }
}

@Composable
private fun StylusCycleColorEditor(
    colors: List<Color>,
    onAddColor: (Color) -> Unit,
    onRemoveColor: (Color) -> Unit,
    onReset: () -> Unit,
) {
    var selectedColor by remember { mutableStateOf(colors.firstOrNull() ?: PRESET_COLORS.first()) }
    var colorInput by remember { mutableStateOf(selectedColor.toHexString(includeAlpha = false)) }
    val parsedColor = remember(colorInput) { parseColorInput(colorInput) }

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = stringResource(R.string.stylus_cycle_colors),
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            text = stringResource(R.string.stylus_cycle_colors_desc),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            colors.chunked(8).forEach { rowColors ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowColors.forEach { color ->
                        ColorToken(
                            color = color,
                            selected = color.toArgb() == selectedColor.toArgb(),
                            onClick = {
                                selectedColor = color
                                colorInput = color.toHexString(includeAlpha = false)
                            },
                            onRemove = { onRemoveColor(color) },
                        )
                    }
                }
            }
        }
        HsvColorPicker(
            color = selectedColor,
            onColorChanged = {
                selectedColor = it.copy(alpha = 1f)
                colorInput = selectedColor.toHexString(includeAlpha = false)
            },
        )
        OutlinedTextField(
            value = colorInput,
            onValueChange = {
                colorInput = it
                parseColorInput(it)?.let { parsed -> selectedColor = parsed }
            },
            label = { Text(stringResource(R.string.color_value_input)) },
            singleLine = true,
            isError = colorInput.isNotBlank() && parsedColor == null,
            modifier = Modifier.fillMaxWidth(),
        )
        if (colorInput.isNotBlank() && parsedColor == null) {
            Text(
                text = stringResource(R.string.color_value_invalid),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { onAddColor(parsedColor ?: selectedColor.copy(alpha = 1f)) },
                modifier = Modifier.weight(1f),
                enabled = colorInput.isBlank() || parsedColor != null,
            ) {
                Text(stringResource(R.string.add_color))
            }
            TextButton(onClick = onReset, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.restore_default))
            }
        }
    }
}

@Composable
private fun ColorToken(
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (selected) 3.dp else 1.dp,
                color = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.42f)
                },
                shape = CircleShape,
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onRemove,
            ),
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onPrimary),
            )
        }
    }
}

@Composable
private fun AboutContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Image(
            painter = painterResource(id = R.drawable.app_icon),
            contentDescription = stringResource(R.string.app_name),
            modifier = Modifier.size(72.dp),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
        )
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "${BuildConfig.VERSION_NAME}${if (BuildConfig.DEBUG) "-dev" else ""} (${BuildConfig.VERSION_CODE})",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.copyright),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.licenses),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun stylusSchemeLabels(): List<String> = StylusButtonScheme.entries.map {
    when (it) {
        StylusButtonScheme.Disabled -> stringResource(R.string.stylus_button_scheme_disabled)
        StylusButtonScheme.XiaomiSmartPen -> stringResource(R.string.stylus_button_scheme_xiaomi)
    }
}

@Composable
private fun stylusActionLabels(): List<String> = StylusButtonAction.entries.map {
    when (it) {
        StylusButtonAction.None -> stringResource(R.string.stylus_action_none)
        StylusButtonAction.CyclePresetColor -> stringResource(R.string.stylus_action_cycle_color)
        StylusButtonAction.ToggleStrokeEraser -> stringResource(R.string.stylus_action_stroke_eraser)
        StylusButtonAction.TogglePixelEraser -> stringResource(R.string.stylus_action_pixel_eraser)
        StylusButtonAction.Undo -> stringResource(R.string.undo)
        StylusButtonAction.Redo -> stringResource(R.string.redo)
        StylusButtonAction.ToggleCanvasVisibility -> stringResource(R.string.stylus_action_toggle_canvas)
        StylusButtonAction.ToggleCanvasPassthrough -> stringResource(R.string.stylus_action_toggle_passthrough)
        StylusButtonAction.ToggleLaser -> stringResource(R.string.laser)
    }
}

@Composable
private fun pressureThresholdLabels(): List<String> = pressureThresholdOptions.map {
    stringResource(R.string.pressure_eraser_threshold_value, (it * 100).toInt())
}

private val toolbarOrientationValues = listOf(
    ToolbarOrientationMode.HORIZONTAL,
    ToolbarOrientationMode.VERTICAL,
)

private fun pressureThresholdOption(value: Float): Float =
    pressureThresholdOptions.minBy { kotlin.math.abs(it - value) }

private fun String.toSaveLocationSummary(): String =
    runCatching {
        Uri.parse(this).lastPathSegment
            ?.substringAfterLast(':')
            ?.ifBlank { null }
    }.getOrNull() ?: this
