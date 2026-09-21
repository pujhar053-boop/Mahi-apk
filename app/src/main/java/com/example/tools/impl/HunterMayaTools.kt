package com.example.tools.impl

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.AlarmClock
import android.provider.Settings
import android.view.KeyEvent
import com.example.data.model.ActionStatus
import com.example.data.repository.MahiRepository
import com.example.tools.MahiTool
import com.example.tools.ToolParameter
import com.example.tools.ToolResult
import kotlinx.coroutines.flow.firstOrNull
import java.text.DecimalFormat

/**
 * Advanced Tools inspired by The Hunter AI - Maya 6.0 suite:
 * Alarms, Timers, SMS, System Health Analytics, Math & Calculations,
 * Live Weather, Google Maps Navigation, Media Control, Memory & Personas.
 */

class SetAlarmTool : MahiTool {
    override val name: String = "set_alarm"
    override val description: String = "Set an alarm for a specific time"
    override val parameters: List<ToolParameter> = listOf(
        ToolParameter("hour", "Hour of alarm (0-23)"),
        ToolParameter("minute", "Minute of alarm (0-59)"),
        ToolParameter("message", "Optional alarm label")
    )

    override suspend fun execute(
        context: Context,
        params: Map<String, String>,
        repository: MahiRepository
    ): ToolResult {
        val hour = params["hour"]?.toIntOrNull() ?: 6
        val minute = params["minute"]?.toIntOrNull() ?: 0
        val label = params["message"] ?: "Mahi Alarm"

        return try {
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, hour)
                putExtra(AlarmClock.EXTRA_MINUTES, minute)
                putExtra(AlarmClock.EXTRA_MESSAGE, label)
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            val formattedTime = String.format("%02d:%02d", hour, minute)
            repository.recordAction("Set Alarm", ActionStatus.SUCCESS, "$formattedTime - $label")
            ToolResult(
                isSuccess = true,
                spokenResponse = "$formattedTime बजे का अलार्म सेट कर दिया है Jan! ⏰",
                displayDetail = "Alarm set for $formattedTime"
            )
        } catch (e: Exception) {
            ToolResult(
                isSuccess = false,
                spokenResponse = "Jan, अलार्म सेट करने में क्लॉक ऐप नहीं मिल सकी।",
                displayDetail = "Error: ${e.message}"
            )
        }
    }
}

class SetTimerTool : MahiTool {
    override val name: String = "set_timer"
    override val description: String = "Set a countdown timer in seconds or minutes"
    override val parameters: List<ToolParameter> = listOf(
        ToolParameter("duration_seconds", "Duration in seconds"),
        ToolParameter("message", "Timer label")
    )

    override suspend fun execute(
        context: Context,
        params: Map<String, String>,
        repository: MahiRepository
    ): ToolResult {
        val seconds = params["duration_seconds"]?.toIntOrNull() ?: 300
        val label = params["message"] ?: "Mahi Timer"

        return try {
            val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                putExtra(AlarmClock.EXTRA_LENGTH, seconds)
                putExtra(AlarmClock.EXTRA_MESSAGE, label)
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            val minutes = seconds / 60
            val durText = if (minutes > 0) "$minutes मिनट" else "$seconds सेकंड"
            repository.recordAction("Set Timer", ActionStatus.SUCCESS, "$durText - $label")
            ToolResult(
                isSuccess = true,
                spokenResponse = "$durText का टाइमर शुरू कर दिया है! ⏳",
                displayDetail = "Timer started for $durText"
            )
        } catch (e: Exception) {
            ToolResult(
                isSuccess = false,
                spokenResponse = "Jan, टाइमर सेट नहीं हो पाया।",
                displayDetail = "Error: ${e.message}"
            )
        }
    }
}

class SendSmsTool : MahiTool {
    override val name: String = "send_sms"
    override val description: String = "Send an SMS message to a contact or number"
    override val parameters: List<ToolParameter> = listOf(
        ToolParameter("phone_number", "Recipient phone number or empty"),
        ToolParameter("message", "SMS text message")
    )

    override suspend fun execute(
        context: Context,
        params: Map<String, String>,
        repository: MahiRepository
    ): ToolResult {
        val number = params["phone_number"]?.trim().orEmpty()
        val text = params["message"]?.trim().orEmpty()

        return try {
            val uri = if (number.isNotBlank()) Uri.parse("smsto:$number") else Uri.parse("smsto:")
            val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
                putExtra("sms_body", text)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            repository.recordAction("Send SMS", ActionStatus.SUCCESS, if (number.isNotBlank()) "To $number: $text" else text)
            ToolResult(
                isSuccess = true,
                spokenResponse = "मैसेज तैयार कर दिया है Jan! 💬",
                displayDetail = "SMS drafted: $text"
            )
        } catch (e: Exception) {
            ToolResult(
                isSuccess = false,
                spokenResponse = "Jan, SMS ऐप नहीं खुल सकी।",
                displayDetail = "Error: ${e.message}"
            )
        }
    }
}

