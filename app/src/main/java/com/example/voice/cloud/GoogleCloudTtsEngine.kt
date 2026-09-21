package com.example.voice.cloud

import android.content.Context
import android.media.MediaPlayer
import android.util.Base64
import com.example.BuildConfig
import com.example.domain.model.VoiceSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.sin

class GoogleCloudTtsEngine(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) {
    private var mediaPlayer: MediaPlayer? = null
    private var amplitudeJob: Job? = null
    private var isPlaying = false

    fun isCurrentlyPlaying(): Boolean = isPlaying

    suspend fun synthesizeAndPlay(
        text: String,
        settings: VoiceSettings,
        customApiKey: String? = null,
        onAmplitudeUpdate: (Float) -> Unit,
        onStarted: () -> Unit,
        onCompleted: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val apiKey = when {
            !customApiKey.isNullOrBlank() -> customApiKey
            !settings.cloudTtsApiKey.isNullOrBlank() -> settings.cloudTtsApiKey
            !BuildConfig.GEMINI_API_KEY.isNullOrBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY" -> BuildConfig.GEMINI_API_KEY
            else -> {
                onError(IllegalStateException("No Google Cloud API key available for Cloud TTS"))
                return
            }
        }

        withContext(Dispatchers.IO) {
            try {
                val isFemale = !settings.gender.equals("male", ignoreCase = true)

                // Select high-quality natural neural / journey / wavenet female voice
                val (languageCode, voiceName) = when (settings.languageCode) {
                    "en" -> "en-IN" to (if (isFemale) "en-IN-Neural2-A" else "en-IN-Neural2-B")
                    "bn" -> "bn-IN" to (if (isFemale) "bn-IN-Wavenet-A" else "bn-IN-Wavenet-B")
                    "bho" -> "hi-IN" to (if (isFemale) "hi-IN-Neural2-A" else "hi-IN-Neural2-B")
                    else -> "hi-IN" to (if (isFemale) "hi-IN-Neural2-A" else "hi-IN-Neural2-B")
                }

                // Optimal human-like expressive pitch and rate for Mahi
                val pitchVal = if (isFemale) {
                    ((settings.pitch - 1.0) * 8.0 + 1.8).coerceIn(0.5, 4.0)
                } else {
                    ((settings.pitch - 1.0) * 8.0 - 1.0).coerceIn(-4.0, 1.0)
                }
                val rateVal = settings.speechRate.toDouble().coerceIn(0.85, 1.30)

                val request = GoogleTtsRequest(
                    input = SynthesisInput(text = text),
                    voice = VoiceSelectionParams(
                        languageCode = languageCode,
                        name = voiceName,
                        ssmlGender = if (isFemale) "FEMALE" else "MALE"
                    ),
                    audioConfig = AudioConfig(
                        audioEncoding = "MP3",
                        speakingRate = rateVal,
                        pitch = pitchVal,
                        volumeGainDb = 0.0,
                        effectsProfileId = listOf("headphone-class-device", "small-bluetooth-speaker-class-device")
                    )
                )

                val response = GoogleCloudTtsClient.apiService.synthesizeSpeech(apiKey, request)
                val base64Audio = response.audioContent

                if (base64Audio.isNullOrBlank()) {
                    val errMsg = response.error?.message ?: "Empty audio content received from Google Cloud TTS"
                    withContext(Dispatchers.Main) {
                        onError(RuntimeException(errMsg))
                    }
                    return@withContext
                }

                // Decode base64 to MP3 file in cache
                val audioBytes = Base64.decode(base64Audio, Base64.DEFAULT)
                val tempFile = File(context.cacheDir, "mahi_cloud_tts_${System.currentTimeMillis()}.mp3")
                FileOutputStream(tempFile).use { fos ->
                    fos.write(audioBytes)
                    fos.flush()
                }

                withContext(Dispatchers.Main) {
                    playAudioFile(tempFile, onAmplitudeUpdate, onStarted, onCompleted, onError)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e)
                }
            }
        }
    }

    private fun playAudioFile(
        file: File,
        onAmplitudeUpdate: (Float) -> Unit,
        onStarted: () -> Unit,
        onCompleted: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        stop()

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                setOnPreparedListener { mp ->
                    this@GoogleCloudTtsEngine.isPlaying = true
                    mp.start()
                    onStarted()
                    startAmplitudeSimulation(onAmplitudeUpdate)
                }
                setOnCompletionListener {
                    stop()
                    file.delete()
                    onCompleted()
                }
                setOnErrorListener { _, what, extra ->
                    stop()
                    file.delete()
                    onError(RuntimeException("MediaPlayer error: what=$what, extra=$extra"))
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            stop()
            file.delete()
            onError(e)
        }
    }

    private fun startAmplitudeSimulation(onAmplitudeUpdate: (Float) -> Unit) {
        amplitudeJob?.cancel()
        amplitudeJob = coroutineScope.launch(Dispatchers.Default) {
            var step = 0
            while (isActive && isPlaying) {
                step++
                // Expressive speech modulation curve (mouth opening and closing naturally with human cadence)
                val base = 0.45f + 0.35f * sin(step * 0.35f).toFloat()
                val jitter = ((step * 17) % 25) / 100f
                val amplitude = (base + jitter).coerceIn(0.15f, 0.95f)

                withContext(Dispatchers.Main) {
                    if (isPlaying) {
                        onAmplitudeUpdate(amplitude)
                    }
                }
                delay(45)
            }
            withContext(Dispatchers.Main) {
                onAmplitudeUpdate(0f)
            }
        }
    }

    fun stop() {
        isPlaying = false
        amplitudeJob?.cancel()
        amplitudeJob = null
        try {
            mediaPlayer?.let { mp ->
                if (mp.isPlaying) {
                    mp.stop()
                }
                mp.reset()
                mp.release()
            }
        } catch (_: Exception) {}
        mediaPlayer = null
    }
}
