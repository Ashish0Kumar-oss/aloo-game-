package com.example.game.bounce

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundEffects
import com.example.data.GamePreferences
import com.example.model.FloatingText
import com.example.model.HazardType
import com.example.model.Particle
import com.example.model.PowerUpType
import com.example.model.SkinType
import com.example.ui.components.AlooCharacterGraphic
import com.example.ui.components.AlooExpression
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

data class FallingItem(
    val id: Long,
    val powerUp: PowerUpType?,
    val hazard: HazardType?,
    var x: Float,
    var y: Float,
    var vy: Float,
    var rotation: Float = 0f,
    var rotSpeed: Float = 0f,
    val radius: Float = 36f,
    var isCollected: Boolean = false
)

@Composable
fun GaramAlooScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { GamePreferences(context) }
    val audio = remember { SoundEffects(context, prefs) }
    val selectedSkin = remember { prefs.getSkins().firstOrNull { it.id == prefs.selectedSkin } ?: prefs.getSkins().first() }

    var isPlaying by remember { mutableStateOf(true) }
    var isPaused by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }

    var score by remember { mutableIntStateOf(0) }
    var combo by remember { mutableIntStateOf(1) }
    var maxCombo by remember { mutableIntStateOf(1) }
    var heatLevel by remember { mutableFloatStateOf(0f) } // 0.0 to 100.0
    var coinsCollected by remember { mutableIntStateOf(0) }

    // Active power-up timers
    var mirchiTimeLeft by remember { mutableFloatStateOf(0f) }
    var dhaniyaTimeLeft by remember { mutableFloatStateOf(0f) }
    var makhanShieldActive by remember { mutableStateOf(selectedSkin.id == SkinType.SAMOSA_WARRIOR) }

    // Physics State for Aloo
    var alooX by remember { mutableFloatStateOf(0f) }
    var alooY by remember { mutableFloatStateOf(0f) }
    var alooVx by remember { mutableFloatStateOf(0f) }
    var alooVy by remember { mutableFloatStateOf(0f) }
    var alooRot by remember { mutableFloatStateOf(0f) }
    var squashX by remember { mutableFloatStateOf(1.0f) }
    var squashY by remember { mutableFloatStateOf(1.0f) }

    val alooRadius = 45f

    val particles = remember { mutableStateListOf<Particle>() }
    val fallingItems = remember { mutableStateListOf<FallingItem>() }
    val floatingTexts = remember { mutableStateListOf<FloatingText>() }

    var lastFrameTimeNanos by remember { mutableStateOf(0L) }
    var itemSpawnTimer by remember { mutableFloatStateOf(0f) }

    fun triggerFloatingText(text: String, x: Float, y: Float, color: Long = 0xFFFFD600) {
        floatingTexts.add(FloatingText(text = text, x = x, y = y, color = color))
    }

    fun spawnSparks(x: Float, y: Float, count: Int, color: Long) {
        for (i in 0 until count) {
            val angle = Random.nextFloat() * 2f * PI.toFloat()
            val speed = Random.nextFloat() * 300f + 100f
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    alpha = 1.0f,
                    size = Random.nextFloat() * 12f + 6f,
                    color = color,
                    rotation = Random.nextFloat() * 360f,
                    rotSpeed = (Random.nextFloat() - 0.5f) * 400f
                )
            )
        }
    }

    fun bounceAloo(tapX: Float, tapY: Float, canvasWidth: Float) {
        if (!isPlaying || isPaused || isGameOver) return

        // Compute angle impulse away from tap point
        val dx = alooX - tapX
        val baseJump = -750f * selectedSkin.bounceMultiplier
        val horizontalImpulse = (dx / canvasWidth) * 550f

        alooVy = baseJump
        alooVx = (alooVx * 0.4f + horizontalImpulse).coerceIn(-450f, 450f)
        alooRot += (alooVx * 0.05f)

        // Squash on hit
        squashX = 1.35f
        squashY = 0.7f

        // Combo increment
        combo++
        if (combo > maxCombo) maxCombo = combo

        val pointsEarned = (10 * combo * (if (mirchiTimeLeft > 0f) 2 else 1) * selectedSkin.coinMultiplier).toInt()
        score += pointsEarned

        // Floating combo text
        val praise = when (combo) {
            3 -> "Waah! 3x"
            5 -> "Kya Baat! 5x"
            8 -> "Chatpata! 8x"
            12 -> "Zabardast! 12x"
            15 -> "Balle Balle! 15x"
            20 -> "ALOO MASTER! 20x"
            else -> "+$pointsEarned"
        }
        triggerFloatingText(praise, alooX, alooY - 40f, if (combo >= 5) 0xFFFF5722 else 0xFFFFD54F)

        audio.playBounce()
        spawnSparks(alooX, alooY + 20f, 6, 0xFFFFCA28)

        // Decrement heat slightly with each solid high juggle
        heatLevel = max(0f, heatLevel - 6f)
    }

    fun restartGame(width: Float, height: Float) {
        alooX = width / 2f
        alooY = height * 0.35f
        alooVx = 0f
        alooVy = -200f
        alooRot = 0f
        heatLevel = 0f
        score = 0
        combo = 1
        coinsCollected = 0
        mirchiTimeLeft = 0f
        dhaniyaTimeLeft = 0f
        makhanShieldActive = selectedSkin.id == SkinType.SAMOSA_WARRIOR
        particles.clear()
        fallingItems.clear()
        floatingTexts.clear()
        isGameOver = false
        isPaused = false
        isPlaying = true
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF140D08))
    ) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()

        // Initialize aloo position
        LaunchedEffect(width, height) {
            if (alooX == 0f) {
                alooX = width / 2f
                alooY = height * 0.35f
            }
        }

        // Game Loop
        LaunchedEffect(isPlaying, isPaused, isGameOver) {
            lastFrameTimeNanos = 0L
            while (isPlaying && !isPaused && !isGameOver) {
                withFrameNanos { frameTimeNanos ->
                    if (lastFrameTimeNanos == 0L) {
                        lastFrameTimeNanos = frameTimeNanos
                        return@withFrameNanos
                    }
                    val dt = ((frameTimeNanos - lastFrameTimeNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
                    lastFrameTimeNanos = frameTimeNanos

                    // 1. Gravity and Aloo Physics
                    val currentGravity = if (dhaniyaTimeLeft > 0f) 550f else 1350f
                    alooVy += currentGravity * dt
                    alooX += alooVx * dt
                    alooY += alooVy * dt

                    // Natural air drag
                    alooVx *= (1f - 0.4f * dt)

                    // Squash & stretch recovery
                    squashX += (1.0f - squashX) * 10f * dt
                    squashY += (1.0f - squashY) * 10f * dt

                    // Wall bounces
                    if (alooX - alooRadius < 10f) {
                        alooX = 10f + alooRadius
                        alooVx = -alooVx * 0.75f
                        audio.playBounce()
                    } else if (alooX + alooRadius > width - 10f) {
                        alooX = width - 10f - alooRadius
                        alooVx = -alooVx * 0.75f
                        audio.playBounce()
                    }

                    // Ceiling limit
                    if (alooY - alooRadius < 60f) {
                        alooY = 60f + alooRadius
                        alooVy = 150f
                    }

                    // 2. Bottom Pan & Heat Hazard
                    val panY = height - 120f
                    if (alooY + alooRadius >= panY) {
                        // Potato hit the bottom hot pan!
                        alooY = panY - alooRadius
                        alooVy = -480f // Weak pan sizzle bounce
                        squashX = 1.4f
                        squashY = 0.6f
                        combo = 1 // Reset combo streak!

                        val heatIncrease = 28f / selectedSkin.heatResistance
                        heatLevel = min(100f, heatLevel + heatIncrease)
                        audio.playSizzle()
                        spawnSparks(alooX, panY, 14, 0xFFFF3D00)
                        triggerFloatingText("HOT PAN! SIZZLE!", alooX, panY - 60f, 0xFFFF1744)

                        if (heatLevel >= 100f) {
                            // Roasted! Game Over!
                            isGameOver = true
                            isPlaying = false
                            audio.playDangerHit()
                            // Save high score & coins
                            if (score > prefs.bounceHighScore) {
                                prefs.bounceHighScore = score
                            }
                            prefs.totalBounces += score / 10
                            prefs.alooCoins += coinsCollected
                        }
                    } else {
                        // Slowly cool down when flying high in cool air
                        if (alooY < height * 0.5f && heatLevel > 0f) {
                            heatLevel = max(0f, heatLevel - 6f * dt)
                        }
                    }

                    // 3. Power-up Timers
                    if (mirchiTimeLeft > 0f) {
                        mirchiTimeLeft = max(0f, mirchiTimeLeft - dt)
                        // Emit fiery trail particles
                        if (Random.nextFloat() < 0.6f) {
                            particles.add(
                                Particle(
                                    x = alooX + (Random.nextFloat() - 0.5f) * 30f,
                                    y = alooY + 25f,
                                    vx = (Random.nextFloat() - 0.5f) * 60f,
                                    vy = Random.nextFloat() * 100f + 40f,
                                    alpha = 0.9f,
                                    size = Random.nextFloat() * 10f + 4f,
                                    color = 0xFFFF5722
                                )
                            )
                        }
                    }
                    if (dhaniyaTimeLeft > 0f) {
                        dhaniyaTimeLeft = max(0f, dhaniyaTimeLeft - dt)
                        // Floating green leaves
                        if (Random.nextFloat() < 0.3f) {
                            particles.add(
                                Particle(
                                    x = alooX + (Random.nextFloat() - 0.5f) * 40f,
                                    y = alooY,
                                    vx = (Random.nextFloat() - 0.5f) * 50f,
                                    vy = -Random.nextFloat() * 60f - 20f,
                                    alpha = 0.9f,
                                    size = 14f,
                                    color = 0xFF4CAF50,
                                    isLeaf = true
                                )
                            )
                        }
                    }

                    // 4. Spawn Falling Spices & Hazards
                    itemSpawnTimer += dt
                    val spawnInterval = (2.2f - (score / 1500f).coerceIn(0f, 1.2f))
                    if (itemSpawnTimer >= spawnInterval) {
                        itemSpawnTimer = 0f
                        val rand = Random.nextFloat()
                        val spawnX = Random.nextFloat() * (width - 120f) + 60f
                        if (rand < 0.65f) {
                            // Power-up or Coin item
                            val subRand = Random.nextFloat()
                            val power = when {
                                subRand < 0.28f -> PowerUpType.MIRCHI_RUSH
                                subRand < 0.50f -> PowerUpType.DHANIYA_FLOAT
                                subRand < 0.70f -> PowerUpType.MAKHAN_SHIELD
                                subRand < 0.85f -> PowerUpType.WATER_COOL
                                else -> PowerUpType.CHAAT_MASALA
                            }
                            fallingItems.add(
                                FallingItem(
                                    id = System.nanoTime(),
                                    powerUp = power,
                                    hazard = null,
                                    x = spawnX,
                                    y = -40f,
                                    vy = Random.nextFloat() * 100f + 160f,
                                    rotSpeed = (Random.nextFloat() - 0.5f) * 120f
                                )
                            )
                        } else {
                            // Kitchen Hazard (Knife or Peeler or Oil)
                            val hazard = if (Random.nextBoolean()) HazardType.KNIFE else HazardType.PEELER
                            fallingItems.add(
                                FallingItem(
                                    id = System.nanoTime(),
                                    powerUp = null,
                                    hazard = hazard,
                                    x = spawnX,
                                    y = -50f,
                                    vy = Random.nextFloat() * 160f + 240f,
                                    rotSpeed = (Random.nextFloat() - 0.5f) * 220f
                                )
                            )
                        }
                    }

                    // 5. Update Falling Items & Collision with Aloo
                    val itemIter = fallingItems.iterator()
                    while (itemIter.hasNext()) {
                        val item = itemIter.next()
                        item.y += item.vy * dt
                        item.rotation += item.rotSpeed * dt

                        // Check collision with Aloo
                        val distSq = (item.x - alooX) * (item.x - alooX) + (item.y - alooY) * (item.y - alooY)
                        val collideDist = alooRadius + item.radius
                        if (distSq <= collideDist * collideDist) {
                            if (item.powerUp != null) {
                                // Collected power-up!
                                audio.playCoin()
                                coinsCollected += 5
                                when (item.powerUp) {
                                    PowerUpType.MIRCHI_RUSH -> {
                                        mirchiTimeLeft = PowerUpType.MIRCHI_RUSH.durationSec
                                        triggerFloatingText("MIRCHI RUSH! 2X!", item.x, item.y, 0xFFFF3D00)
                                        audio.playPowerUp()
                                    }
                                    PowerUpType.DHANIYA_FLOAT -> {
                                        dhaniyaTimeLeft = PowerUpType.DHANIYA_FLOAT.durationSec
                                        triggerFloatingText("DHANIYA FLOAT!", item.x, item.y, 0xFF66BB6A)
                                        audio.playPowerUp()
                                    }
                                    PowerUpType.MAKHAN_SHIELD -> {
                                        makhanShieldActive = true
                                        triggerFloatingText("MAKHAN SHIELD!", item.x, item.y, 0xFFFFEE58)
                                        audio.playPowerUp()
                                    }
                                    PowerUpType.WATER_COOL -> {
                                        heatLevel = 0f
                                        triggerFloatingText("COOL SPLASH! 0% HEAT", item.x, item.y, 0xFF40C4FF)
                                        audio.playPowerUp()
                                    }
                                    PowerUpType.CHAAT_MASALA -> {
                                        score += 150
                                        fallingItems.filter { it.hazard != null }.forEach {
                                            spawnSparks(it.x, it.y, 10, 0xFFFFD700)
                                        }
                                        fallingItems.removeAll { it.hazard != null }
                                        triggerFloatingText("CHAAT MASALA BLAST!", item.x, item.y, 0xFFFFC107)
                                        audio.playVictory()
                                    }
                                }
                                spawnSparks(item.x, item.y, 12, 0xFFFFD700)
                                itemIter.remove()
                                continue
                            } else if (item.hazard != null) {
                                // Hit Hazard!
                                if (makhanShieldActive) {
                                    // Shield absorbs the blow!
                                    makhanShieldActive = false
                                    audio.playBounce()
                                    spawnSparks(item.x, item.y, 15, 0xFFFFF176)
                                    triggerFloatingText("SHIELD DEFLECT!", item.x, item.y, 0xFFFFF9C4)
                                    itemIter.remove()
                                    continue
                                } else {
                                    // Take Damage!
                                    audio.playDangerHit()
                                    heatLevel = min(100f, heatLevel + 35f)
                                    alooVy = 300f // knocked down
                                    combo = 1
                                    triggerFloatingText("OUCH! HAZARD HIT!", item.x, item.y, 0xFFFF1744)
                                    spawnSparks(item.x, item.y, 18, 0xFFD50000)
                                    itemIter.remove()
                                    if (heatLevel >= 100f) {
                                        isGameOver = true
                                        isPlaying = false
                                        if (score > prefs.bounceHighScore) prefs.bounceHighScore = score
                                        prefs.alooCoins += coinsCollected
                                    }
                                    continue
                                }
                            }
                        }

                        // Remove off screen
                        if (item.y > height + 80f) {
                            itemIter.remove()
                        }
                    }

                    // 6. Update Particles
                    val pIter = particles.iterator()
                    while (pIter.hasNext()) {
                        val p = pIter.next()
                        p.x += p.vx * dt
                        p.y += p.vy * dt
                        p.rotation += p.rotSpeed * dt
                        p.alpha -= 1.4f * dt
                        if (p.alpha <= 0f) {
                            pIter.remove()
                        }
                    }

                    // 7. Update Floating Texts
                    val tIter = floatingTexts.iterator()
                    while (tIter.hasNext()) {
                        val t = tIter.next()
                        t.y -= 70f * dt
                        t.alpha -= 0.85f * dt
                        if (t.alpha <= 0f) {
                            tIter.remove()
                        }
                    }
                }
            }
        }

        // Tap gestures to juggle potato anywhere or direct tap
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        bounceAloo(offset.x, offset.y, width)
                    }
                }
        ) {
            // Main Canvas Rendering for Pan, Background Kitchen, Flames, Falling Items, Particles
            Canvas(modifier = Modifier.fillMaxSize()) {
                val panY = height - 120f

                // 1. Kitchen Tile Background pattern
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF2B1D16), Color(0xFF19110B), Color(0xFF0F0A07)),
                        startY = 0f,
                        endY = height
                    )
                )

                // Counter backsplash lines
                val gridSpacing = 80f
                var gy = 0f
                while (gy < height) {
                    drawLine(
                        color = Color(0x15FFFFFF),
                        start = Offset(0f, gy),
                        end = Offset(width, gy),
                        strokeWidth = 1.5f
                    )
                    gy += gridSpacing
                }

                // 2. Sizzling Frying Pan at the bottom
                // Stove burner flames
                val flameCount = 12
                val flameSpacing = width / flameCount
                for (i in 0 until flameCount) {
                    val fx = i * flameSpacing + flameSpacing * 0.5f
                    val flameH = 25f + sin((System.currentTimeMillis() * 0.01f + i).toDouble()).toFloat() * 12f
                    val flamePath = Path().apply {
                        moveTo(fx - 14f, height - 10f)
                        quadraticTo(fx, height - 10f - flameH, fx + 14f, height - 10f)
                        close()
                    }
                    drawPath(
                        flamePath,
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFFFFEA00), Color(0xFFFF3D00)),
                            startY = height - 10f - flameH,
                            endY = height - 10f
                        )
                    )
                }

                // Cast Iron Pan Body
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF3E2723), Color(0xFF211510), Color(0xFF140D0A)),
                        startY = panY,
                        endY = height
                    ),
                    topLeft = Offset(16f, panY),
                    size = Size(width - 32f, 100f),
                    cornerRadius = CornerRadius(24f, 24f)
                )
                // Pan Rim & Sizzle butter line
                drawRoundRect(
                    color = Color(0xFF8D6E63),
                    topLeft = Offset(16f, panY),
                    size = Size(width - 32f, 18f),
                    cornerRadius = CornerRadius(12f, 12f)
                )

                // Hot Pan Glow based on Heat Level
                if (heatLevel > 20f) {
                    val glowAlpha = (heatLevel / 100f).coerceIn(0.1f, 0.7f)
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFFFF3D00).copy(alpha = glowAlpha), Color.Transparent),
                            startY = panY - 30f,
                            endY = panY + 40f
                        ),
                        topLeft = Offset(16f, panY - 30f),
                        size = Size(width - 32f, 70f),
                        cornerRadius = CornerRadius(24f, 24f)
                    )
                }

                // Sizzling oil drops on pan
                for (i in 0..5) {
                    val sx = 60f + i * ((width - 120f) / 5f)
                    val sRadius = 5f + sin((System.currentTimeMillis() * 0.008f + i * 2).toDouble()).toFloat() * 2f
                    drawCircle(
                        color = Color(0xFFFFD54F),
                        radius = sRadius,
                        center = Offset(sx, panY + 10f)
                    )
                }

                // 3. Falling Items (PowerUps & Hazards)
                for (item in fallingItems) {
                    rotate(degrees = item.rotation, pivot = Offset(item.x, item.y)) {
                        if (item.powerUp != null) {
                            drawPowerUpGraphic(item.powerUp, item.x, item.y, item.radius)
                        } else if (item.hazard != null) {
                            drawHazardGraphic(item.hazard, item.x, item.y, item.radius)
                        }
                    }
                }

                // 4. Particles
                for (p in particles) {
                    if (p.isLeaf) {
                        // Coriander leaf
                        drawOval(
                            color = Color(p.color).copy(alpha = p.alpha),
                            topLeft = Offset(p.x - p.size / 2f, p.y - p.size / 3f),
                            size = Size(p.size, p.size * 0.6f)
                        )
                    } else {
                        // Spark / Ember / Coin
                        drawCircle(
                            color = Color(p.color).copy(alpha = p.alpha),
                            radius = p.size,
                            center = Offset(p.x, p.y)
                        )
                    }
                }
            }

            // Draw Potato Character (Composed using AlooCharacterGraphic with live physics coordinates)
            val expression = when {
                isGameOver -> AlooExpression.DEAD
                mirchiTimeLeft > 0f -> AlooExpression.FIERY
                heatLevel > 65f || (alooY > height - 260f) -> AlooExpression.PANIC
                dhaniyaTimeLeft > 0f -> AlooExpression.CHILL
                else -> AlooExpression.HAPPY
            }

            val displayDensity = context.resources.displayMetrics.density
            val alooDrawSizeDp = ((alooRadius * 2.3f) / displayDensity).dp

            Box(
                modifier = Modifier.offset {
                    androidx.compose.ui.unit.IntOffset(
                        kotlin.math.round((alooX - alooRadius * 1.15f)).toInt(),
                        kotlin.math.round((alooY - alooRadius * 1.15f)).toInt()
                    )
                }
            ) {
                AlooCharacterGraphic(
                    skinType = selectedSkin.id,
                    expression = expression,
                    squashX = squashX,
                    squashY = squashY,
                    rotationDeg = alooRot,
                    isShielded = makhanShieldActive,
                    size = alooDrawSizeDp
                )
            }

            // Floating Texts Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                for (t in floatingTexts) {
                    // Draw glowing indicator dot
                    drawCircle(
                        color = Color(t.color).copy(alpha = t.alpha),
                        radius = 6f,
                        center = Offset(t.x, t.y)
                    )
                }
            }

            // Top HUD Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, start = 16.dp, end = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Score & Combo
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Score: $score",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD54F)
                                )
                            )
                            if (combo > 1) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = if (combo >= 5) Color(0xFFFF5722) else Color(0xFFFFB300),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "${combo}x STREAK",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Aloo Coins: +$coinsCollected 🥔",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFFFECB3))
                        )
                    }

                    // Action buttons
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { isPaused = !isPaused },
                            modifier = Modifier
                                .background(Color(0x66000000), CircleShape)
                                .testTag("pause_button")
                        ) {
                            Icon(
                                imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = if (isPaused) "Resume" else "Pause",
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .background(Color(0x66000000), CircleShape)
                                .testTag("close_game_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Exit",
                                tint = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Heat Gauge Meter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (heatLevel > 70f) "🔥 SIZZLE PANIC!" else "🍳 Pan Heat:",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (heatLevel > 70f) Color(0xFFFF1744) else Color(0xFFFFCA28)
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    LinearProgressIndicator(
                        progress = { (heatLevel / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .weight(1f)
                            .height(10.dp)
                            .background(Color(0x44FFFFFF), RoundedCornerShape(5.dp)),
                        color = when {
                            heatLevel > 75f -> Color(0xFFFF1744)
                            heatLevel > 45f -> Color(0xFFFF9800)
                            else -> Color(0xFF4CAF50)
                        },
                        trackColor = Color(0x33000000),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${heatLevel.toInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
                    )
                }

                // Active Power-up Badges
                Row(modifier = Modifier.padding(top = 6.dp)) {
                    if (mirchiTimeLeft > 0f) {
                        Surface(
                            color = Color(0xFFD50000),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text(
                                text = "🌶️ Mirchi ${mirchiTimeLeft.toInt()}s",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                    if (dhaniyaTimeLeft > 0f) {
                        Surface(
                            color = Color(0xFF2E7D32),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text(
                                text = "🌿 Float ${dhaniyaTimeLeft.toInt()}s",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                    if (makhanShieldActive) {
                        Surface(
                            color = Color(0xFFFBC02D),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "🧈 Shield Active",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF3E2723), fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            // Pause Overlay Dialog
            AnimatedVisibility(
                visible = isPaused,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xCC000000))
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {},
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2B1D16)),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Game Paused",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD54F)
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Tap the screen or potato to keep Chotu Aloo juggling in the air. Avoid the hot pan at the bottom!",
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFFFECB3))
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = { isPaused = false },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("resume_game_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57C00))
                            ) {
                                Text("Resume Playing", fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { restartGame(width, height) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Restart", color = Color(0xFFFFD54F))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = onBack,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Exit to Menu", color = Color.White)
                            }
                        }
                    }
                }
            }

            // Game Over Dialog
            AnimatedVisibility(
                visible = isGameOver,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xDD000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.88f)
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2E1911)),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🔥 ALOO ROASTED! 🔥",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFFF3D00)
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Chotu Aloo touched the sizzling pan too long!",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFFFCC80))
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Stats breakdown
                            Surface(
                                color = Color(0xFF1B100B),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Score:", color = Color.White)
                                        Text("$score", fontWeight = FontWeight.Bold, color = Color(0xFFFFD54F))
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Best Combo Streak:", color = Color.White)
                                        Text("${maxCombo}x", fontWeight = FontWeight.Bold, color = Color(0xFFFF7043))
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Aloo Coins Earned:", color = Color.White)
                                        Text("+$coinsCollected 🥔", fontWeight = FontWeight.Bold, color = Color(0xFF81C784))
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("All-Time High Score:", color = Color.White)
                                        Text("${prefs.bounceHighScore}", fontWeight = FontWeight.Bold, color = Color(0xFFFFCA28))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = { restartGame(width, height) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("play_again_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57C00))
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Bounce Again!", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = onBack,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("game_over_home_button")
                            ) {
                                Text("Back to Kitchen Menu", color = Color(0xFFFFE082))
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawPowerUpGraphic(type: PowerUpType, x: Float, y: Float, radius: Float) {
    when (type) {
        PowerUpType.MIRCHI_RUSH -> {
            // Bright red fiery chilli with green stalk
            val chilliPath = Path().apply {
                moveTo(x, y - radius * 0.9f)
                quadraticTo(x + radius * 0.9f, y, x + radius * 0.2f, y + radius * 0.9f)
                quadraticTo(x - radius * 0.1f, y + radius * 0.5f, x - radius * 0.5f, y - radius * 0.4f)
                close()
            }
            drawPath(chilliPath, color = Color(0xFFD50000))
            // Green cap
            drawCircle(color = Color(0xFF2E7D32), radius = radius * 0.3f, center = Offset(x, y - radius * 0.75f))
        }
        PowerUpType.DHANIYA_FLOAT -> {
            // Triple green coriander leaves
            drawCircle(color = Color(0xFF4CAF50), radius = radius * 0.5f, center = Offset(x - radius * 0.3f, y - radius * 0.2f))
            drawCircle(color = Color(0xFF388E3C), radius = radius * 0.5f, center = Offset(x + radius * 0.3f, y - radius * 0.2f))
            drawCircle(color = Color(0xFF2E7D32), radius = radius * 0.55f, center = Offset(x, y + radius * 0.2f))
        }
        PowerUpType.MAKHAN_SHIELD -> {
            // Melting golden butter cube
            drawRoundRect(
                color = Color(0xFFFFD54F),
                topLeft = Offset(x - radius * 0.7f, y - radius * 0.6f),
                size = Size(radius * 1.4f, radius * 1.2f),
                cornerRadius = CornerRadius(8f, 8f)
            )
            drawRoundRect(
                color = Color(0xFFFFF9C4),
                topLeft = Offset(x - radius * 0.5f, y - radius * 0.5f),
                size = Size(radius * 1.0f, radius * 0.5f),
                cornerRadius = CornerRadius(6f, 6f)
            )
        }
        PowerUpType.WATER_COOL -> {
            // Cool cyan water droplet
            val dropPath = Path().apply {
                moveTo(x, y - radius * 0.8f)
                lineTo(x + radius * 0.65f, y + radius * 0.3f)
                quadraticTo(x, y + radius * 0.9f, x - radius * 0.65f, y + radius * 0.3f)
                close()
            }
            drawPath(dropPath, color = Color(0xFF00E5FF))
        }
        PowerUpType.CHAAT_MASALA -> {
            // Golden spice pouch with sparkles
            drawCircle(color = Color(0xFFFFC107), radius = radius * 0.8f, center = Offset(x, y))
            drawCircle(color = Color(0xFFFF6F00), radius = radius * 0.4f, center = Offset(x, y))
            drawCircle(color = Color.White, radius = radius * 0.2f, center = Offset(x - radius * 0.2f, y - radius * 0.2f))
        }
    }
}

private fun DrawScope.drawHazardGraphic(type: HazardType, x: Float, y: Float, radius: Float) {
    when (type) {
        HazardType.KNIFE -> {
            // Kitchen knife blade + wooden handle
            val bladePath = Path().apply {
                moveTo(x - radius * 0.3f, y - radius * 1.1f)
                lineTo(x + radius * 0.3f, y - radius * 0.7f)
                lineTo(x + radius * 0.2f, y + radius * 0.2f)
                lineTo(x - radius * 0.3f, y + radius * 0.2f)
                close()
            }
            drawPath(bladePath, color = Color(0xFFECEFF1))
            drawPath(bladePath, color = Color(0xFF90A4AE), style = Stroke(width = 2.dp.toPx()))
            // Wooden Handle
            drawRoundRect(
                color = Color(0xFF5D4037),
                topLeft = Offset(x - radius * 0.25f, y + radius * 0.2f),
                size = Size(radius * 0.5f, radius * 0.9f),
                cornerRadius = CornerRadius(4f, 4f)
            )
        }
        HazardType.PEELER -> {
            // Steel Y-peeler
            drawRoundRect(
                color = Color(0xFFB0BEC5),
                topLeft = Offset(x - radius * 0.5f, y - radius * 0.8f),
                size = Size(radius * 1.0f, radius * 0.4f),
                cornerRadius = CornerRadius(4f, 4f)
            )
            // Handle
            drawLine(
                color = Color(0xFF37474F),
                start = Offset(x, y - radius * 0.4f),
                end = Offset(x, y + radius * 1.0f),
                strokeWidth = 10f
            )
        }
        HazardType.OIL_DROP -> {
            // Boiling splattering oil droplet
            drawCircle(color = Color(0xFFFFAB00), radius = radius * 0.75f, center = Offset(x, y))
            drawCircle(color = Color(0xFFFF6D00), radius = radius * 0.45f, center = Offset(x, y))
        }
        HazardType.FLAME_JET -> {
            drawCircle(color = Color(0xFFFF1744), radius = radius * 0.8f, center = Offset(x, y))
        }
    }
}
