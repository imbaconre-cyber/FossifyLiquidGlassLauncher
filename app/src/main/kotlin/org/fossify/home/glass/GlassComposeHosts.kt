package org.fossify.home.glass

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView

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

fun ComposeView.bindLiquidSlider(controller: LiquidSliderController) {
    setContent {
        FossifyLiquidSlider(controller = controller, modifier = Modifier.fillMaxSize())
    }
}
