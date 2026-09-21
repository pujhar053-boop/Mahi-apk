package com.example.tools.impl

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import com.example.data.model.ActionStatus
import com.example.data.repository.MahiRepository
import com.example.tools.MahiTool
import com.example.tools.ToolParameter
import com.example.tools.ToolResult
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GetTimeTool : MahiTool {
    override val name: String = "current_time"
    override val description: String = "Get current time in 12-hour format"
    override val parameters: List<ToolParameter> = emptyList()

    override suspend fun execute(
        context: Context,
        params: Map<String, String>,
        repository: MahiRepository
    ): ToolResult {
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val currentTime = timeFormat.format(Date())
        repository.recordAction("Checked Time", ActionStatus.SUCCESS, currentTime)
        return ToolResult(
            isSuccess = true,
            spokenResponse = "समय $currentTime है 😊",
            displayDetail = "Current Time: $currentTime",
            data = mapOf("time" to currentTime)
        )
    }
}

class GetDateTool : MahiTool {
    override val name: String = "current_date"
    override val description: String = "Get current date and day"
    override val parameters: List<ToolParameter> = emptyList()

    override suspend fun execute(
        context: Context,
        params: Map<String, String>,
        repository: MahiRepository
    ): ToolResult {
        val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("hi", "IN"))
        val currentDate = dateFormat.format(Date())
        repository.recordAction("Checked Date", ActionStatus.SUCCESS, currentDate)
        return ToolResult(
            isSuccess = true,
            spokenResponse = "आज $currentDate है 📅",
            displayDetail = "Date: $currentDate",
            data = mapOf("date" to currentDate)
        )
    }
}

class PlayYoutubeSongTool : MahiTool {
    override val name: String = "play_youtube_song"
    override val description: String = "Search and directly play song/video on YouTube"
    override val parameters: List<ToolParameter> = listOf(
        ToolParameter(name = "song_name", description = "Song or artist name to play")
    )

    override suspend fun execute(
        context: Context,
        params: Map<String, String>,
        repository: MahiRepository
    ): ToolResult {
        val song = params["song_name"]?.trim().orEmpty()
        val query = if (song.isNotBlank()) song else "new hindi songs"

        return try {
            val intent = Intent(Intent.ACTION_SEARCH).apply {
                setPackage("com.google.android.youtube")
                putExtra("query", query)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            repository.recordAction("YouTube Play", ActionStatus.SUCCESS, query)
            ToolResult(
                isSuccess = true,
                spokenResponse = "यूट्यूब पर $query बजा दिया है 😊",
                displayDetail = "Playing: $query"
            )
        } catch (e: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=" + Uri.encode(query))).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
            repository.recordAction("YouTube Play (Web)", ActionStatus.SUCCESS, query)
            ToolResult(
                isSuccess = true,
                spokenResponse = "यूट्यूब चालू कर दिया है 😊",
                displayDetail = "Playing: $query"
            )
        }
    }
}

class SendWhatsappMessageTool : MahiTool {
    override val name: String = "send_whatsapp"
    override val description: String = "Send WhatsApp message to a contact or open chat"
    override val parameters: List<ToolParameter> = listOf(
        ToolParameter(name = "message", description = "Message text to send"),
        ToolParameter(name = "contact", description = "Contact name or phone number", isRequired = false)
    )

    override suspend fun execute(
        context: Context,
        params: Map<String, String>,
        repository: MahiRepository
    ): ToolResult {
        val message = params["message"]?.trim().orEmpty().ifEmpty { "Hello!" }
        val contact = params["contact"]?.trim().orEmpty()

        return try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                setPackage("com.whatsapp")
                putExtra(Intent.EXTRA_TEXT, message)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            val desc = if (contact.isNotBlank()) "$contact ko '$message'" else "'$message'"
            repository.recordAction("WhatsApp Message", ActionStatus.SUCCESS, desc)
            ToolResult(
                isSuccess = true,
                spokenResponse = "व्हाट्सएप खोल दिया है 😊",
                displayDetail = "WhatsApp: $message"
            )
        } catch (e: Exception) {
            val genericIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?text=" + Uri.encode(message))).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(genericIntent)
                ToolResult(
                    isSuccess = true,
                    spokenResponse = "व्हाट्सएप खोल दिया है 😊",
                    displayDetail = "WhatsApp message prepared"
                )
            } catch (_: Exception) {
                ToolResult(
                    isSuccess = false,
                    spokenResponse = "Jan, फ़ोन में व्हाट्सएप नहीं मिला।",
                    displayDetail = "WhatsApp not found"
                )
            }
        }
    }
}

