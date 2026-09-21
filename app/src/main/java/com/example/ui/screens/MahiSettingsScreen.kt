package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.NavScreen
import com.example.domain.model.UserProfile

@Composable
fun MahiSettingsScreen(
    userProfile: UserProfile,
    onNavigate: (NavScreen) -> Unit,
    onOpenVoiceSettings: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F19))
            .padding(horizontal = 18.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Top Bar matching Screenshot 3
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Settings",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(contentAlignment = Alignment.TopEnd) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Color(0xFFCBD5E1),
                        modifier = Modifier.size(22.dp)
                    )
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF97316))
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2563EB)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userProfile.name.take(1).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ACCOUNT
        SettingsSectionHeader("ACCOUNT")
        SettingsRowItem(
            title = "Personal",
            subtitle = "Your name, music, Gemini & YouTube keys",
            onClick = { onNavigate(NavScreen.PERSONAL_SETTINGS) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ASSISTANT
        SettingsSectionHeader("ASSISTANT")
        SettingsRowItem(
            title = "Mahi",
            subtitle = "Persona, girlfriend mode, voice, language",
            onClick = onOpenVoiceSettings
        )
        SettingsRowItem(
            title = "Skills",
            subtitle = "Installed playbooks and the online skill store",
            onClick = { onNavigate(NavScreen.MEMORIES) }
        )
        SettingsRowItem(
            title = "Sub-agents",
            subtitle = "Coding models and background agents",
            onClick = { onNavigate(NavScreen.ADVANCED_SETTINGS) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // WORK & MESSAGES
        SettingsSectionHeader("WORK & MESSAGES")
        SettingsRowItem(
            title = "Email",
            subtitle = "Let Mahi send mail from your address",
            onClick = { onNavigate(NavScreen.OPTIONAL_SETTINGS) }
        )
        SettingsRowItem(
            title = "WhatsApp groups & reports",
            subtitle = "Your groups, and the report formats she fills in",
            onClick = { onNavigate(NavScreen.ADVANCED_SETTINGS) }
        )
        SettingsRowItem(
            title = "Social media",
            subtitle = "Handle, caption voice, daily story, scheduled posts",
            onClick = { onNavigate(NavScreen.OPTIONAL_SETTINGS) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // CONNECTED ACCOUNTS
        SettingsSectionHeader("CONNECTED ACCOUNTS")
        SettingsRowItem(
            title = "Connectors",
            subtitle = "GitHub, Notion, Telegram and more",
            onClick = { onNavigate(NavScreen.OPTIONAL_SETTINGS) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // MEMORY & DATA
        SettingsSectionHeader("MEMORY & DATA")
        SettingsRowItem(
            title = "Backup",
            subtitle = "Export & restore your memories and chats",
            onClick = { onNavigate(NavScreen.MEMORIES) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // SYSTEM
        SettingsSectionHeader("SYSTEM")
        SettingsRowItem(
            title = "Advanced",
            subtitle = "Behaviour, safety, permissions",
            onClick = { onNavigate(NavScreen.ADVANCED_SETTINGS) }
        )
        SettingsRowItem(
            title = "Optional",
            subtitle = "Extra integrations — Maps / Places",
            onClick = { onNavigate(NavScreen.OPTIONAL_SETTINGS) }
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Mahi v4.15.1",
            color = Color(0xFF64748B),
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        color = Color(0xFF64748B),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.1.sp,
        modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp)
    )
}

@Composable
private fun SettingsRowItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF131B2E))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
            }
            Text(
                text = "›",
                fontSize = 20.sp,
                color = Color(0xFF64748B),
                fontWeight = FontWeight.Bold
            )
        }
    }
}
