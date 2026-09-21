package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.domain.model.MahiVoiceState
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Live smiling anime companion (Mahi) inspired by Maya AI.
 * Renders an animated anime girl character standing, smiling happily,
 * with breathing physics, mouth movement reacting live to speech/amplitude,
 * and magical glowing floating particle orbs drifting around her.
 */
@Composable
fun MahiLiveCompanionAvatar(
    voiceState: MahiVoiceState,
    audioAmplitude: Float,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isSpeaking = voiceState == MahiVoiceState.SPEAKING
    val isListening = voiceState == MahiVoiceState.LISTENING

    val infiniteTransition = rememberInfiniteTransition(label = "mahi_live_anim")

    // Smooth breathing float animation
    val breathingOffsetY by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing"
    )

    // Gentle head sway / life physics
    val headSwayAngle by infiniteTransition.animateFloat(
        initialValue = -2.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sway"
    )

    // Particle orbits
    val particlePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particles"
    )

    // Eye blink cycle (quick blink every 3.6s)
    val blinkPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "blink"
    )

    // Speaking mouth shape oscillation
    val speechMouthPulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 180, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mouth"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .sizeIn(minHeight = 220.dp, maxHeight = 300.dp)
            .aspectRatio(1.15f)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick?.invoke() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            val canvasW = size.width
            val canvasH = size.height
            val centerX = canvasW / 2f
            val centerY = canvasH / 2f + breathingOffsetY

            // 1. Draw glowing cosmic background aura behind Mahi
            val auraRadius = canvasW * 0.42f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x3500F0FF),
                        Color(0x20A855F7),
                        Color.Transparent
                    ),
                    center = Offset(centerX, centerY - 20f),
                    radius = auraRadius
                ),
                radius = auraRadius,
                center = Offset(centerX, centerY - 20f)
            )

            // 2. Draw Floating magical orbs/particles (inspired by Maya's green/cyan/pink dots)
            val particles = listOf(
                Triple(0.85f, 0.35f, Color(0xFF10B981)), // Emerald green
                Triple(0.65f, 0.75f, Color(0xFF00F0FF)), // Cyan
                Triple(0.90f, 1.25f, Color(0xFFEC4899)), // Pink
                Triple(0.70f, 1.85f, Color(0xFFF59E0B)), // Amber
                Triple(0.80f, 2.45f, Color(0xFFA855F7)), // Violet
                Triple(0.60f, 2.95f, Color(0xFF34D399))  // Mint
            )
            for (p in particles) {
                val dist = canvasW * 0.38f * p.first
                val ang = particlePhase + p.second * PI.toFloat()
                val px = centerX + cos(ang) * dist
                val py = centerY - 10f + sin(ang) * (dist * 0.75f)
                val pRadius = 4.5f + (sin(ang * 2) * 1.5f)

                // Particle outer soft glow
                drawCircle(
                    color = p.third.copy(alpha = 0.35f),
                    radius = pRadius * 2.8f,
                    center = Offset(px, py)
                )
                // Particle bright core
                drawCircle(
                    color = p.third,
                    radius = pRadius,
                    center = Offset(px, py)
                )
            }

            // 3. Draw Character Body (Oversized stylish hoodie & jacket)
            val bodyWidth = canvasW * 0.44f
            val bodyHeight = canvasH * 0.38f
            val bodyTop = centerY + 18f

            // Hoodie jacket base
            val jacketPath = Path().apply {
                moveTo(centerX - bodyWidth * 0.42f, bodyTop + 24f)
                cubicTo(
                    centerX - bodyWidth * 0.48f, bodyTop + bodyHeight * 0.75f,
                    centerX - bodyWidth * 0.38f, bodyTop + bodyHeight,
                    centerX - bodyWidth * 0.15f, bodyTop + bodyHeight
                )
                lineTo(centerX + bodyWidth * 0.15f, bodyTop + bodyHeight)
                cubicTo(
                    centerX + bodyWidth * 0.38f, bodyTop + bodyHeight,
                    centerX + bodyWidth * 0.48f, bodyTop + bodyHeight * 0.75f,
                    centerX + bodyWidth * 0.42f, bodyTop + 24f
                )
                close()
            }
            drawPath(
                path = jacketPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E1B4B), // Deep Indigo
                        Color(0xFF312E81),
                        Color(0xFF4338CA)
                    )
                )
            )

            // Jacket colorful zipper & trim lines (Neon Cyan & Pink accents)
            drawLine(
                color = Color(0xFF00F0FF),
                start = Offset(centerX, bodyTop + 22f),
                end = Offset(centerX, bodyTop + bodyHeight - 4f),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
            // Cute hoodie collar / drawstrings
            drawLine(
                color = Color(0xFFEC4899),
                start = Offset(centerX - 16f, bodyTop + 26f),
                end = Offset(centerX - 12f, bodyTop + 65f),
                strokeWidth = 2.5f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color(0xFFEC4899),
                start = Offset(centerX + 16f, bodyTop + 26f),
                end = Offset(centerX + 12f, bodyTop + 65f),
                strokeWidth = 2.5f,
                cap = StrokeCap.Round
            )

            // 4. Draw Hair Back (Warm orange anime hair flowing behind shoulders)
            val hairColor = Color(0xFFF97316) // Warm energetic orange hair
            val hairShade = Color(0xFFEA580C)

            drawCircle(
                color = hairShade,
                radius = canvasW * 0.22f,
                center = Offset(centerX, centerY - 25f)
            )

            // Side hair bunches / twintails flowing down
            val leftHairPath = Path().apply {
                moveTo(centerX - 45f, centerY - 20f)
                cubicTo(centerX - 80f, centerY + 10f, centerX - 75f, centerY + 65f, centerX - 55f, centerY + 80f)
                cubicTo(centerX - 60f, centerY + 50f, centerX - 50f, centerY + 20f, centerX - 35f, centerY - 10f)
                close()
            }
            drawPath(leftHairPath, color = hairColor)

            val rightHairPath = Path().apply {
                moveTo(centerX + 45f, centerY - 20f)
                cubicTo(centerX + 80f, centerY + 10f, centerX + 75f, centerY + 65f, centerX + 55f, centerY + 80f)
                cubicTo(centerX + 60f, centerY + 50f, centerX + 50f, centerY + 20f, centerX + 35f, centerY - 10f)
                close()
            }
            drawPath(rightHairPath, color = hairColor)

            // 5. Draw Face (Cute anime chin & soft skin tone)
            val faceRadius = canvasW * 0.16f
            val faceCenter = Offset(centerX, centerY - 18f)

            val facePath = Path().apply {
                moveTo(faceCenter.x - faceRadius * 0.95f, faceCenter.y - 10f)
                cubicTo(
                    faceCenter.x - faceRadius * 0.95f, faceCenter.y + faceRadius * 0.55f,
                    faceCenter.x - faceRadius * 0.45f, faceCenter.y + faceRadius * 0.98f,
                    faceCenter.x, faceCenter.y + faceRadius * 1.05f // Cute chin
                )
                cubicTo(
                    faceCenter.x + faceRadius * 0.45f, faceCenter.y + faceRadius * 0.98f,
                    faceCenter.x + faceRadius * 0.95f, faceCenter.y + faceRadius * 0.55f,
                    faceCenter.x + faceRadius * 0.95f, faceCenter.y - 10f
                )
                close()
            }
            drawPath(
                path = facePath,
                color = Color(0xFFFFF1E6) // Soft peachy anime skin
            )

            // Cute Pink Blush on cheeks
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFF7E95).copy(alpha = 0.55f), Color.Transparent),
                    center = Offset(faceCenter.x - 28f, faceCenter.y + 14f),
                    radius = 16f
                ),
                radius = 16f,
                center = Offset(faceCenter.x - 28f, faceCenter.y + 14f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFF7E95).copy(alpha = 0.55f), Color.Transparent),
                    center = Offset(faceCenter.x + 28f, faceCenter.y + 14f),
                    radius = 16f
                ),
                radius = 16f,
                center = Offset(faceCenter.x + 28f, faceCenter.y + 14f)
            )

            // 6. Draw Eyes (Sparkling, cheerful anime eyes / smiling crescents)
            val isBlinking = (blinkPhase > 0.94f)

            if (isBlinking) {
                // Closed smiling happy curved lines (^_^)
                val eyeLeft = Path().apply {
                    moveTo(faceCenter.x - 34f, faceCenter.y)
                    cubicTo(faceCenter.x - 26f, faceCenter.y - 6f, faceCenter.x - 18f, faceCenter.y - 6f, faceCenter.x - 10f, faceCenter.y)
                }
                drawPath(eyeLeft, color = Color(0xFF2E1065), style = Stroke(width = 3.5f, cap = StrokeCap.Round))

                val eyeRight = Path().apply {
                    moveTo(faceCenter.x + 10f, faceCenter.y)
                    cubicTo(faceCenter.x + 18f, faceCenter.y - 6f, faceCenter.x + 26f, faceCenter.y - 6f, faceCenter.x + 34f, faceCenter.y)
                }
                drawPath(eyeRight, color = Color(0xFF2E1065), style = Stroke(width = 3.5f, cap = StrokeCap.Round))
            } else {
                // Wide sparkling anime eyes with blue/cyan gradient irises
                val eyeW = 16f
                val eyeH = 20f

                // Left Eye
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF0284C7), Color(0xFF00F0FF))
                    ),
                    topLeft = Offset(faceCenter.x - 30f, faceCenter.y - 10f),
                    size = Size(eyeW, eyeH),
                    cornerRadius = CornerRadius(8f, 8f)
                )
                // Left pupil highlight (Sparkle)
                drawCircle(color = Color.White, radius = 3.2f, center = Offset(faceCenter.x - 25f, faceCenter.y - 5f))
                drawCircle(color = Color.White.copy(alpha = 0.8f), radius = 1.6f, center = Offset(faceCenter.x - 20f, faceCenter.y + 4f))

                // Right Eye
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF0284C7), Color(0xFF00F0FF))
                    ),
                    topLeft = Offset(faceCenter.x + 14f, faceCenter.y - 10f),
                    size = Size(eyeW, eyeH),
                    cornerRadius = CornerRadius(8f, 8f)
                )
                // Right pupil highlight (Sparkle)
                drawCircle(color = Color.White, radius = 3.2f, center = Offset(faceCenter.x + 19f, faceCenter.y - 5f))
                drawCircle(color = Color.White.copy(alpha = 0.8f), radius = 1.6f, center = Offset(faceCenter.x + 24f, faceCenter.y + 4f))

                // Cute upper eyelashes
                drawLine(
                    color = Color(0xFF1E1B4B),
                    start = Offset(faceCenter.x - 34f, faceCenter.y - 9f),
                    end = Offset(faceCenter.x - 11f, faceCenter.y - 9f),
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = Color(0xFF1E1B4B),
                    start = Offset(faceCenter.x + 11f, faceCenter.y - 9f),
                    end = Offset(faceCenter.x + 34f, faceCenter.y - 9f),
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )
            }

            // 7. Draw Nose (Cute tiny dot)
            drawCircle(
                color = Color(0xFFE07A5F),
                radius = 1.8f,
                center = Offset(faceCenter.x, faceCenter.y + 11f)
            )

            // 8. Draw Smiling Mouth (Reacts dynamically to speech!)
            val mouthCenterY = faceCenter.y + 24f
            val mouthOpenAmount = if (isSpeaking) {
                // Lip sync with speech rhythm and audio amplitude
                val ampBoost = (audioAmplitude.coerceIn(0f, 1f) * 12f)
                (speechMouthPulse * 7f + ampBoost).coerceIn(3f, 14f)
            } else if (isListening) {
                // Listening attentive slight open smile
                2.5f
            } else {
                // Peaceful happy closed smile
                0f
            }

            if (mouthOpenAmount > 1.5f) {
                // Open speaking mouth (animated singing/talking smile)
                val openMouthPath = Path().apply {
                    moveTo(centerX - 12f, mouthCenterY - 2f)
                    quadraticTo(centerX, mouthCenterY - 4f, centerX + 12f, mouthCenterY - 2f)
                    quadraticTo(centerX, mouthCenterY + mouthOpenAmount, centerX - 12f, mouthCenterY - 2f)
                    close()
                }
                // Mouth interior (rose red)
                drawPath(openMouthPath, color = Color(0xFFE11D48))
                // Cute little white teeth bar
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(centerX - 6f, mouthCenterY - 2f),
                    size = Size(12f, 3.5f),
                    cornerRadius = CornerRadius(2f, 2f)
                )
            } else {
                // Sweet cheerful closed curved smile
                val smilePath = Path().apply {
                    moveTo(centerX - 11f, mouthCenterY)
                    quadraticTo(centerX, mouthCenterY + 7f, centerX + 11f, mouthCenterY)
                }
                drawPath(
                    path = smilePath,
                    color = Color(0xFF9F1239),
                    style = Stroke(width = 2.8f, cap = StrokeCap.Round)
                )
            }

            // 9. Draw Front Hair Bangs (Cute anime fringe over forehead)
            val bang1 = Path().apply {
                moveTo(centerX - 42f, centerY - 32f)
                cubicTo(centerX - 35f, centerY - 12f, centerX - 28f, centerY - 8f, centerX - 20f, centerY - 2f)
                cubicTo(centerX - 24f, centerY - 18f, centerX - 28f, centerY - 28f, centerX - 30f, centerY - 35f)
                close()
            }
            drawPath(bang1, color = hairColor)

            val bang2 = Path().apply {
                moveTo(centerX - 22f, centerY - 35f)
                cubicTo(centerX - 10f, centerY - 8f, centerX - 2f, centerY - 4f, centerX + 6f, centerY + 2f)
                cubicTo(centerX + 2f, centerY - 16f, centerX - 5f, centerY - 26f, centerX - 8f, centerY - 35f)
                close()
            }
            drawPath(bang2, color = hairColor)

            val bang3 = Path().apply {
                moveTo(centerX + 8f, centerY - 35f)
                cubicTo(centerX + 18f, centerY - 8f, centerX + 26f, centerY - 4f, centerX + 34f, centerY)
                cubicTo(centerX + 28f, centerY - 16f, centerX + 22f, centerY - 26f, centerX + 18f, centerY - 35f)
                close()
            }
            drawPath(bang3, color = hairColor)

            // 10. Draw Witch Hat (Inspired by Maya companion's cute purple witch hat)
            val hatCenterY = centerY - 35f
            val hatBrimWidth = canvasW * 0.46f

            // Hat Brim (Curved ellipse)
            val hatBrimPath = Path().apply {
                moveTo(centerX - hatBrimWidth / 2f, hatCenterY)
                cubicTo(
                    centerX - hatBrimWidth * 0.25f, hatCenterY + 14f,
                    centerX + hatBrimWidth * 0.25f, hatCenterY + 14f,
                    centerX + hatBrimWidth / 2f, hatCenterY
                )
                cubicTo(
                    centerX + hatBrimWidth * 0.25f, hatCenterY - 14f,
                    centerX - hatBrimWidth * 0.25f, hatCenterY - 14f,
                    centerX - hatBrimWidth / 2f, hatCenterY
                )
                close()
            }
            drawPath(
                path = hatBrimPath,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF4338CA), Color(0xFF2E1065))
                )
            )

            // Hat Cone (Tilted cute magical cone)
            val hatConePath = Path().apply {
                moveTo(centerX - hatBrimWidth * 0.28f, hatCenterY - 2f)
                cubicTo(
                    centerX - hatBrimWidth * 0.15f, hatCenterY - 45f,
                    centerX - 10f, hatCenterY - 70f,
                    centerX + 20f, hatCenterY - 82f // Tilted tip
                )
                cubicTo(
                    centerX + 10f, hatCenterY - 55f,
                    centerX + hatBrimWidth * 0.22f, hatCenterY - 35f,
                    centerX + hatBrimWidth * 0.28f, hatCenterY - 2f
                )
                close()
            }
            drawPath(
                path = hatConePath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF6366F1), Color(0xFF312E81))
                )
            )

            // Hat Ribbon & Star / Bunny Pin (Glowing Gold / Pink)
            val hatRibbonPath = Path().apply {
                moveTo(centerX - hatBrimWidth * 0.27f, hatCenterY - 3f)
                lineTo(centerX + hatBrimWidth * 0.27f, hatCenterY - 3f)
                lineTo(centerX + hatBrimWidth * 0.25f, hatCenterY - 15f)
                lineTo(centerX - hatBrimWidth * 0.25f, hatCenterY - 15f)
                close()
            }
            drawPath(hatRibbonPath, color = Color(0xFFEC4899)) // Pink ribbon

            // Cute glowing yellow star on hat ribbon
            drawCircle(
                color = Color(0xFFFBBF24),
                radius = 5.5f,
                center = Offset(centerX - 18f, hatCenterY - 9f)
            )

            // 11. State Indicator Glow Ring at base
            val baseGlowColor = when {
                isSpeaking -> Color(0xFFEC4899)
                isListening -> Color(0xFF00F0FF)
                else -> Color(0xFF10B981)
            }
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, baseGlowColor.copy(alpha = 0.45f), Color.Transparent)
                ),
                topLeft = Offset(centerX - canvasW * 0.28f, centerY + bodyHeight + 12f),
                size = Size(canvasW * 0.56f, 6f),
                cornerRadius = CornerRadius(3f, 3f)
            )
        }
    }
}
