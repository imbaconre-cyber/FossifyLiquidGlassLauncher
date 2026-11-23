package org.fossify.home.glass

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp

/** Bridges the View-based Fossify launcher to the custom Compose glass components. */

fun ComposeView.bindLiquidDock() {
    setContent {
        FossifyLiquidDock(modifier = Modifier.fillMaxSize())
    }
}

fun ComposeView.bindLiquidDrawerBackground() {
    setContent {
        LiquidGlassDrawer(modifier = Modifier.fillMaxSize(), content = {})
    }
}

fun ComposeView.bindLiquidSearchBar() {
    setContent {
        LiquidGlassPanel(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(20.dp),
            blurRadius = 12.dp,
            refractionHeight = 20f,
            refractionAmount = 28f,
            tint = Color.White.copy(alpha = 0.14f)
        )
    }
}

fun ComposeView.bindLiquidSlider(controller: LiquidSliderController) {
    setContent {
        FossifyLiquidSlider(controller = controller, modifier = Modifier.fillMaxSize())
    }
}
