package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.VoiceGender
import com.example.domain.model.VoiceProfile
import com.example.domain.model.VoiceSettings
import com.example.voice.SpeechManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VoiceSettingsUiState(
    val selectedGender: VoiceGender = VoiceGender.FEMALE,
    val availableProfiles: List<VoiceProfile> = emptyList(),
    val selectedProfile: VoiceProfile,
    val selectedLanguage: String = "hi", // "hi", "bho", "bn", "en"
    val pitch: Float = 1.28f,
    val speechRate: Float = 1.00f,
    val isPreviewPlaying: Boolean = false,
    val previewMessage: String = ""
)

class VoiceSettingsViewModel(
    private val speechManager: SpeechManager
) : ViewModel() {

    private val allProfiles: List<VoiceProfile> = listOf(
        // FEMALE PROFILES
        VoiceProfile(
            id = "mahi_sweet",
            name = "Mahi (Sweet Girl)",
            gender = VoiceGender.FEMALE,
            badge = "⭐ Default",
            description = "मीठी, सजीव और आत्मीय भारतीय लड़की की आवाज़",
            defaultPitch = 1.28f,
            defaultRate = 1.00f,
            previewTextHi = "नमस्ते Jan! मैं आपकी अपनी माही हूँ, हमेशा आपके साथ। 😊",
            previewTextEn = "Hello Jan! I am your sweet Mahi, always here by your side.",
            previewTextBho = "प्रणाम Jan! हम रउआ के अपनी माही हईं, का हुकुम बा? 🌸",
            previewTextBn = "নমস্কার Jan! আমি আপনার মিষ্টি মাহি, সবসময় আপনার পাশে।"
        ),
        VoiceProfile(
            id = "mahi_playful",
            name = "Mahi (Playful / चुलबुली)",
            gender = VoiceGender.FEMALE,
            badge = "🌸 Playful",
            description = "हंसमुख, चुलबुली और खुशमिजाज लड़की की आवाज़",
            defaultPitch = 1.34f,
            defaultRate = 1.04f,
            previewTextHi = "अरे Jan! मैं तो बहुत खुश हूँ आज, मुस्कुराते रहिए! 🌸",
            previewTextEn = "Hey Jan! I am super cheerful today, keep smiling! ✨",
            previewTextBho = "अरे Jan! तनी मुस्कुराईं, आज बहुत खुशी के दिन बा!",
            previewTextBn = "আরে Jan! একটু হাসুন, আজ খুব সুন্দর একটা দিন!"
        ),
        VoiceProfile(
            id = "mahi_caring",
            name = "Mahi (Soft & Caring)",
            gender = VoiceGender.FEMALE,
            badge = "💖 Caring",
            description = "शांत, कोमल और सुकून देने वाली आत्मीय आवाज़",
            defaultPitch = 1.24f,
            defaultRate = 0.94f,
            previewTextHi = "Jan, चिंता मत कीजिए। मैं हमेशा आपके साथ हूँ, सब अच्छा होगा। 🌸",
            previewTextEn = "Jan, don't worry. I am right here with you, everything is fine.",
            previewTextBho = "चिंता मत करीं Jan, रउआ के माही हमेशा संगे बाड़ी।",
            previewTextBn = "Jan, কোনো চিন্তা করবেন না। মাহি সবসময় আপনার সাথে আছে।"
        ),
        VoiceProfile(
            id = "mahi_smart",
            name = "Mahi (Smart & Crisp)",
            gender = VoiceGender.FEMALE,
            badge = "✨ Smart",
            description = "स्पष्ट, समझदार और आधुनिक लड़की की आवाज़",
            defaultPitch = 1.26f,
            defaultRate = 1.02f,
            previewTextHi = "नमस्ते Jan, मैं तैयार हूँ। बताइए आज क्या काम करना है? ✨",
            previewTextEn = "Hi Jan, all systems optimal. What are we tackling today?",
            previewTextBho = "गोर लागल Jan! सब काम तुरंत हो जाई, आदेश दीं।",
            previewTextBn = "হ্যালো Jan! সমস্ত কাজ করার জন্য আমি সম্পূর্ণ প্রস্তুত।"
        ),

        // MALE PROFILES
        VoiceProfile(
            id = "jarvis_smart",
            name = "Jarvis (Tech Butler)",
            gender = VoiceGender.MALE,
            badge = "⚡ Sharp",
            description = "शार्प, बुद्धिमान और वफादार पुरुष आवाज़",
            defaultPitch = 0.88f,
            defaultRate = 1.00f,
            previewTextHi = "नमस्ते Jan! जार्विस आपकी सेवा में हाज़िर है। बताइए क्या आदेश है?",
            previewTextEn = "Good day, Jan. Jarvis at your service. All systems operational.",
            previewTextBho = "प्रणाम Jan साहब! हम जार्विस हईं, बताईं का काम बा?",
            previewTextBn = "নমস্কার Jan! জার্ভিস আপনার সেবায় প্রস্তুত। বলুন কি করতে পারি?"
        ),
        VoiceProfile(
            id = "vikram_deep",
            name = "Vikram (Deep Baritone)",
            gender = VoiceGender.MALE,
            badge = "🦁 Deep",
            description = "गंभीर, आत्मविश्वासी और भारी पुरुष स्वर",
            defaultPitch = 0.78f,
            defaultRate = 0.95f,
            previewTextHi = "नमस्ते Jan! विक्रम यहाँ है। मैं हर कदम पर आपके साथ हूँ।",
            previewTextEn = "Greetings Jan, Vikram here. Standing ready for your commands.",
            previewTextBho = "राम राम Jan साहब! विक्रम हाजिर बा, बोलिए का आदेश बाटे।",
            previewTextBn = "নমস্কার Jan! বিক্রম এখানে, আপনার প্রতিটি পদক্ষেপে পাশে আছি।"
        ),
        VoiceProfile(
            id = "kabir_upbeat",
            name = "Kabir (Friendly & Upbeat)",
            gender = VoiceGender.MALE,
            badge = "🚀 Upbeat",
            description = "दोस्ताना, ऊर्जावान और उत्साही पुरुष आवाज़",
            defaultPitch = 0.96f,
            defaultRate = 1.05f,
            previewTextHi = "अरे Jan भाई! कैसे हैं आप? आज बहुत मज़ा आने वाला है!",
            previewTextEn = "Hey Jan! How are you doing today? Let's make things happen!",
            previewTextBho = "अरे Jan भाई, का हाल चाल बा? आज कुछ धमाल कईल जाव!",
            previewTextBn = "আরে Jan ভাই! কেমন আছো? আজ দারুণ কিছু করা যাক!"
        )
    )

    private val _uiState = MutableStateFlow(
        run {
            val initialSettings = speechManager.voiceSettings.value
            val initialGender = if (initialSettings.gender.equals("male", ignoreCase = true)) VoiceGender.MALE else VoiceGender.FEMALE
            val matchingProfiles = allProfiles.filter { it.gender == initialGender }
            val initialProfile = matchingProfiles.find { it.id == initialSettings.activeProfileId }
                ?: matchingProfiles.first()

            VoiceSettingsUiState(
                selectedGender = initialGender,
                availableProfiles = matchingProfiles,
                selectedProfile = initialProfile,
                selectedLanguage = initialSettings.languageCode,
                pitch = initialSettings.pitch,
                speechRate = initialSettings.speechRate
            )
        }
    )
    val uiState: StateFlow<VoiceSettingsUiState> = _uiState.asStateFlow()

    init {
        applyCurrentState()
    }

    fun setGender(gender: VoiceGender) {
        val matching = allProfiles.filter { it.gender == gender }
        val newProfile = matching.first()
        _uiState.update {
            it.copy(
                selectedGender = gender,
                availableProfiles = matching,
                selectedProfile = newProfile,
                pitch = newProfile.defaultPitch,
                speechRate = newProfile.defaultRate
            )
        }
        applyCurrentParametersToEngine(
            profileId = newProfile.id,
            pitch = newProfile.defaultPitch,
            rate = newProfile.defaultRate,
            lang = _uiState.value.selectedLanguage,
            gender = if (gender == VoiceGender.MALE) "male" else "female"
        )
    }

    fun selectProfile(profile: VoiceProfile) {
        _uiState.update {
            it.copy(
                selectedProfile = profile,
                pitch = profile.defaultPitch,
                speechRate = profile.defaultRate
            )
        }
        applyCurrentParametersToEngine(
            profileId = profile.id,
            pitch = profile.defaultPitch,
            rate = profile.defaultRate,
            lang = _uiState.value.selectedLanguage,
            gender = if (_uiState.value.selectedGender == VoiceGender.MALE) "male" else "female"
        )
    }

    fun setLanguage(langCode: String) {
        _uiState.update { it.copy(selectedLanguage = langCode) }
        val currentProfile = _uiState.value.selectedProfile
        applyCurrentParametersToEngine(
            profileId = currentProfile.id,
            pitch = _uiState.value.pitch,
            rate = _uiState.value.speechRate,
            lang = langCode,
            gender = if (_uiState.value.selectedGender == VoiceGender.MALE) "male" else "female"
        )
    }

    fun setPitch(newPitch: Float) {
        _uiState.update { it.copy(pitch = newPitch) }
        applyCurrentState()
    }

    fun setSpeechRate(newRate: Float) {
        _uiState.update { it.copy(speechRate = newRate) }
        applyCurrentState()
    }

    private fun applyCurrentState() {
        val state = _uiState.value
        applyCurrentParametersToEngine(
            profileId = state.selectedProfile.id,
            pitch = state.pitch,
            rate = state.speechRate,
            lang = state.selectedLanguage,
            gender = if (state.selectedGender == VoiceGender.MALE) "male" else "female"
        )
    }

    private fun applyCurrentParametersToEngine(
        profileId: String,
        pitch: Float,
        rate: Float,
        lang: String,
        gender: String
    ) {
        val updated = VoiceSettings(
            pitch = pitch,
            speechRate = rate,
            languageCode = lang,
            gender = gender,
            activeProfileId = profileId
        )
        speechManager.updateSettings(updated)
    }

    fun playPreview(profile: VoiceProfile = _uiState.value.selectedProfile) {
        val lang = _uiState.value.selectedLanguage
        val previewText = profile.getPreview(lang)
        val genderStr = if (_uiState.value.selectedGender == VoiceGender.MALE) "male" else "female"

        applyCurrentParametersToEngine(
            profileId = profile.id,
            pitch = if (profile.id == _uiState.value.selectedProfile.id) _uiState.value.pitch else profile.defaultPitch,
            rate = if (profile.id == _uiState.value.selectedProfile.id) _uiState.value.speechRate else profile.defaultRate,
            lang = lang,
            gender = genderStr
        )

        _uiState.update {
            it.copy(
                isPreviewPlaying = true,
                previewMessage = previewText
            )
        }

        speechManager.speak(previewText) {
            viewModelScope.launch {
                _uiState.update { it.copy(isPreviewPlaying = false) }
            }
        }
    }

    fun stopPreview() {
        speechManager.stopSpeaking()
        _uiState.update { it.copy(isPreviewPlaying = false) }
    }

    fun openSystemTtsSettings() {
        speechManager.openTtsSettings()
    }
}
