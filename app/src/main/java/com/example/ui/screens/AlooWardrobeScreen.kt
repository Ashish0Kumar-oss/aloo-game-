package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.model.AlooSkin
import com.example.model.SkinType
import com.example.ui.components.AlooCharacterGraphic
import com.example.ui.components.AlooExpression

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlooWardrobeScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { GamePreferences(context) }
    val audio = remember { SoundEffects(context, prefs) }

    var coins by remember { mutableIntStateOf(prefs.alooCoins) }
    var skins by remember { mutableStateOf(prefs.getSkins()) }
    var selectedSkinType by remember { mutableStateOf(prefs.selectedSkin) }

    val currentSkin = skins.firstOrNull { it.id == selectedSkinType } ?: skins.first()
    val isEquipped = prefs.selectedSkin == currentSkin.id

    val infiniteTransition = rememberInfiniteTransition(label = "idle_spud")
    val squishY by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "squish"
    )

    fun equipSkin(skin: AlooSkin) {
        prefs.selectedSkin = skin.id
        selectedSkinType = skin.id
        audio.playVictory()
    }

    fun buySkin(skin: AlooSkin) {
        if (coins < skin.price) return
        coins -= skin.price
        prefs.alooCoins = coins
        prefs.unlockSkin(skin.id)
        prefs.selectedSkin = skin.id
        selectedSkinType = skin.id
        skins = prefs.getSkins()
        audio.playPowerUp()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Spud Wardrobe", fontWeight = FontWeight.Bold, color = Color(0xFFFFD54F))
                        Text("Equip & Unlock Potato Heroes", style = MaterialTheme.typography.bodySmall, color = Color(0xFFFFECB3))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("wardrobe_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    Surface(
                        color = Color(0xFF3E2723),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🥔", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$coins",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD54F)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1B100B))
            )
        },
        containerColor = Color(0xFF140D08),
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Live 3D-effect Potato Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFF3E2723), Color(0xFF1A110B)),
                            radius = 400f
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Background pedestal glow
                Surface(
                    color = Color(0x33FFB300),
                    shape = CircleShape,
                    modifier = Modifier.size(190.dp)
                ) {}

                AlooCharacterGraphic(
                    skinType = currentSkin.id,
                    expression = AlooExpression.HAPPY,
                    squashX = 2.0f - squishY,
                    squashY = squishY,
                    size = 160.dp
                )

                if (isEquipped) {
                    Surface(
                        color = Color(0xFF2E7D32),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ACTIVE", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Skin Details Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF241711))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = currentSkin.name,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD54F)
                                )
                            )
                            Text(
                                text = currentSkin.hindiTitle,
                                style = MaterialTheme.typography.labelMedium.copy(color = Color(0xFFFFAB40))
                            )
                        }

                        if (!currentSkin.isUnlocked) {
                            Surface(
                                color = Color(0x33FFA000),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFFFCA28), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${currentSkin.price} 🥔", fontWeight = FontWeight.Bold, color = Color(0xFFFFD54F))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentSkin.description,
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFFFECB3))
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Stats meters
                    StatBar(label = "Bounce Height", value = currentSkin.bounceMultiplier, maxVal = 1.4f)
                    Spacer(modifier = Modifier.height(8.dp))
                    StatBar(label = "Heat Resistance", value = currentSkin.heatResistance, maxVal = 1.6f)
                    Spacer(modifier = Modifier.height(8.dp))
                    StatBar(label = "Coin Multiplier", value = currentSkin.coinMultiplier, maxVal = 2.0f)

                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = Color(0x332E7D32),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Perk: ${currentSkin.perk}",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFFFE082), fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (currentSkin.isUnlocked) {
                        Button(
                            onClick = { equipSkin(currentSkin) },
                            enabled = !isEquipped,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("equip_skin_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF57C00),
                                disabledContainerColor = Color(0x553E2723)
                            )
                        ) {
                            Text(
                                text = if (isEquipped) "Already Equipped" else "Equip Skin",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        val canAfford = coins >= currentSkin.price
                        Button(
                            onClick = { buySkin(currentSkin) },
                            enabled = canAfford,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("unlock_skin_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF57C00),
                                disabledContainerColor = Color(0x335D4037)
                            )
                        ) {
                            Text(
                                text = if (canAfford) "Unlock for ${currentSkin.price} 🥔" else "Need ${currentSkin.price} 🥔 (You have $coins)",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Skin Selector Row
            Text(
                text = "Select Spud Hero",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(skins, key = { it.id }) { skin ->
                    val isSelected = skin.id == selectedSkinType
                    Card(
                        modifier = Modifier
                            .size(105.dp)
                            .clickable {
                                selectedSkinType = skin.id
                                audio.playBounce()
                            }
                            .then(
                                if (isSelected) Modifier.border(2.5.dp, Color(0xFFFFD54F), RoundedCornerShape(16.dp))
                                else Modifier
                            )
                            .testTag("skin_item_${skin.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFF3E2723) else Color(0xFF20150F)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            AlooCharacterGraphic(
                                skinType = skin.id,
                                size = 52.dp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = skin.name,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color(0xFFFFD54F) else Color.White,
                                    fontSize = 11.sp
                                ),
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatBar(label: String, value: Float, maxVal: Float) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.width(120.dp),
            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFFFECB3))
        )
        LinearProgressIndicator(
            progress = { (value / maxVal).coerceIn(0f, 1f) },
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .background(Color(0x33FFFFFF), RoundedCornerShape(4.dp)),
            color = Color(0xFFFF9800),
            trackColor = Color(0x22000000)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "${(value * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
        )
    }
}
