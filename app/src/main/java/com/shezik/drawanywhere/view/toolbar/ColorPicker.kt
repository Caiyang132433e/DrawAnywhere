package com.shezik.drawanywhere.view.toolbar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shezik.drawanywhere.R
import com.shezik.drawanywhere.model.PRESET_COLORS
import com.shezik.drawanywhere.ui.theme.Spacing
import kotlin.math.roundToInt

@Composable
fun ColorPicker(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    onPresetSelected: (Color) -> Unit = onColorSelected,
    recentColors: List<Color> = emptyList(),
) {
    var paletteColor by remember(selectedColor) { mutableStateOf(selectedColor) }

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        SectionLabel(stringResource(R.string.color))
        PresetRows(
            selectedColor = paletteColor,
            onPresetSelected = { color ->
                val colorWithCurrentAlpha = color.copy(alpha = paletteColor.alpha)
                paletteColor = colorWithCurrentAlpha
                onPresetSelected(colorWithCurrentAlpha)
            },
        )

        if (recentColors.isNotEmpty()) {
            SectionLabel(stringResource(R.string.recent), subdued = true)
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                recentColors.take(6).forEach { color ->
                    key(color.toArgb()) {
                        ColorSwatchButton(
                            color = color,
                            isSelected = color.toArgb() == paletteColor.toArgb(),
                            onClick = {
                                paletteColor = color
                                onColorSelected(color)
                            },
                        )
                    }
                }
            }
        }

        HsvColorPicker(
            color = paletteColor,
            onColorChanged = { paletteColor = it },
        )

        Button(
            onClick = { onColorSelected(paletteColor) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = paletteColor),
        ) {
            Text(stringResource(R.string.apply_color))
        }
    }
}

/** Material 3 replacement for the Miuix HSV palette: hue / saturation / brightness sliders. */
@Composable
fun HsvColorPicker(
    color: Color,
    onColorChanged: (Color) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hsv = remember(color) {
        FloatArray(3).also { android.graphics.Color.colorToHSV(color.toArgb(), it) }
    }
    fun fromHsv(h: Float, s: Float, v: Float): Color =
        Color(android.graphics.Color.HSVToColor(floatArrayOf(h, s, v)))

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        ColorSlider(
            label = stringResource(R.string.hue),
            value = hsv[0],
            valueRange = 0f..360f,
            display = { "${it.roundToInt()}°" },
            onValueChange = { onColorChanged(fromHsv(it, hsv[1], hsv[2])) },
        )
        ColorSlider(
            label = stringResource(R.string.saturation),
            value = hsv[1],
            valueRange = 0f..1f,
            display = { "${(it * 100).roundToInt()}%" },
            onValueChange = { onColorChanged(fromHsv(hsv[0], it, hsv[2])) },
        )
        ColorSlider(
            label = stringResource(R.string.brightness),
            value = hsv[2],
            valueRange = 0f..1f,
            display = { "${(it * 100).roundToInt()}%" },
            onValueChange = { onColorChanged(fromHsv(hsv[0], hsv[1], it)) },
        )
    }
}

@Composable
private fun ColorSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    display: (Float) -> String,
    onValueChange: (Float) -> Unit,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = display(value),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun PresetRows(
    selectedColor: Color,
    onPresetSelected: (Color) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        PRESET_COLORS.chunked(6).forEach { row ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                row.forEach { color ->
                    ColorSwatchButton(
                        color = color,
                        isSelected = color.rgbEquals(selectedColor),
                        onClick = { onPresetSelected(color) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorSwatchButton(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                },
                shape = CircleShape,
            )
            .clickable { onClick() },
    )
}

@Composable
private fun SectionLabel(
    text: String,
    subdued: Boolean = false,
) {
    Text(
        text = text,
        modifier = Modifier.padding(top = if (subdued) 0.dp else 2.dp),
        style = if (subdued) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Medium,
        color = if (subdued) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
    )
}
