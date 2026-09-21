package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.MahiVoiceState
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun VoiceStatePanel(
    currentState: MahiVoiceState,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        Triple(MahiVoiceState.LISTENING, "🎙️", "Listening"),
        Triple(MahiVoiceState.THINKING, "🧠", "Thinking"),
        Triple(MahiVoiceState.SPEAKING, "💬", "Speaking"),
        Triple(MahiVoiceState.ERROR, "⚠️", "Error")
    )

    val infiniteTransition = rememberInfiniteTransition(label = "voice_state_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "state_pulse"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(GlassSurface)
            .border(1.dp, GlassBorder, RoundedCornerShape(18.dp))
            .padding(vertical = 10.dp, horizontal = 12.dp)
            .testTag("voice_state_panel")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { (state, icon, label) ->
                val isActive = currentState == state

                val activeColor = when (state) {
                    MahiVoiceState.LISTENING -> NeonCyan
                    MahiVoiceState.THINKING -> NeonViolet
                    MahiVoiceState.SPEAKING -> NeonPink
                    MahiVoiceState.ERROR -> NeonAmber
                    else -> NeonCyan
                }

                val backgroundColor by animateColorAsState(
                    targetValue = if (isActive) activeColor.copy(alpha = 0.22f) else Color.Transparent,
                    label = "bg_color"
                )

                val borderColor by animateColorAsState(
                    targetValue = if (isActive) activeColor else Color.Transparent,
                    label = "border_color"
                )

                Box(
                    modifier = Modifier
                        .scale(if (isActive) pulseScale else 1.0f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(backgroundColor)
                        .border(if (isActive) 1.5.dp else 0.dp, borderColor, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("voice_state_pill_${state.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = icon, fontSize = 13.sp)
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                            color = if (isActive) TextPrimary else TextMuted
                        )
                    }
                }
            }
        }
    }
}
