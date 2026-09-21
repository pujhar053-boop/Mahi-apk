package com.example.ui

import kotlinx.coroutines.delay

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.MahiAiEngine
import com.example.data.db.MahiDatabase
import com.example.data.model.ActionStatus
import com.example.data.model.LiveActionItem
import com.example.data.model.ScratchNote
import com.example.data.repository.MahiRepository
import com.example.domain.model.MahiVibe
import com.example.domain.model.MahiVoiceState
import com.example.domain.model.VoiceSettings
import com.example.tools.ToolRegistry
import com.example.voice.SpeechManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MahiViewModel(application: Application) : AndroidViewModel(application) {

    private val database = MahiDatabase.getInstance(application)
    private val repository = MahiRepository(database.noteDao(), database.actionDao())
    private val toolRegistry = ToolRegistry(repository)
    private val aiEngine = MahiAiEngine()

    val speechManager = SpeechManager(application, viewModelScope)
    val voiceSettingsViewModel: com.example.ui.viewmodel.VoiceSettingsViewModel by lazy {
        com.example.ui.viewmodel.VoiceSettingsViewModel(speechManager)
    }

    val voiceState: StateFlow<MahiVoiceState> = speechManager.voiceState
    val audioAmplitude: StateFlow<Float> = speechManager.audioAmplitude
    val lastRecognizedText: StateFlow<String> = speechManager.lastRecognizedText
    val voiceSettings: StateFlow<VoiceSettings> = speechManager.voiceSettings

    private val _currentVibe = MutableStateFlow(MahiVibe.HAPPY)
    val currentVibe: StateFlow<MahiVibe> = _currentVibe.asStateFlow()

    private val _currentScreen = MutableStateFlow(com.example.domain.model.NavScreen.HOME)
    val currentScreen: StateFlow<com.example.domain.model.NavScreen> = _currentScreen.asStateFlow()

    val userProfile = MutableStateFlow(com.example.domain.model.UserProfile(name = "Ajay", gender = "Male"))

    private val _statusTitle = MutableStateFlow("Companion Ready")
    val statusTitle: StateFlow<String> = _statusTitle.asStateFlow()

    private val _statusSubtitle = MutableStateFlow("अजय, मैं यहीं हूँ 😊")
    val statusSubtitle: StateFlow<String> = _statusSubtitle.asStateFlow()

    private val _lastSpokenResponse = MutableStateFlow("")
    val lastSpokenResponse: StateFlow<String> = _lastSpokenResponse.asStateFlow()

    private val _hasGreeted = MutableStateFlow(false)

    val notes: StateFlow<List<ScratchNote>> = repository.allNotes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val liveActions: StateFlow<List<LiveActionItem>> = repository.recentActions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        speechManager.onSpeechRecognized = { recognizedText ->
            processUserInput(recognizedText)
        }

        speechManager.onSpeechInterrupted = {
            _statusTitle.value = "Listening Intently"
            _statusSubtitle.value = "${userProfile.value.name}, मैं सुन रही हूँ..."
        }

        // Listen for voice state changes to sync status text
        viewModelScope.launch {
            voiceState.collect { state ->
                updateStatusForState(state)
            }
        }
    }

    fun navigateTo(screen: com.example.domain.model.NavScreen) {
        _currentScreen.value = screen
    }

    fun updateUserProfile(profile: com.example.domain.model.UserProfile) {
        userProfile.value = profile
        val updatedVoice = voiceSettings.value.copy(
            gender = profile.gender
        )
        speechManager.updateSettings(updatedVoice)
    }

    fun onAppStarted() {
        if (!_hasGreeted.value) {
            _hasGreeted.value = true
            val name = userProfile.value.name
            val welcomeText = "नमस्ते $name! आपकी माही हाज़िर है। बताइए आज क्या करना है? 😊"
            _statusTitle.value = "Mahi Ready"
            _statusSubtitle.value = welcomeText
            _lastSpokenResponse.value = welcomeText
            viewModelScope.launch {
                repository.recordAction("Welcome Greeting", ActionStatus.SUCCESS, "Greeted $name")
                speechManager.speak(welcomeText)
            }
        }
    }

    fun stopAllAudioAndMic() {
        speechManager.stopAll()
    }

    private fun updateStatusForState(state: MahiVoiceState) {
        val name = userProfile.value.name
        when (state) {
            MahiVoiceState.IDLE -> {
                _statusTitle.value = "Companion Ready"
                if (_lastSpokenResponse.value.isBlank()) {
                    _statusSubtitle.value = "$name, मैं यहीं हूँ 😊"
                }
            }
            MahiVoiceState.CONNECTING -> {
                _statusTitle.value = "Connecting Mic"
                _statusSubtitle.value = "$name, आवाज़ कनेक्ट हो रही है..."
            }
            MahiVoiceState.LISTENING -> {
                _statusTitle.value = "Listening Intently"
                _statusSubtitle.value = "$name, मैं सुन रही हूँ..."
            }
            MahiVoiceState.THINKING -> {
                _statusTitle.value = "Thinking..."
                _statusSubtitle.value = "तुरंत कर रही हूँ $name..."
            }
            MahiVoiceState.SPEAKING -> {
                _statusTitle.value = "Mahi Speaking"
                _statusSubtitle.value = "$name, मैं बता रही हूँ..."
            }
            MahiVoiceState.ERROR -> {
                _statusTitle.value = "Listening Stalled"
                _statusSubtitle.value = "अरे $name, एक बार फिर से बोलिए ना?"
            }
        }
    }

    fun onMicClicked() {
        when (voiceState.value) {
            MahiVoiceState.SPEAKING -> {
                // Support interruptions!
                speechManager.stopSpeaking()
                speechManager.startListening()
            }
            MahiVoiceState.LISTENING, MahiVoiceState.CONNECTING -> {
                speechManager.stopListening()
            }
            else -> {
                speechManager.startListening()
            }
        }
    }

    fun processUserInput(input: String) {
        if (input.isBlank()) return

        viewModelScope.launch {
            _statusTitle.value = "Thinking..."
            _statusSubtitle.value = "एक सेकंड Jan, सोच रही हूँ..."

            val context = getApplication<Application>()

            // 1. Check Tool Registry first for device actions or creator question
            val toolResult = toolRegistry.tryExecuteAction(context, input)

            val finalResponse = if (toolResult != null) {
                toolResult.spokenResponse
            } else {
                val currentSettings = voiceSettings.value
                val reply = aiEngine.getResponse(
                    input,
                    _currentVibe.value,
                    currentSettings.languageCode,
                    currentSettings.gender
                )
                repository.recordAction("Mahi AI Response", ActionStatus.SUCCESS, input.take(30))
                reply
            }

            _lastSpokenResponse.value = finalResponse
            _statusSubtitle.value = finalResponse
            
            // Speak with smiling human tone, and once done speaking, auto-listen for natural continuous conversation
            speechManager.speak(finalResponse) {
                // Auto listen callback for seamless continuous chat!
                viewModelScope.launch {
                    delay(350)
                    if (voiceState.value == MahiVoiceState.IDLE) {
                        speechManager.startListening()
                    }
                }
            }
        }
    }

    fun setVibe(vibe: MahiVibe) {
        _currentVibe.value = vibe
        // Adjust voice synthesis parameters based on vibe
        val currentSettings = voiceSettings.value
        val isMale = currentSettings.gender.equals("male", ignoreCase = true)
        val updatedSettings = when (vibe) {
            MahiVibe.CALM -> currentSettings.copy(pitch = if (isMale) 0.82f else 1.24f, speechRate = 0.94f)
            MahiVibe.HAPPY -> currentSettings.copy(pitch = if (isMale) 0.92f else 1.30f, speechRate = 1.02f)
            MahiVibe.HYPER -> currentSettings.copy(pitch = if (isMale) 0.98f else 1.36f, speechRate = 1.12f)
            MahiVibe.SAD -> currentSettings.copy(pitch = if (isMale) 0.78f else 1.22f, speechRate = 0.90f)
            MahiVibe.AUTO -> currentSettings.copy(pitch = if (isMale) 0.88f else 1.28f, speechRate = 1.00f)
        }
        speechManager.updateSettings(updatedSettings)

        viewModelScope.launch {
            repository.recordAction("Vibe Changed", ActionStatus.SUCCESS, "${vibe.emoji} ${vibe.title}")
            val vibeAck = when (vibe) {
                MahiVibe.CALM -> "Jan, ab main ekdam shant aur calm vibe me hoon 🧘"
                MahiVibe.HAPPY -> "Jan, super happy aur cheerful mode on! 😊"
                MahiVibe.HYPER -> "Jan, full energy aur hyper mode activated! 🔥"
                MahiVibe.SAD -> "Jan, main aapki har baat bade dhyan se sunungi 🥺"
                MahiVibe.AUTO -> "Jan, auto vibe mode set ho gaya hai 🔒"
            }
            _statusSubtitle.value = vibeAck
            speechManager.speak(vibeAck)
        }
    }

    fun saveNote(title: String, content: String, existingId: Long? = null) {
        viewModelScope.launch {
            repository.saveNote(title, content, existingId)
        }
    }

    fun deleteNote(id: Long) {
        viewModelScope.launch {
            repository.deleteNote(id)
        }
    }

    fun clearActions() {
        viewModelScope.launch {
            repository.clearActions()
        }
    }

    fun openTtsSettings() {
        speechManager.openTtsSettings()
    }

    fun updateVoiceSettings(pitch: Float, rate: Float, lang: String, gender: String = "female") {
        val updated = voiceSettings.value.copy(
            pitch = pitch,
            speechRate = rate,
            languageCode = lang,
            gender = gender
        )
        speechManager.updateSettings(updated)
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.destroy()
    }
}
