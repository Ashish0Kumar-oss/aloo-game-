package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.SkinType

enum class AlooExpression {
    HAPPY,
    PANIC,
    FIERY,
    CHILL,
    DEAD
}

@Composable
fun AlooCharacterGraphic(
    modifier: Modifier = Modifier,
    skinType: SkinType = SkinType.CLASSIC_CHOTU,
    expression: AlooExpression = AlooExpression.HAPPY,
    squashX: Float = 1.0f,
    squashY: Float = 1.0f,
    rotationDeg: Float = 0f,
    isShielded: Boolean = false,
    size: Dp = 100.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "aloo_anim")
    val blinkProgress by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "blink"
    )

    val flamePulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flame"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val canvasW = this.size.width
            val canvasH = this.size.height
            val centerX = canvasW / 2f
            val centerY = canvasH / 2f

            rotate(degrees = rotationDeg, pivot = Offset(centerX, centerY)) {
                scale(scaleX = squashX, scaleY = squashY, pivot = Offset(centerX, centerY)) {

                    // 1. Butter Shield Bubble if active
                    if (isShielded) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0x33FFF59D), Color(0x99FFE082), Color(0xEEFFD54F)),
                                center = Offset(centerX, centerY),
                                radius = canvasW * 0.58f
                            ),
                            radius = canvasW * 0.52f,
                            center = Offset(centerX, centerY)
                        )
                        drawCircle(
                            color = Color(0xFFFFEE58),
                            radius = canvasW * 0.52f,
                            center = Offset(centerX, centerY),
                            style = Stroke(width = 4.dp.toPx())
                        )
                    }

                    // 2. Base Potato Body based on SkinType
                    val baseColor = when (skinType) {
                        SkinType.CLASSIC_CHOTU -> Color(0xFFF5B041)
                        SkinType.GARAM_SPICY -> Color(0xFFFF7043)
                        SkinType.SAMOSA_WARRIOR -> Color(0xFFE5A968)
                        SkinType.FRENCH_FRY_FREDDY -> Color(0xFFFFD54F)
                        SkinType.ALOO_TIKKI_KING -> Color(0xFFBCAAA4)
                        SkinType.PARATHA_PRINCE -> Color(0xFFFFE082)
                        SkinType.SPACE_SPUD -> Color(0xFF80DEEA)
                    }

                    val shadowColor = when (skinType) {
                        SkinType.CLASSIC_CHOTU -> Color(0xFFD38A28)
                        SkinType.GARAM_SPICY -> Color(0xFFD84315)
                        SkinType.SAMOSA_WARRIOR -> Color(0xFF935116)
                        SkinType.FRENCH_FRY_FREDDY -> Color(0xFFFFB300)
                        SkinType.ALOO_TIKKI_KING -> Color(0xFF6D4C41)
                        SkinType.PARATHA_PRINCE -> Color(0xFFFFB300)
                        SkinType.SPACE_SPUD -> Color(0xFF00ACC1)
                    }

                    if (skinType == SkinType.SAMOSA_WARRIOR) {
                        // Triangular crispy samosa body!
                        val samosaPath = Path().apply {
                            moveTo(centerX, centerY - canvasH * 0.38f)
                            lineTo(centerX + canvasW * 0.42f, centerY + canvasH * 0.35f)
                            quadraticTo(centerX, centerY + canvasH * 0.40f, centerX - canvasW * 0.42f, centerY + canvasH * 0.35f)
                            close()
                        }
                        drawPath(samosaPath, color = shadowColor)
                        scale(scale = 0.94f, pivot = Offset(centerX, centerY)) {
                            drawPath(samosaPath, color = baseColor)
                        }
                    } else if (skinType == SkinType.FRENCH_FRY_FREDDY) {
                        // Red fry cup with crispy fries poking out
                        val fryColor = Color(0xFFFFCA28)
                        val fryShadow = Color(0xFFF57F17)
                        // Fries behind
                        for (i in -2..2) {
                            val fx = centerX + i * (canvasW * 0.12f)
                            val fy = centerY - canvasH * 0.32f + (kotlin.math.abs(i) * 6f)
                            drawRoundRect(
                                color = fryShadow,
                                topLeft = Offset(fx - 10f, fy),
                                size = Size(20f, canvasH * 0.5f),
                                cornerRadius = CornerRadius(6f, 6f)
                            )
                            drawRoundRect(
                                color = fryColor,
                                topLeft = Offset(fx - 8f, fy),
                                size = Size(16f, canvasH * 0.48f),
                                cornerRadius = CornerRadius(5f, 5f)
                            )
                        }
                        // Red container cup
                        val cupPath = Path().apply {
                            moveTo(centerX - canvasW * 0.36f, centerY - canvasH * 0.05f)
                            lineTo(centerX + canvasW * 0.36f, centerY - canvasH * 0.05f)
                            lineTo(centerX + canvasW * 0.28f, centerY + canvasH * 0.40f)
                            lineTo(centerX - canvasW * 0.28f, centerY + canvasH * 0.40f)
                            close()
                        }
                        drawPath(cupPath, color = Color(0xFFD32F2F))
                        // Gold emblem on red cup
                        drawCircle(color = Color(0xFFFFD54F), radius = canvasW * 0.10f, center = Offset(centerX, centerY + canvasH * 0.18f))
                    } else {
                        // Classic potato potato-shaped organic oval
                        val bodyW = canvasW * 0.72f
                        val bodyH = canvasH * 0.82f
                        val bodyLeft = centerX - bodyW / 2f
                        val bodyTop = centerY - bodyH / 2f + 5f

                        // Shadow layer
                        drawOval(
                            color = shadowColor,
                            topLeft = Offset(bodyLeft, bodyTop + 6f),
                            size = Size(bodyW, bodyH)
                        )
                        // Main body layer
                        drawOval(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFFFFF9C4), baseColor, shadowColor),
                                center = Offset(centerX - bodyW * 0.15f, centerY - bodyH * 0.15f),
                                radius = bodyW * 0.8f
                            ),
                            topLeft = Offset(bodyLeft, bodyTop),
                            size = Size(bodyW, bodyH)
                        )

                        // Potato dimples/freckles
                        val dimpleColor = shadowColor.copy(alpha = 0.45f)
                        drawCircle(color = dimpleColor, radius = 3.5f, center = Offset(centerX - bodyW * 0.28f, centerY - bodyH * 0.2f))
                        drawCircle(color = dimpleColor, radius = 4f, center = Offset(centerX + bodyW * 0.30f, centerY + bodyH * 0.1f))
                        drawCircle(color = dimpleColor, radius = 3f, center = Offset(centerX - bodyW * 0.25f, centerY + bodyH * 0.25f))
                    }

                    // 3. Accessory based on SkinType
                    when (skinType) {
                        SkinType.CLASSIC_CHOTU -> {
                            // Cute Chef Hat
                            val hatBaseY = centerY - canvasH * 0.32f
                            drawRoundRect(
                                color = Color(0xFFFFFFFF),
                                topLeft = Offset(centerX - canvasW * 0.18f, hatBaseY),
                                size = Size(canvasW * 0.36f, canvasH * 0.12f),
                                cornerRadius = CornerRadius(4f, 4f)
                            )
                            drawRect(
                                color = Color(0xFFE53935),
                                topLeft = Offset(centerX - canvasW * 0.18f, hatBaseY + canvasH * 0.08f),
                                size = Size(canvasW * 0.36f, canvasH * 0.035f)
                            )
                            // Puffs
                            drawCircle(color = Color(0xFFFFFFFF), radius = canvasW * 0.13f, center = Offset(centerX, hatBaseY - 4f))
                            drawCircle(color = Color(0xFFF5F5F5), radius = canvasW * 0.11f, center = Offset(centerX - canvasW * 0.14f, hatBaseY + 2f))
                            drawCircle(color = Color(0xFFF5F5F5), radius = canvasW * 0.11f, center = Offset(centerX + canvasW * 0.14f, hatBaseY + 2f))
                        }
                        SkinType.GARAM_SPICY -> {
                            // Flaming headband with flame tips
                            val bandY = centerY - canvasH * 0.18f
                            drawRoundRect(
                                color = Color(0xFFB71C1C),
                                topLeft = Offset(centerX - canvasW * 0.34f, bandY),
                                size = Size(canvasW * 0.68f, canvasH * 0.09f),
                                cornerRadius = CornerRadius(6f, 6f)
                            )
                            // Flame emblem
                            val flameH = canvasH * 0.15f * flamePulse
                            val flamePath = Path().apply {
                                moveTo(centerX, bandY - flameH)
                                quadraticTo(centerX + 16f, bandY - flameH * 0.4f, centerX + 10f, bandY)
                                lineTo(centerX - 10f, bandY)
                                quadraticTo(centerX - 16f, bandY - flameH * 0.4f, centerX, bandY - flameH)
                                close()
                            }
                            drawPath(flamePath, color = Color(0xFFFFD600))
                        }
                        SkinType.ALOO_TIKKI_KING -> {
                            // Mint chutney swirl and red pomegranate seeds
                            val swirlPath = Path().apply {
                                moveTo(centerX - canvasW * 0.25f, centerY - canvasH * 0.22f)
                                quadraticTo(centerX, centerY - canvasH * 0.30f, centerX + canvasW * 0.20f, centerY - canvasH * 0.18f)
                            }
                            drawPath(swirlPath, color = Color(0xFF2E7D32), style = Stroke(width = 8.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round))
                            drawCircle(color = Color(0xFFC2185B), radius = 5f, center = Offset(centerX - 15f, centerY - canvasH * 0.25f))
                            drawCircle(color = Color(0xFFC2185B), radius = 6f, center = Offset(centerX + 20f, centerY - canvasH * 0.22f))
                        }
                        SkinType.PARATHA_PRINCE -> {
                            // Golden butter crown
                            val crownY = centerY - canvasH * 0.36f
                            val crownPath = Path().apply {
                                moveTo(centerX - canvasW * 0.22f, crownY + canvasH * 0.10f)
                                lineTo(centerX - canvasW * 0.25f, crownY)
                                lineTo(centerX - canvasW * 0.10f, crownY + canvasH * 0.06f)
                                lineTo(centerX, crownY - canvasH * 0.04f)
                                lineTo(centerX + canvasW * 0.10f, crownY + canvasH * 0.06f)
                                lineTo(centerX + canvasW * 0.25f, crownY)
                                lineTo(centerX + canvasW * 0.22f, crownY + canvasH * 0.10f)
                                close()
                            }
                            drawPath(crownPath, color = Color(0xFFFFD700))
                            // Butter melting cube on top
                            drawRoundRect(
                                color = Color(0xFFFFF9C4),
                                topLeft = Offset(centerX - 12f, crownY + 8f),
                                size = Size(24f, 20f),
                                cornerRadius = CornerRadius(4f, 4f)
                            )
                        }
                        SkinType.SPACE_SPUD -> {
                            // Astronaut helmet visor
                            drawCircle(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xAA00E5FF), Color(0x770091EA)),
                                    start = Offset(centerX - 40f, centerY - 40f),
                                    end = Offset(centerX + 40f, centerY + 40f)
                                ),
                                radius = canvasW * 0.40f,
                                center = Offset(centerX, centerY - 5f)
                            )
                            drawCircle(
                                color = Color(0xFFECEFF1),
                                radius = canvasW * 0.40f,
                                center = Offset(centerX, centerY - 5f),
                                style = Stroke(width = 6.dp.toPx())
                            )
                            // Antenna on helmet
                            drawLine(
                                color = Color(0xFFB0BEC5),
                                start = Offset(centerX, centerY - canvasW * 0.40f),
                                end = Offset(centerX, centerY - canvasW * 0.52f),
                                strokeWidth = 5f
                            )
                            drawCircle(
                                color = Color(0xFFFF5252),
                                radius = 8f,
                                center = Offset(centerX, centerY - canvasW * 0.54f)
                            )
                        }
                        else -> {}
                    }

                    // 4. Eyes & Expressions
                    val eyeSpacing = canvasW * 0.16f
                    val eyeY = centerY - canvasH * 0.04f
                    val eyeRadius = canvasW * 0.065f

                    if (expression == AlooExpression.DEAD) {
                        // X eyes
                        val strokeW = 4.dp.toPx()
                        for (sign in listOf(-1, 1)) {
                            val ex = centerX + sign * eyeSpacing
                            drawLine(Color(0xFF212121), Offset(ex - 12f, eyeY - 12f), Offset(ex + 12f, eyeY + 12f), strokeW)
                            drawLine(Color(0xFF212121), Offset(ex + 12f, eyeY - 12f), Offset(ex - 12f, eyeY + 12f), strokeW)
                        }
                    } else if (expression == AlooExpression.PANIC) {
                        // Big wide frantic eyes + sweat drop
                        drawCircle(Color(0xFFFFFFFF), radius = eyeRadius * 1.3f, center = Offset(centerX - eyeSpacing, eyeY))
                        drawCircle(Color(0xFFFFFFFF), radius = eyeRadius * 1.3f, center = Offset(centerX + eyeSpacing, eyeY))
                        drawCircle(Color(0xFF212121), radius = eyeRadius * 0.7f, center = Offset(centerX - eyeSpacing, eyeY + 2f))
                        drawCircle(Color(0xFF212121), radius = eyeRadius * 0.7f, center = Offset(centerX + eyeSpacing, eyeY + 2f))

                        // Blue sweat drop dripping from head
                        val dropPath = Path().apply {
                            val dx = centerX + canvasW * 0.32f
                            val dy = centerY - canvasH * 0.20f
                            moveTo(dx, dy)
                            lineTo(dx + 10f, dy + 18f)
                            lineTo(dx - 10f, dy + 18f)
                            close()
                        }
                        drawPath(dropPath, color = Color(0xFF40C4FF))
                        drawCircle(Color(0xFF40C4FF), radius = 10f, center = Offset(centerX + canvasW * 0.32f, centerY - canvasH * 0.20f + 18f))
                    } else {
                        // Animated happy/fiery eyes
                        val pupilOffset = if (expression == AlooExpression.FIERY) -3f else 0f
                        for (sign in listOf(-1, 1)) {
                            val ex = centerX + sign * eyeSpacing
                            // Sclera
                            drawCircle(Color(0xFFFFFFFF), radius = eyeRadius, center = Offset(ex, eyeY))
                            // Pupil
                            drawCircle(Color(0xFF212121), radius = eyeRadius * 0.65f, center = Offset(ex, eyeY + pupilOffset))
                            // Specular catchlight
                            drawCircle(Color(0xFFFFFFFF), radius = eyeRadius * 0.26f, center = Offset(ex - eyeRadius * 0.22f, eyeY - eyeRadius * 0.22f))
                        }

                        // Blushing Cheeks
                        drawCircle(Color(0xFFFF7043).copy(alpha = 0.75f), radius = eyeRadius * 0.85f, center = Offset(centerX - eyeSpacing - 18f, eyeY + 16f))
                        drawCircle(Color(0xFFFF7043).copy(alpha = 0.75f), radius = eyeRadius * 0.85f, center = Offset(centerX + eyeSpacing + 18f, eyeY + 16f))
                    }

                    // 5. Mouth & Expression
                    val mouthY = centerY + canvasH * 0.12f
                    when (expression) {
                        AlooExpression.DEAD -> {
                            val wavyMouth = Path().apply {
                                moveTo(centerX - 16f, mouthY)
                                quadraticTo(centerX - 8f, mouthY - 6f, centerX, mouthY)
                                quadraticTo(centerX + 8f, mouthY + 6f, centerX + 16f, mouthY)
                            }
                            drawPath(wavyMouth, Color(0xFF212121), style = Stroke(width = 3.dp.toPx()))
                        }
                        AlooExpression.PANIC -> {
                            // O-shaped mouth screaming!
                            drawOval(
                                color = Color(0xFF212121),
                                topLeft = Offset(centerX - 14f, mouthY - 8f),
                                size = Size(28f, 32f)
                            )
                            drawOval(
                                color = Color(0xFFE53935),
                                topLeft = Offset(centerX - 9f, mouthY),
                                size = Size(18f, 18f)
                            )
                        }
                        AlooExpression.FIERY -> {
                            // Roaring fiery grin
                            val grinPath = Path().apply {
                                moveTo(centerX - 24f, mouthY - 4f)
                                quadraticTo(centerX, mouthY + 22f, centerX + 24f, mouthY - 4f)
                                close()
                            }
                            drawPath(grinPath, Color(0xFFC62828))
                            drawPath(grinPath, Color(0xFF212121), style = Stroke(width = 2.dp.toPx()))
                        }
                        else -> {
                            // Joyful open cartoon smile
                            val smilePath = Path().apply {
                                moveTo(centerX - 18f, mouthY)
                                quadraticTo(centerX, mouthY + 20f, centerX + 18f, mouthY)
                                close()
                            }
                            drawPath(smilePath, Color(0xFFD32F2F))
                            // Tongue
                            drawCircle(
                                color = Color(0xFFFF8A80),
                                radius = 9f,
                                center = Offset(centerX, mouthY + 14f)
                            )
                            drawPath(smilePath, Color(0xFF873600), style = Stroke(width = 2.dp.toPx()))
                        }
                    }
                }
            }
        }
    }
}
