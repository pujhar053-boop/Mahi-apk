package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.MahiVibe
import com.example.ui.theme.DeepSpaceCard
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.VibeCalmCyan
import com.example.ui.theme.VibeHappyViolet
import com.example.ui.theme.VibeHyperOrange
import com.example.ui.theme.VibeSadBlue

@Composable
fun VibeMatrixCard(
    currentVibe: MahiVibe,
    onSelectVibe: (MahiVibe) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("vibe_matrix_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DeepSpaceCard.copy(alpha = 0.85f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "❤️ MAHI VIBE MATRIX",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    color = NeonPink
                )

                Text(
                    text = currentVibe.description,
                    fontSize = 11.sp,
                    color = TextMuted,
                    maxLines = 1
                )
            }

            // Vibe selection pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MahiVibe.values().forEach { vibe ->
                    val isSelected = currentVibe == vibe

                    val vibeColor = when (vibe) {
                        MahiVibe.CALM -> VibeCalmCyan
                        MahiVibe.HAPPY -> VibeHappyViolet
                        MahiVibe.HYPER -> VibeHyperOrange
                        MahiVibe.SAD -> VibeSadBlue
                        MahiVibe.AUTO -> NeonCyan
                    }

                    val bgColor by animateColorAsState(
                        targetValue = if (isSelected) vibeColor.copy(alpha = 0.25f) else Color(0x202D1B54),
                        label = "vibe_bg"
                    )

                    val borderColor by animateColorAsState(
                        targetValue = if (isSelected) vibeColor else Color.Transparent,
                        label = "vibe_border"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(bgColor)
                            .border(1.2.dp, borderColor, RoundedCornerShape(12.dp))
                            .clickable { onSelectVibe(vibe) }
                            .padding(vertical = 10.dp, horizontal = 4.dp)
                            .testTag("vibe_button_${vibe.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(text = vibe.emoji, fontSize = 16.sp)
                            Text(
                                text = vibe.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) TextPrimary else TextMuted,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
