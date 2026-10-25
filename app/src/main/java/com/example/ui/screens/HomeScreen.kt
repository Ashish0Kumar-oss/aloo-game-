package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundEffects
import com.example.data.GamePreferences
import com.example.model.SkinType
import com.example.ui.components.AlooCharacterGraphic
import com.example.ui.components.AlooExpression
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onPlayGaramAloo: () -> Unit,
    onPlayKitchenDash: () -> Unit,
    onOpenKitchen: () -> Unit,
    onOpenWardrobe: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { GamePreferences(context) }
    val audio = remember { SoundEffects(context, prefs) }
    val scope = rememberCoroutineScope()

    var showSettings by remember { mutableStateOf(false) }
    var showHowToPlay by remember { mutableStateOf(false) }

    var isSoundOn by remember { mutableStateOf(prefs.isSoundEnabled) }
    var isHapticOn by remember { mutableStateOf(prefs.isHapticEnabled) }

    val selectedSkin = remember { prefs.getSkins().firstOrNull { it.id == prefs.selectedSkin } ?: prefs.getSkins().first() }

    // Tap-to-squish interactive physics on home hero
    val squishAnim = remember { Animatable(1.0f) }
    var tapCount by remember { mutableIntStateOf(0) }

    val infiniteTransition = rememberInfiniteTransition(label = "hero_idle")
    val idleFloat by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hero_float"
    )

    fun tapHeroAloo() {
        tapCount++
        audio.playBounce()
        scope.launch {
            squishAnim.snapTo(0.65f)
            squishAnim.animateTo(1.0f, tween(350, easing = FastOutSlowInEasing))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0xFFFFB74D),
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🥔", fontSize = 20.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Aloo Game",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFFFD54F)
                                )
                            )
                            Text(
                                text = "Garam Masala Spud Arcade",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFFFCC80))
                            )
                        }
                    }
                },
                actions = {
                    Surface(
                        color = Color(0xFF3E2723),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🥔", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${prefs.alooCoins}",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD54F)
                            )
                        }
                    }
                    IconButton(
                        onClick = { showSettings = true },
                        modifier = Modifier.testTag("home_settings_button")
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1B100B))
            )
        },
        containerColor = Color(0xFF140D08),
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Interactive Hero Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2B1B13))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Tap Prompt
                        Text(
                            text = if (tapCount == 0) "Tap Chotu Aloo to squish!" else "Boing! $tapCount bounces!",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFAB40)
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Animated Interactive Character
                        Box(
                            modifier = Modifier
                                .size(150.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    tapHeroAloo()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            AlooCharacterGraphic(
                                skinType = selectedSkin.id,
                                expression = if (squishAnim.value < 0.85f) AlooExpression.PANIC else AlooExpression.HAPPY,
                                squashX = 2.0f - squishAnim.value,
                                squashY = squishAnim.value,
                                rotationDeg = idleFloat * 0.4f,
                                size = 140.dp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = selectedSkin.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD54F)
                            )
                        )
                        Text(
                            text = selectedSkin.perk,
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFFFECB3))
                        )
                    }
                }
            }

            // Stats Quick Banner
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Juggle High Score Card
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF23160F))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = Color(0xFFFF5722), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Juggle Best", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFFFCC80)))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${prefs.bounceHighScore}",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFFFD54F)
                                )
                            )
                        }
                    }

                    // Dash Record Card
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF23160F))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DirectionsRun, contentDescription = null, tint = Color(0xFF81C784), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Dash Best", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFFFCC80)))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${prefs.dashHighScore}m",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF81C784)
                                )
                            )
                        }
                    }
                }
            }

            // Mode 1: Garam Aloo (Juggle & Sizzle)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPlayGaramAloo() }
                        .testTag("play_garam_aloo_button"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF381F14))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color(0xFFD84315),
                            shape = CircleShape,
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🍳", fontSize = 28.sp)
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Garam Aloo",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = Color(0xFFFF5722),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "HOT JUGGLE",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Bounce Chotu Aloo before the tawa burns him! Catch chillies, coriander float & combo blasts.",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFFFCC80))
                            )
                        }

                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color(0xFFFFD54F))
                    }
                }
            }

            // Mode 2: Kitchen Dash (Runner)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPlayKitchenDash() }
                        .testTag("play_kitchen_dash_button"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2C1D2B))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color(0xFF6A1B9A),
                            shape = CircleShape,
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🏃", fontSize = 28.sp)
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Kitchen Dash",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = Color(0xFFAB47BC),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "3-LANE RUNNER",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Sprint across marble kitchen counters, jump rolling pins, slide under ladles & turbo rush!",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFE1BEE7))
                            )
                        }

                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color(0xFFCE93D8))
                    }
                }
            }

            // Secondary Hub Cards: Aloo Kitchen & Spud Wardrobe
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Kitchen Recipe Lab
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onOpenKitchen() }
                            .testTag("open_kitchen_button"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF241812))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Icon(Icons.Default.Restaurant, contentDescription = null, tint = Color(0xFFFFB74D), modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Aloo Kitchen",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "Cook Samosas & Chaat",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFFFCC80))
                            )
                        }
                    }

                    // Wardrobe Skins
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onOpenWardrobe() }
                            .testTag("open_wardrobe_button"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF241812))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = Color(0xFFFFCA28), modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Spud Wardrobe",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "7 Epic Characters",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFFFCC80))
                            )
                        }
                    }
                }
            }

            // How To Play Button
            item {
                Button(
                    onClick = { showHowToPlay = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("how_to_play_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B1D16))
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFFFD54F))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("How to Play & Spicy Tips", color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Settings Dialog
    if (showSettings) {
        AlertDialog(
            onDismissRequest = { showSettings = false },
            title = {
                Text("Game Settings", fontWeight = FontWeight.Bold, color = Color(0xFFFFD54F))
            },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Sound Effects", color = Color.White)
                        }
                        Switch(
                            checked = isSoundOn,
                            onCheckedChange = {
                                isSoundOn = it
                                prefs.isSoundEnabled = it
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFFF9800))
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Vibration, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Haptic Vibration", color = Color.White)
                        }
                        Switch(
                            checked = isHapticOn,
                            onCheckedChange = {
                                isHapticOn = it
                                prefs.isHapticEnabled = it
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFFF9800))
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSettings = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57C00))
                ) {
                    Text("Done")
                }
            },
            containerColor = Color(0xFF261912)
        )
    }

    // How to Play Dialog
    if (showHowToPlay) {
        AlertDialog(
            onDismissRequest = { showHowToPlay = false },
            title = {
                Text("How to Master Aloo Game 🥔", fontWeight = FontWeight.Bold, color = Color(0xFFFFD54F))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "1. Garam Aloo (Hot Juggle):",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF7043)
                    )
                    Text(
                        text = "• Tap the screen to bounce Chotu Aloo.\n• Tap left or right of potato to guide trajectory.\n• Prevent Aloo from touching the hot tawa at the bottom!\n• Collect red chillies for speed, coriander for float, butter for shields.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFFECB3)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "2. Kitchen Dash (3-Lane Runner):",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF81C784)
                    )
                    Text(
                        text = "• Swipe or tap left/right to change lanes.\n• Jump over rolling pins.\n• Slide under hanging ladles.\n• Grab golden Aloo wedges to buy recipes & skins!",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFFECB3)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showHowToPlay = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57C00))
                ) {
                    Text("Let's Sizzle!")
                }
            },
            containerColor = Color(0xFF261912)
        )
    }
}