class DeviceSystemHealthTool : MahiTool {
    override val name: String = "system_health"
    override val description: String = "Check comprehensive RAM, Storage, and Battery system health analytics"
    override val parameters: List<ToolParameter> = emptyList()

    override suspend fun execute(
        context: Context,
        params: Map<String, String>,
        repository: MahiRepository
    ): ToolResult {
        // 1. RAM Analytics
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        actManager?.getMemoryInfo(memoryInfo)
        val freeRamGb = memoryInfo.availMem / (1024.0 * 1024.0 * 1024.0)
        val totalRamGb = memoryInfo.totalMem / (1024.0 * 1024.0 * 1024.0)

        // 2. Storage Analytics
        val stat = StatFs(Environment.getDataDirectory().path)
        val freeStorageGb = (stat.availableBlocksLong * stat.blockSizeLong) / (1024.0 * 1024.0 * 1024.0)
        val totalStorageGb = (stat.blockCountLong * stat.blockSizeLong) / (1024.0 * 1024.0 * 1024.0)

        // 3. Battery status
        val bIntent = context.registerReceiver(null, android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = bIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = bIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batPct = if (level >= 0 && scale > 0) (level * 100 / scale) else 100
        val isCharging = bIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) == BatteryManager.BATTERY_STATUS_CHARGING

        val df = DecimalFormat("#.#")
        val spoken = "Jan, आपके फ़ोन का सिस्टम स्वास्थ्य एकदम बेहतरीन है! RAM ${df.format(freeRamGb)} GB खाली है, स्टोरेज ${df.format(freeStorageGb)} GB बची है, और बैटरी $batPct% ${if (isCharging) "चार्ज हो रही है" else "मस्त चल रही है"}! 🚀"
        val display = "RAM: ${df.format(freeRamGb)}/${df.format(totalRamGb)}GB | Storage: ${df.format(freeStorageGb)}/${df.format(totalStorageGb)}GB | Bat: $batPct%"

        repository.recordAction("System Health", ActionStatus.SUCCESS, display)
        return ToolResult(
            isSuccess = true,
            spokenResponse = spoken,
            displayDetail = display,
            data = mapOf(
                "freeRamGb" to df.format(freeRamGb),
                "freeStorageGb" to df.format(freeStorageGb),
                "battery" to "$batPct%"
            )
        )
    }
}

class InstantMathCalculatorTool : MahiTool {
    override val name: String = "calculator"
    override val description: String = "Calculate mathematical or financial operations quickly"
    override val parameters: List<ToolParameter> = listOf(
        ToolParameter("expression", "Math expression e.g. 50 * 20, 1500 + 300, 500 का 18 percent")
    )

