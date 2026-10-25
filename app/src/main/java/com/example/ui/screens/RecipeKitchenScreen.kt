package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Restaurant
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundEffects
import com.example.data.GamePreferences
import com.example.model.RecipeDish
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeKitchenScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { GamePreferences(context) }
    val audio = remember { SoundEffects(context, prefs) }
    val scope = rememberCoroutineScope()

    var coins by remember { mutableIntStateOf(prefs.alooCoins) }
    var recipes by remember { mutableStateOf(prefs.getRecipes()) }
    var cookingRecipeId by remember { mutableStateOf<String?>(null) }
    var cookingProgress by remember { mutableStateOf(0f) }

    fun cookDish(dish: RecipeDish) {
        if (coins < dish.requiredCoins || cookingRecipeId != null) return

        // Deduct coins
        coins -= dish.requiredCoins
        prefs.alooCoins = coins
        cookingRecipeId = dish.id
        cookingProgress = 0f
        audio.playSizzle()

        scope.launch {
            val totalSteps = 20
            for (step in 1..totalSteps) {
                delay((dish.cookTimeSec * 1000L) / totalSteps)
                cookingProgress = step / totalSteps.toFloat()
                if (step == totalSteps / 2) {
                    audio.playCookFlip()
                }
            }
            // Done!
            prefs.incrementRecipeCooked(dish.id)
            recipes = prefs.getRecipes()
            cookingRecipeId = null
            audio.playVictory()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Aloo Kitchen", fontWeight = FontWeight.Bold, color = Color(0xFFFFD54F))
                        Text("Cook Dishes & Unlock Perks", style = MaterialTheme.typography.bodySmall, color = Color(0xFFFFECB3))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("recipe_back_button")) {
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Header Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2E1911))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    Brush.radialGradient(listOf(Color(0xFFFFB74D), Color(0xFFE65100))),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Restaurant, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "The Halwai Counter",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD54F)
                                )
                            )
                            Text(
                                text = "Fry crisp samosas, spicy chaat, and buttery parathas to empower Chotu Aloo with permanent skills!",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFFFCC80))
                            )
                        }
                    }
                }
            }

            items(recipes, key = { it.id }) { dish ->
                val isCookingThis = cookingRecipeId == dish.id
                val canAfford = coins >= dish.requiredCoins

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (dish.timesCooked > 0) Color(0xFF2B1D16) else Color(0xFF1F140F)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = dish.name,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Text(
                                    text = dish.hindiName,
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFFFB74D))
                                )
                            }

                            if (dish.timesCooked > 0) {
                                Surface(
                                    color = Color(0xFF2E7D32),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Mastered x${dish.timesCooked}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = dish.description,
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFFFECB3))
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = Color(0x33FFA000),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = Color(0xFFFF7043), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Perk: ${dish.perkUnlocked}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFFFFD54F),
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (isCookingThis) {
                            Column {
                                Text(
                                    text = "Sizzling in the tawa... ${(cookingProgress * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelMedium.copy(color = Color(0xFFFF9800), fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { cookingProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(10.dp)
                                        .background(Color(0x33FFFFFF), RoundedCornerShape(5.dp)),
                                    color = Color(0xFFFF9800),
                                    trackColor = Color(0x22000000)
                                )
                            }
                        } else {
                            Button(
                                onClick = { cookDish(dish) },
                                enabled = canAfford && cookingRecipeId == null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("cook_button_${dish.id}"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFF57C00),
                                    disabledContainerColor = Color(0x335D4037)
                                )
                            ) {
                                Text(
                                    text = if (canAfford) "Fry & Cook (${dish.requiredCoins} 🥔)" else "Need ${dish.requiredCoins} 🥔",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
