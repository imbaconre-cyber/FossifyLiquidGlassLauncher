package org.fossify.home.glass

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
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
 * Glass panel built directly on Kyant0's backdrop modifiers (AndroidLiquidGlass 1.0.6).
 * No high-level widgets are shipped by that library, so all surfaces here are custom.
 *
 * Backdrop capture/RenderEffect is only used on API 33+. Older devices get a static
 * tinted surface, which keeps the launcher stable on Samsung/Exynos API 31 builds.
 */
@Composable
fun LiquidGlassPanel(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    blurRadius: Dp = 18.dp,
    refractionHeight: Float = 32f,
    refractionAmount: Float = 48f,
    tint: Color = Color.White.copy(alpha = 0.14f),
    content: @Composable () -> Unit = {}
) {
    val density = LocalDensity.current
    val blurPx = with(density) { blurRadius.toPx() }
    val fullGlass = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    if (!fullGlass) {
        Box(
            modifier = modifier
                .clip(shape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.20f),
                            tint
                        )
                    )
                )
        ) {
            content()
        }
        return
    }

    val backdrop = rememberLayerBackdrop()
    Box(modifier = modifier.layerBackdrop(backdrop)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { shape },
                    effects = {
                        vibrancy()
                        blur(blurPx)
                        lens(refractionHeight, refractionAmount, true)
                    },
                    onDrawSurface = { drawRect(tint) }
                )
        ) {
            content()
        }
    }
}

/** Glass container rendered behind the launcher dock icons. */
@Composable
fun FossifyLiquidDock(
    modifier: Modifier = Modifier
) {
    LiquidGlassPanel(
        modifier = modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        shape = RoundedCornerShape(32.dp),
        blurRadius = 20.dp,
        refractionHeight = 36f,
        refractionAmount = 56f,
        tint = Color.White.copy(alpha = 0.16f)
    )
}

/** Glass wallpaper used behind the app drawer and its search bar. */
@Composable
fun LiquidGlassDrawer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val blurPx = with(density) { 24.dp.toPx() }
    val drawerShape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    val fullGlass = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    if (!fullGlass) {
        Box(modifier = modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF16213E),
                            Color(0xFF1A1A2E),
                            Color(0xFF0F3460)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(0f, 1200f)
                    )
                )
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(drawerShape)
                    .background(Color.White.copy(alpha = 0.10f))
            ) {
                content()
            }
        }
        return
    }

    val backdrop = rememberLayerBackdrop()
    Box(modifier = modifier.fillMaxSize().layerBackdrop(backdrop)) {
        // A soft wallpaper-ish gradient gives the backdrop something to refract on all APIs.
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF16213E),
                        Color(0xFF1A1A2E),
                        Color(0xFF0F3460)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(0f, 1200f)
                )
            )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { drawerShape },
                    effects = {
                        vibrancy()
                        blur(blurPx)
                        lens(40f, 64f, true)
                    },
                    onDrawSurface = { drawRect(Color.White.copy(alpha = 0.10f)) }
                )
        ) {
            content()
        }
    }
}

/** Glass tint for home-screen folders. */
@Composable
fun LiquidGlassFolder(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    LiquidGlassPanel(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        blurRadius = 12.dp,
        tint = Color.White.copy(alpha = 0.20f),
        content = content
    )
}

/** Subtle glass tint behind widgets. */
@Composable
fun LiquidGlassWidget(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    LiquidGlassPanel(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        blurRadius = 8.dp,
        tint = Color.White.copy(alpha = 0.10f),
        content = content
    )
}
