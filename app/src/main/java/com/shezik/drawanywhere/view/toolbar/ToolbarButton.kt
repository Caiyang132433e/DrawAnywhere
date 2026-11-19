package com.shezik.drawanywhere.view.toolbar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shezik.drawanywhere.R
import com.shezik.drawanywhere.UiState
import com.shezik.drawanywhere.model.PenType
import com.shezik.drawanywhere.view.canvas.LockMode

/**
 * Stable ids for every button that can appear in the floating toolbar.
 * The order here is also the render order of the first layer.
 */
internal val toolbarButtonOrder = listOf(
    "visibility",
    "undo",
    "clear",
    "tool_controls",
    "color_picker",
    "redo",
    "zoom_lock",
    "passthrough",
    "save",
    "settings",
    "orientation",
    "minimize",
    "quit",
)

internal data class ToolbarButtonMeta(
    val id: String,
    val labelRes: Int,
    val icon: ImageVector,
)

/** Static metadata used by the settings screen and by [createAllToolbarButtons]. */
internal val toolbarButtonCatalog: List<ToolbarButtonMeta> = listOf(
    ToolbarButtonMeta("visibility", R.string.toolbar_button_visibility, Icons.Default.Visibility),
    ToolbarButtonMeta("undo", R.string.undo, Icons.AutoMirrored.Filled.Undo),
    ToolbarButtonMeta("clear", R.string.clear_canvas, Icons.Default.Delete),
    ToolbarButtonMeta("tool_controls", R.string.tool_controls, Icons.Default.Edit),
    ToolbarButtonMeta("color_picker", R.string.color_picker, Icons.Default.Palette),
    ToolbarButtonMeta("redo", R.string.redo, Icons.AutoMirrored.Filled.Redo),
    ToolbarButtonMeta("zoom_lock", R.string.lock_zoom, Icons.Default.Lock),
    ToolbarButtonMeta("passthrough", R.string.toolbar_button_passthrough, Icons.Default.TouchApp),
    ToolbarButtonMeta("save", R.string.save_drawing, Icons.Default.SaveAlt),
    ToolbarButtonMeta("settings", R.string.settings, Icons.Default.Tune),
    ToolbarButtonMeta("orientation", R.string.toggle_toolbar_orientation, Icons.Default.ScreenRotation),
    ToolbarButtonMeta("minimize", R.string.minimize_toolbar, Icons.Default.CloseFullscreen),
    ToolbarButtonMeta("quit", R.string.quit, Icons.Default.Close),
)

