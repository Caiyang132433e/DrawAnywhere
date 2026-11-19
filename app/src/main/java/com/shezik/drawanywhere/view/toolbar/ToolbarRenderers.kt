package com.shezik.drawanywhere.view.toolbar

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shezik.drawanywhere.R

@Composable
internal fun RenderButton(button: ToolbarButton, popupAlignment: Alignment, modifier: Modifier = Modifier) {
    if (button.hasPopup) {
        PopupToolbarButton(modifier = modifier, button = button)
    } else {
        AnimatedToolbarButton(modifier = modifier, button = button)
    }
}

@Composable
internal fun ToolbarExpandButton(
    modifier: Modifier,
    isExpanded: Boolean,
    onClick: () -> Unit,
    orientation: ToolbarOrientation
) {
    val targetAngles = when (orientation) {
        ToolbarOrientation.HORIZONTAL -> 180f to 0f
        ToolbarOrientation.VERTICAL -> 270f to 90f
    }
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) targetAngles.first else targetAngles.second,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "toggle_rotation"
    )
    IconButton(
        onClick = onClick,
        modifier = modifier
            .background(
                color = if (isExpanded) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                shape = CircleShape
            )
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = if (isExpanded) stringResource(R.string.collapse_toolbar) else stringResource(R.string.expand_toolbar),
            tint = if (isExpanded) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.graphicsLayer { rotationZ = rotationAngle }
        )
    }
}

@Composable
internal fun AnimatedToolbarButton(modifier: Modifier, button: ToolbarButton) {
    val iconColor = button.color ?: MaterialTheme.colorScheme.onSurface
    val scale by animateFloatAsState(
        targetValue = if (button.isEnabled) 1f else 0.9f,
        animationSpec = tween(200),
        label = "button_scale"
    )
    IconButton(
        onClick = button.onClick ?: {},
        enabled = button.isEnabled,
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .background(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), shape = CircleShape)
    ) {
        button.iconContent?.invoke()
            ?: Icon(
                imageVector = button.icon,
                contentDescription = button.contentDescription,
                tint = if (button.isEnabled) iconColor else iconColor.copy(alpha = 0.4f)
            )
    }
}

@Composable
internal fun PopupToolbarButton(
    modifier: Modifier,
    button: ToolbarButton,
) {
    var isPopupOpen by remember { mutableStateOf(false) }
    var page by remember { mutableIntStateOf(0) }
    Box(modifier = modifier) {
        IconButton(
            onClick = { isPopupOpen = !isPopupOpen },
            enabled = button.isEnabled,
            modifier = Modifier.background(
                color = if (isPopupOpen) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                shape = CircleShape
            )
        ) {
            button.iconContent?.invoke()
                ?: Icon(
                    imageVector = button.icon,
                    contentDescription = button.contentDescription,
                    tint = if (isPopupOpen) button.color ?: MaterialTheme.colorScheme.onPrimaryContainer
                    else if (button.isEnabled) button.color ?: MaterialTheme.colorScheme.onSurface
                    else (button.color ?: MaterialTheme.colorScheme.onSurface).copy(alpha = 0.4f)
                )
        }
        DropdownMenu(
            expanded = isPopupOpen,
            onDismissRequest = { isPopupOpen = false },
            modifier = Modifier.width(280.dp),
        ) {
            Column(
                modifier = Modifier
                    .heightIn(max = 560.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (button.popupPages.size > 1) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        button.popupPages.indices.forEach { index ->
                            FilterChip(
                                selected = index == page,
                                onClick = { page = index },
                                label = {
                                    Text(
                                        if (index == 0) {
                                            stringResource(R.string.tools)
                                        } else {
                                            stringResource(R.string.width)
                                        }
                                    )
                                },
                            )
                        }
                    }
                }
                button.popupPages.getOrNull(page)?.invoke()
            }
        }
    }
}
