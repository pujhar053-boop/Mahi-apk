package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.VoiceGender
import com.example.domain.model.VoiceProfile
import com.example.ui.theme.DeepSpaceSurface
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.VoiceSettingsViewModel

@Composable
fun VoiceSettingsDialog(
    viewModel: VoiceSettingsViewModel,
    onDismiss: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepSpaceSurface,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "🎙️", fontSize = 22.sp)
                Column {
                    Text(
                        text = "वॉयस व भाषा सेटिंग्स",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Male / Female आवाज़ व भाषा चुनें",
                        color = NeonCyan,
                        fontSize = 11.sp
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 490.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. GENDER TOGGLE (Female vs Male)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "आवाज़ का प्रकार (Gender: Female / Male)",
                        fontSize = 12.sp,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Female Button
                        val isFemaleSelected = uiState.selectedGender == VoiceGender.FEMALE
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isFemaleSelected) NeonPink.copy(alpha = 0.35f)
                                    else Color(0x18FFFFFF)
                                )
                                .border(
                                    width = if (isFemaleSelected) 1.8.dp else 0.6.dp,
                                    color = if (isFemaleSelected) NeonPink else GlassBorder,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { viewModel.setGender(VoiceGender.FEMALE) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "👩", fontSize = 20.sp)
                                Text(
                                    text = "लड़की / Female",
                                    fontSize = 12.sp,
                                    fontWeight = if (isFemaleSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isFemaleSelected) Color.White else TextMuted
                                )
                                Text(
                                    text = "Mahi Sweet Voice",
                                    fontSize = 9.sp,
                                    color = if (isFemaleSelected) NeonPink else TextMuted
                                )
                            }
                        }

                        // Male Button
                        val isMaleSelected = uiState.selectedGender == VoiceGender.MALE
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isMaleSelected) NeonCyan.copy(alpha = 0.35f)
                                    else Color(0x18FFFFFF)
                                )
                                .border(
                                    width = if (isMaleSelected) 1.8.dp else 0.6.dp,
                                    color = if (isMaleSelected) NeonCyan else GlassBorder,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { viewModel.setGender(VoiceGender.MALE) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "👨", fontSize = 20.sp)
                                Text(
                                    text = "लड़का / Male",
                                    fontSize = 12.sp,
                                    fontWeight = if (isMaleSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isMaleSelected) Color.White else TextMuted
                                )
                                Text(
                                    text = "Jarvis / Vikram",
                                    fontSize = 9.sp,
                                    color = if (isMaleSelected) NeonCyan else TextMuted
                                )
                            }
                        }
                    }
                }

                // 2. Language Selection Bar
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "बोलने की भाषा (Language)",
                            fontSize = 12.sp,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = when (uiState.selectedLanguage) {
                                "bho" -> "भोजपुरी (Bhojpuri)"
                                "bn" -> "বাংলা (Bangla)"
                                "en" -> "Indian English"
                                else -> "हिंदी (Hindi)"
                            },
                            fontSize = 10.sp,
                            color = NeonCyan
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val languages = listOf(
                            "hi" to "🇮🇳 हिंदी",
                            "bho" to "🌾 भोजपुरी",
                            "bn" to "🌸 বাংলা",
                            "en" to "🌐 English"
                        )
                        languages.forEach { (code, label) ->
                            val isSelected = uiState.selectedLanguage == code
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) NeonViolet.copy(alpha = 0.40f)
                                        else Color(0x15FFFFFF)
                                    )
                                    .border(
                                        width = if (isSelected) 1.5.dp else 0.5.dp,
                                        color = if (isSelected) NeonViolet else GlassBorder,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.setLanguage(code) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) TextPrimary else TextMuted
                                )
                            }
                        }
                    }
                }

                // 3. Voice Profiles List for Selected Gender
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (uiState.selectedGender == VoiceGender.FEMALE)
                            "चुने लड़की की आवाज़ (Female Profiles)"
                        else
                            "चुने लड़के की आवाज़ (Male Profiles)",
                        fontSize = 12.sp,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )

                    uiState.availableProfiles.forEach { profile ->
                        VoiceProfileCard(
                            profile = profile,
                            isSelected = uiState.selectedProfile.id == profile.id,
                            isPlaying = uiState.isPreviewPlaying && uiState.selectedProfile.id == profile.id,
                            onSelect = { viewModel.selectProfile(profile) },
                            onPreview = { viewModel.playPreview(profile) }
                        )
                    }
                }

                // 4. Acoustic Synthesis Sliders (Pitch & Speed)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x15FFFFFF))
                        .border(0.5.dp, GlassBorder, RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "आवाज़ की ट्यूनिंग (Pitch & Speed)",
                        fontSize = 12.sp,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Pitch
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (uiState.selectedGender == VoiceGender.FEMALE) "Pitch (अधिक = पतली/मीठी आवाज़)" else "Pitch (कम = भारी पुरुष स्वर)",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = String.format("%.2fx", uiState.pitch),
                                fontSize = 11.sp,
                                color = if (uiState.selectedGender == VoiceGender.FEMALE) NeonPink else NeonCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = uiState.pitch,
                            onValueChange = { viewModel.setPitch(it) },
                            valueRange = if (uiState.selectedGender == VoiceGender.FEMALE) 1.15f..1.45f else 0.70f..1.10f,
                            colors = SliderDefaults.colors(
                                thumbColor = if (uiState.selectedGender == VoiceGender.FEMALE) NeonPink else NeonCyan,
                                activeTrackColor = if (uiState.selectedGender == VoiceGender.FEMALE) NeonPink else NeonCyan,
                                inactiveTrackColor = GlassBorder
                            ),
                            modifier = Modifier.testTag("pitch_slider")
                        )
                    }

                    // Speed
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "बोलने की गति (Speech Rate)", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                text = String.format("%.2fx", uiState.speechRate),
                                fontSize = 11.sp,
                                color = NeonAmber,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = uiState.speechRate,
                            onValueChange = { viewModel.setSpeechRate(it) },
                            valueRange = 0.85f..1.25f,
                            colors = SliderDefaults.colors(
                                thumbColor = NeonAmber,
                                activeTrackColor = NeonAmber,
                                inactiveTrackColor = GlassBorder
                            ),
                            modifier = Modifier.testTag("rate_slider")
                        )
                    }
                }

                // 5. Google Cloud High-Quality Natural Voice Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("✨", fontSize = 20.sp)
                        Column {
                            Text(
                                text = "Google Cloud Natural TTS Engine",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Ultra-realistic human female neural voices (Neural2 & Wavenet) with expressive natural inflection instead of robotic speech.",
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8),
                                lineHeight = 14.sp
                            )
                        }
                    }
                }

                // 6. System TTS Button & Guidance Tip
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedButton(
                        onClick = { viewModel.openSystemTtsSettings() },
                        modifier = Modifier.fillMaxWidth(),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f))
                    ) {
                        Text("🔊 फोन की Google TTS इंजन सेटिंग्स खोलें", fontSize = 11.sp, color = NeonCyan)
                    }
                    Text(
                        text = "💡 सुझाव: फोन की Google TTS सेटिंग में जाकर Hindi के अंदर 'Voice II' या 'Voice IV' (Female) या 'Voice I' (Male) चुन सकते हैं।",
                        fontSize = 10.sp,
                        color = TextMuted,
                        lineHeight = 14.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
                modifier = Modifier.testTag("save_voice_settings_button")
            ) {
                Text("लागू करें (Save)", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Button(
                onClick = { viewModel.playPreview() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (uiState.isPreviewPlaying) NeonAmber else Color(0x30FFFFFF),
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = if (uiState.isPreviewPlaying) "⏹️ बोल रहा/रही हूँ..." else "▶️ टेस्ट सुनें",
                    fontSize = 12.sp
                )
            }
        }
    )
}

