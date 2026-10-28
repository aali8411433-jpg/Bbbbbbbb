package com.example.router

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import com.example.accessibility.JarvisAccessibilityService
import com.example.model.CommandCategory
import com.example.model.ConfirmationAction
import com.example.model.Language
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

sealed interface CommandResult {
    data class ImmediateResponse(
        val spokenText: String,
        val displayText: String = spokenText,
        val category: CommandCategory
    ) : CommandResult

    data class LaunchIntent(
        val intent: Intent,
        val spokenText: String,
        val displayText: String = spokenText,
        val category: CommandCategory = CommandCategory.APP_LAUNCH
    ) : CommandResult

    data class RequireConfirmation(
        val action: ConfirmationAction,
        val spokenText: String,
        val displayText: String = spokenText
    ) : CommandResult

    data class RouteToAi(
        val query: String
    ) : CommandResult
}

class CommandRouter(
    private val context: Context
) {

    fun route(
        rawInput: String,
        language: Language,
        customGreeting: String
    ): CommandResult {
        val trimmed = rawInput.trim()
        val lower = trimmed.lowercase(Locale.ROOT)

        // 1. Wake phrase / Greeting
        if (isWakePhrase(lower)) {
            val greeting = if (customGreeting.isNotBlank()) customGreeting else language.defaultGreeting
            return CommandResult.ImmediateResponse(
                spokenText = greeting,
                displayText = greeting,
                category = CommandCategory.GREETING
            )
        }

        // 2. Stop listening / Silence
        if (lower in listOf("stop listening", "stop", "quiet", "থেমে যাও", "চুপ করো", "रुको", "शांत रहो")) {
            val stopMessage = when (language) {
                Language.BENGALI -> "লিসেনিং বন্ধ করা হলো।"
                Language.HINDI -> "सुनना बंद कर दिया गया है।"
                Language.ENGLISH -> "Listening stopped."
            }
            return CommandResult.ImmediateResponse(
                spokenText = stopMessage,
                displayText = stopMessage,
                category = CommandCategory.DEVICE_ACTION
            )
        }

        // 3. Real Device Time & Date
        if (isTimeQuery(lower)) {
            val (spokenTime, displayTime) = getCurrentFormattedTime(language)
            return CommandResult.ImmediateResponse(
                spokenText = spokenTime,
                displayText = displayTime,
                category = CommandCategory.SYSTEM_INFO
            )
        }

        if (isDateQuery(lower)) {
            val (spokenDate, displayDate) = getCurrentFormattedDate(language)
            return CommandResult.ImmediateResponse(
                spokenText = spokenDate,
                displayText = displayDate,
                category = CommandCategory.SYSTEM_INFO
            )
        }

        // 4. Accessibility Screen Actions (Scroll, Tap, Text, Back, Home)
        val screenActionResult = handleScreenActions(lower, trimmed, language)
        if (screenActionResult != null) {
            return screenActionResult
        }

        // 5. System Settings Shortcuts
        val settingsResult = handleSettingsCommands(lower, language)
        if (settingsResult != null) {
            return settingsResult
        }

        // 6. App Launching (YouTube, Chrome, WhatsApp, Calculator, Camera, etc.)
        val appLaunchResult = handleAppOpening(lower, trimmed, language)
        if (appLaunchResult != null) {
            return appLaunchResult
        }

        // 7. Phone Call & Messaging Confirmation
        val callMsgResult = handleCallAndMessage(lower, trimmed, language)
        if (callMsgResult != null) {
            return callMsgResult
        }

        // 8. Web / Video Search
        val searchResult = handleSearch(lower, trimmed, language)
        if (searchResult != null) {
            return searchResult
        }

        // 9. If not handled locally, route to Gemini AI for reasoning and conversational intelligence!
        return CommandResult.RouteToAi(trimmed)
    }

    private fun isWakePhrase(input: String): Boolean {
        val wakeKeywords = listOf(
            "hey jarvis", "hi jarvis", "hello jarvis", "jarvis",
            "হাই জার্ভিস", "হ্যালো জার্ভিস", "জার্ভিস", "হে জার্ভিস",
            "हे जार्विस", "नमस्ते जार्विस", "जार्विस"
        )
        return wakeKeywords.any { input == it || input.startsWith("$it ") }
    }

    private fun isTimeQuery(input: String): Boolean {
        return input.contains("time") ||
                input.contains("কয়টা বাজে") ||
                input.contains("সময় কত") ||
                input.contains("समय") ||
                input.contains("time is it")
    }

    private fun isDateQuery(input: String): Boolean {
        return input.contains("date") ||
                input.contains("আজকের তারিখ") ||
                input.contains("তারিখ কত") ||
                input.contains("तारीख") ||
                input.contains("today's date")
    }

    private fun getCurrentFormattedTime(language: Language): Pair<String, String> {
        val now = Date()
        val timeFormat = SimpleDateFormat("h:mm a", Locale.US)
        val formattedTime = timeFormat.format(now)

        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR)
        val minute = cal.get(Calendar.MINUTE)
        val amPm = if (cal.get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM"

        val spoken = when (language) {
            Language.BENGALI -> {
                val hourStr = if (hour == 0) "১২" else toBengaliDigits(hour)
                val minStr = if (minute == 0) "" else " ${toBengaliDigits(minute)} মিনিট"
                val partOfDay = if (amPm == "AM") {
                    if (hour < 5) "রাত" else "সকাল"
                } else {
                    if (hour < 5) "দুপুর" else if (hour < 7) "সন্ধ্যা" else "রাত"
                }
                "এখন $partOfDay $hourStr টা$minStr।"
            }
            Language.HINDI -> {
                val part = if (amPm == "AM") "सुबह" else "शाम"
                "अभी $part $hour बजकर $minute मिनट हुए हैं।"
            }
            Language.ENGLISH -> "It is currently $formattedTime on your device."
        }

        val display = "Device Time: $formattedTime"
        return Pair(spoken, display)
    }

    private fun getCurrentFormattedDate(language: Language): Pair<String, String> {
        val now = Date()
        val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.US)
        val formattedDate = dateFormat.format(now)

        val spoken = when (language) {
            Language.BENGALI -> "আজকের তারিখ $formattedDate"
            Language.HINDI -> "आज की तारीख $formattedDate है।"
            Language.ENGLISH -> "Today is $formattedDate."
        }
        val display = "Date: $formattedDate"
        return Pair(spoken, display)
    }

    private fun toBengaliDigits(number: Int): String {
        val bnDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
        return number.toString().map { bnDigits[it - '0'] }.joinToString("")
    }

    private fun handleScreenActions(lower: String, raw: String, language: Language): CommandResult? {
        val service = JarvisAccessibilityService.instance
        val isServiceEnabled = JarvisAccessibilityService.isAccessibilityEnabled(context)

        // If command is an accessibility action but service is not running
        val isScrollDown = lower.contains("scroll down") || lower.contains("নিচে স্ক্রোল") || lower.contains("नीचे स्क्रॉल")
        val isScrollUp = lower.contains("scroll up") || lower.contains("উপরে স্ক্রোল") || lower.contains("ऊपर स्क्रॉल")
        val isClick = lower.startsWith("click ") || lower.startsWith("tap ") || lower.startsWith("ট্যাপ করো ") || lower.startsWith("ক্লিক করো ")
        val isType = lower.startsWith("type ") || lower.startsWith("write ") || lower.startsWith("লিখুন ") || lower.startsWith("লিখো ")
        val isBack = lower in listOf("go back", "back", "ফিরে যাও", "পিছনে যাও", "पीछे जाओ", "वापस")
        val isHome = lower in listOf("go home", "home screen", "home", "হোমে যাও", "হোম স্ক্রিন", "होम स्क्रीन", "होम")
        val isRecents = lower in listOf("recent apps", "overview", "অ্যাপ হিস্ট্রি", "हाल के ऐप्स")
        val isNotifs = lower in listOf("notifications", "open notifications", "নোটিফিকেশন")

        if (!isScrollDown && !isScrollUp && !isClick && !isType && !isBack && !isHome && !isRecents && !isNotifs) {
            return null
        }

        if (service == null || !isServiceEnabled) {
            val helpMsg = when (language) {
                Language.BENGALI -> "স্ক্রিন নিয়ন্ত্রণের জন্য JARVIS এক্সেসিবিলিটি সার্ভিস চালু করা প্রয়োজন। সেটিংস থেকে এটি চালু করুন।"
                Language.HINDI -> "स्क्रीन एक्शन के लिए JARVIS एक्सेसिबिलिटी सेवा को चालू करें।"
                Language.ENGLISH -> "Accessibility Service is not enabled. Please enable JARVIS in Android Settings -> Accessibility."
            }
            return CommandResult.ImmediateResponse(
                spokenText = helpMsg,
                displayText = "$helpMsg\n(Android Settings → Accessibility → JARVIS AI)",
                category = CommandCategory.ACCESSIBILITY
            )
        }

        return when {
            isScrollDown -> {
                val result = service.scrollScreen(scrollDown = true)
                CommandResult.ImmediateResponse(result.message, result.message, CommandCategory.ACCESSIBILITY)
            }
            isScrollUp -> {
                val result = service.scrollScreen(scrollDown = false)
                CommandResult.ImmediateResponse(result.message, result.message, CommandCategory.ACCESSIBILITY)
            }
            isClick -> {
                val target = raw.substringAfter(" ").trim()
                val result = service.clickElement(target)
                CommandResult.ImmediateResponse(result.message, result.message, CommandCategory.ACCESSIBILITY)
            }
            isType -> {
                val text = raw.substringAfter(" ").trim()
                val result = service.enterText(text)
                CommandResult.ImmediateResponse(result.message, result.message, CommandCategory.ACCESSIBILITY)
            }
            isBack -> {
                val result = service.performBack()
                CommandResult.ImmediateResponse(result.message, result.message, CommandCategory.ACCESSIBILITY)
            }
            isHome -> {
                val result = service.performHome()
                CommandResult.ImmediateResponse(result.message, result.message, CommandCategory.ACCESSIBILITY)
            }
            isRecents -> {
                val result = service.performRecents()
                CommandResult.ImmediateResponse(result.message, result.message, CommandCategory.ACCESSIBILITY)
            }
            isNotifs -> {
                val result = service.performNotifications()
                CommandResult.ImmediateResponse(result.message, result.message, CommandCategory.ACCESSIBILITY)
            }
            else -> null
        }
    }

    private fun handleSettingsCommands(lower: String, language: Language): CommandResult? {
        val (action, name) = when {
            lower.contains("wifi") || lower.contains("wi-fi") || lower.contains("ওয়াইফাই") ->
                Pair(Settings.ACTION_WIFI_SETTINGS, "Wi-Fi Settings")
            lower.contains("bluetooth") || lower.contains("ব্লুটুথ") ->
                Pair(Settings.ACTION_BLUETOOTH_SETTINGS, "Bluetooth Settings")
            lower.contains("sound") || lower.contains("volume") || lower.contains("সাউন্ড") || lower.contains("শব্দ") ->
                Pair(Settings.ACTION_SOUND_SETTINGS, "Sound Settings")
            lower.contains("accessibility") || lower.contains("এক্সেসিবিলিটি") ->
                Pair(Settings.ACTION_ACCESSIBILITY_SETTINGS, "Accessibility Settings")
            lower.contains("permission") || lower.contains("অ্যাপ পারমিশন") ->
                Pair(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, "App Settings")
            else -> null
        } ?: return null

        val intent = if (action == Settings.ACTION_APPLICATION_DETAILS_SETTINGS) {
            Intent(action).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } else {
            Intent(action).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }

        val spoken = when (language) {
            Language.BENGALI -> "$name খোলা হচ্ছে।"
            Language.HINDI -> "$name खोला जा रहा है।"
            Language.ENGLISH -> "Opening $name."
        }

        return CommandResult.LaunchIntent(
            intent = intent,
            spokenText = spoken,
            displayText = "Opened $name",
            category = CommandCategory.SETTINGS
        )
    }

    private fun handleAppOpening(lower: String, raw: String, language: Language): CommandResult? {
        val openTriggers = listOf("open ", "launch ", "স্টার্ট ", "খুলুন ", "খোলো ", "खोलो ", "चालू करो ")
        val matchedTrigger = openTriggers.firstOrNull { lower.startsWith(it) } ?: return null
        val targetApp = lower.substring(matchedTrigger.length).trim()

        val pm = context.packageManager

        // Package lookup map
        val packageMap = mapOf(
            "youtube" to "com.google.android.youtube",
            "ইউটিউব" to "com.google.android.youtube",
            "chrome" to "com.android.chrome",
            "ক্রোম" to "com.android.chrome",
            "whatsapp" to "com.whatsapp",
            "হোয়াটসঅ্যাপ" to "com.whatsapp",
            "calculator" to "com.google.android.calculator",
            "ক্যালকুলেটর" to "com.google.android.calculator",
            "camera" to "camera",
            "ক্যামেরা" to "camera",
            "maps" to "com.google.android.apps.maps",
            "ম্যাপস" to "com.google.android.apps.maps",
            "gmail" to "com.google.android.gm",
            "ইমেইল" to "com.google.android.gm",
            "settings" to "settings",
            "সেটিংস" to "settings"
        )

        val targetKey = packageMap.keys.firstOrNull { targetApp.contains(it) }

        if (targetKey != null) {
            val pkg = packageMap[targetKey]
            when (pkg) {
                "settings" -> {
                    val intent = Intent(Settings.ACTION_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    return CommandResult.LaunchIntent(
                        intent = intent,
                        spokenText = "Opening Android Settings.",
                        displayText = "Opened Settings",
                        category = CommandCategory.SETTINGS
                    )
                }
                "camera" -> {
                    val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    return CommandResult.LaunchIntent(
                        intent = intent,
                        spokenText = "Opening Camera.",
                        displayText = "Opened Camera",
                        category = CommandCategory.APP_LAUNCH
                    )
                }
                else -> {
                    if (pkg != null) {
                        val launchIntent = pm.getLaunchIntentForPackage(pkg)
                        if (launchIntent != null) {
                            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            val name = targetKey.replaceFirstChar { it.uppercase() }
                            return CommandResult.LaunchIntent(
                                intent = launchIntent,
                                spokenText = "Opening $name.",
                                displayText = "Opening $name ($pkg)",
                                category = CommandCategory.APP_LAUNCH
                            )
                        } else {
                            val msg = "App $targetKey is not installed on this device."
                            return CommandResult.ImmediateResponse(
                                spokenText = msg,
                                displayText = msg,
                                category = CommandCategory.APP_LAUNCH
                            )
                        }
                    }
                }
            }
        }

        // Generic app name search in installed packages
        val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        for (appInfo in installedApps) {
            val appLabel = pm.getApplicationLabel(appInfo).toString().lowercase(Locale.ROOT)
            if (appLabel == targetApp || appLabel.contains(targetApp)) {
                val launchIntent = pm.getLaunchIntentForPackage(appInfo.packageName)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    val title = pm.getApplicationLabel(appInfo).toString()
                    return CommandResult.LaunchIntent(
                        intent = launchIntent,
                        spokenText = "Opening $title.",
                        displayText = "Opening $title",
                        category = CommandCategory.APP_LAUNCH
                    )
                }
            }
        }

        val notFoundMsg = "Could not find or open app matching \"$targetApp\"."
        return CommandResult.ImmediateResponse(
            spokenText = notFoundMsg,
            displayText = notFoundMsg,
            category = CommandCategory.APP_LAUNCH
        )
    }

    private fun handleCallAndMessage(lower: String, raw: String, language: Language): CommandResult? {
        // Call detection
        val callTriggers = listOf("call ", "কোল করো ", "ফোন করো ", "কল করো ", "कॉल करो ")
        for (trigger in callTriggers) {
            if (lower.startsWith(trigger)) {
                val target = raw.substring(trigger.length).trim()
                val digitsOnly = target.filter { it.isDigit() || it == '+' }
                val phoneNumber = if (digitsOnly.length >= 3) digitsOnly else target
                val spoken = when (language) {
                    Language.BENGALI -> "$phoneNumber নম্বরে কল করার জন্য অনুমতি চাই।"
                    Language.HINDI -> "$phoneNumber पर कॉल करने के लिए पुष्टि करें।"
                    Language.ENGLISH -> "Please confirm calling $phoneNumber."
                }
                return CommandResult.RequireConfirmation(
                    action = ConfirmationAction.PlaceCall(phoneNumber = phoneNumber),
                    spokenText = spoken,
                    displayText = "Call Request: $phoneNumber"
                )
            }
        }

        // SMS / Message detection
        if (lower.startsWith("send message") || lower.startsWith("sms ") || lower.startsWith("মেসেজ পাঠাও ") || lower.startsWith("मैसेज भेजो ")) {
            val remainder = raw.substringAfter("to ", "").ifBlank { raw.substringAfter(" ", "") }
            val parts = remainder.split(" saying ", " that ", " লিখে ", " কি ")
            val phone = parts.firstOrNull()?.filter { it.isDigit() || it == '+' } ?: ""
            val body = if (parts.size > 1) parts[1] else "Hello from JARVIS"

            val spoken = when (language) {
                Language.BENGALI -> "মেসেজ পাঠানোর আগে নিশ্চিত করুন।"
                Language.HINDI -> "संदेश भेजने के लिए पुष्टि करें।"
                Language.ENGLISH -> "Please confirm sending SMS to $phone."
            }
            return CommandResult.RequireConfirmation(
                action = ConfirmationAction.SendSms(phoneNumber = phone, messageBody = body),
                spokenText = spoken,
                displayText = "SMS Request to $phone"
            )
        }

        // WhatsApp message detection
        if (lower.startsWith("whatsapp ") || lower.contains("whatsapp message")) {
            val remainder = raw.substringAfter("to ", "").ifBlank { raw.substringAfter("whatsapp ", "") }
            val phone = remainder.filter { it.isDigit() || it == '+' }
            val spoken = "Please confirm opening WhatsApp to message."
            return CommandResult.RequireConfirmation(
                action = ConfirmationAction.OpenWhatsApp(phoneNumber = phone, messageBody = "Hello from JARVIS"),
                spokenText = spoken,
                displayText = "WhatsApp Message Request"
            )
        }

        return null
    }

    private fun handleSearch(lower: String, raw: String, language: Language): CommandResult? {
        val searchTriggers = listOf("search for ", "google ", "সার্চ করো ", "খুঁজে বের করো ", "सर्च करो ")
        val matched = searchTriggers.firstOrNull { lower.startsWith(it) } ?: return null
        val query = raw.substring(matched.length).trim()

        if (query.isBlank()) return null

        val isYouTube = lower.contains("on youtube") || lower.contains("ইউটিউবে")
        val intent = if (isYouTube) {
            val cleanQuery = query.replace("on youtube", "", ignoreCase = true).trim()
            Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=" + Uri.encode(cleanQuery)))
        } else {
            Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + Uri.encode(query)))
        }.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val spoken = when (language) {
            Language.BENGALI -> "\"$query\" সার্চ করা হচ্ছে।"
            Language.HINDI -> "\"$query\" सर्च किया जा रहा है।"
            Language.ENGLISH -> "Searching for $query."
        }

        return CommandResult.LaunchIntent(
            intent = intent,
            spokenText = spoken,
            displayText = "Web Search: $query",
            category = CommandCategory.DEVICE_ACTION
        )
    }
}