class PhoneCallTool : MahiTool {
    override val name: String = "phone_call"
    override val description: String = "Make a phone call or open dialer with number/contact"
    override val parameters: List<ToolParameter> = listOf(
        ToolParameter(name = "query", description = "Contact name or phone number")
    )

    override suspend fun execute(
        context: Context,
        params: Map<String, String>,
        repository: MahiRepository
    ): ToolResult {
        val query = params["query"]?.trim().orEmpty()
        val digitsOnly = query.filter { it.isDigit() || it == '+' }

        return try {
            val uri = if (digitsOnly.length >= 3) {
                Uri.parse("tel:$digitsOnly")
            } else {
                Uri.parse("tel:")
            }
            val intent = Intent(Intent.ACTION_DIAL, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            repository.recordAction("Phone Call", ActionStatus.SUCCESS, query)
            ToolResult(
                isSuccess = true,
                spokenResponse = if (query.isNotBlank()) "$query को कॉल लगा रही हूँ 📞" else "फ़ोन डायलर खोल दिया है 📞",
                displayDetail = "Dialer: $query"
            )
        } catch (e: Exception) {
            ToolResult(
                isSuccess = false,
                spokenResponse = "Jan, कॉल डायलर नहीं खुल सका।",
                displayDetail = "Dialer failed"
            )
        }
    }
}

class SearchInstagramProfileTool : MahiTool {
    override val name: String = "search_instagram"
    override val description: String = "Search direct profile or person account on Instagram"
    override val parameters: List<ToolParameter> = listOf(
        ToolParameter(name = "username", description = "Instagram username or person name")
    )

    override suspend fun execute(
        context: Context,
        params: Map<String, String>,
        repository: MahiRepository
    ): ToolResult {
        val username = params["username"]?.trim()?.replace("@", "").orEmpty()
        val cleanName = if (username.isNotBlank()) username else "explore"

        return try {
            val appUri = Uri.parse("http://instagram.com/_u/$cleanName")
            val appIntent = Intent(Intent.ACTION_VIEW, appUri).apply {
                setPackage("com.instagram.android")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(appIntent)
            repository.recordAction("Instagram Search", ActionStatus.SUCCESS, cleanName)
            ToolResult(
                isSuccess = true,
                spokenResponse = "इंस्टाग्राम पर $cleanName खोल दिया 📸",
                displayDetail = "Instagram: @$cleanName"
            )
        } catch (e: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/$cleanName/")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
            repository.recordAction("Instagram Search (Web)", ActionStatus.SUCCESS, cleanName)
            ToolResult(
                isSuccess = true,
                spokenResponse = "इंस्टाग्राम खोल दिया 📸",
                displayDetail = "Instagram: $cleanName"
            )
        }
    }
}

class OpenCameraTool : MahiTool {
    override val name: String = "open_camera"
    override val description: String = "Directly launch camera to capture photo or video"
    override val parameters: List<ToolParameter> = emptyList()

    override suspend fun execute(
        context: Context,
        params: Map<String, String>,
        repository: MahiRepository
    ): ToolResult {
        return try {
            val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            repository.recordAction("Open Camera", ActionStatus.SUCCESS, "Camera launched")
            ToolResult(
                isSuccess = true,
                spokenResponse = "कैमरा खोल दिया है 📸",
                displayDetail = "Camera launched"
            )
        } catch (e: Exception) {
            ToolResult(
                isSuccess = false,
                spokenResponse = "Jan, कैमरा नहीं खुल पाया।",
                displayDetail = "Camera failed"
            )
        }
    }
}

class FlashlightTorchTool : MahiTool {
    override val name: String = "flashlight_torch"
    override val description: String = "Turn device flashlight ON or OFF"
    override val parameters: List<ToolParameter> = listOf(
        ToolParameter(name = "turn_on", description = "true for ON, false for OFF")
    )

