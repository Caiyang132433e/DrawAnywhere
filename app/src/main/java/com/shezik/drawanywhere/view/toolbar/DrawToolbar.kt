package com.shezik.drawanywhere.view.toolbar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.remember
import com.shezik.drawanywhere.DrawViewModel
import com.shezik.drawanywhere.R
import com.shezik.drawanywhere.ui.theme.DrawAnywhereTheme

/**
 * Material 3 floating toolbar.
 *
 * The first layer is fully driven by [com.shezik.drawanywhere.UiState.firstDrawerButtons],
 * so the settings screen can decide which buttons are always visible. Every button that is
 * not on the first layer automatically moves into the expandable second layer. The toolbar
 * uses wrap-content width/height, so it grows and shrinks with the selection instead of
 * reserving a fixed panel.
 */
@Composable
fun DrawToolbar(
    viewModel: DrawViewModel,
    canRedo: Boolean,
    onRedo: () -> Unit,
    passthroughEnabled: Boolean,
    onTogglePassthrough: () -> Unit,
    onSaveTransparent: () -> Unit,
    onSaveWithBackdrop: () -> Unit,
    onSaveWithScreenBackdrop: () -> Unit,
    onOpenSettings: () -> Unit,
    onQuit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val canUndo by viewModel.canUndo.collectAsState()
    val canClearCanvas by viewModel.canClearCanvas.collectAsState()
    val lockMode by viewModel.lockMode.collectAsState()
    val haptics = LocalHapticFeedback.current

    val allButtons = createAllToolbarButtons(
        uiState = uiState,
        canUndo = canUndo,
        canRedo = canRedo,
        canClearCanvas = canClearCanvas,
        onCanvasVisibilityToggle = viewModel::toggleCanvasVisibility,
        onCanvasPassthroughToggle = onTogglePassthrough,
        onClearCanvas = viewModel::clearCanvas,
        onUndo = viewModel::undo,
        onRedo = onRedo,
        onPenTypeSwitch = viewModel::switchToPen,
        onColorChange = viewModel::setPenColor,
        onPresetColorChange = viewModel::setPresetColor,
        onStrokeWidthChange = viewModel::setStrokeWidth,
        onAlphaChange = viewModel::setStrokeAlpha,
        onCycleLockMode = viewModel::cycleLockMode,
        lockMode = lockMode,
        onSaveTransparent = onSaveTransparent,
        onSaveWithBackdrop = onSaveWithBackdrop,
        onSaveWithScreenBackdrop = onSaveWithScreenBackdrop,
        onOpenSettings = onOpenSettings,
        onToggleOrientation = viewModel::toggleToolbarOrientation,
        onChangeToolbarMinimized = viewModel::setToolbarMinimized,
        onQuitApplication = onQuit,
    ).associateBy { it.id }

    val firstLayerIds = toolbarButtonOrder.filter { it in uiState.firstDrawerButtons }
    val secondLayerIds = toolbarButtonOrder.filter { it !in uiState.firstDrawerButtons }
    val hasSecondLayer = secondLayerIds.isNotEmpty()
    val isVertical = uiState.toolbarOrientation == ToolbarOrientation.VERTICAL
    val popupAlignment = if (isVertical) Alignment.CenterEnd else Alignment.TopCenter
    val spacing = if (isVertical) Arrangement.spacedBy(4.dp) else Arrangement.spacedBy(4.dp)

    DrawAnywhereTheme(dynamicColor = uiState.dynamicColor) {
        Box {
            DraggableToolbarSurface(
                // Leave room inside the overlay window for the Material surface shadow
                // and rounded corner anti-aliasing; otherwise WindowManager crops them.
                modifier = modifier.padding(8.dp),
                haptics = haptics,
                onPositionChange = viewModel::updateToolbarPosition,
                onPositionSaved = viewModel::saveToolbarPosition,
                onInteracted = viewModel::resetToolbarTimer,
            ) {
                if (uiState.toolbarMinimized) {
                    CollapsedToolbar(
                        uiState = uiState,
                        onExpand = { viewModel.setToolbarMinimized(false) },
                    )
                } else {
                    if (isVertical) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = spacing,
                        ) {
                            ToolbarDragHandle(
                                haptics = haptics,
                                onPositionChange = viewModel::updateToolbarPosition,
                                onPositionSaved = viewModel::saveToolbarPosition,
                            )
                            // Vertical: fixed two-column grid for both layers.
                            firstLayerIds.chunked(2).forEach { rowIds ->
                                Row(horizontalArrangement = spacing) {
                                    rowIds.forEach { id ->
                                        allButtons[id]?.let { button ->
                                            RenderButton(button = button, popupAlignment = popupAlignment)
                                        }
                                    }
                                }
                            }
                            if (hasSecondLayer) {
                                ToolbarExpandButton(
                                    modifier = Modifier,
                                    isExpanded = uiState.secondDrawerOpen,
                                    onClick = viewModel::toggleSecondDrawer,
                                    orientation = uiState.toolbarOrientation,
                                )
                                AnimatedVisibility(
                                    visible = uiState.secondDrawerOpen,
                                    enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                                    exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
                                ) {
                                    Column(verticalArrangement = spacing) {
                                        secondLayerIds.chunked(2).forEach { rowIds ->
                                            Row(horizontalArrangement = spacing) {
                                                rowIds.forEach { id ->
                                                    allButtons[id]?.let { button ->
                                                        RenderButton(
                                                            button = button,
                                                            popupAlignment = popupAlignment
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Horizontal: one continuous bar. The second layer expands
                        // horizontally inside the same row, and MainService keeps the
                        // whole bar on screen by shifting its position while it grows.
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = spacing,
                        ) {
                            ToolbarDragHandle(
                                haptics = haptics,
                                onPositionChange = viewModel::updateToolbarPosition,
                                onPositionSaved = viewModel::saveToolbarPosition,
                            )
                            firstLayerIds.forEach { id ->
                                allButtons[id]?.let { button ->
                                    RenderButton(button = button, popupAlignment = popupAlignment)
                                }
                            }
                            if (hasSecondLayer) {
                                ToolbarExpandButton(
                                    modifier = Modifier,
                                    isExpanded = uiState.secondDrawerOpen,
                                    onClick = viewModel::toggleSecondDrawer,
                                    orientation = uiState.toolbarOrientation,
                                )
                                AnimatedVisibility(
                                    visible = uiState.secondDrawerOpen,
                                    enter = expandHorizontally(expandFrom = Alignment.Start) + fadeIn(),
                                    exit = shrinkHorizontally(shrinkTowards = Alignment.Start) + fadeOut(),
                                ) {
                                    Row(horizontalArrangement = spacing) {
                                        secondLayerIds.forEach { id ->
                                            allButtons[id]?.let { button ->
                                                RenderButton(button = button, popupAlignment = popupAlignment)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolbarDragHandle(
    haptics: HapticFeedback,
    onPositionChange: (Offset) -> Unit,
    onPositionSaved: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onPositionChange(dragAmount)
                    },
                    onDragEnd = { onPositionSaved() },
                    onDragCancel = { onPositionSaved() },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.DragHandle,
            contentDescription = stringResource(R.string.move_toolbar),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun CollapsedToolbar(
    uiState: com.shezik.drawanywhere.UiState,
    onExpand: () -> Unit,
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 3.dp,
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier.padding(2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onExpand) {
                Icon(
                    imageVector = uiState.currentPenType.icon,
                    contentDescription = stringResource(R.string.expand_toolbar),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp).padding(end = 6.dp),
            )
        }
    }
}

/**
 * A Material 3 surface that can be moved by long-pressing anywhere on it.
 * Keeping the drag gesture on the shell (instead of a dedicated handle) matches the
 * clean look of the README graphic while leaving all buttons free for taps/popups.
 */
@Composable
private fun DraggableToolbarSurface(
    modifier: Modifier = Modifier,
    haptics: HapticFeedback,
    onPositionChange: (Offset) -> Unit,
    onPositionSaved: () -> Unit,
    onInteracted: () -> Unit,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.pointerInput(Unit) {
            detectDragGesturesAfterLongPress(
                onDragStart = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onInteracted()
                },
                onDrag = { change, dragAmount ->
                    change.consume()
                    onPositionChange(dragAmount)
                },
                onDragEnd = { onPositionSaved() },
                onDragCancel = { onPositionSaved() },
            )
        },
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 3.dp,
        shadowElevation = 6.dp,
    ) {
        content()
    }
}

