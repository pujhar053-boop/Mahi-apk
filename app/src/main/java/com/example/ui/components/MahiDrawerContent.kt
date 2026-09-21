package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.HorizontalDivider
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
fun MahiDrawerContent(
    userProfile: UserProfile,
    currentScreen: NavScreen,
    onNavigate: (NavScreen) -> Unit,
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(Color(0xFF0D1322))
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Top Header Card matching Screenshot 1
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF131B2E))
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF38BDF8))
                            .border(1.5.dp, Color(0xFF00F0FF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("💖", fontSize = 20.sp)
                    }

                    Column {
                        Text(
                            text = "Mahi",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "by Ajay AI",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Notification Bell with Alert Dot
                    Box(contentAlignment = Alignment.TopEnd) {
                        IconButton(
                            onClick = {
                                onNavigate(NavScreen.HOME)
                                onCloseDrawer()
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = Color(0xFFCBD5E1),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .padding(top = 6.dp, end = 6.dp)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF97316))
                        )
                    }

                    // Profile avatar button
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2563EB))
                            .clickable {
                                onNavigate(NavScreen.PERSONAL_SETTINGS)
                                onCloseDrawer()
                            },
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
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION: HOME
        DrawerSectionHeader(title = "HOME")
        DrawerItem(
            icon = "🏠",
            label = "Home",
            isSelected = currentScreen == NavScreen.HOME,
            onClick = { onNavigate(NavScreen.HOME); onCloseDrawer() }
        )
        DrawerItem(
            icon = "🏠",
            label = "Mahi Home",
            isSelected = currentScreen == NavScreen.HOME,
            onClick = { onNavigate(NavScreen.HOME); onCloseDrawer() }
        )
        DrawerItem(
            icon = "⚙️",
            label = "Memories",
            badge = "Trained tasks",
            isSelected = currentScreen == NavScreen.MEMORIES,
            onClick = { onNavigate(NavScreen.MEMORIES); onCloseDrawer() }
        )
        DrawerItem(
            icon = "💬",
            label = "Chat",
            isSelected = currentScreen == NavScreen.CHAT,
            onClick = { onNavigate(NavScreen.CHAT); onCloseDrawer() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION: PRODUCTIVITY
        DrawerSectionHeader(title = "PRODUCTIVITY")
        DrawerItem(
            icon = "📈",
            label = "Markets",
            isSelected = currentScreen == NavScreen.MARKETS,
            onClick = { onNavigate(NavScreen.MARKETS); onCloseDrawer() }
        )
        DrawerItem(
            icon = "📄",
            label = "Documents",
            isSelected = currentScreen == NavScreen.DOCUMENTS,
            onClick = { onNavigate(NavScreen.DOCUMENTS); onCloseDrawer() }
        )
        DrawerItem(
            icon = "💻",
            label = "Website / Coding",
            isSelected = currentScreen == NavScreen.WEBSITE_CODING,
            onClick = { onNavigate(NavScreen.WEBSITE_CODING); onCloseDrawer() }
        )
        DrawerItem(
            icon = "✏️",
            label = "Study / Whiteboard",
            isSelected = currentScreen == NavScreen.STUDY_WHITEBOARD,
            onClick = { onNavigate(NavScreen.STUDY_WHITEBOARD); onCloseDrawer() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION: SYSTEM
        DrawerSectionHeader(title = "SYSTEM")
        DrawerItem(
            icon = "⚙️",
            label = "Settings",
            isSelected = currentScreen == NavScreen.SETTINGS,
            onClick = { onNavigate(NavScreen.SETTINGS); onCloseDrawer() }
        )
        DrawerItem(
            icon = "🛡️",
            label = "Mahi Rules",
            isSelected = false,
            onClick = { onNavigate(NavScreen.ADVANCED_SETTINGS); onCloseDrawer() }
        )
        DrawerItem(
            icon = "🔄",
            label = "PC ⇄ Phone",
            isSelected = false,
            onClick = { onNavigate(NavScreen.OPTIONAL_SETTINGS); onCloseDrawer() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION: OTHER
        DrawerSectionHeader(title = "OTHER")
        DrawerItem(
            icon = "🔒",
            label = "Privacy Policy",
            isSelected = false,
            onClick = { onNavigate(NavScreen.ABOUT); onCloseDrawer() }
        )
        DrawerItem(
            icon = "ℹ️",
            label = "About",
            isSelected = currentScreen == NavScreen.ABOUT,
            onClick = { onNavigate(NavScreen.ABOUT); onCloseDrawer() }
        )
        DrawerItem(
            icon = "⭐",
            label = "Upgrade",
            isSelected = false,
            onClick = { onNavigate(NavScreen.ABOUT); onCloseDrawer() }
        )
        DrawerItem(
            icon = "🔔",
            label = "Notifications",
            hasDot = true,
            isSelected = false,
            onClick = { onNavigate(NavScreen.HOME); onCloseDrawer() }
        )

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = Color(0xFF1E293B), thickness = 0.8.dp)
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "v4.15.1 • Ajay AI",
            color = Color(0xFF64748B),
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun DrawerSectionHeader(title: String) {
    Text(
        text = title,
        color = Color(0xFF64748B),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.1.sp,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
    )
}

@Composable
private fun DrawerItem(
    icon: String,
    label: String,
    badge: String? = null,
    hasDot: Boolean = false,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) Color(0xFF1E293B) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = icon, fontSize = 16.sp)
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) Color(0xFF38BDF8) else Color(0xFFE2E8F0)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (badge != null) {
                Text(
                    text = badge,
                    fontSize = 10.sp,
                    color = Color(0xFF64748B)
                )
            }
            if (hasDot) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF97316))
                )
            }
        }
    }
}