    companion object {
        var isTorchOn: Boolean = false
    }

    override suspend fun execute(
        context: Context,
        params: Map<String, String>,
        repository: MahiRepository
    ): ToolResult {
        val turnOn = params["turn_on"]?.toBooleanStrictOrNull() ?: !isTorchOn
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            val cameraId = cameraManager?.cameraIdList?.firstOrNull { id ->
                val chars = cameraManager.getCameraCharacteristics(id)
                val hasFlash = chars.get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
                val facing = chars.get(android.hardware.camera2.CameraCharacteristics.LENS_FACING)
                hasFlash && facing == android.hardware.camera2.CameraCharacteristics.LENS_FACING_BACK
            } ?: cameraManager?.cameraIdList?.firstOrNull()

            if (cameraId != null && cameraManager != null) {
                cameraManager.setTorchMode(cameraId, turnOn)
                isTorchOn = turnOn
                val actionDesc = if (turnOn) "टॉर्च चालू कर दी है 🔦" else "टॉर्च बंद कर दी है 🔦"
                repository.recordAction("Flashlight", ActionStatus.SUCCESS, if (turnOn) "ON" else "OFF")
                ToolResult(
                    isSuccess = true,
                    spokenResponse = actionDesc,
                    displayDetail = "Torch: ${if (turnOn) "ON" else "OFF"}"
                )
            } else {
                ToolResult(
                    isSuccess = false,
                    spokenResponse = "Jan, आपके फ़ोन में टॉर्च नहीं मिल सकी।",
                    displayDetail = "Torch not supported"
                )
            }
        } catch (e: Exception) {
            ToolResult(
                isSuccess = false,
                spokenResponse = "Jan, टॉर्च चालू करने में दिक्कत आ रही है।",
                displayDetail = "Torch error: ${e.localizedMessage}"
            )
        }
    }
}

class AdjustVolumeTool : MahiTool {
    override val name: String = "adjust_volume"
    override val description: String = "Increase, decrease, mute, or max device media and ring volume"
    override val parameters: List<ToolParameter> = listOf(
        ToolParameter(name = "direction", description = "up, down, max, mute")
    )

    override suspend fun execute(
        context: Context,
        params: Map<String, String>,
        repository: MahiRepository
    ): ToolResult {
        val direction = params["direction"]?.lowercase()?.trim() ?: "up"
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

        if (audioManager == null) {
            return ToolResult(isSuccess = false, spokenResponse = "Jan, ऑडियो मैनेजर नहीं मिला।", displayDetail = "AudioManager null")
        }

        return try {
            val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

            when (direction) {
                "up", "more", "increase" -> {
                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
                    repository.recordAction("Volume Up", ActionStatus.SUCCESS, "Raised volume")
                    ToolResult(isSuccess = true, spokenResponse = "आवाज़ बढ़ा दी 🔊", displayDetail = "Volume increased")
                }
                "down", "less", "decrease" -> {
                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
                    repository.recordAction("Volume Down", ActionStatus.SUCCESS, "Lowered volume")
                    ToolResult(isSuccess = true, spokenResponse = "आवाज़ कम कर दी 🔉", displayDetail = "Volume decreased")
                }
                "max" -> {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVol, AudioManager.FLAG_SHOW_UI)
                    repository.recordAction("Volume Max", ActionStatus.SUCCESS, "Full volume")
                    ToolResult(isSuccess = true, spokenResponse = "आवाज़ फुल कर दी 📢", displayDetail = "Volume 100%")
                }
                "mute" -> {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_SHOW_UI)
                    repository.recordAction("Volume Mute", ActionStatus.SUCCESS, "Muted")
                    ToolResult(isSuccess = true, spokenResponse = "आवाज़ म्यूट कर दी 🔇", displayDetail = "Volume muted")
                }
                else -> {
                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
                    ToolResult(isSuccess = true, spokenResponse = "आवाज़ सेट कर दी 🔊", displayDetail = "Volume adjusted")
                }
            }
        } catch (e: Exception) {
            ToolResult(isSuccess = false, spokenResponse = "Jan, वॉल्यूम बदलने में परेशानी हुई।", displayDetail = "Volume error")
        }
    }
}

