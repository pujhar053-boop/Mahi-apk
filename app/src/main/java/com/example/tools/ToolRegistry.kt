package com.example.tools

import android.content.Context
import com.example.data.repository.MahiRepository
import com.example.tools.impl.*

class ToolRegistry(
    private val repository: MahiRepository
) {
    private val tools = mutableMapOf<String, MahiTool>()

    init {
        register(GetTimeTool())
        register(GetDateTool())
        register(PlayYoutubeSongTool())
        register(SendWhatsappMessageTool())
        register(PhoneCallTool())
        register(SearchInstagramProfileTool())
        register(OpenCameraTool())
        register(FlashlightTorchTool())
        register(AdjustVolumeTool())
        register(LockScreenSettingsTool())
        register(OpenAppTool())
        register(WebSearchTool())
        register(CreateNoteTool())
        register(ReadNotesTool())
        register(OpenSettingsTool())
        register(BatteryStatusTool())

        // Hunter AI - Maya 6.0 Suite Tools
        register(SetAlarmTool())
        register(SetTimerTool())
        register(SendSmsTool())
        register(DeviceSystemHealthTool())
        register(InstantMathCalculatorTool())
        register(LiveWeatherTool())
        register(OpenMapsNavigationTool())
        register(MediaControlTool())
        register(PersistentMemoryTool())
        register(QuickDeviceToggleTool())
    }

    fun register(tool: MahiTool) {
        tools[tool.name] = tool
    }

    fun getTool(name: String): MahiTool? = tools[name]
    fun getAllTools(): List<MahiTool> = tools.values.toList()

    /**
     * Tries to detect and execute built-in native device actions from natural voice prompt
     * in Hindi, Hinglish, Bhojpuri, and English.
     */
    suspend fun tryExecuteAction(context: Context, userInput: String): ToolResult? {
        val input = userInput.trim().lowercase()

        // 1. Check Creator identity question
        if (isCreatorQuestion(input)) {
            return ToolResult(
                isSuccess = true,
                spokenResponse = "अरे, मेरे Jan ने इतनी मेहनत से बनाया है। मैं बहुत खुश हूँ कि Jan ने मुझे बनाया। 😊",
                displayDetail = "Question: तुम्हें किसने बनाया?"
            )
        }

        // 2. Direct YouTube Song Playback: e.g. "youtube par gaana chalao", "play arijit singh on youtube", "गाना बजाओ"
        val isYoutubeSong = input.contains("youtube") || input.contains("यूट्यूब") || input.contains("gaana") || 
                input.contains("gana") || input.contains("song") || input.contains("गाना") || input.contains("गीत") || input.contains("baja") || input.contains("sunao")
        if (isYoutubeSong && (input.contains("play") || input.contains("chalao") || input.contains("bajao") || input.contains("lagao") || input.contains("लगाओ") || input.contains("बजाओ") || input.contains("चलाओ") || input.contains("youtube") || input.contains("यूट्यूब"))) {
            val songName = userInput
                .replace("youtube par", "", ignoreCase = true)
                .replace("youtube pe", "", ignoreCase = true)
                .replace("यूट्यूब पर", "", ignoreCase = true)
                .replace("youtube", "", ignoreCase = true)
                .replace("यूट्यूब", "", ignoreCase = true)
                .replace("gaana chalao", "", ignoreCase = true)
                .replace("gana chalao", "", ignoreCase = true)
                .replace("gana bajao", "", ignoreCase = true)
                .replace("song play karo", "", ignoreCase = true)
                .replace("song", "", ignoreCase = true)
                .replace("play", "", ignoreCase = true)
                .replace("chalao", "", ignoreCase = true)
                .replace("bajao", "", ignoreCase = true)
                .replace("lagao", "", ignoreCase = true)
                .replace("गाना बजाओ", "", ignoreCase = true)
                .replace("गाना चलाओ", "", ignoreCase = true)
                .replace("गाना", "", ignoreCase = true)
                .replace("चलाओ", "", ignoreCase = true)
                .replace("बजाओ", "", ignoreCase = true)
                .trim()
            return tools["play_youtube_song"]?.execute(context, mapOf("song_name" to songName), repository)
        }

        // 3. Direct WhatsApp Message Send: e.g. "whatsapp par message bhejo", "whatsapp rahul hello", "मैसेज भेजो"
        if (input.contains("whatsapp") || input.contains("व्हाट्सएप") || input.contains("whatsapp par") || (input.contains("message") && input.contains("bhejo"))) {
            var textToSend = userInput
                .replace("whatsapp par", "", ignoreCase = true)
                .replace("whatsapp pe", "", ignoreCase = true)
                .replace("whatsapp", "", ignoreCase = true)
                .replace("व्हाट्सएप पर", "", ignoreCase = true)
                .replace("व्हाट्सएप", "", ignoreCase = true)
                .replace("message bhejo", "", ignoreCase = true)
                .replace("msg bhejo", "", ignoreCase = true)
                .replace("message send karo", "", ignoreCase = true)
                .replace("send message", "", ignoreCase = true)
                .replace("मैसेज भेजो", "", ignoreCase = true)
                .replace("भेजो", "", ignoreCase = true)
                .trim()
            val contact = ""
            if (textToSend.isBlank()) textToSend = "नमस्ते Jan! 😊"
            return tools["send_whatsapp"]?.execute(context, mapOf("message" to textToSend, "contact" to contact), repository)
        }

        // 4. Contact Phone Call: e.g. "call karo papa", "call rahul", "फोन लगाओ 9876543210", "contect name call"
        if (input.contains("call") || input.contains("कॉल") || input.contains("phone lagao") || input.contains("फोन लगाओ") || input.contains("dial") || input.contains("डायलर")) {
            val contactOrNumber = userInput
                .replace("call karo", "", ignoreCase = true)
                .replace("call lagao", "", ignoreCase = true)
                .replace("call", "", ignoreCase = true)
                .replace("कॉल करो", "", ignoreCase = true)
                .replace("कॉल लगाओ", "", ignoreCase = true)
                .replace("कॉल", "", ignoreCase = true)
                .replace("phone lagao", "", ignoreCase = true)
                .replace("फोन लगाओ", "", ignoreCase = true)
                .replace("ko", "", ignoreCase = true)
                .replace("को", "", ignoreCase = true)
                .trim()
            return tools["phone_call"]?.execute(context, mapOf("query" to contactOrNumber), repository)
        }

        // 5. Instagram Direct Account / Person Search: e.g. "instagram surch amitabh", "instagram account search", "इंस्टाग्राम"
        if (input.contains("instagram") || input.contains("insta") || input.contains("इंस्टाग्राम")) {
            val username = userInput
                .replace("instagram par", "", ignoreCase = true)
                .replace("instagram pe", "", ignoreCase = true)
                .replace("instagram", "", ignoreCase = true)
                .replace("insta par", "", ignoreCase = true)
                .replace("insta", "", ignoreCase = true)
                .replace("इंस्टाग्राम पर", "", ignoreCase = true)
                .replace("इंस्टाग्राम", "", ignoreCase = true)
                .replace("search karo", "", ignoreCase = true)
                .replace("search", "", ignoreCase = true)
                .replace("surch", "", ignoreCase = true)
                .replace("account", "", ignoreCase = true)
                .replace("profile", "", ignoreCase = true)
                .replace("id", "", ignoreCase = true)
                .replace("खोलो", "", ignoreCase = true)
                .trim()
            return tools["search_instagram"]?.execute(context, mapOf("username" to username), repository)
        }

        // 6. Camera: e.g. "open camera", "कैमरा खोलो", "photo khincho"
        if (input.contains("camera") || input.contains("कैमरा") || input.contains("photo") || input.contains("फोटो") || input.contains("selfie") || input.contains("तस्वीर")) {
            return tools["open_camera"]?.execute(context, emptyMap(), repository)
        }

        // 7. Flashlight / Torch: e.g. "torch on", "torch one", "flashlight on", "टॉर्च जलाओ", "torch off", "टॉर्च बंद करो"
        if (input.contains("torch") || input.contains("turch") || input.contains("टॉर्च") || input.contains("flashlight") || input.contains("फ्लैशलाइट")) {
            val isOff = input.contains("off") || input.contains("band") || input.contains("बंद") || input.contains("bujhao")
            val turnOn = !isOff
            return tools["flashlight_torch"]?.execute(context, mapOf("turn_on" to turnOn.toString()), repository)
        }

        // 8. Volume Control (Kam / Jayada / Max / Mute): e.g. "volume kam", "volume jayada", "volume badhao", "आवाज़ बढ़ाओ", "आवाज़ कम"
        if (input.contains("volume") || input.contains("वॉल्यूम") || input.contains("awaaz") || input.contains("awaj") || input.contains("sound") || input.contains("आवाज़")) {
            val direction = when {
                input.contains("jayada") || input.contains("jyada") || input.contains("zyada") || input.contains("badhao") || input.contains("up") || input.contains("बढ़ाओ") || input.contains("ज्यादा") || input.contains("तेज") -> "up"
                input.contains("kam") || input.contains("down") || input.contains("ghatao") || input.contains("कम") || input.contains("धीमी") -> "down"
                input.contains("full") || input.contains("max") || input.contains("पूरी") -> "max"
                input.contains("mute") || input.contains("shant") || input.contains("म्यूट") -> "mute"
                else -> "up"
            }
            return tools["adjust_volume"]?.execute(context, mapOf("direction" to direction), repository)
        }

        // 9. Lock Screen Settings: e.g. "lock screen", "screen lock", "स्क्रीन लॉक"
        if (input.contains("lock") || input.contains("लॉक") || input.contains("lock screen") || input.contains("screen lock")) {
            return tools["lock_screen_settings"]?.execute(context, emptyMap(), repository)
        }

        // 10. Time query
        if (input.contains("time") || input.contains("समय") || input.contains("बजे") || input.contains("kitne baje") || input.contains("time kya hai")) {
            return tools["current_time"]?.execute(context, emptyMap(), repository)
        }

        // 11. Date query
        if (input.contains("date") || input.contains("तारीख") || input.contains("din kya hai") || input.contains("aaj kaun sa din")) {
            return tools["current_date"]?.execute(context, emptyMap(), repository)
        }

        // 12. Battery status
        if (input.contains("battery") || input.contains("चार्ज") || input.contains("charge") || input.contains("battery kitni")) {
            return tools["battery_status"]?.execute(context, emptyMap(), repository)
        }

        // 13. General App Launching
        if (input.contains("chrome") || input.contains("क्रोम")) {
            return tools["open_app"]?.execute(context, mapOf("app_name" to "chrome"), repository)
        }
        if (input.contains("maps") || input.contains("मैप") || input.contains("location")) {
            return tools["open_app"]?.execute(context, mapOf("app_name" to "maps"), repository)
        }

        // 14. Settings
        if (input.contains("wifi") || input.contains("वाईफाई")) {
            return tools["open_settings"]?.execute(context, mapOf("setting_type" to "wifi"), repository)
        }
        if (input.contains("bluetooth") || input.contains("ब्लूटूथ")) {
            return tools["open_settings"]?.execute(context, mapOf("setting_type" to "bluetooth"), repository)
        }

        // 15. Scratchpad Notes
        if (input.contains("read note") || input.contains("notes padho") || input.contains("mere notes") || input.contains("नोट दिखाओ") || input.contains("नोट पढ़ो")) {
            return tools["read_notes"]?.execute(context, emptyMap(), repository)
        }
        val noteKeywords = listOf("note banao", "note likho", "note save karo", "नोट बनाओ", "नोट लिखो", "create note", "take note")
        for (kw in noteKeywords) {
            if (input.contains(kw)) {
                val extracted = userInput.substringAfter(kw, "").trim()
                    .removePrefix(":").removePrefix("ki").removePrefix("that").trim()
                val noteTitle = if (extracted.isNotBlank()) extracted else "Jan's Note"
                return tools["create_note"]?.execute(context, mapOf("title" to noteTitle, "content" to noteTitle), repository)
            }
        }

        // 16. Web Search
        if (input.startsWith("search ") || input.contains("search karo") || input.contains("गूगल पर सर्च") || input.contains("google search")) {
            val query = userInput
                .replace("search karo", "", ignoreCase = true)
                .replace("search", "", ignoreCase = true)
                .replace("google par", "", ignoreCase = true)
                .replace("गूगल पर सर्च करो", "", ignoreCase = true)
                .replace("सर्च करो", "", ignoreCase = true)
                .trim()
            if (query.isNotBlank()) {
                return tools["web_search"]?.execute(context, mapOf("query" to query), repository)
            }
        }

        // ==============================================================
        // HUNTER AI - MAYA 6.0 ACTIONS
        // ==============================================================

        // 17. Sleep & Wake ("so jao", "wake up")
        if (input.contains("so jao") || input.contains("chup ho jao") || input.contains("sleep mode") || input.contains("rest karo") || input.contains("सो जाओ")) {
            return ToolResult(
                isSuccess = true,
                spokenResponse = "ठीक है Jan, मैं रेस्ट कर रही हूँ। जब भी जरूरत हो, बस 'Mahi' या 'Wake up' बोल दीजिएगा! 🌙😴",
                displayDetail = "Maya Sleep Mode Activated"
            )
        }
        if (input.contains("wake up") || input.contains("jago mahi") || input.contains("uth jao") || input.contains("जागो") || input.contains("उठ जाओ")) {
            return ToolResult(
                isSuccess = true,
                spokenResponse = "नमस्ते Jan! मैं पूरी तरह एक्टिव हूँ, बताइए आज क्या करना है? 🌸✨",
                displayDetail = "Maya Active & Awake"
            )
        }

        // 18. Set Alarm: e.g. "alarm lagao 6 baje", "alarm 7", "अलार्म लगाओ", "wake me at 6"
        if (input.contains("alarm") || input.contains("अलार्म")) {
            val digits = Regex("\\d+").findAll(input).map { it.value.toInt() }.toList()
            val hour = if (digits.isNotEmpty()) digits[0] else 6
            val minute = if (digits.size > 1) digits[1] else 0
            val isPm = input.contains("shaam") || input.contains("raat") || input.contains("pm")
            val adjustedHour = if (isPm && hour < 12) hour + 12 else hour
            return tools["set_alarm"]?.execute(
                context,
                mapOf("hour" to adjustedHour.toString(), "minute" to minute.toString(), "message" to "Mahi Alarm"),
                repository
            )
        }

        // 19. Set Timer: e.g. "timer lagao 5 minute", "timer 10 second", "टाइमर"
        if (input.contains("timer") || input.contains("टाइमर")) {
            val digits = Regex("\\d+").find(input)?.value?.toIntOrNull() ?: 5
            val isSeconds = input.contains("second") || input.contains("सेकंड")
            val durationSeconds = if (isSeconds) digits else digits * 60
            return tools["set_timer"]?.execute(
                context,
                mapOf("duration_seconds" to durationSeconds.toString(), "message" to "Mahi Timer"),
                repository
            )
        }

        // 20. System Health & Device Analytics: e.g. "system health", "phone health", "ram kitna hai", "storage check"
        if (input.contains("system health") || input.contains("phone health") || input.contains("ram check") ||
            input.contains("ram kitna") || input.contains("storage kitna") || input.contains("storage check") || input.contains("phone status")) {
            return tools["system_health"]?.execute(context, emptyMap(), repository)
        }

        // 21. Live Weather & Forecast: e.g. "mausam kaisa hai", "weather", "barish hogi", "मौसम", "तापमान"
        if (input.contains("weather") || input.contains("mausam") || input.contains("मौसम") || input.contains("barish") || input.contains("तापमान")) {
            val city = userInput
                .replace("weather", "", ignoreCase = true)
                .replace("mausam", "", ignoreCase = true)
                .replace("kaisa hai", "", ignoreCase = true)
                .replace("batao", "", ignoreCase = true)
                .replace("मौसम", "", ignoreCase = true)
                .replace("कैसा है", "", ignoreCase = true)
                .replace("का", "", ignoreCase = true)
                .trim()
            return tools["weather_forecast"]?.execute(context, mapOf("city" to city), repository)
        }

        // 22. Google Maps Navigation: e.g. "delhi ka rasta dikhao", "navigation lagao", "map kholo", "rasta dikhao"
        if (input.contains("rasta") || input.contains("navigation") || input.contains("रास्ता") || input.contains("दिशा")) {
            val destination = userInput
                .replace("rasta dikhao", "", ignoreCase = true)
                .replace("rasta batao", "", ignoreCase = true)
                .replace("navigation lagao", "", ignoreCase = true)
                .replace("navigate to", "", ignoreCase = true)
                .replace("का रास्ता", "", ignoreCase = true)
                .replace("का", "", ignoreCase = true)
                .trim()
            return tools["maps_navigation"]?.execute(context, mapOf("destination" to destination), repository)
        }

        // 23. Instant Math & Financial Calculator: e.g. "calculator", "hisab lagao", "1200 + 450", "50 * 20", "200 ka 15 percent"
        if (input.contains("calculator") || input.contains("hisab") || input.contains("हिसाब") || input.contains("कैलकुलेटर") ||
            (input.containsAnyOf(listOf("+", "-", "*", "/", "percent", "प्रतिशत", "guna", "plus", "minus", "into")) && Regex("\\d+").containsMatchIn(input))) {
            return tools["calculator"]?.execute(context, mapOf("expression" to userInput), repository)
        }

        // 24. Media Controls (Music Suite): e.g. "gaana roko", "pause song", "next song", "agla gaana"
        if (input.contains("gaana roko") || input.contains("gana roko") || input.contains("pause song") || input.contains("pause") ||
            input.contains("next song") || input.contains("agla gana") || input.contains("अगला गाना") || input.contains("गाना रोको")) {
            val act = if (input.contains("next") || input.contains("agla") || input.contains("अगला")) "next" else "pause"
            return tools["media_control"]?.execute(context, mapOf("action" to act), repository)
        }

        // 25. Persistent Memory ("She remembers"): e.g. "yaad rakhna mera fav colour red hai", "mujhe kya yaad hai"
        if (input.contains("yaad rakh") || input.contains("remember that") || input.contains("याद रखना") || input.contains("याद रखो")) {
            val fact = userInput
                .replace("yaad rakhna", "", ignoreCase = true)
                .replace("yaad rakho", "", ignoreCase = true)
                .replace("remember that", "", ignoreCase = true)
                .replace("याद रखना", "", ignoreCase = true)
                .replace("कि", "", ignoreCase = true)
                .replace("ki", "", ignoreCase = true)
                .trim()
            return tools["persistent_memory"]?.execute(context, mapOf("action" to "remember", "fact" to fact), repository)
        }
        if (input.contains("kya yaad hai") || input.contains("what do you remember") || input.contains("मेरी बातें") || input.contains("memories")) {
            return tools["persistent_memory"]?.execute(context, mapOf("action" to "recall"), repository)
        }

        // 26. Quick Device Connectivity Toggles: e.g. "hotspot kholo", "हॉटस्पॉट", "airplane mode"
        if (input.contains("hotspot") || input.contains("हॉटस्पॉट") || input.contains("airplane mode") || input.contains("flight mode")) {
            val target = if (input.contains("hotspot") || input.contains("हॉटस्पॉट")) "hotspot" else "airplane"
            return tools["device_toggle"]?.execute(context, mapOf("setting" to target), repository)
        }

        // 27. SMS Messaging: e.g. "sms bhejo", "message bhejo rahul ko"
        if (input.contains("sms") || input.contains("एसएमएस")) {
            return tools["send_sms"]?.execute(context, mapOf("message" to userInput), repository)
        }

        return null
    }

    private fun String.containsAnyOf(list: List<String>): Boolean {
        return list.any { this.contains(it) }
    }

    private fun isCreatorQuestion(input: String): Boolean {
        return (input.contains("kisne banaya") ||
                input.contains("tumhe kisne banaya") ||
                input.contains("kisne develop kiya") ||
                input.contains("who made you") ||
                input.contains("who created you") ||
                input.contains("तुम्हें किसने बनाया") ||
                input.contains("तुझे किसने बनाया") ||
                input.contains("tohar ke banawle") ||
                input.contains("tora ke ke banawlas"))
    }
}