    override suspend fun execute(
        context: Context,
        params: Map<String, String>,
        repository: MahiRepository
    ): ToolResult {
        val expr = params["expression"]?.trim().orEmpty()
        val result = evaluateMath(expr)

        if (result != null) {
            val spoken = "Jan, इसका जवाब $result है! 🧮"
            repository.recordAction("Calculator", ActionStatus.SUCCESS, "$expr = $result")
            return ToolResult(
                isSuccess = true,
                spokenResponse = spoken,
                displayDetail = "$expr = $result"
            )
        } else {
            // Fallback: open native Calculator app
            return try {
                val calcIntent = Intent().apply {
                    action = Intent.ACTION_MAIN
                    addCategory(Intent.CATEGORY_APP_CALCULATOR)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(calcIntent)
                repository.recordAction("Calculator", ActionStatus.SUCCESS, "Opened App")
                ToolResult(
                    isSuccess = true,
                    spokenResponse = "कैलकुलेटर खोल दिया है Jan! 🔢",
                    displayDetail = "Opened Calculator"
                )
            } catch (e: Exception) {
                ToolResult(
                    isSuccess = false,
                    spokenResponse = "Jan, हिसाब नहीं लग पाया।",
                    displayDetail = "Expression: $expr"
                )
            }
        }
    }

    private fun evaluateMath(raw: String): String? {
        val cleaned = raw.lowercase()
            .replace("गुना", "*")
            .replace("into", "*")
            .replace("x", "*")
            .replace("multiplied by", "*")
            .replace("भाग", "/")
            .replace("divided by", "/")
            .replace("divide by", "/")
            .replace("by", "/")
            .replace("प्लस", "+")
            .replace("plus", "+")
            .replace("माइनस", "-")
            .replace("minus", "-")
            .replace("का", " ")
            .replace("percent", "%")
            .replace("प्रतिशत", "%")
            .replace(" ", "")

        try {
            // Percentage: e.g. 500%18 or 18%500
            if (cleaned.contains("%")) {
                val parts = cleaned.split("%")
                if (parts.size == 2) {
                    val a = parts[0].toDoubleOrNull()
                    val b = parts[1].toDoubleOrNull()
                    if (a != null && b != null) {
                        val pct = (a * b) / 100.0
                        return if (pct % 1.0 == 0.0) pct.toLong().toString() else DecimalFormat("#.##").format(pct)
                    }
                }
            }

            // Simple binary operations
            val op = cleaned.firstOrNull { it in listOf('+', '-', '*', '/') } ?: return null
            val parts = cleaned.split(op)
            if (parts.size == 2) {
                val num1 = parts[0].toDoubleOrNull() ?: return null
                val num2 = parts[1].toDoubleOrNull() ?: return null
                val res = when (op) {
                    '+' -> num1 + num2
                    '-' -> num1 - num2
                    '*' -> num1 * num2
                    '/' -> if (num2 != 0.0) num1 / num2 else return "इन्फिनिटी (Zero से भाग नहीं कर सकते)"
                    else -> return null
                }
                return if (res % 1.0 == 0.0) res.toLong().toString() else DecimalFormat("#.##").format(res)
            }
        } catch (e: Exception) {
            return null
        }
        return null
    }
}

class LiveWeatherTool : MahiTool {
    override val name: String = "weather_forecast"
    override val description: String = "Check weather conditions and temperature"
    override val parameters: List<ToolParameter> = listOf(
        ToolParameter("city", "City name or current location")
    )

    override suspend fun execute(
        context: Context,
        params: Map<String, String>,
        repository: MahiRepository
    ): ToolResult {
        val city = params["city"]?.trim().orEmpty().ifBlank { "your location" }
        val searchCity = if (city == "your location") "current location" else city
        val query = "weather $searchCity"

        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + Uri.encode(query))).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            val spoken = if (city == "your location") "आपके यहाँ का मौसम और तापमान देख रही हूँ ⛅" else "$city का मौसम और तापमान दिखा रही हूँ ☀️"
            repository.recordAction("Weather", ActionStatus.SUCCESS, query)
            ToolResult(
                isSuccess = true,
                spokenResponse = spoken,
                displayDetail = "Weather for $city"
            )
        } catch (e: Exception) {
            ToolResult(
                isSuccess = false,
                spokenResponse = "मौसम की जानकारी नहीं खुल सकी।",
                displayDetail = "Error: ${e.message}"
            )
        }
    }
}

class OpenMapsNavigationTool : MahiTool {
    override val name: String = "maps_navigation"
    override val description: String = "Show route or navigate to a destination on Google Maps"
    override val parameters: List<ToolParameter> = listOf(
        ToolParameter("destination", "Destination city, place or address")
    )

    override suspend fun execute(
        context: Context,
        params: Map<String, String>,
        repository: MahiRepository
    ): ToolResult {
        val dest = params["destination"]?.trim().orEmpty()
        val query = if (dest.isNotBlank()) dest else "nearby restaurants"

        return try {
            val uri = Uri.parse("google.navigation:q=" + Uri.encode(query))
            val mapIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.google.android.apps.maps")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(mapIntent)
            repository.recordAction("Navigation", ActionStatus.SUCCESS, query)
            ToolResult(
                isSuccess = true,
                spokenResponse = "$query का रास्ता मैप्स पर लगा दिया है Jan! 🗺️",
                displayDetail = "Navigating to: $query"
            )
        } catch (e: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/" + Uri.encode(query))).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
            repository.recordAction("Navigation", ActionStatus.SUCCESS, query)
            ToolResult(
                isSuccess = true,
                spokenResponse = "$query का मैप खोल दिया है Jan! 📍",
                displayDetail = "Maps: $query"
            )
        }
    }
}

class MediaControlTool : MahiTool {
    override val name: String = "media_control"
    override val description: String = "Play, pause, skip or toggle media playback"
    override val parameters: List<ToolParameter> = listOf(
        ToolParameter("action", "play, pause, next, previous")
    )