@Composable
private fun VoiceProfileCard(
    profile: VoiceProfile,
    isSelected: Boolean,
    isPlaying: Boolean,
    onSelect: () -> Unit,
    onPreview: () -> Unit
) {
    val highlightColor = if (profile.gender == VoiceGender.FEMALE) NeonPink else NeonCyan
    val borderColor = if (isSelected) highlightColor else GlassBorder
    val bgColor = if (isSelected) highlightColor.copy(alpha = 0.15f) else Color(0x0EFFFFFF)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(if (isSelected) 1.5.dp else 0.5.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable(onClick = onSelect)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(highlightColor.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = if (profile.gender == VoiceGender.FEMALE) "👩" else "👨", fontSize = 18.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = profile.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = profile.badge,
                        fontSize = 9.sp,
                        color = TextSecondary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0x20FFFFFF))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = profile.description,
                    fontSize = 10.sp,
                    color = TextMuted,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (isPlaying) NeonAmber.copy(alpha = 0.3f) else Color(0x1AFFFFFF))
                .border(0.5.dp, if (isPlaying) NeonAmber else GlassBorder, RoundedCornerShape(6.dp))
                .clickable(onClick = onPreview)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isPlaying) "⏹️ बंद" else "▶️ टेस्ट",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isPlaying) NeonAmber else TextPrimary
            )
        }
    }
}
