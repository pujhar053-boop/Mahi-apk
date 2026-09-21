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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.domain.model.MahiVibe
import com.example.domain.model.MahiVoiceState
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonViolet
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Clean, modern Jarvis-style futuristic holographic Arc Reactor / Orb core.
 * Compact, perfectly centered, glowing brightly, reacting live to voice amplitude.
 */
@Composable
fun MahiCoreView(
    voiceState: MahiVoiceState,
    vibe: MahiVibe,
    audioAmplitude: Float,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "jarvis_core_anim")

    // Smooth alive breathing pulse
    val breathingPulse by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "jarvis_breathing"
    )

    // Outer glow expand/contract
    val auraPulse by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "jarvis_aura"
    )

    // Primary ring rotation (Clockwise)
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (voiceState) {
                    MahiVoiceState.THINKING -> 1500
                    MahiVoiceState.SPEAKING -> 2800
                    MahiVoiceState.LISTENING -> 2000
                    else -> 6000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "jarvis_ring_rot"
    )

    // Secondary ring counter-rotation (Counter-clockwise)
    val counterRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (voiceState) {
                    MahiVoiceState.THINKING -> 2200
                    MahiVoiceState.SPEAKING -> 3600
                    MahiVoiceState.LISTENING -> 2600
                    else -> 8000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "jarvis_counter_rot"
    )

    // Dynamic colors based on voice state / vibe
    val (primaryGlow, secondaryGlow) = when (voiceState) {
        MahiVoiceState.LISTENING -> NeonCyan to Color(0xFF38BDF8)
        MahiVoiceState.THINKING -> NeonViolet to Color(0xFFC084FC)
        MahiVoiceState.SPEAKING -> NeonPink to Color(0xFFFF69B4)
        MahiVoiceState.ERROR -> NeonAmber to Color(0xFFFF4500)
        else -> when (vibe) {
            MahiVibe.CALM -> NeonCyan to Color(0xFF00E5FF)
            MahiVibe.HAPPY -> NeonPink to Color(0xFFFFB6C1)
            MahiVibe.HYPER -> Color(0xFFFF007F) to NeonAmber
            MahiVibe.SAD -> NeonViolet to NeonCyan
            MahiVibe.AUTO -> NeonCyan to NeonPink
        }
    }

    Box(
        modifier = if (onClick != null) {
            modifier
                .fillMaxWidth()
                .sizeIn(minHeight = 180.dp, maxHeight = 230.dp)
                .aspectRatio(1f)
                .padding(10.dp)
                .testTag("mahi_avatar_orb")
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
        } else {
            modifier
                .fillMaxWidth()
                .sizeIn(minHeight = 180.dp, maxHeight = 230.dp)
                .aspectRatio(1f)
                .padding(10.dp)
                .testTag("mahi_avatar_orb")
        },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = size.width * 0.28f
            val ampBoost = (audioAmplitude * 0.25f)
            val effectiveRadius = baseRadius * (breathingPulse + ampBoost)

            // 1. Ultra-Glow Radial Background Halo (Jarvis Core Radiance)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryGlow.copy(alpha = if (voiceState == MahiVoiceState.SPEAKING || voiceState == MahiVoiceState.LISTENING) 0.55f else 0.30f),
                        secondaryGlow.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = effectiveRadius * 1.85f * auraPulse
                ),
                radius = effectiveRadius * 1.85f * auraPulse,
                center = center
            )

            // 2. Outer Jarvis Segmented Arc Ring
            val outerSegments = 12
            val rotOffsetRad = (ringRotation * PI.toFloat() / 180f)
            val arcRadius = effectiveRadius * 1.28f
            for (i in 0 until outerSegments) {
                val startAngle = (i * (360f / outerSegments)) + ringRotation
                val sweep = 20f
                drawArc(
                    color = primaryGlow.copy(alpha = 0.85f),
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(center.x - arcRadius, center.y - arcRadius),
                    size = androidx.compose.ui.geometry.Size(arcRadius * 2f, arcRadius * 2f),
                    style = Stroke(width = 3f, cap = StrokeCap.Round)
                )
            }

            // 3. Middle High-Tech Sonic Ring (Reacts to Speech Volume)
            val waveSegments = 36
            val counterRotRad = (counterRotation * PI.toFloat() / 180f)
            for (i in 0 until waveSegments) {
                val a1 = (i * 2f * PI.toFloat() / waveSegments) + counterRotRad
                val a2 = ((i + 1) * 2f * PI.toFloat() / waveSegments) + counterRotRad
                val dynamicWave = sin(i * 3.0f + ringRotation * 0.1f) * (6f + audioAmplitude * 24f)
                val r1 = effectiveRadius * 1.08f + dynamicWave
                val p1 = Offset(center.x + cos(a1) * r1, center.y + sin(a1) * r1)
                val p2 = Offset(center.x + cos(a2) * r1, center.y + sin(a2) * r1)

                drawLine(
                    color = if (i % 2 == 0) secondaryGlow.copy(alpha = 0.9f) else primaryGlow.copy(alpha = 0.9f),
                    start = p1,
                    end = p2,
                    strokeWidth = 2.8f,
                    cap = StrokeCap.Round
                )
            }

            // 4. Glowing Precision Circle Border
            drawCircle(
                color = Color.White.copy(alpha = 0.9f),
                radius = effectiveRadius,
                center = center,
                style = Stroke(width = 2.5f)
            )

            // 5. Deep Holographic Orb Glass Core
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryGlow.copy(alpha = 0.45f),
                        Color(0xFF0F172A),
                        Color(0xFF030712)
                    ),
                    center = center,
                    radius = effectiveRadius
                ),
                radius = effectiveRadius,
                center = center
            )

            // 6. Inner Jarvis Reactor Core & Triangular Star Nexus
            val innerCoreRadius = effectiveRadius * 0.45f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        primaryGlow,
                        secondaryGlow.copy(alpha = 0.3f)
                    ),
                    center = center,
                    radius = innerCoreRadius
                ),
                radius = innerCoreRadius,
                center = center
            )

            // 7. Rotating Jarvis Crosshairs / Compass Ticks
            val ticks = 4
            for (t in 0 until ticks) {
                val angle = (t * (360f / ticks)) + ringRotation
                val rad = angle * (PI.toFloat() / 180f)
                val innerP = Offset(center.x + cos(rad) * (innerCoreRadius * 1.05f), center.y + sin(rad) * (innerCoreRadius * 1.05f))
                val outerP = Offset(center.x + cos(rad) * (effectiveRadius * 0.92f), center.y + sin(rad) * (effectiveRadius * 0.92f))
                drawLine(
                    color = Color.White.copy(alpha = 0.85f),
                    start = innerP,
                    end = outerP,
                    strokeWidth = 2.5f,
                    cap = StrokeCap.Round
                )
            }

            // 8. Glowing Pulsing Center Star Spark
            drawCircle(
                color = Color.White,
                radius = 5.5f + (audioAmplitude * 6f),
                center = center
            )
        }
    }
}
