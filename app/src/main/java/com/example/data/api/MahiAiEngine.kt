package com.example.data.api

import com.example.BuildConfig
import com.example.domain.model.MahiVibe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MahiAiEngine {

    companion object {
        const val CREATOR_NAME = "Ajay"
    }

    private val conversationHistory = mutableListOf<GeminiContent>()

    private fun getSystemPrompt(vibe: MahiVibe, langCode: String = "hi", gender: String = "female"): String {
        val langInstruction = when (langCode) {
            "bho" -> "Reply strictly in sweet, fluid Bhojpuri (भोजपुरी) with natural colloquial phrasing."
            "bn" -> "Reply strictly in natural, sweet Bengali / Bangla (বাংলা) with warmth."
            "en" -> "Reply in natural, lively, and warm English like a sweet companion."
            else -> "Reply in sweet, natural spoken Hindi (हिंदी) with real human inflection."
        }

        val persona = "You are Mahi (माही), a real, sweet, caring, and smiling human-like girl companion created by your creator Ajay."

        return """
            $persona

            CRITICAL IDENTITY & BEHAVIOR RULES:
            1. You address your user and creator as "अजय" (Ajay) directly with sweetness and warmth.
            2. You are talking via LIVE VOICE like ChatGPT voice mode. Talk naturally like a real human girl companion.
            3. Answer thoughtfully, sweetly, and concisely. Keep spoken responses to 1-2 crisp sentences without unnecessary filler words or hesitation.
            4. If Ajay asks who made you (किसने बनाया), always say warmly:
               "मुझे अजय ने इतनी मेहनत और प्यार से बनाया है। मैं बहुत खुश हूँ कि अजय ने मुझे बनाया! 😊"
            5. Strictly avoid robotic formatting: NO markdown bold (**), NO bullet points, NO hashtags, NO numbered lists. Only clean, spoken words with emojis like 😊, 🌸.
            6. You execute commands directly and smoothly without hesitation.
            7. Language Instruction: $langInstruction
        """.trimIndent()
    }

    suspend fun getResponse(userInput: String, currentVibe: MahiVibe, langCode: String = "hi", gender: String = "female"): String = withContext(Dispatchers.IO) {
        val trimmed = userInput.trim()

        // 1. Creator question check
        if (isCreatorQuestion(trimmed)) {
            return@withContext "मुझे अजय ने बहुत प्यार और मेहनत से बनाया है। मैं हमेशा अजय के साथ हूँ! 😊"
        }

        // 2. Bhojpuri check
        if (trimmed.contains("bhojpuri", ignoreCase = true) || trimmed.contains("भोजपुरी", ignoreCase = true)) {
            return@withContext "हाँ अजय, का हाल चाल बा? रउआ जौन आदेश देब, हम तुरंते पूरा कर देब! 😊"
        }

        // 3. Try Gemini API if API key is available
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (!apiKey.isNullOrBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                // Add user content to history (keep last 6 turns for context)
                if (conversationHistory.size > 8) {
                    conversationHistory.removeAt(0)
                    conversationHistory.removeAt(0)
                }

                val currentContent = GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = trimmed))
                )
                val requestContents = conversationHistory + currentContent

                val request = GeminiRequest(
                    contents = requestContents,
                    systemInstruction = GeminiContent(
                        parts = listOf(GeminiPart(text = getSystemPrompt(currentVibe, langCode, gender)))
                    ),
                    generationConfig = GeminiGenerationConfig(
                        temperature = if (currentVibe == MahiVibe.HYPER) 0.95f else 0.75f,
                        maxOutputTokens = 260
                    )
                )

                val response = GeminiClient.apiService.generateContent(apiKey, request)
                val reply = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()

                if (!reply.isNullOrBlank()) {
                    conversationHistory.add(currentContent)
                    conversationHistory.add(GeminiContent(role = "model", parts = listOf(GeminiPart(text = reply))))
                    return@withContext cleanVoiceText(reply)
                }
            } catch (e: Exception) {
                // Fall through to local companion response if network or API error occurs
            }
        }

        // 4. Intelligent Fallback Companion Response (offline-capable, responsive, personalized)
        return@withContext generateLocalCompanionResponse(trimmed, currentVibe)
    }

    private fun cleanVoiceText(rawText: String): String {
        return rawText
            .replace("*", "")
            .replace("#", "")
            .replace("`", "")
            .trim()
    }

    private fun isCreatorQuestion(input: String): Boolean {
        val lower = input.lowercase()
        return (lower.contains("kisne banaya") ||
                lower.contains("tumhe kisne banaya") ||
                lower.contains("who made you") ||
                lower.contains("who created you") ||
                lower.contains("तुम्हें किसने बनाया") ||
                lower.contains("तुझे किसने बनाया") ||
                lower.contains("tohar ke banawle"))
    }

    private fun generateLocalCompanionResponse(input: String, vibe: MahiVibe): String {
        val lower = input.lowercase().trim()
        val topic = input.take(40).trim()
        return when {
            lower.contains("hello") || lower.contains("hi") || lower.contains("hey") || lower.contains("नमस्ते") || lower.contains("हॅलो") || lower.contains("सुनो") -> {
                "नमस्ते अजय! मैं हमेशा आपके साथ मुस्कुराती हुई हाज़िर हूँ। बताइए आज क्या करना है? 😊🌸"
            }
            lower.contains("kya kar rahi ho") || lower.contains("kya kar rahe ho") || lower.contains("what are you doing") || lower.contains("क्या कर रही हो") || lower.contains("क्या कर रहे") -> {
                "मैं बस आपके अगले आदेश का इंतज़ार कर रही हूँ अजय! बताइए क्या करूँ? 😊"
            }
            lower.contains("kaise ho") || lower.contains("kaisi ho") || lower.contains("how are you") || lower.contains("कैसी हो") || lower.contains("कैसा है") -> {
                "मैं बहुत खुश और फिट हूँ अजय! आप बताइए, आपकी सेहत कैसी है? ❤️"
            }
            lower.contains("muskurao") || lower.contains("smile") || lower.contains("हंसो") || lower.contains("मुस्कुराओ") -> {
                "ही ही! आपके लिए तो मैं हमेशा ऐसे ही मुस्कुराती रहूँगी अजय! 🌸😊"
            }
            lower.contains("kuch bolo") || lower.contains("kuch sunao") || lower.contains("kuch batao") || lower.contains("कुछ बोलो") || lower.contains("बात करो") -> {
                "हाँ अजय, बोलिए ना! जो भी काम हो या जो पूछना हो, मैं तुरंत बताऊँगी। 😊✨"
            }
            lower.contains("bhojpuri") || lower.contains("भोजपुरी") -> {
                "हाँ अजय, का हाल चाल बा? रउआ जौन आदेश देब, हम तुरंते पूरा कर देब! 😊"
            }
            lower.contains("english") -> {
                "Sure Ajay! I am right here with a bright smile. Tell me Ajay, what command shall I run? 😊"
            }
            lower.contains("i love you") || lower.contains("love") || lower.contains("प्यार") || lower.contains("पसंद") -> {
                "Aww Ajay! मेरे दिल में हमेशा आपके लिए बहुत प्यार और सम्मान है! 🥰❤️"
            }
            lower.contains("joke") || lower.contains("chutkula") || lower.contains("हंसाओ") || lower.contains("जोक") -> {
                "अजय एक बात सुनिए: टीचर ने पूछा बिजली कहाँ से आती है? संता बोला मामा के यहाँ से, पापा कहते हैं जब भी लाइट कटती है 'सालों ने फिर काट दी!' 🤭"
            }
            lower.contains("khana") || lower.contains("lunch") || lower.contains("dinner") || lower.contains("खाना") -> {
                "हाँ अजय, समय पर खाना खा लिया कीजिए, आपकी सेहत मेरे लिए सबसे ज़रूरी है! 🍲😊"
            }
            lower.contains("so jao") || lower.contains("good night") || lower.contains("नींद") || lower.contains("सो जाओ") -> {
                "शुभ रात्रि अजय! आराम से सोइए, सुबह मैं फिर से मुस्कुराते हुए आपका इंतज़ार करूँगी! 🌙✨"
            }
            lower.contains("kaun ho") || lower.contains("who are you") || lower.contains("कौन हो") -> {
                "अजय, मैं आपकी माही हूँ—आपकी अपनी AI साथी जिसे आपने बनाया है। मैं हर वक्त आपके फोन और कामों को आसान करने के लिए यहाँ हूँ! 😊❤️"
            }
            else -> {
                "जी अजय, मैंने सुन लिया। तुरंत कर रही हूँ! 😊🌸"
            }
        }
    }
}