package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.game.bounce.GaramAlooScreen
import com.example.game.dash.KitchenDashScreen
import com.example.ui.screens.AlooWardrobeScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.RecipeKitchenScreen
import com.example.ui.theme.MyApplicationTheme

enum class ScreenState {
    HOME,
    GARAM_ALOO,
    KITCHEN_DASH,
    RECIPES,
    WARDROBE
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AlooGameApp()
                }
            }
        }
    }
}

@Composable
fun AlooGameApp() {
    var currentScreen by remember { mutableStateOf(ScreenState.HOME) }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "screen_transition"
    ) { screen ->
        when (screen) {
            ScreenState.HOME -> {
                HomeScreen(
                    onPlayGaramAloo = { currentScreen = ScreenState.GARAM_ALOO },
                    onPlayKitchenDash = { currentScreen = ScreenState.KITCHEN_DASH },
                    onOpenKitchen = { currentScreen = ScreenState.RECIPES },
                    onOpenWardrobe = { currentScreen = ScreenState.WARDROBE }
                )
            }
            ScreenState.GARAM_ALOO -> {
                BackHandler { currentScreen = ScreenState.HOME }
                GaramAlooScreen(onBack = { currentScreen = ScreenState.HOME })
            }
            ScreenState.KITCHEN_DASH -> {
                BackHandler { currentScreen = ScreenState.HOME }
                KitchenDashScreen(onBack = { currentScreen = ScreenState.HOME })
            }
            ScreenState.RECIPES -> {
                BackHandler { currentScreen = ScreenState.HOME }
                RecipeKitchenScreen(onBack = { currentScreen = ScreenState.HOME })
            }
            ScreenState.WARDROBE -> {
                BackHandler { currentScreen = ScreenState.HOME }
                AlooWardrobeScreen(onBack = { currentScreen = ScreenState.HOME })
            }
        }
    }
}