@Composable
private fun LockZoomIcon() {
    val tint = MaterialTheme.colorScheme.onSurface
    Box(contentAlignment = Alignment.Center) {
        Icon(
            Icons.Default.ZoomOutMap,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
        Icon(
            Icons.Default.Lock,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(12.dp)
        )
    }
}

internal data class ToolbarButton(
    val id: String,
    val icon: ImageVector,
    val color: Color? = null,
    val contentDescription: String,
    val isEnabled: Boolean = true,
    val onClick: (() -> Unit)? = null,
    val popupPages: List<@Composable () -> Unit> = emptyList(),
    val iconContent: (@Composable () -> Unit)? = null,
) {
    val hasPopup: Boolean
        get() = popupPages.isNotEmpty()
}

@Composable
internal fun createAllToolbarButtons(
    uiState: UiState,
    canUndo: Boolean,
    canRedo: Boolean,
    canClearCanvas: Boolean,
    onCanvasVisibilityToggle: () -> Unit,
    onCanvasPassthroughToggle: () -> Unit,
    onClearCanvas: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onPenTypeSwitch: (PenType) -> Unit,
    onColorChange: (Color) -> Unit,
    onPresetColorChange: (Color) -> Unit = onColorChange,
    onStrokeWidthChange: (Float) -> Unit,
    onAlphaChange: (Float) -> Unit,
    onCycleLockMode: () -> Unit,
    lockMode: LockMode,
    onSaveTransparent: () -> Unit,
    onSaveWithBackdrop: () -> Unit,
    onSaveWithScreenBackdrop: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggleOrientation: () -> Unit,
    onChangeToolbarMinimized: (Boolean) -> Unit,
    onQuitApplication: () -> Unit,
): List<ToolbarButton> {
    val buttons = mapOf(
        "visibility" to ToolbarButton(
            id = "visibility",
            icon = if (uiState.canvasVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
            contentDescription = if (uiState.canvasVisible) {
                stringResource(R.string.hide_canvas)
            } else {
                stringResource(R.string.show_canvas)
            },
            onClick = onCanvasVisibilityToggle
        ),
        "undo" to ToolbarButton(
            id = "undo",
            icon = Icons.AutoMirrored.Filled.Undo,
            contentDescription = stringResource(R.string.undo),
            isEnabled = uiState.canvasVisible && canUndo,
            onClick = onUndo
        ),
        "clear" to ToolbarButton(
            id = "clear",
            icon = if (canClearCanvas) Icons.Filled.Delete else Icons.Outlined.Delete,
            contentDescription = stringResource(R.string.clear_canvas),
            isEnabled = uiState.canvasVisible && canClearCanvas,
            onClick = onClearCanvas
        ),
        "tool_controls" to ToolbarButton(
            id = "tool_controls",
            icon = uiState.currentPenType.icon,
            contentDescription = stringResource(R.string.tool_controls),
            popupPages = listOf(
                {
                    PenTypeSelector(
                        currentPenType = uiState.currentPenType,
                        onPenTypeSwitch = onPenTypeSwitch
                    )
                },
                {
                    PenControls(
                        penConfig = uiState.currentPenConfig,
                        onStrokeWidthChange = onStrokeWidthChange
                    )
                }
            )
        ),
        "color_picker" to ToolbarButton(
            id = "color_picker",
            icon = Icons.Default.Palette,
            color = if (!uiState.currentPenType.isEraser) uiState.currentPenConfig.color else null,
            contentDescription = stringResource(R.string.color_picker),
            isEnabled = !uiState.currentPenType.isEraser,
            popupPages = listOf(
                {
                    ColorPicker(
                        selectedColor = uiState.currentPenConfig.color.copy(alpha = uiState.currentPenConfig.alpha),
                        onColorSelected = { color ->
                            onColorChange(color.copy(alpha = 1f))
                            onAlphaChange(color.alpha)
                        },
                        onPresetSelected = { color ->
                            onPresetColorChange(color.copy(alpha = 1f))
                            onAlphaChange(color.alpha)
                        },
                        recentColors = uiState.recentColors
                    )
                }
            )
        ),
        "redo" to ToolbarButton(
            id = "redo",
            icon = Icons.AutoMirrored.Filled.Redo,
            contentDescription = stringResource(R.string.redo),
            isEnabled = uiState.canvasVisible && canRedo,
            onClick = onRedo
        ),
        "zoom_lock" to ToolbarButton(
            id = "zoom_lock",
            icon = if (lockMode == LockMode.NONE) Icons.Default.LockOpen else Icons.Default.Lock,
            iconContent = if (lockMode == LockMode.ZOOM) {{ LockZoomIcon() }} else null,
            contentDescription = when (lockMode) {
                LockMode.NONE -> stringResource(R.string.lock_zoom)
                LockMode.ZOOM -> stringResource(R.string.lock_all)
                LockMode.ALL -> stringResource(R.string.unlock_all)
            },
            isEnabled = uiState.canvasVisible,
            onClick = onCycleLockMode
        ),
        "passthrough" to ToolbarButton(
            id = "passthrough",
            icon = if (uiState.canvasPassthrough) Icons.Default.DoNotTouch else Icons.Default.TouchApp,
            contentDescription = if (uiState.canvasPassthrough) {
                stringResource(R.string.disable_passthrough)
            } else {
                stringResource(R.string.enable_passthrough)
            },
            isEnabled = uiState.canvasVisible,
            onClick = onCanvasPassthroughToggle
        ),
        "save" to ToolbarButton(
            id = "save",
            icon = Icons.Default.SaveAlt,
            contentDescription = stringResource(R.string.save_drawing),
            popupPages = listOf(
                {
                    SaveActions(
                        onSaveTransparent = onSaveTransparent,
                        onSaveWithBackdrop = onSaveWithBackdrop,
                        onSaveWithScreenBackdrop = onSaveWithScreenBackdrop
                    )
                }
            )
        ),
        "settings" to ToolbarButton(
            id = "settings",
            icon = Icons.Default.Tune,
            contentDescription = stringResource(R.string.settings),
            onClick = onOpenSettings
        ),
        "orientation" to ToolbarButton(
            id = "orientation",
            icon = Icons.Default.ScreenRotation,
            contentDescription = stringResource(R.string.toggle_toolbar_orientation),
            onClick = onToggleOrientation
        ),
        "minimize" to ToolbarButton(
            id = "minimize",
            icon = Icons.Default.CloseFullscreen,
            contentDescription = stringResource(R.string.minimize_toolbar),
            onClick = { onChangeToolbarMinimized(true) }
        ),
        "quit" to ToolbarButton(
            id = "quit",
            icon = Icons.Default.Close,
            contentDescription = stringResource(R.string.quit),
            onClick = onQuitApplication
        )
    )
    return toolbarButtonOrder.mapNotNull { buttons[it] }
}

@Composable
private fun SaveActions(
    onSaveTransparent: () -> Unit,
    onSaveWithBackdrop: () -> Unit,
    onSaveWithScreenBackdrop: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = stringResource(R.string.save_drawing),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        )
        TextButton(
            onClick = onSaveTransparent,
            modifier = Modifier.fillMaxWidth(),
        ) { Text(stringResource(R.string.save_transparent_png), modifier = Modifier.fillMaxWidth()) }
        TextButton(
            onClick = onSaveWithBackdrop,
            modifier = Modifier.fillMaxWidth(),
        ) { Text(stringResource(R.string.save_backdrop_png), modifier = Modifier.fillMaxWidth()) }
        TextButton(
            onClick = onSaveWithScreenBackdrop,
            modifier = Modifier.fillMaxWidth(),
        ) { Text(stringResource(R.string.save_screen_backdrop_png), modifier = Modifier.fillMaxWidth()) }
    }
}