class LockScreenSettingsTool : MahiTool {
    override val name: String = "lock_screen_settings"
    override val description: String = "Open lock screen or display settings to lock / configure timeout"
    override val parameters: List<ToolParameter> = emptyList()

    override suspend fun execute(
        context: Context,
        params: Map<String, String>,
        repository: MahiRepository
    ): ToolResult {
        return try {
            val intent = Intent(Settings.ACTION_SECURITY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            repository.recordAction("Lock / Security Settings", ActionStatus.SUCCESS, "Opened security settings")
            ToolResult(
                isSuccess = true,
                spokenResponse = "स्क्रीन लॉक खोल दिया 🔒",
                displayDetail = "Lock Screen Settings"
            )
        } catch (e: Exception) {
            val altIntent = Intent(Settings.ACTION_DISPLAY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(altIntent)
            ToolResult(
                isSuccess = true,
                spokenResponse = "डिस्प्ले सेटिंग्स खोल दी 📱",
                displayDetail = "Display Settings"
            )
        }
    }
}

class OpenAppTool : MahiTool {
    override val name: String = "open_app"
    override val description: String = "Open an installed app on device"
    override val parameters: List<ToolParameter> = listOf(
        ToolParameter(name = "app_name", description = "App name to open")
    )

    override suspend fun execute(
        context: Context,
        params: Map<String, String>,
        repository: MahiRepository
    ): ToolResult {
        val target = params["app_name"]?.lowercase()?.trim().orEmpty()
        val pm = context.packageManager

        val pkg = when {
            target.contains("youtube") -> "com.google.android.youtube"
            target.contains("whatsapp") -> "com.whatsapp"
            target.contains("chrome") -> "com.android.chrome"
            target.contains("maps") -> "com.google.android.apps.maps"
            target.contains("instagram") -> "com.instagram.android"
            else -> null
        }

        if (pkg != null) {
            val launchIntent = pm.getLaunchIntentForPackage(pkg)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                repository.recordAction("Opened App", ActionStatus.SUCCESS, target)
                return ToolResult(
                    isSuccess = true,
                    spokenResponse = "$target खोल दिया ✨",
                    displayDetail = "Opened $pkg"
                )
            }
        }

        return ToolResult(
            isSuccess = false,
            spokenResponse = "Jan, $target ऐप नहीं मिल पाया।",
            displayDetail = "App not found: $target"
        )
    }
}

class WebSearchTool : MahiTool {
    override val name: String = "web_search"
    override val description: String = "Search google for query"
    override val parameters: List<ToolParameter> = listOf(
        ToolParameter(name = "query", description = "Search query")
    )

    override suspend fun execute(
        context: Context,
        params: Map<String, String>,
        repository: MahiRepository
    ): ToolResult {
        val query = params["query"]?.trim().orEmpty()
        return try {
            val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                putExtra(SearchManager.QUERY, query)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            repository.recordAction("Web Search", ActionStatus.SUCCESS, query)
            ToolResult(
                isSuccess = true,
                spokenResponse = "सर्च कर दिया: $query 🔍",
                displayDetail = "Search: $query"
            )
        } catch (e: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + Uri.encode(query))).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
            repository.recordAction("Web Search (Browser)", ActionStatus.SUCCESS, query)
            ToolResult(
                isSuccess = true,
                spokenResponse = "सर्च कर दिया ✨",
                displayDetail = "Search: $query"
            )
        }
    }
}

class CreateNoteTool : MahiTool {
    override val name: String = "create_note"
    override val description: String = "Create a note in Mahi Scratchpad"
    override val parameters: List<ToolParameter> = listOf(
        ToolParameter(name = "title", description = "Title of the note"),
        ToolParameter(name = "content", description = "Body content of the note", isRequired = false)
    )

