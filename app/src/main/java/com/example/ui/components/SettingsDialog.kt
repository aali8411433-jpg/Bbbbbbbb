package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.Language
import com.example.ui.theme.ArcBlueSecondary
import com.example.ui.theme.ArcCyanPrimary
import com.example.ui.theme.SpaceNavyCard
import com.example.ui.theme.SpaceNavyCardBorder
import com.example.ui.theme.SpaceNavyDark
import com.example.ui.theme.SpaceNavyInput
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import com.example.ui.theme.TextMutedBlue
import com.example.ui.theme.TextPaleIce

@Composable
fun SettingsDialog(
    initialApiKey: String,
    initialModel: String,
    initialLanguage: Language,
    initialGreeting: String,
    initialSpeechRate: Float,
    initialSpeechPitch: Float,
    isAccessibilityEnabled: Boolean,
    onOpenAccessibilitySettings: () -> Unit,
    onSaveSettings: (apiKey: String, model: String, language: Language, greeting: String, rate: Float, pitch: Float) -> Unit,
    onDismiss: () -> Unit
) {
    var apiKey by remember { mutableStateOf(initialApiKey) }
    var showApiKey by remember { mutableStateOf(false) }
    var selectedModel by remember { mutableStateOf(initialModel) }
    var selectedLanguage by remember { mutableStateOf(initialLanguage) }
    var customGreeting by remember { mutableStateOf(initialGreeting) }
    var speechRate by remember { mutableFloatStateOf(initialSpeechRate) }
    var speechPitch by remember { mutableFloatStateOf(initialSpeechPitch) }

    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 680.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(SpaceNavyDark)
                .border(1.dp, ArcCyanPrimary.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .testTag("settings_dialog")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = ArcCyanPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "JARVIS CONFIGURATION",
                            color = ArcCyanPrimary,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 15.sp,
                            letterSpacing = 1.2.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp).testTag("close_settings_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMutedBlue
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(scrollState)
                ) {
                    // SECTION 1: GEMINI API KEY
                    Text(
                        text = "GEMINI API KEY",
                        color = TextPaleIce,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Enter your Google Gemini API key. If left blank, JARVIS will use the system key from the Secrets panel.",
                        color = TextMutedBlue,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("api_key_input"),
                        singleLine = true,
                        placeholder = { Text("Paste AIzaSy... key", color = TextMutedBlue.copy(alpha = 0.5f)) },
                        visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showApiKey = !showApiKey }) {
                                Icon(
                                    imageVector = if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showApiKey) "Hide key" else "Show key",
                                    tint = TextMutedBlue
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SpaceNavyInput,
                            unfocusedContainerColor = SpaceNavyInput,
                            focusedBorderColor = ArcCyanPrimary,
                            unfocusedBorderColor = SpaceNavyCardBorder,
                            focusedTextColor = TextPaleIce,
                            unfocusedTextColor = TextPaleIce
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // SECTION 2: AI MODEL SELECTION
                    Text(
                        text = "MODEL ENGINE",
                        color = TextPaleIce,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("gemini-2.0-flash", "gemini-1.5-flash", "gemini-2.5-flash").forEach { model ->
                            val isSelected = selectedModel == model
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedModel = model }
                                    .testTag("model_chip_$model"),
                                color = if (isSelected) ArcCyanPrimary.copy(alpha = 0.2f) else SpaceNavyCard,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) ArcCyanPrimary else SpaceNavyCardBorder
                                )
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = model.replace("gemini-", "").replace("-flash", " Flash"),
                                        color = if (isSelected) ArcCyanPrimary else TextMutedBlue,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // SECTION 3: VOICE LANGUAGE
                    Text(
                        text = "ASSISTANT LANGUAGE",
                        color = TextPaleIce,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Language.entries.forEach { lang ->
                            val isSelected = selectedLanguage == lang
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        selectedLanguage = lang
                                        if (customGreeting.isBlank() || customGreeting in Language.entries.map { it.defaultGreeting }) {
                                            customGreeting = lang.defaultGreeting
                                        }
                                    }
                                    .testTag("lang_chip_${lang.name}"),
                                color = if (isSelected) ArcCyanPrimary.copy(alpha = 0.2f) else SpaceNavyCard,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) ArcCyanPrimary else SpaceNavyCardBorder
                                )
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = lang.displayName,
                                        color = if (isSelected) ArcCyanPrimary else TextMutedBlue,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // SECTION 4: WAKE GREETING
                    Text(
                        text = "WAKE GREETING PHRASE",
                        color = TextPaleIce,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = customGreeting,
                        onValueChange = { customGreeting = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("greeting_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SpaceNavyInput,
                            unfocusedContainerColor = SpaceNavyInput,
                            focusedBorderColor = ArcCyanPrimary,
                            unfocusedBorderColor = SpaceNavyCardBorder,
                            focusedTextColor = TextPaleIce,
                            unfocusedTextColor = TextPaleIce
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // SECTION 5: SPEECH RATE & PITCH
                    Text(
                        text = "VOICE SPEED & PITCH",
                        color = TextPaleIce,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Speech Rate (${String.format("%.1fx", speechRate)})", color = TextMutedBlue, fontSize = 11.sp)
                    }
                    Slider(
                        value = speechRate,
                        onValueChange = { speechRate = it },
                        valueRange = 0.7f..1.4f,
                        steps = 6,
                        colors = SliderDefaults.colors(
                            thumbColor = ArcCyanPrimary,
                            activeTrackColor = ArcCyanPrimary,
                            inactiveTrackColor = SpaceNavyCardBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // SECTION 6: ACCESSIBILITY SERVICE STATUS
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SpaceNavyCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SpaceNavyCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Accessibility,
                                        contentDescription = null,
                                        tint = if (isAccessibilityEnabled) StatusGreen else StatusRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Screen Control Service",
                                        color = TextPaleIce,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }

                                Surface(
                                    color = (if (isAccessibilityEnabled) StatusGreen else StatusRed).copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = if (isAccessibilityEnabled) "ACTIVE" else "DISABLED",
                                        color = if (isAccessibilityEnabled) StatusGreen else StatusRed,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Required for scrolling, tapping buttons, and typing via voice commands.",
                                color = TextMutedBlue,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedButton(
                                onClick = onOpenAccessibilitySettings,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("open_accessibility_btn"),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ArcCyanPrimary),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ArcCyanPrimary)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OpenInNew,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Open Accessibility Settings", fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Save Action
                Button(
                    onClick = {
                        onSaveSettings(apiKey, selectedModel, selectedLanguage, customGreeting, speechRate, speechPitch)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_settings_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ArcCyanPrimary,
                        contentColor = Color(0xFF00222B)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SAVE CONFIGURATION",
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
