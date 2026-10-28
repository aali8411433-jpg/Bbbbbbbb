package com.example.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.accessibility.JarvisAccessibilityService
import com.example.model.AssistantState
import com.example.model.Language
import com.example.ui.components.AccessibilityHelpDialog
import com.example.ui.components.ActionConfirmationDialog
import com.example.ui.components.ArcReactorVisualizer
import com.example.ui.components.MessageList
import com.example.ui.components.SettingsDialog
import com.example.ui.theme.ArcBlueSecondary
import com.example.ui.theme.ArcCyanGlow
import com.example.ui.theme.ArcCyanPrimary
import com.example.ui.theme.ArcVioletTertiary
import com.example.ui.theme.SpaceNavyCard
import com.example.ui.theme.SpaceNavyCardBorder
import com.example.ui.theme.SpaceNavyDark
import com.example.ui.theme.SpaceNavyInput
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import com.example.ui.theme.TextMutedBlue
import com.example.ui.theme.TextPaleIce

@Composable
fun JarvisScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val assistantState by viewModel.assistantState.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val pendingConfirmation by viewModel.pendingConfirmation.collectAsState()
    val showSettingsDialog by viewModel.showSettingsDialog.collectAsState()
    val showAccessibilityDialog by viewModel.showAccessibilityDialog.collectAsState()

    val partialText by viewModel.voiceManager.partialText.collectAsState()
    val isAccessibilityActive by viewModel.isAccessibilityConnected.collectAsState()

    var manualCommandText by remember { mutableStateOf("") }
    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasMicPermission = granted
        if (granted) {
            viewModel.startListening()
        }
    }

    // Handle intents emitted from viewModel
    LaunchedEffect(Unit) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is UiEvent.LaunchIntent -> {
                    try {
                        context.startActivity(event.intent)
                    } catch (e: Exception) {
                        viewModel.processVoiceInput("Error opening intent: ${e.message}")
                    }
                }
                is UiEvent.ShowToast -> {}
            }
        }
    }

    // Check accessibility status on resume
    val isAccessibilityEnabledInSystem = remember(assistantState) {
        JarvisAccessibilityService.isAccessibilityEnabled(context)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(SpaceNavyDark, Color(0xFF04070E))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // TOP HUD HEADER BAR
            TopHudBar(
                isAccessibilityActive = isAccessibilityActive || isAccessibilityEnabledInSystem,
                onOpenAccessibilityDialog = { viewModel.openAccessibilityDialog() },
                onOpenSettings = { viewModel.openSettingsDialog() },
                onClearHistory = { viewModel.clearMessages() }
            )

            // LANGUAGE SELECTOR CHIPS
            LanguageChipsRow(
                currentLanguage = selectedLanguage,
                onLanguageSelected = { viewModel.setLanguage(it) }
            )

            Spacer(modifier = Modifier.height(6.dp))

            // ARC REACTOR HUD VISUALIZER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                ArcReactorVisualizer(
                    assistantState = assistantState
                )
            }

            // LIVE TRANSCRIPTION DISPLAY
            if (partialText.isNotBlank()) {
                Surface(
                    color = ArcCyanPrimary.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ArcCyanPrimary.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "\"$partialText\"",
                        color = ArcCyanGlow,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            // QUICK COMMAND CHIPS
            QuickCommandsRow(
                onCommandSelected = { command ->
                    viewModel.processVoiceInput(command)
                }
            )

            // CONVERSATION LOG / CHAT STREAM
            MessageList(
                messages = messages,
                onSpeakMessage = { text -> viewModel.speakMessage(text) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )

            // BOTTOM CONTROL BAR
            BottomControlBar(
                assistantState = assistantState,
                manualText = manualCommandText,
                onManualTextChange = { manualCommandText = it },
                onSendManualText = {
                    if (manualCommandText.isNotBlank()) {
                        viewModel.processVoiceInput(manualCommandText)
                        manualCommandText = ""
                    }
                },
                onToggleListening = {
                    if (assistantState is AssistantState.Listening) {
                        viewModel.stopListening()
                    } else {
                        if (hasMicPermission) {
                            viewModel.startListening()
                        } else {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                },
                onStopAll = {
                    viewModel.stopListening()
                }
            )
        }

        // CONFIRMATION DIALOG
        pendingConfirmation?.let { action ->
            ActionConfirmationDialog(
                action = action,
                onConfirm = { viewModel.confirmAction() },
                onDismiss = { viewModel.cancelAction() }
            )
        }

        // SETTINGS DIALOG
        if (showSettingsDialog) {
            val prefs = viewModel.prefsManager
            val currentApiKey by prefs.apiKey.collectAsState()
            val currentModel by prefs.modelName.collectAsState()
            val currentGreeting by prefs.customGreeting.collectAsState()
            val currentRate by prefs.speechRate.collectAsState()
            val currentPitch by prefs.speechPitch.collectAsState()

            SettingsDialog(
                initialApiKey = currentApiKey,
                initialModel = currentModel,
                initialLanguage = selectedLanguage,
                initialGreeting = currentGreeting,
                initialSpeechRate = currentRate,
                initialSpeechPitch = currentPitch,
                isAccessibilityEnabled = isAccessibilityActive || isAccessibilityEnabledInSystem,
                onOpenAccessibilitySettings = {
                    context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                },
                onSaveSettings = { apiKey, model, language, greeting, rate, pitch ->
                    viewModel.saveSettings(apiKey, model, language, greeting, rate, pitch)
                },
                onDismiss = { viewModel.closeSettingsDialog() }
            )
        }

        // ACCESSIBILITY SETUP HELP DIALOG
        if (showAccessibilityDialog) {
            AccessibilityHelpDialog(
                isServiceEnabled = isAccessibilityActive || isAccessibilityEnabledInSystem,
                onOpenAccessibilitySettings = {
                    context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                },
                onDismiss = { viewModel.closeAccessibilityDialog() }
            )
        }
    }
}

@Composable
private fun TopHudBar(
    isAccessibilityActive: Boolean,
    onOpenAccessibilityDialog: () -> Unit,
    onOpenSettings: () -> Unit,
    onClearHistory: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(ArcCyanPrimary)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "JARVIS AI",
                color = ArcCyanPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Accessibility Service Status Badge
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onOpenAccessibilityDialog() }
                    .testTag("accessibility_status_chip"),
                color = (if (isAccessibilityActive) StatusGreen else Color(0xFFFFB300)).copy(alpha = 0.16f),
                border = androidx.compose.foundation.BorderStroke(
                    0.8.dp,
                    if (isAccessibilityActive) StatusGreen else Color(0xFFFFB300)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Accessibility,
                        contentDescription = null,
                        tint = if (isAccessibilityActive) StatusGreen else Color(0xFFFFB300),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isAccessibilityActive) "SCREEN ON" else "SCREEN SETUP",
                        color = if (isAccessibilityActive) StatusGreen else Color(0xFFFFB300),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = onClearHistory,
                modifier = Modifier.size(34.dp).testTag("clear_history_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Clear History",
                    tint = TextMutedBlue,
                    modifier = Modifier.size(19.dp)
                )
            }

            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.size(34.dp).testTag("open_settings_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = ArcCyanPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun LanguageChipsRow(
    currentLanguage: Language,
    onLanguageSelected: (Language) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Language.entries.forEach { lang ->
            val isSelected = currentLanguage == lang
            Surface(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onLanguageSelected(lang) }
                    .testTag("lang_toggle_${lang.name}"),
                color = if (isSelected) ArcCyanPrimary.copy(alpha = 0.22f) else SpaceNavyCard,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) ArcCyanPrimary else SpaceNavyCardBorder
                )
            ) {
                Text(
                    text = lang.displayName,
                    color = if (isSelected) ArcCyanPrimary else TextMutedBlue,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun QuickCommandsRow(
    onCommandSelected: (String) -> Unit
) {
    val quickCommands = listOf(
        "Open YouTube",
        "Scroll Down",
        "Scroll Up",
        "What time is it?",
        "Open Chrome",
        "Open WhatsApp",
        "Open Calculator",
        "Open Wi-Fi",
        "Go Back",
        "Go Home",
        "Hey Jarvis"
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth().testTag("quick_commands_row")
    ) {
        items(quickCommands) { cmd ->
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onCommandSelected(cmd) }
                    .testTag("quick_cmd_${cmd.replace(" ", "_")}"),
                color = SpaceNavyCard,
                border = androidx.compose.foundation.BorderStroke(0.8.dp, SpaceNavyCardBorder)
            ) {
                Text(
                    text = cmd,
                    color = TextPaleIce,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }
}

@Composable
private fun BottomControlBar(
    assistantState: AssistantState,
    manualText: String,
    onManualTextChange: (String) -> Unit,
    onSendManualText: () -> Unit,
    onToggleListening: () -> Unit,
    onStopAll: () -> Unit
) {
    val isListening = assistantState is AssistantState.Listening
    val isSpeaking = assistantState is AssistantState.Speaking

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SpaceNavyDark)
            .border(
                width = 1.dp,
                color = SpaceNavyCardBorder,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        // Manual Command Text Input Row (allows typing commands or questions directly)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = manualText,
                onValueChange = onManualTextChange,
                placeholder = {
                    Text("Type command or query...", color = TextMutedBlue.copy(alpha = 0.5f), fontSize = 12.sp)
                },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("manual_command_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SpaceNavyInput,
                    unfocusedContainerColor = SpaceNavyInput,
                    focusedBorderColor = ArcCyanPrimary,
                    unfocusedBorderColor = SpaceNavyCardBorder,
                    focusedTextColor = TextPaleIce,
                    unfocusedTextColor = TextPaleIce
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onSendManualText,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SpaceNavyCard)
                    .border(1.dp, SpaceNavyCardBorder, RoundedCornerShape(12.dp))
                    .testTag("send_manual_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = ArcCyanPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Main Voice Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // STOP ACTION BUTTON
            OutlinedButton(
                onClick = onStopAll,
                modifier = Modifier
                    .weight(0.4f)
                    .height(52.dp)
                    .testTag("stop_listening_btn"),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusRed),
                border = androidx.compose.foundation.BorderStroke(1.dp, StatusRed.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "STOP",
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
            }

            // PRIMARY VOICE MICROPHONE BUTTON
            Button(
                onClick = onToggleListening,
                modifier = Modifier
                    .weight(0.6f)
                    .height(52.dp)
                    .testTag("start_listening_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isListening) StatusRed else ArcCyanPrimary,
                    contentColor = if (isListening) Color.White else Color(0xFF00222B)
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isListening) "STOP LISTENING" else "START LISTENING",
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
