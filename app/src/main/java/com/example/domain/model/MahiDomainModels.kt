package com.example.domain.model

enum class MahiVoiceState {
    IDLE,
    CONNECTING,
    LISTENING,
    THINKING,
    SPEAKING,
    ERROR
}

enum class MahiVibe(
    val title: String,
    val emoji: String,
    val description: String
) {
    CALM("Calm", "🧘", "Peaceful, gentle & soothing"),
    HAPPY("Happy", "😊", "Joyful, cheerful & playful"),
    HYPER("Hyper", "🔥", "High energy & super excited"),
    SAD("Sad", "🥺", "Tender, soft & deeply caring"),
    AUTO("Auto Vibe", "🔒", "Adapts dynamically to context")
}

enum class VoiceGender(val label: String, val icon: String) {
    FEMALE("Female (स्त्री)", "👩"),
    MALE("Male (पुरुष)", "👨")
}

data class VoiceProfile(
    val id: String,
    val name: String,
    val gender: VoiceGender = VoiceGender.FEMALE,
    val badge: String,
    val description: String,
    val defaultPitch: Float,
    val defaultRate: Float,
    val previewTextHi: String,
    val previewTextEn: String,
    val previewTextBho: String,
    val previewTextBn: String
) {
    fun getPreview(langCode: String): String = when (langCode) {
        "en" -> previewTextEn
        "bho" -> previewTextBho
        "bn" -> previewTextBn
        else -> previewTextHi
    }
}

data class VoiceSettings(
    val pitch: Float = 1.25f,        // Sweet, natural real girl pitch like Maya/ChatGPT
    val speechRate: Float = 1.05f,   // Smooth natural conversational speed
    val languageCode: String = "hi", // "hi" (Hindi), "en" (English), "bho" (Bhojpuri), "bn" (Bangla)
    val gender: String = "female",   // Female-first sweet voice
    val activeProfileId: String = "mahi_sweet",
    val autoListenOnStart: Boolean = false,
    val useCloudTts: Boolean = true, // Google Cloud TTS enabled for natural expressive female voice
    val cloudVoiceName: String = "hi-IN-Neural2-A", // Premium Neural2 female voice
    val cloudTtsApiKey: String = ""
)

data class UserProfile(
    val name: String = "Ajay",
    val gender: String = "Male", // "Male", "Female", "Prefer not to say"
    val phoneNumber: String = "+91 98765 43210",
    val musicApp: String = "YouTube", // "YT Music", "Spotify", "YouTube"
    val favoriteSong: String = "Kesariya",
    val geminiApiKey: String = "",
    val cloudTtsApiKey: String = "",
    val youtubeChannel: String = "",
    val youtubeApiKey: String = "",
    val mapsApiKey: String = "",
    val searchApiKey: String = "",
    val energyRemaining: Int = 2,
    val freeModeMinutesLeft: String = "10:00",
    val girlfriendMode: Boolean = true,
    val touchGuardEnabled: Boolean = false,
    val emergencySosContacts: String = "Mom, Dad"
)

data class TrainedTaskItem(
    val id: String,
    val name: String,
    val stepsCount: Int,
    val category: String,
    val isBuiltIn: Boolean = true
)

enum class NavScreen {
    HOME,
    SCAN,
    MEMORIES,
    CHAT,
    SETTINGS,
    PERSONAL_SETTINGS,
    ADVANCED_SETTINGS,
    OPTIONAL_SETTINGS,
    ABOUT,
    MARKETS,
    DOCUMENTS,
    WEBSITE_CODING,
    STUDY_WHITEBOARD
}
