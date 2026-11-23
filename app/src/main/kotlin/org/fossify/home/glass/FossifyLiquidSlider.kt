package org.fossify.home.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy

/**
 * Controller used to bridge a View-based settings screen to the Compose slider.
 * The callbacks mirror the original Android slider contract.
 */
class LiquidSliderController(
    initialValue: Float,
    val valueRange: ClosedFloatingPointRange<Float> = 0f..100f,
    var onValueChange: ((Float) -> Unit)? = null,
    var onValueChangeFinished: ((Float) -> Unit)? = null
) {
    val value = mutableStateOf(initialValue.coerceIn(valueRange))
    fun update(newValue: Float, finished: Boolean) {
        val coerced = newValue.coerceIn(valueRange)
        value.value = coerced
        if (finished) onValueChangeFinished?.invoke(coerced) else onValueChange?.invoke(coerced)
    }
}

/**
 * Custom Liquid Glass slider built from AndroidLiquidGlass backdrop modifiers.
 * Supports drag, tap-to-set and the standard value-change callbacks.
 */
@Composable
fun FossifyLiquidSlider(
    controller: LiquidSliderController,
    modifier: Modifier = Modifier,
    trackHeight: Dp = 10.dp,
    thumbSize: Dp = 28.dp,
    activeColor: Color = Color(0xFF0A84FF),
    inactiveColor: Color = Color.White.copy(alpha = 0.18f)
) {
    val backdrop = rememberLayerBackdrop()
    val density = LocalDensity.current
    val blurPx = with(density) { 8.dp.toPx() }
    val thumbPx = with(density) { thumbSize.toPx() }
    val halfThumb = thumbPx / 2f

    val trackWidth = remember { mutableFloatStateOf(0f) }

    fun valueToOffset(value: Float, width: Float): Float {
        val fraction = (value - controller.valueRange.start) /
            (controller.valueRange.endInclusive - controller.valueRange.start)
        val span = (width - thumbPx).coerceAtLeast(0f)
        return halfThumb + fraction.coerceIn(0f, 1f) * span
    }

    fun offsetToValue(offset: Float, width: Float): Float {
        val span = (width - thumbPx).coerceAtLeast(1f)
        val fraction = ((offset - halfThumb) / span).coerceIn(0f, 1f)
        return controller.valueRange.start + fraction *
            (controller.valueRange.endInclusive - controller.valueRange.start)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(thumbSize + 12.dp)
            .layerBackdrop(backdrop)
            .onSizeChanged { trackWidth.value = it.width.toFloat() }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        val width = trackWidth.value
                        if (width <= thumbPx) return@detectDragGestures
                        change.consume()
                        val current = valueToOffset(controller.value.value, width)
                        val next = (current + dragAmount.x).coerceIn(halfThumb, width - halfThumb)
                        controller.update(offsetToValue(next, width), finished = false)
                    },
                    onDragEnd = { controller.update(controller.value.value, finished = true) }
                )
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val width = trackWidth.value
                    if (width > thumbPx) controller.update(offsetToValue(offset.x, width), finished = true)
                }
            }
    ) {
        val width = trackWidth.value
        val fraction = (controller.value.value - controller.valueRange.start) /
            (controller.valueRange.endInclusive - controller.valueRange.start)
        val activeWidth = (width * fraction.coerceIn(0f, 1f)).coerceAtLeast(0f)
        val thumbOffset = valueToOffset(controller.value.value, width)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(trackHeight)
                .align(Alignment.Center)
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { RoundedCornerShape(trackHeight) },
                    effects = {
                        vibrancy()
                        blur(blurPx)
                        lens(16f, 24f, true)
                    },
                    onDrawSurface = { drawRect(inactiveColor) }
                )
        )

        Box(
            modifier = Modifier
                .width(with(density) { activeWidth.toDp() })
                .height(trackHeight)
                .align(Alignment.CenterStart)
                .background(activeColor, RoundedCornerShape(trackHeight))
        )

        Box(
            modifier = Modifier
                .size(thumbSize)
                .align(Alignment.CenterStart)
                .offset(x = with(density) { thumbOffset.toDp() - halfThumb.toDp() })
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { CircleShape },
                    effects = {
                        vibrancy()
                        blur(4f)
                        lens(12f, 18f, true)
                    },
                    onDrawSurface = { drawRect(Color.White.copy(alpha = 0.85f)) }
                )
        )
    }
}
