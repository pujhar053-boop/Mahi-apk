package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.MahiVoiceState
import com.example.domain.model.NavScreen
import com.example.ui.MahiViewModel
import com.example.ui.components.MahiDrawerContent
import com.example.ui.components.MahiLiveCompanionAvatar
import com.example.ui.components.UpgradeDialog
import com.example.ui.components.VoiceSettingsDialog
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun MahiHomeScreen(
    viewModel: MahiViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    val voiceState by viewModel.voiceState.collectAsStateWithLifecycle()
    val audioAmplitude by viewModel.audioAmplitude.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val statusSubtitle by viewModel.statusSubtitle.collectAsStateWithLifecycle()
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()

    var showUpgradeDialog by remember { mutableStateOf(false) }
    var showVoiceSettingsDialog by remember { mutableStateOf(false) }
    var textInput by remember { mutableStateOf("") }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.onMicClicked()
        }
    }

    fun handleMicAction() {
        val hasAudioPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasAudioPermission) {
            viewModel.onMicClicked()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Mic pulsing animation
    val infiniteTransition = rememberInfiniteTransition(label = "mic_halo")
    val micPulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_scale"
    )

    // Dynamic greeting based on time of day
    val greetingText = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour < 12 -> "Good morning,"
            hour < 17 -> "Good afternoon,"
            else -> "Good evening,"
        }
    }

    val todayDayNumber = remember { SimpleDateFormat("dd", Locale.getDefault()).format(Date()) }
    val todayDayName = remember { SimpleDateFormat("EEE, MMM", Locale.getDefault()).format(Date()) }

    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color(0xFF0D1322),
                drawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
            ) {
                MahiDrawerContent(
                    userProfile = userProfile,
                    currentScreen = currentScreen,
                    onNavigate = { screen ->
                        viewModel.navigateTo(screen)
                    },
                    onCloseDrawer = {
                        coroutineScope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        // Render sub-screens or the main home screen
        when (currentScreen) {
            NavScreen.SETTINGS -> {
                MahiSettingsScreen(
                    userProfile = userProfile,
                    onNavigate = { viewModel.navigateTo(it) },
                    onOpenVoiceSettings = { showVoiceSettingsDialog = true },
                    onBack = { viewModel.navigateTo(NavScreen.HOME) }
                )
            }
            NavScreen.PERSONAL_SETTINGS -> {
                PersonalSettingsScreen(
                    userProfile = userProfile,
                    onSaveProfile = { viewModel.updateUserProfile(it) },
                    onBack = { viewModel.navigateTo(NavScreen.SETTINGS) }
                )
            }
            NavScreen.ADVANCED_SETTINGS -> {
                AdvancedSettingsScreen(
                    userProfile = userProfile,
                    onBack = { viewModel.navigateTo(NavScreen.SETTINGS) }
                )
            }
            NavScreen.OPTIONAL_SETTINGS -> {
                OptionalSettingsScreen(
                    userProfile = userProfile,
                    onSaveProfile = { viewModel.updateUserProfile(it) },
                    onBack = { viewModel.navigateTo(NavScreen.SETTINGS) }
                )
            }
            NavScreen.MEMORIES -> {
                MahiMemoriesScreen(
                    userProfile = userProfile,
                    onBack = { viewModel.navigateTo(NavScreen.HOME) },
                    onRunTask = { taskName ->
                        viewModel.processUserInput(taskName)
                    }
                )
            }
            NavScreen.CHAT -> {
                MahiChatScreen(
                    userProfile = userProfile,
                    voiceState = voiceState,
                    lastSpokenResponse = viewModel.lastSpokenResponse.collectAsStateWithLifecycle().value,
                    onSendMessage = { viewModel.processUserInput(it) },
                    onMicClick = { handleMicAction() },
                    onBack = { viewModel.navigateTo(NavScreen.HOME) }
                )
            }
            NavScreen.SCAN -> {
                MahiScanScreen(
                    userProfile = userProfile,
                    onBack = { viewModel.navigateTo(NavScreen.HOME) },
                    onScanRequested = { viewModel.processUserInput("camera kholo") }
                )
            }
            NavScreen.ABOUT -> {
                MahiAboutScreen(
                    userProfile = userProfile,
                    onBack = { viewModel.navigateTo(NavScreen.HOME) }
                )
            }
            NavScreen.MARKETS -> {
                ProductivityScreen(
                    title = "Markets",
                    icon = "📈",
                    description = "Stock market indices, crypto and economic trends tracking.",
                    userProfile = userProfile,
                    onBack = { viewModel.navigateTo(NavScreen.HOME) }
                )
            }
            NavScreen.DOCUMENTS -> {
                ProductivityScreen(
                    title = "Documents",
                    icon = "📄",
                    description = "Instant PDF summarizer, document scanner and notes editor.",
                    userProfile = userProfile,
                    onBack = { viewModel.navigateTo(NavScreen.HOME) }
                )
            }
            NavScreen.WEBSITE_CODING -> {
                ProductivityScreen(
                    title = "Website / Coding",
                    icon = "💻",
                    description = "Coding assistant, scripts runner and web inspection.",
                    userProfile = userProfile,
                    onBack = { viewModel.navigateTo(NavScreen.HOME) }
                )
            }
            NavScreen.STUDY_WHITEBOARD -> {
                ProductivityScreen(
                    title = "Study / Whiteboard",
                    icon = "✏️",
                    description = "Study companion, whiteboard scratchpad, and flashcards.",
                    userProfile = userProfile,
                    onBack = { viewModel.navigateTo(NavScreen.HOME) }
                )
            }
            NavScreen.HOME -> {
                // THE MAIN HOME COMPANION SCREEN
                Scaffold(
                    modifier = modifier.fillMaxSize(),
                    containerColor = Color(0xFF0B0F19),
                    contentWindowInsets = WindowInsets(0, 0, 0, 0)
                ) { _ ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF0B0F19))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = statusBarPadding)
                                .imePadding()
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // 1. TOP BAR matching Screenshot 2
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 18.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        coroutineScope.launch { drawerState.open() }
                                    },
                                    modifier = Modifier.testTag("drawer_menu_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = "Menu",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Text(
                                    text = "Mahi",
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )

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
                                            .background(Color(0xFF2563EB))
                                            .border(1.5.dp, Color(0xFF38BDF8), CircleShape)
                                            .clickable { viewModel.navigateTo(NavScreen.PERSONAL_SETTINGS) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = userProfile.name.take(1).uppercase(),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }

                            // 2. BLUE FREE MODE BANNER matching Screenshot 2
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 18.dp, vertical = 4.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF132347))
                                    .border(1.dp, Color(0xFF2563EB), RoundedCornerShape(14.dp))
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(text = "🔒", fontSize = 16.sp)
                                        Column {
                                            Text(
                                                text = "Free mode • ${userProfile.freeModeMinutesLeft} min left today",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "Activate a license for tools, PC link and unlimited talk",
                                                fontSize = 11.sp,
                                                color = Color(0xFF94A3B8)
                                            )
                                        }
                                    }

                                    Text(
                                        text = "Activate",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF38BDF8),
                                        modifier = Modifier
                                            .clickable { showUpgradeDialog = true }
                                            .padding(4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // 3. GREETING HEADER: "Good morning," / "Ajay" + "⚡ 2 Energy"
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    Text(
                                        text = greetingText,
                                        fontSize = 13.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                    Text(
                                        text = userProfile.name,
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        letterSpacing = (-0.5).sp
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFF131B2E))
                                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "⚡ ${userProfile.energyRemaining} Energy",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                }
                            }

                            // 4. SUBTITLE STATUS TEXT
                            val isSpeaking = voiceState == MahiVoiceState.SPEAKING
                            val isListening = voiceState == MahiVoiceState.LISTENING

                            val statusDisplay = when {
                                isSpeaking -> "💬 $statusSubtitle"
                                isListening -> "🎤 Listening to you Ajay..."
                                else -> "🌙 Mahi is asleep — say \"Hey Mahi\" or tap mic"
                            }

                            Text(
                                text = statusDisplay,
                                fontSize = 13.sp,
                                color = if (isSpeaking) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 4.dp)
                            )

                            // 5. CENTER HERO: LIVE SMILING ANIME COMPANION AVATAR
                            MahiLiveCompanionAvatar(
                                voiceState = voiceState,
                                audioAmplitude = audioAmplitude,
                                onClick = { handleMicAction() },
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            // 6. QUICK ACTION CHIPS ROW: [🎵 Music] [📖 Study] [🖊️ Journal]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                QuickChip(
                                    icon = "🎵",
                                    label = "Music",
                                    onClick = {
                                        viewModel.processUserInput("play ${userProfile.favoriteSong} on ${userProfile.musicApp}")
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                QuickChip(
                                    icon = "📖",
                                    label = "Study",
                                    onClick = { viewModel.navigateTo(NavScreen.STUDY_WHITEBOARD) },
                                    modifier = Modifier.weight(1f)
                                )
                                QuickChip(
                                    icon = "🖊️",
                                    label = "Journal",
                                    onClick = { viewModel.processUserInput("read notes") },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // 7. 3 METRIC STATUS CARDS matching Screenshot 2
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Card 1: Weather
                                MetricCard(
                                    icon = "☁️",
                                    title = "Weather",
                                    mainValue = "28°C",
                                    subtitle = "Sunny",
                                    onClick = { viewModel.processUserInput("weather kaisa hai") },
                                    modifier = Modifier.weight(1f)
                                )
                                // Card 2: Today
                                MetricCard(
                                    icon = "📅",
                                    title = "Today",
                                    mainValue = todayDayNumber,
                                    subtitle = todayDayName,
                                    onClick = { viewModel.processUserInput("aaj ki date") },
                                    modifier = Modifier.weight(1f)
                                )
                                // Card 3: Mood
                                MetricCard(
                                    icon = "❤️",
                                    title = "Mood",
                                    mainValue = "Warm",
                                    subtitle = "All good",
                                    onClick = { viewModel.processUserInput("kya chal raha hai") },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // 8. BOTTOM INPUT BAR matching Screenshot 2
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "📎",
                                    fontSize = 18.sp,
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .clickable { viewModel.navigateTo(NavScreen.SCAN) }
                                        .padding(6.dp)
                                )

                                OutlinedTextField(
                                    value = textInput,
                                    onValueChange = { textInput = it },
                                    placeholder = { Text("Ask Mahi anything...", fontSize = 13.sp, color = Color(0xFF475569)) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                    keyboardActions = KeyboardActions(onSend = {
                                        if (textInput.isNotBlank()) {
                                            val txt = textInput
                                            textInput = ""
                                            keyboardController?.hide()
                                            viewModel.processUserInput(txt)
                                        }
                                    }),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color(0xFF131B2E),
                                        unfocusedContainerColor = Color(0xFF131B2E),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedIndicatorColor = Color(0xFF38BDF8),
                                        unfocusedIndicatorColor = Color(0xFF1E293B)
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("home_text_input")
                                )

                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF2563EB))
                                        .clickable {
                                            if (textInput.isNotBlank()) {
                                                val txt = textInput
                                                textInput = ""
                                                keyboardController?.hide()
                                                viewModel.processUserInput(txt)
                                            }
                                        }
                                        .testTag("home_send_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Send",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // 9. BOTTOM NAVIGATION BAR (5 items) matching Screenshot 2
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF0D1322))
                                    .padding(bottom = navBarPadding + 4.dp, top = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // 1. Home
                                    BottomNavItem(
                                        icon = "🏠",
                                        label = "Home",
                                        isSelected = true,
                                        onClick = { viewModel.navigateTo(NavScreen.HOME) }
                                    )

                                    // 2. Scan
                                    BottomNavItem(
                                        icon = "🔲",
                                        label = "Scan",
                                        isSelected = false,
                                        onClick = { viewModel.navigateTo(NavScreen.SCAN) }
                                    )

                                    // 3. ELEVATED BIG GLOWING BLUE MIC BUTTON IN CENTER
                                    Box(
                                        modifier = Modifier
                                            .scale(if (isListening || isSpeaking) micPulseScale else 1.0f)
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.radialGradient(
                                                    colors = if (isSpeaking) {
                                                        listOf(Color(0xFFEC4899), Color(0xFFBE185D))
                                                    } else {
                                                        listOf(Color(0xFF38BDF8), Color(0xFF2563EB))
                                                    }
                                                )
                                            )
                                            .border(
                                                2.dp,
                                                if (isSpeaking) Color(0xFFF472B6) else Color(0xFF00F0FF),
                                                CircleShape
                                            )
                                            .clickable { handleMicAction() }
                                            .testTag("elevated_main_mic"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Mic,
                                            contentDescription = "Speak to Mahi",
                                            tint = Color.White,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }

                                    // 4. Memories
                                    BottomNavItem(
                                        icon = "⚙️",
                                        label = "Memories",
                                        isSelected = false,
                                        onClick = { viewModel.navigateTo(NavScreen.MEMORIES) }
                                    )

                                    // 5. Chat
                                    BottomNavItem(
                                        icon = "💬",
                                        label = "Chat",
                                        isSelected = false,
                                        onClick = { viewModel.navigateTo(NavScreen.CHAT) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Voice Settings Dialog
    if (showVoiceSettingsDialog) {
        VoiceSettingsDialog(
            viewModel = viewModel.voiceSettingsViewModel,
            onDismiss = { showVoiceSettingsDialog = false }
        )
    }

    // Upgrade Dialog
    if (showUpgradeDialog) {
        UpgradeDialog(
            onDismiss = { showUpgradeDialog = false },
            onActivate = { code ->
                viewModel.userProfile.value = userProfile.copy(freeModeMinutesLeft = "Unlimited")
            }
        )
    }
}

@Composable
private fun QuickChip(
    icon: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF131B2E))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = icon, fontSize = 14.sp)
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun MetricCard(
    icon: String,
    title: String,
    mainValue: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF131B2E))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = icon, fontSize = 13.sp)
                Text(
                    text = title,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = mainValue,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = Color(0xFF64748B)
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = icon,
            fontSize = 18.sp
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (isSelected) Color(0xFF38BDF8) else Color(0xFF64748B),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
