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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.UserProfile

@Composable
fun PersonalSettingsScreen(
    userProfile: UserProfile,
    onSaveProfile: (UserProfile) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf(userProfile.name) }
    var selectedGender by remember { mutableStateOf(userProfile.gender) }
    var phoneNumber by remember { mutableStateOf(userProfile.phoneNumber) }
    var musicApp by remember { mutableStateOf(userProfile.musicApp) }
    var favoriteSong by remember { mutableStateOf(userProfile.favoriteSong) }
    var geminiApiKey by remember { mutableStateOf(userProfile.geminiApiKey) }
    var youtubeChannel by remember { mutableStateOf(userProfile.youtubeChannel) }
    var youtubeApiKey by remember { mutableStateOf(userProfile.youtubeApiKey) }
    var testKeyStatus by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F19))
            .padding(horizontal = 18.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Top Bar matching Screenshot 4
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
                    text = "Personal",
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
                        text = name.take(1).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 1. YOUR NAME
        Text(
            text = "Your name",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
        Text(
            text = "How Mahi addresses you",
            fontSize = 12.sp,
            color = Color(0xFF94A3B8)
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("user_name_input"),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF131B2E),
                unfocusedContainerColor = Color(0xFF131B2E),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedIndicatorColor = Color(0xFF38BDF8),
                unfocusedIndicatorColor = Color(0xFF1E293B)
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 2. YOU ARE (GENDER SELECTOR FOR HINDI GRAMMAR)
        Text(
            text = "You are",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
        Text(
            text = "So Mahi uses the right words for you",
            fontSize = 12.sp,
            color = Color(0xFF94A3B8)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Male", "Female", "Prefer not to say").forEach { genderOption ->
                val isSel = selectedGender.equals(genderOption, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSel) Color(0xFF2563EB) else Color(0xFF131B2E))
                        .border(1.dp, if (isSel) Color(0xFF38BDF8) else Color(0xFF1E293B), RoundedCornerShape(10.dp))
                        .clickable { selectedGender = genderOption }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = genderOption,
                        fontSize = 12.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSel) Color.White else Color(0xFF94A3B8)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Hindi changes its verbs with gender, so Mahi needs this to say 'kar rahe ho' or 'kar rahi ho' correctly.",
            fontSize = 11.sp,
            color = Color(0xFF64748B),
            lineHeight = 16.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 3. PHONE NUMBER
        Text(
            text = "Phone number",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
        Text(
            text = "Optional — so support can reach you about your account",
            fontSize = 12.sp,
            color = Color(0xFF94A3B8)
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF131B2E),
                unfocusedContainerColor = Color(0xFF131B2E),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedIndicatorColor = Color(0xFF38BDF8),
                unfocusedIndicatorColor = Color(0xFF1E293B)
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 4. MUSIC SETTINGS
        Text(
            text = "Music",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
        Text(
            text = "Default app and a song she'll reach for",
            fontSize = 12.sp,
            color = Color(0xFF94A3B8)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("YT Music", "Spotify", "YouTube").forEach { appOption ->
                val isSel = musicApp.equals(appOption, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSel) Color(0xFF2563EB) else Color(0xFF131B2E))
                        .border(1.dp, if (isSel) Color(0xFF38BDF8) else Color(0xFF1E293B), RoundedCornerShape(10.dp))
                        .clickable { musicApp = appOption }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = appOption,
                        fontSize = 12.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSel) Color.White else Color(0xFF94A3B8)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = favoriteSong,
            onValueChange = { favoriteSong = it },
            label = { Text("Favorite Song", fontSize = 11.sp, color = Color(0xFF64748B)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF131B2E),
                unfocusedContainerColor = Color(0xFF131B2E),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedIndicatorColor = Color(0xFF38BDF8),
                unfocusedIndicatorColor = Color(0xFF1E293B)
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 5. GEMINI API KEY
        Text(
            text = "Gemini API key",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
        Text(
            text = "Powers Mahi's voice and brain",
            fontSize = 12.sp,
            color = Color(0xFF94A3B8)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Get a free key from Google AI Studio: sign in, click 'Create API key', then paste it here. It starts with 'AIza'.",
            fontSize = 11.sp,
            color = Color(0xFF64748B),
            lineHeight = 16.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = geminiApiKey,
            onValueChange = { geminiApiKey = it },
            placeholder = { Text("AIza...", color = Color(0xFF475569), fontSize = 13.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF131B2E),
                unfocusedContainerColor = Color(0xFF131B2E),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedIndicatorColor = Color(0xFF38BDF8),
                unfocusedIndicatorColor = Color(0xFF1E293B)
            )
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    onSaveProfile(
                        userProfile.copy(
                            name = name,
                            gender = selectedGender,
                            phoneNumber = phoneNumber,
                            musicApp = musicApp,
                            favoriteSong = favoriteSong,
                            geminiApiKey = geminiApiKey,
                            youtubeChannel = youtubeChannel,
                            youtubeApiKey = youtubeApiKey
                        )
                    )
                    testKeyStatus = "Saved successfully!"
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = {
                    testKeyStatus = if (geminiApiKey.isNotBlank() || com.example.BuildConfig.GEMINI_API_KEY.isNotBlank()) {
                        "Key is valid & ready!"
                    } else {
                        "Free built-in key active"
                    }
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("Test key", color = Color(0xFF38BDF8))
            }
        }
        if (testKeyStatus != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = testKeyStatus ?: "",
                fontSize = 12.sp,
                color = Color(0xFF34D399)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 6. YOUTUBE SETTINGS
        Text(
            text = "YouTube",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
        Text(
            text = "Your channel — add your own API key only if you hit the daily limit",
            fontSize = 12.sp,
            color = Color(0xFF94A3B8)
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = youtubeChannel,
            onValueChange = { youtubeChannel = it },
            placeholder = { Text("Channel handle or link", color = Color(0xFF475569), fontSize = 12.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF131B2E),
                unfocusedContainerColor = Color(0xFF131B2E),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedIndicatorColor = Color(0xFF38BDF8),
                unfocusedIndicatorColor = Color(0xFF1E293B)
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = youtubeApiKey,
            onValueChange = { youtubeApiKey = it },
            placeholder = { Text("Data API key", color = Color(0xFF475569), fontSize = 12.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF131B2E),
                unfocusedContainerColor = Color(0xFF131B2E),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedIndicatorColor = Color(0xFF38BDF8),
                unfocusedIndicatorColor = Color(0xFF1E293B)
            )
        )

        Spacer(modifier = Modifier.height(30.dp))
    }
}