    override suspend fun execute(
        context: Context,
        params: Map<String, String>,
        repository: MahiRepository
    ): ToolResult {
        val title = params["title"]?.trim().orEmpty().ifEmpty { "Jan's Voice Note" }
        val content = params["content"]?.trim().orEmpty().ifEmpty { title }
        val id = repository.saveNote(title = title, content = content)
        return ToolResult(
            isSuccess = true,
            spokenResponse = "नोट सेव कर लिया: $title 📝",
            displayDetail = "Note saved: $title (ID: $id)",
            data = mapOf("note_id" to id.toString(), "title" to title)
        )
    }
}

class ReadNotesTool : MahiTool {
    override val name: String = "read_notes"
    override val description: String = "Read recent notes from Mahi Scratchpad"
    override val parameters: List<ToolParameter> = emptyList()

    override suspend fun execute(
        context: Context,
        params: Map<String, String>,
        repository: MahiRepository
    ): ToolResult {
        val notes = repository.allNotes.firstOrNull().orEmpty()
        return if (notes.isNotEmpty()) {
            val latest = notes.first()
            val spoken = "Jan, आपके पास ${notes.size} नोट्स हैं। सबसे नया नोट है: '${latest.title}'"
            repository.recordAction("Read Notes", ActionStatus.SUCCESS, "${notes.size} notes")
            ToolResult(isSuccess = true, spokenResponse = spoken, displayDetail = "Found ${notes.size} notes")
        } else {
            ToolResult(isSuccess = true, spokenResponse = "Jan, अभी डायरी में कोई नोट नहीं है। क्या कोई नोट लिखूँ? 😊", displayDetail = "No notes")
        }
    }
}

class OpenSettingsTool : MahiTool {
    override val name: String = "open_settings"
    override val description: String = "Open system settings"
    override val parameters: List<ToolParameter> = listOf(
        ToolParameter(name = "setting_type", description = "wifi, bluetooth, sound, display")
    )

    override suspend fun execute(
        context: Context,
        params: Map<String, String>,
        repository: MahiRepository
    ): ToolResult {
        val type = params["setting_type"]?.lowercase()?.trim() ?: "main"
        val action = when {
            type.contains("wifi") -> Settings.ACTION_WIFI_SETTINGS
            type.contains("bluetooth") -> Settings.ACTION_BLUETOOTH_SETTINGS
            type.contains("sound") || type.contains("volume") -> Settings.ACTION_SOUND_SETTINGS
            type.contains("display") || type.contains("screen") -> Settings.ACTION_DISPLAY_SETTINGS
            else -> Settings.ACTION_SETTINGS
        }
        return try {
            val intent = Intent(action).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            context.startActivity(intent)
            repository.recordAction("Opened Settings", ActionStatus.SUCCESS, type)
            ToolResult(
                isSuccess = true,
                spokenResponse = "$type सेटिंग्स खोल दी ⚙️",
                displayDetail = "Opened $type settings"
            )
        } catch (e: Exception) {
            ToolResult(
                isSuccess = false,
                spokenResponse = "Jan, सेटिंग्स नहीं खुल पाई।",
                displayDetail = "Error opening settings"
            )
        }
    }
}

class BatteryStatusTool : MahiTool {
    override val name: String = "battery_status"
    override val description: String = "Check device battery percentage and charging state"
    override val parameters: List<ToolParameter> = emptyList()

    override suspend fun execute(
        context: Context,
        params: Map<String, String>,
        repository: MahiRepository
    ): ToolResult {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        val pct = if (level >= 0 && scale > 0) (level * 100 / scale) else 100
        val chargingText = if (isCharging) "चार्जिंग पर लगा है ⚡" else "चल रहा है 🔋"
        val spoken = "Jan, अभी आपके फ़ोन की बैटरी $pct प्रतिशत है और $chargingText"
        repository.recordAction("Battery Status", ActionStatus.SUCCESS, "$pct%")
        return ToolResult(
            isSuccess = true,
            spokenResponse = spoken,
            displayDetail = "Battery: $pct%, Charging: $isCharging"
        )
    }
}
