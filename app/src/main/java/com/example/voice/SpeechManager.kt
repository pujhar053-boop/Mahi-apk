package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.domain.model.MahiVoiceState
import com.example.domain.model.VoiceSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID
import kotlin.math.max
import kotlin.math.min

class SpeechManager(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) : TextToSpeech.OnInitListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized = false

    private val _voiceState = MutableStateFlow(MahiVoiceState.IDLE)
    val voiceState: StateFlow<MahiVoiceState> = _voiceState.asStateFlow()

    private val _audioAmplitude = MutableStateFlow(0f)
    val audioAmplitude: StateFlow<Float> = _audioAmplitude.asStateFlow()

    private val _lastRecognizedText = MutableStateFlow("")
    val lastRecognizedText: StateFlow<String> = _lastRecognizedText.asStateFlow()

    private val _voiceSettings = MutableStateFlow(VoiceSettings())
    val voiceSettings: StateFlow<VoiceSettings> = _voiceSettings.asStateFlow()

    var onSpeechRecognized: ((String) -> Unit)? = null
    var onSpeechInterrupted: (() -> Unit)? = null

    private var speakingAmplitudeJob: Job? = null
    private val googleCloudTtsEngine = com.example.voice.cloud.GoogleCloudTtsEngine(context, coroutineScope)

    init {
        try {
            val pm = context.packageManager
            val isGoogleTtsInstalled = try {
                pm.getPackageInfo("com.google.android.tts", 0) != null
            } catch (_: Exception) {
                false
            }
            textToSpeech = if (isGoogleTtsInstalled) {
                TextToSpeech(context.applicationContext, this, "com.google.android.tts")
            } else {
                TextToSpeech(context.applicationContext, this)
            }
        } catch (_: Exception) {
            textToSpeech = TextToSpeech(context.applicationContext, this)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsInitialized = true
            applyVoiceSettings(_voiceSettings.value)
        }
    }

    fun updateSettings(settings: VoiceSettings) {
        _voiceSettings.value = settings
        if (isTtsInitialized) {
            applyVoiceSettings(settings)
        }
    }

    private fun applyVoiceSettings(settings: VoiceSettings) {
        textToSpeech?.let { tts ->
            val targetLocale = when (settings.languageCode) {
                "en" -> Locale.forLanguageTag("en-IN")
                "bn" -> Locale.forLanguageTag("bn-IN")
                "bho" -> Locale.forLanguageTag("hi-IN") // Bhojpuri uses Hindi TTS engine with Bhojpuri acoustic modulation
                else -> Locale.forLanguageTag("hi-IN")
            }

            val result = tts.setLanguage(targetLocale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts.language = Locale.getDefault()
            }

            val isFemale = !settings.gender.equals("male", ignoreCase = true)

            // Dynamic pitch and speech rate calculation
            val basePitch = if (isFemale) {
                settings.pitch.coerceIn(1.15f, 1.45f)
            } else {
                settings.pitch.coerceIn(0.70f, 1.05f)
            }
            val baseRate = settings.speechRate.coerceIn(0.85f, 1.30f)

            val (dynamicPitch, dynamicRate) = when (settings.languageCode) {
                "bho" -> (basePitch + if (isFemale) 0.02f else -0.02f).coerceIn(0.7f, 1.5f) to (baseRate * 0.96f).coerceIn(0.7f, 1.4f)
                "bn" -> (basePitch + if (isFemale) 0.02f else 0.0f).coerceIn(0.7f, 1.5f) to (baseRate * 0.98f).coerceIn(0.7f, 1.4f)
                "en" -> (basePitch + if (isFemale) 0.02f else 0.0f).coerceIn(0.7f, 1.5f) to (baseRate * 1.02f).coerceIn(0.7f, 1.4f)
                else -> basePitch to baseRate
            }

            tts.setPitch(dynamicPitch)
            tts.setSpeechRate(dynamicRate)

            // Select optimal high-quality voice for target gender and language
            try {
                val voices = tts.voices
                if (!voices.isNullOrEmpty()) {
                    val candidate = voices.sortedWith(compareByDescending<android.speech.tts.Voice> { v ->
                        var score = 0
                        val name = v.name.lowercase()
                        val lang = v.locale.language

                        // Language match
                        if (lang == targetLocale.language) score += 80

                        if (isFemale) {
                            // Boost sweet female Google neural markers (Mahi sweet girl)
                            if (name.contains("hi-in-x-hie") || name.contains("hi-in-x-hid") || name.contains("hi-in-x-hif")) score += 200
                            if (name.contains("en-in-x-end") || name.contains("en-in-x-enc")) score += 180
                            if (name.contains("bn-in-x-bnf") || name.contains("bn-in-x-")) score += 180
                            if (name.contains("female") || name.contains("fem") || name.contains("woman") || name.contains("-f-") || name.contains("_f_")) score += 100
                            if (name.contains("male") || name.contains("-m-") || name.contains("man") || name.contains("-hia") || name.contains("-hic")) score -= 150
                        } else {
                            // Boost male neural voices (Jarvis male)
                            if (name.contains("hi-in-x-hic") || name.contains("hi-in-x-hia") || name.contains("hi-in-x-hib")) score += 200
                            if (name.contains("en-in-x-ena") || name.contains("en-in-x-enb")) score += 180
                            if (name.contains("male") || name.contains("man") || name.contains("-m-") || name.contains("_m_")) score += 100
                            if (name.contains("female") || name.contains("fem") || name.contains("woman") || name.contains("-f-")) score -= 150
                        }

                        // Prefer High Quality Neural / Network voices
                        if (v.quality >= android.speech.tts.Voice.QUALITY_HIGH) score += 30
                        if (v.latency <= android.speech.tts.Voice.LATENCY_NORMAL) score += 10
                        score
                    }).firstOrNull()

                    if (candidate != null) {
                        tts.voice = candidate
                    }
                }
            } catch (_: Exception) {
                // Fallback safely to elevated pitch
            }
        }
    }

    fun startListening() {
        // Interruption: if Mahi was speaking, stop immediately!
        stopSpeaking()

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _voiceState.value = MahiVoiceState.ERROR
            return
        }

        coroutineScope.launch(Dispatchers.Main) {
            try {
                speechRecognizer?.destroy()
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(createRecognitionListener())
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    val recLang = when (_voiceSettings.value.languageCode) {
                        "en" -> "en-IN"
                        "bn" -> "bn-IN"
                        "bho" -> "hi-IN"
                        else -> "hi-IN"
                    }
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, recLang)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, recLang)
                    putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                }

                _voiceState.value = MahiVoiceState.CONNECTING
                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                _voiceState.value = MahiVoiceState.ERROR
            }
        }
    }

    fun stopListening() {
        coroutineScope.launch(Dispatchers.Main) {
            try {
                speechRecognizer?.stopListening()
            } catch (_: Exception) {}
            if (_voiceState.value == MahiVoiceState.LISTENING || _voiceState.value == MahiVoiceState.CONNECTING) {
                _voiceState.value = MahiVoiceState.IDLE
                _audioAmplitude.value = 0f
            }
        }
    }

    fun stopAll() {
        stopSpeaking()
        stopListening()
    }

    fun speak(text: String, onDoneCallback: (() -> Unit)? = null) {
        stopSpeaking()

        if (_voiceSettings.value.useCloudTts) {
            coroutineScope.launch(Dispatchers.Main) {
                _voiceState.value = MahiVoiceState.THINKING
                googleCloudTtsEngine.synthesizeAndPlay(
                    text = text,
                    settings = _voiceSettings.value,
                    onAmplitudeUpdate = { amp ->
                        _audioAmplitude.value = amp
                    },
                    onStarted = {
                        _voiceState.value = MahiVoiceState.SPEAKING
                    },
                    onCompleted = {
                        _voiceState.value = MahiVoiceState.IDLE
                        _audioAmplitude.value = 0f
                        onDoneCallback?.invoke()
                    },
                    onError = {
                        // Seamless fallback to device's enhanced female TTS engine
                        speakWithLocalTts(text, onDoneCallback)
                    }
                )
            }
        } else {
            speakWithLocalTts(text, onDoneCallback)
        }
    }

    private fun speakWithLocalTts(text: String, onDoneCallback: (() -> Unit)? = null) {
        if (!isTtsInitialized || textToSpeech == null) {
            _voiceState.value = MahiVoiceState.IDLE
            onDoneCallback?.invoke()
            return
        }

        val utteranceId = UUID.randomUUID().toString()

        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _voiceState.value = MahiVoiceState.SPEAKING
                startSpeakingAmplitudeSimulation()
            }

            override fun onDone(utteranceId: String?) {
                stopSpeakingAmplitudeSimulation()
                _voiceState.value = MahiVoiceState.IDLE
                _audioAmplitude.value = 0f
                coroutineScope.launch(Dispatchers.Main) {
                    onDoneCallback?.invoke()
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                stopSpeakingAmplitudeSimulation()
                _voiceState.value = MahiVoiceState.IDLE
                _audioAmplitude.value = 0f
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                stopSpeakingAmplitudeSimulation()
                _voiceState.value = MahiVoiceState.IDLE
                _audioAmplitude.value = 0f
            }
        })

        applyVoiceSettings(_voiceSettings.value)
        val params = Bundle()
        params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    fun stopSpeaking() {
        if (_voiceState.value == MahiVoiceState.SPEAKING) {
            onSpeechInterrupted?.invoke()
        }
        googleCloudTtsEngine.stop()
        stopSpeakingAmplitudeSimulation()
        textToSpeech?.stop()
        _audioAmplitude.value = 0f
    }

    private fun startSpeakingAmplitudeSimulation() {
        speakingAmplitudeJob?.cancel()
        speakingAmplitudeJob = coroutineScope.launch(Dispatchers.Default) {
            var step = 0
            while (isActive && _voiceState.value == MahiVoiceState.SPEAKING) {
                // Natural speaking cadence fluctuation
                val base = kotlin.math.sin(step * 0.4).toFloat() * 0.35f + 0.5f
                val jitter = (kotlin.random.Random.nextFloat() * 0.3f)
                _audioAmplitude.value = (base + jitter).coerceIn(0.15f, 0.95f)
                step++
                delay(60)
            }
            _audioAmplitude.value = 0f
        }
    }

    private fun stopSpeakingAmplitudeSimulation() {
        speakingAmplitudeJob?.cancel()
        speakingAmplitudeJob = null
    }

    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _voiceState.value = MahiVoiceState.LISTENING
            }

            override fun onBeginningOfSpeech() {
                _voiceState.value = MahiVoiceState.LISTENING
            }

            override fun onRmsChanged(rmsdB: Float) {
                if (_voiceState.value == MahiVoiceState.LISTENING) {
                    // Map rmsdB (typically -2 to 10) to 0f - 1f
                    val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                    _audioAmplitude.value = max(_audioAmplitude.value * 0.4f, normalized)
                }
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                _voiceState.value = MahiVoiceState.THINKING
                _audioAmplitude.value = 0f
            }

            override fun onError(error: Int) {
                _audioAmplitude.value = 0f
                when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH,
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                        _voiceState.value = MahiVoiceState.IDLE
                    }
                    else -> {
                        _voiceState.value = MahiVoiceState.IDLE
                    }
                }
            }

            override fun onResults(results: Bundle?) {
                _audioAmplitude.value = 0f
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val recognized = matches?.firstOrNull()?.trim().orEmpty()
                if (recognized.isNotBlank()) {
                    _lastRecognizedText.value = recognized
                    _voiceState.value = MahiVoiceState.THINKING
                    onSpeechRecognized?.invoke(recognized)
                } else {
                    _voiceState.value = MahiVoiceState.IDLE
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                matches?.firstOrNull()?.let {
                    _lastRecognizedText.value = it
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    fun openTtsSettings() {
        try {
            val intent = Intent("com.android.settings.TTS_SETTINGS").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                val altIntent = Intent(android.provider.Settings.ACTION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(altIntent)
            } catch (_: Exception) {}
        }
    }

    fun destroy() {
        googleCloudTtsEngine.stop()
        stopSpeakingAmplitudeSimulation()
        try {
            speechRecognizer?.destroy()
        } catch (_: Exception) {}
        try {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        } catch (_: Exception) {}
    }
}
