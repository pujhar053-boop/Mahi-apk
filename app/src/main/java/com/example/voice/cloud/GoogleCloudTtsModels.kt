package com.example.voice.cloud

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GoogleTtsRequest(
    @param:Json(name = "input") val input: SynthesisInput,
    @param:Json(name = "voice") val voice: VoiceSelectionParams,
    @param:Json(name = "audioConfig") val audioConfig: AudioConfig
)

@JsonClass(generateAdapter = true)
data class SynthesisInput(
    @param:Json(name = "text") val text: String
)

@JsonClass(generateAdapter = true)
data class VoiceSelectionParams(
    @param:Json(name = "languageCode") val languageCode: String,
    @param:Json(name = "name") val name: String? = null,
    @param:Json(name = "ssmlGender") val ssmlGender: String = "FEMALE"
)

@JsonClass(generateAdapter = true)
data class AudioConfig(
    @param:Json(name = "audioEncoding") val audioEncoding: String = "MP3",
    @param:Json(name = "speakingRate") val speakingRate: Double = 1.04,
    @param:Json(name = "pitch") val pitch: Double = 1.8,
    @param:Json(name = "volumeGainDb") val volumeGainDb: Double = 0.0,
    @param:Json(name = "effectsProfileId") val effectsProfileId: List<String>? = listOf("headphone-class-device")
)

@JsonClass(generateAdapter = true)
data class GoogleTtsResponse(
    @param:Json(name = "audioContent") val audioContent: String?,
    @param:Json(name = "error") val error: GoogleTtsError? = null
)

@JsonClass(generateAdapter = true)
data class GoogleTtsError(
    @param:Json(name = "code") val code: Int?,
    @param:Json(name = "message") val message: String?,
    @param:Json(name = "status") val status: String?
)