    override suspend fun execute(
        context: Context,
        params: Map<String, String>,
        repository: MahiRepository
    ): ToolResult {
        val act = params["action"]?.lowercase()?.trim() ?: "toggle"
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

        val keyEventCode = when {
            act.contains("pause") -> KeyEvent.KEYCODE_MEDIA_PAUSE
            act.contains("play") -> KeyEvent.KEYCODE_MEDIA_PLAY
            act.contains("next") || act.contains("aage") -> KeyEvent.KEYCODE_MEDIA_NEXT
            act.contains("previous") || act.contains("peeche") -> KeyEvent.KEYCODE_MEDIA_PREVIOUS
            else -> KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
        }

        return try {
            audioManager?.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode))
            audioManager?.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyEventCode))
            val spoken = when {
                act.contains("pause") -> "गाना रोक दिया ⏸️"
                act.contains("play") -> "गाना शुरू कर दिया ▶️"
                act.contains("next") -> "अगला गाना ⏭️"
                else -> "म्यूजिक टॉगल कर दिया 🎵"
            }
            repository.recordAction("Media Control", ActionStatus.SUCCESS, act)
            ToolResult(isSuccess = true, spokenResponse = spoken, displayDetail = "Media: $act")
        } catch (e: Exception) {
            ToolResult(isSuccess = false, spokenResponse = "मीडिया कंट्रोल नहीं हो पाया।", displayDetail = "Error: ${e.message}")
        }
    }
}

class PersistentMemoryTool : MahiTool {
    override val name: String = "persistent_memory"
    override val description: String = "Remember personal facts across sessions or recall them"
    override val parameters: List<ToolParameter> = listOf(
        ToolParameter("action", "remember or recall"),
        ToolParameter("fact", "Fact to remember")
    )

    override suspend fun execute(
        context: Context,
        params: Map<String, String>,
        repository: MahiRepository
    ): ToolResult {
        val action = params["action"]?.lowercase()?.trim() ?: "recall"
        val fact = params["fact"]?.trim().orEmpty()

        return if (action.contains("remember") || action.contains("save") || fact.isNotBlank()) {
            val contentToSave = if (fact.isNotBlank()) fact else "Fact: User Note"
            repository.saveNote("Memory", contentToSave)
            repository.recordAction("Saved Memory", ActionStatus.SUCCESS, contentToSave)
            ToolResult(
                isSuccess = true,
                spokenResponse = "याद रख लिया Jan! यह बात हमेशा मेरी याददाश्त में रहेगी। 🧠✨",
                displayDetail = "Saved: $contentToSave"
            )
        } else {
            val notes = repository.allNotes.firstOrNull().orEmpty()
            if (notes.isNotEmpty()) {
                val latest = notes.take(3).joinToString("। ") { it.content }
                ToolResult(
                    isSuccess = true,
                    spokenResponse = "मुझे आपकी ये बातें याद हैं: $latest 😊",
                    displayDetail = "Recalled ${notes.size} memories"
                )
            } else {
                ToolResult(
                    isSuccess = true,
                    spokenResponse = "Jan, अभी मेरी याददाश्त में कोई खास बात नहीं है। आप कुछ कहिए, मैं याद रखूँगी! 🌸",
                    displayDetail = "No memories saved yet"
                )
            }
        }
    }
}

class QuickDeviceToggleTool : MahiTool {
    override val name: String = "device_toggle"
    override val description: String = "Toggle or open device connectivity like Hotspot, Wi-Fi, Bluetooth"
    override val parameters: List<ToolParameter> = listOf(
        ToolParameter("setting", "hotspot, wifi, bluetooth, display")
    )

    override suspend fun execute(
        context: Context,
        params: Map<String, String>,
        repository: MahiRepository
    ): ToolResult {
        val target = params["setting"]?.lowercase()?.trim().orEmpty()
        val action = when {
            target.contains("hotspot") || target.contains("tether") -> "android.settings.TETHER_SETTINGS"
            target.contains("wifi") -> Settings.ACTION_WIFI_SETTINGS
            target.contains("bluetooth") -> Settings.ACTION_BLUETOOTH_SETTINGS
            target.contains("airplane") || target.contains("flight") -> Settings.ACTION_AIRPLANE_MODE_SETTINGS
            else -> Settings.ACTION_SETTINGS
        }

        return try {
            val intent = Intent(action).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            context.startActivity(intent)
            repository.recordAction("Device Toggle", ActionStatus.SUCCESS, target)
            ToolResult(
                isSuccess = true,
                spokenResponse = "$target सेटिंग्स खोल दी है Jan! 📶",
                displayDetail = "Opened $target"
            )
        } catch (e: Exception) {
            ToolResult(
                isSuccess = false,
                spokenResponse = "सेटिंग्स नहीं खुल पाई।",
                displayDetail = "Error: ${e.message}"
            )
        }
    }
}
