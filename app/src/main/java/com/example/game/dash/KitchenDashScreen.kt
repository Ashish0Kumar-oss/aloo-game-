package com.example.game.dash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import com.example.model.Particle
import com.example.model.SkinType
import com.example.ui.components.AlooCharacterGraphic
import com.example.ui.components.AlooExpression
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random

enum class DashObstacleType {
    ROLLING_PIN,      // Ground hazard - must Jump over
    HANGING_LADLE,    // Overhead hazard - must Slide under
    BOILING_KETTLE,   // Full lane blocker - must dodge left/right
    CHILLI_BARRIER    // Full lane blocker
}

enum class DashPickupType {
    ALOO_COIN,
    SPICY_TURBO,
    SHIELD_BUTTER
}

data class DashEntity(
    val id: Long,
    val lane: Int, // 0 = Left, 1 = Center, 2 = Right
    var y: Float,  // 0.0 (top horizon) to height (player)
    val obstacle: DashObstacleType?,
    val pickup: DashPickupType?,
    var isCollected: Boolean = false
)

@Composable
fun KitchenDashScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { GamePreferences(context) }
    val audio = remember { SoundEffects(context, prefs) }
    val selectedSkin = remember { prefs.getSkins().firstOrNull { it.id == prefs.selectedSkin } ?: prefs.getSkins().first() }

    var isRunning by remember { mutableStateOf(true) }
    var isGameOver by remember { mutableStateOf(false) }

    var playerLane by remember { mutableIntStateOf(1) } // 0 = Left, 1 = Center, 2 = Right
    var playerLaneX by remember { mutableFloatStateOf(0f) }
    var targetLaneX by remember { mutableFloatStateOf(0f) }

    var isJumping by remember { mutableStateOf(false) }
    var jumpProgress by remember { mutableFloatStateOf(0f) }
    var jumpVelocity by remember { mutableFloatStateOf(0f) }

    var isSliding by remember { mutableStateOf(false) }
    var slideTimer by remember { mutableFloatStateOf(0f) }

    var distanceMeters by remember { mutableFloatStateOf(0f) }
    var currentSpeed by remember { mutableFloatStateOf(350f) }
    var coinsCollected by remember { mutableIntStateOf(0) }
    var turboTimeLeft by remember { mutableFloatStateOf(0f) }
    var hasShield by remember { mutableStateOf(selectedSkin.id == SkinType.SAMOSA_WARRIOR) }

    var rollRotation by remember { mutableFloatStateOf(0f) }
    var lastFrameTimeNanos by remember { mutableStateOf(0L) }
    var spawnTimer by remember { mutableFloatStateOf(0f) }

    val entities = remember { mutableStateListOf<DashEntity>() }
    val particles = remember { mutableStateListOf<Particle>() }

    fun jump() {
        if (!isJumping && !isSliding) {
            isJumping = true
            jumpProgress = 0f
            jumpVelocity = 750f
            audio.playBounce()
        }
    }

    fun slide() {
        if (!isSliding && !isJumping) {
            isSliding = true
            slideTimer = 0.6f
            audio.playBounce()
        }
    }

    fun moveLane(delta: Int) {
        val newLane = (playerLane + delta).coerceIn(0, 2)
        if (newLane != playerLane) {
            playerLane = newLane
            audio.playBounce()
        }
    }

    fun restart() {
        playerLane = 1
        distanceMeters = 0f
        currentSpeed = 350f
        coinsCollected = 0
        turboTimeLeft = 0f
        hasShield = selectedSkin.id == SkinType.SAMOSA_WARRIOR
        isJumping = false
        isSliding = false
        jumpProgress = 0f
        slideTimer = 0f
        entities.clear()
        particles.clear()
        isGameOver = false
        isRunning = true
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF19110B))
    ) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()

        val laneWidth = width / 3f
        val laneCenters = floatArrayOf(laneWidth * 0.5f, laneWidth * 1.5f, laneWidth * 2.5f)
        val playerBaseY = height - 190f

        targetLaneX = laneCenters[playerLane]
        if (playerLaneX == 0f) playerLaneX = targetLaneX

        // Game Loop
        LaunchedEffect(isRunning, isGameOver) {
            lastFrameTimeNanos = 0L
            while (isRunning && !isGameOver) {
                withFrameNanos { frameTimeNanos ->
                    if (lastFrameTimeNanos == 0L) {
                        lastFrameTimeNanos = frameTimeNanos
                        return@withFrameNanos
                    }
                    val dt = ((frameTimeNanos - lastFrameTimeNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
                    lastFrameTimeNanos = frameTimeNanos

                    // Smooth lane shift
                    playerLaneX += (targetLaneX - playerLaneX) * 16f * dt

                    // Speed and Distance
                    val effectiveSpeed = if (turboTimeLeft > 0f) currentSpeed * 1.8f else currentSpeed
                    distanceMeters += (effectiveSpeed * dt * 0.08f)
                    currentSpeed = (350f + distanceMeters * 0.8f).coerceAtMost(700f)
                    rollRotation += effectiveSpeed * dt * 0.5f

                    // Turbo countdown
                    if (turboTimeLeft > 0f) {
                        turboTimeLeft = (turboTimeLeft - dt).coerceAtLeast(0f)
                    }

                    // Jump Physics
                    if (isJumping) {
                        jumpProgress += jumpVelocity * dt
                        jumpVelocity -= 2200f * dt
                        if (jumpProgress <= 0f) {
                            jumpProgress = 0f
                            isJumping = false
                        }
                    }

                    // Slide Timer
                    if (isSliding) {
                        slideTimer -= dt
                        if (slideTimer <= 0f) {
                            isSliding = false
                        }
                    }

                    // Spawn Entities (Obstacles and Pickups)
                    spawnTimer += dt
                    val spawnInterval = (1.4f - (distanceMeters / 1500f).coerceIn(0f, 0.6f))
                    if (spawnTimer >= spawnInterval) {
                        spawnTimer = 0f
                        val lane = Random.nextInt(3)
                        val isPickup = Random.nextFloat() < 0.45f
                        if (isPickup) {
                            val pType = if (Random.nextFloat() < 0.25f) DashPickupType.SPICY_TURBO else DashPickupType.ALOO_COIN
                            entities.add(DashEntity(System.nanoTime(), lane, -40f, null, pType))
                        } else {
                            val r = Random.nextFloat()
                            val obs = when {
                                r < 0.4f -> DashObstacleType.ROLLING_PIN
                                r < 0.7f -> DashObstacleType.HANGING_LADLE
                                else -> DashObstacleType.BOILING_KETTLE
                            }
                            entities.add(DashEntity(System.nanoTime(), lane, -40f, obs, null))
                        }
                    }

                    // Move entities & Check collisions
                    val iter = entities.iterator()
                    while (iter.hasNext()) {
                        val e = iter.next()
                        e.y += effectiveSpeed * dt

                        // Collision window near player
                        val hitWindowY = abs(e.y - playerBaseY) < 40f
                        val sameLane = e.lane == playerLane

                        if (sameLane && hitWindowY && !e.isCollected) {
                            if (e.pickup != null) {
                                e.isCollected = true
                                audio.playCoin()
                                if (e.pickup == DashPickupType.ALOO_COIN) {
                                    coinsCollected += 3
                                } else if (e.pickup == DashPickupType.SPICY_TURBO) {
                                    turboTimeLeft = 5.0f
                                    audio.playPowerUp()
                                }
                                iter.remove()
                                continue
                            } else if (e.obstacle != null) {
                                // Check if player successfully avoided
                                val dodged = when (e.obstacle) {
                                    DashObstacleType.ROLLING_PIN -> isJumping && jumpProgress > 35f
                                    DashObstacleType.HANGING_LADLE -> isSliding
                                    else -> false // Kettle must be dodged by switching lanes!
                                }

                                if (!dodged) {
                                    if (turboTimeLeft > 0f) {
                                        // Turbo destroys obstacle!
                                        audio.playVictory()
                                        iter.remove()
                                        continue
                                    } else if (hasShield) {
                                        hasShield = false
                                        audio.playBounce()
                                        iter.remove()
                                        continue
                                    } else {
                                        // CRASH!
                                        isGameOver = true
                                        isRunning = false
                                        audio.playDangerHit()
                                        if (distanceMeters.toInt() > prefs.dashHighScore) {
                                            prefs.dashHighScore = distanceMeters.toInt()
                                        }
                                        prefs.alooCoins += coinsCollected
                                        break
                                    }
                                }
                            }
                        }

                        if (e.y > height + 60f) {
                            iter.remove()
                        }
                    }
                }
            }
        }

        // Gesture Detection for Swipe (Left, Right, Up, Down)
        var totalDragX = 0f
        var totalDragY = 0f

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            totalDragX = 0f
                            totalDragY = 0f
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            totalDragX += dragAmount.x
                            totalDragY += dragAmount.y
                        },
                        onDragEnd = {
                            if (abs(totalDragX) > abs(totalDragY)) {
                                if (totalDragX < -40f) moveLane(-1)
                                else if (totalDragX > 40f) moveLane(1)
                            } else {
                                if (totalDragY < -40f) jump()
                                else if (totalDragY > 40f) slide()
                            }
                        }
                    )
                }
        ) {
            // Kitchen Counter Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Background Counter Top
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF23160F), Color(0xFF160E0A)),
                        startY = 0f,
                        endY = height
                    )
                )

                // Marble Kitchen Counter Lanes
                for (lane in 0..2) {
                    val lx = lane * laneWidth
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = if (lane % 2 == 0) listOf(Color(0xFF332016), Color(0xFF261810)) else listOf(Color(0xFF2E1C13), Color(0xFF1E120B)),
                            startY = 0f,
                            endY = height
                        ),
                        topLeft = Offset(lx + 4f, 0f),
                        size = Size(laneWidth - 8f, height),
                        cornerRadius = CornerRadius(0f, 0f)
                    )
                }

                // Lane divider lines
                for (i in 1..2) {
                    val lineX = i * laneWidth
                    drawLine(
                        color = Color(0x33FFB74D),
                        start = Offset(lineX, 0f),
                        end = Offset(lineX, height),
                        strokeWidth = 3f
                    )
                }

                // Scrolling counter grain lines
                val grainOffset = (rollRotation * 2f) % 120f
                var gy = grainOffset
                while (gy < height) {
                    drawLine(
                        color = Color(0x15FFFFFF),
                        start = Offset(0f, gy),
                        end = Offset(width, gy),
                        strokeWidth = 2f
                    )
                    gy += 120f
                }

                // Draw Entities
                for (e in entities) {
                    val cx = laneCenters[e.lane]
                    val cy = e.y

                    if (e.pickup != null) {
                        when (e.pickup) {
                            DashPickupType.ALOO_COIN -> {
                                // Golden Aloo Wedge
                                drawCircle(Color(0xFFFFD54F), radius = 24f, center = Offset(cx, cy))
                                drawCircle(Color(0xFFFFB300), radius = 16f, center = Offset(cx, cy))
                                drawCircle(Color.White, radius = 5f, center = Offset(cx - 5f, cy - 5f))
                            }
                            DashPickupType.SPICY_TURBO -> {
                                // Fiery Red Chilli
                                val chilliPath = Path().apply {
                                    moveTo(cx, cy - 25f)
                                    quadraticTo(cx + 20f, cy, cx + 5f, cy + 25f)
                                    quadraticTo(cx - 5f, cy + 10f, cx - 15f, cy - 10f)
                                    close()
                                }
                                drawPath(chilliPath, color = Color(0xFFD50000))
                                drawCircle(color = Color(0xFF2E7D32), radius = 7f, center = Offset(cx, cy - 20f))
                            }
                            DashPickupType.SHIELD_BUTTER -> {
                                drawRoundRect(
                                    color = Color(0xFFFFF59D),
                                    topLeft = Offset(cx - 18f, cy - 18f),
                                    size = Size(36f, 36f),
                                    cornerRadius = CornerRadius(6f, 6f)
                                )
                            }
                        }
                    } else if (e.obstacle != null) {
                        when (e.obstacle) {
                            DashObstacleType.ROLLING_PIN -> {
                                // Wooden Rolling Pin (Jump over!)
                                drawRoundRect(
                                    color = Color(0xFF8D6E63),
                                    topLeft = Offset(cx - laneWidth * 0.42f, cy - 14f),
                                    size = Size(laneWidth * 0.84f, 28f),
                                    cornerRadius = CornerRadius(14f, 14f)
                                )
                                drawRoundRect(
                                    color = Color(0xFF5D4037),
                                    topLeft = Offset(cx - laneWidth * 0.48f, cy - 7f),
                                    size = Size(laneWidth * 0.96f, 14f),
                                    cornerRadius = CornerRadius(7f, 7f)
                                )
                                // Red "JUMP" indicator arrow
                                drawCircle(Color(0xFFFF1744), radius = 12f, center = Offset(cx, cy - 28f))
                            }
                            DashObstacleType.HANGING_LADLE -> {
                                // Steel Ladle hanging (Slide under!)
                                drawRoundRect(
                                    color = Color(0xFFB0BEC5),
                                    topLeft = Offset(cx - laneWidth * 0.38f, cy - 25f),
                                    size = Size(laneWidth * 0.76f, 20f),
                                    cornerRadius = CornerRadius(6f, 6f)
                                )
                                // Yellow "SLIDE" indicator arrow
                                drawCircle(Color(0xFFFFEA00), radius = 12f, center = Offset(cx, cy + 20f))
                            }
                            DashObstacleType.BOILING_KETTLE -> {
                                // Full lane barrier Kettle
                                drawCircle(Color(0xFF37474F), radius = 30f, center = Offset(cx, cy))
                                drawCircle(Color(0xFFCFD8DC), radius = 12f, center = Offset(cx - 8f, cy - 8f))
                            }
                            DashObstacleType.CHILLI_BARRIER -> {
                                drawRect(Color(0xFFD50000), topLeft = Offset(cx - laneWidth * 0.4f, cy - 10f), size = Size(laneWidth * 0.8f, 20f))
                            }
                        }
                    }
                }
            }

            // Player Potato Graphic
            val playerY = playerBaseY - jumpProgress
            val (squashX, squashY) = when {
                isSliding -> Pair(1.4f, 0.55f)
                isJumping -> Pair(0.85f, 1.25f)
                else -> Pair(1.0f, 1.0f)
            }

            val expression = when {
                isGameOver -> AlooExpression.DEAD
                turboTimeLeft > 0f -> AlooExpression.FIERY
                isJumping -> AlooExpression.HAPPY
                else -> AlooExpression.HAPPY
            }

            Box(
                modifier = Modifier.offset {
                    androidx.compose.ui.unit.IntOffset(
                        kotlin.math.round((playerLaneX - 45f)).toInt(),
                        kotlin.math.round((playerY - 45f)).toInt()
                    )
                }
            ) {
                AlooCharacterGraphic(
                    skinType = selectedSkin.id,
                    expression = expression,
                    squashX = squashX,
                    squashY = squashY,
                    rotationDeg = rollRotation,
                    isShielded = hasShield,
                    size = 90.dp
                )
            }

            // Top HUD
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
                    Column {
                        Text(
                            text = "${distanceMeters.toInt()} m",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFFD54F)
                            )
                        )
                        Text(
                            text = "+$coinsCollected Aloo Coins",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFFFECB3))
                        )
                    }

                    Row {
                        if (turboTimeLeft > 0f) {
                            Surface(
                                color = Color(0xFFD50000),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text(
                                    text = "🔥 TURBO ${turboTimeLeft.toInt()}s",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .background(Color(0x66000000), CircleShape)
                                .testTag("dash_close_button")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Exit", tint = Color.White)
                        }
                    }
                }
            }

            // Bottom On-Screen Tactile Controls (For comfortable thumb playing)
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left & Right Lane Switch Buttons
                Row {
                    FilledIconButton(
                        onClick = { moveLane(-1) },
                        modifier = Modifier
                            .size(54.dp)
                            .testTag("dash_left_button"),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0x993E2723))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Move Left", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    FilledIconButton(
                        onClick = { moveLane(1) },
                        modifier = Modifier
                            .size(54.dp)
                            .testTag("dash_right_button"),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0x993E2723))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Move Right", tint = Color.White)
                    }
                }

                // Jump & Slide Action Buttons
                Row {
                    FilledIconButton(
                        onClick = { slide() },
                        modifier = Modifier
                            .size(54.dp)
                            .testTag("dash_slide_button"),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0x99E65100))
                    ) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Slide Down", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    FilledIconButton(
                        onClick = { jump() },
                        modifier = Modifier
                            .size(54.dp)
                            .testTag("dash_jump_button"),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFFF57C00))
                    ) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Jump Up", tint = Color.White)
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
                                text = "💥 KITCHEN CRASH! 💥",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFFF5722)
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Chotu Aloo ran into kitchen cutlery!",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFFFCC80))
                            )

                            Spacer(modifier = Modifier.height(16.dp))

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
                                        Text("Distance Ran:", color = Color.White)
                                        Text("${distanceMeters.toInt()} meters", fontWeight = FontWeight.Bold, color = Color(0xFFFFD54F))
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Aloo Coins Collected:", color = Color.White)
                                        Text("+$coinsCollected 🥔", fontWeight = FontWeight.Bold, color = Color(0xFF81C784))
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Best Dash Record:", color = Color.White)
                                        Text("${prefs.dashHighScore} meters", fontWeight = FontWeight.Bold, color = Color(0xFFFFCA28))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = { restart() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("dash_play_again_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57C00))
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Sprint Again!", fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = onBack,
                                modifier = Modifier.fillMaxWidth()
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
