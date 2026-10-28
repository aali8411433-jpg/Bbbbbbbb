package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.ArcCyanPrimary
import com.example.ui.theme.ArcVioletTertiary
import com.example.ui.theme.SpaceNavyCard
import com.example.ui.theme.SpaceNavyCardBorder
import com.example.ui.theme.SpaceNavyDark
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import com.example.ui.theme.TextMutedBlue
import com.example.ui.theme.TextPaleIce

@Composable
fun AccessibilityHelpDialog(
    isServiceEnabled: Boolean,
    onOpenAccessibilitySettings: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(SpaceNavyDark)
                .border(1.dp, ArcVioletTertiary.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                .padding(20.dp)
                .testTag("accessibility_help_dialog")
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Accessibility,
                            contentDescription = null,
                            tint = ArcVioletTertiary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SCREEN CONTROL SETUP",
                            color = ArcVioletTertiary,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp,
                            letterSpacing = 1.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp).testTag("close_help_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMutedBlue
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Current Status banner
                Surface(
                    color = (if (isServiceEnabled) StatusGreen else StatusAmber).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        (if (isServiceEnabled) StatusGreen else StatusAmber).copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isServiceEnabled) Icons.Default.Accessibility else Icons.Default.Info,
                            contentDescription = null,
                            tint = if (isServiceEnabled) StatusGreen else StatusAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isServiceEnabled)
                                "Service is ACTIVE and ready to accept scroll & touch voice commands."
                            else
                                "Service is currently DISABLED. Follow the steps below to enable it.",
                            color = TextPaleIce,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "HOW TO ENABLE MANUALLY:",
                    color = TextPaleIce,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(10.dp))

                SetupStep(
                    number = "1",
                    title = "Open Android Accessibility",
                    description = "Tap the button below to navigate to your device's Accessibility Settings page."
                )

                Spacer(modifier = Modifier.height(8.dp))

                SetupStep(
                    number = "2",
                    title = "Locate \"JARVIS AI Screen Assistant\"",
                    description = "Under Downloaded Apps or Installed Services, find \"JARVIS AI Screen Assistant\"."
                )

                Spacer(modifier = Modifier.height(8.dp))

                SetupStep(
                    number = "3",
                    title = "Toggle ON & Confirm",
                    description = "Turn on the toggle and confirm the system dialog to authorize voice touch & scroll."
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Security & Privacy Disclaimer Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = SpaceNavyCard),
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, SpaceNavyCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = TextMutedBlue,
                            modifier = Modifier.size(16.dp).padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Security notice: Screen actions only work on standard accessible views. Android policy strictly restricts interaction with passwords, banking screens, or protected device controls.",
                            color = TextMutedBlue,
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        onDismiss()
                        onOpenAccessibilitySettings()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("launch_accessibility_settings_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ArcVioletTertiary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "OPEN ACCESSIBILITY SETTINGS",
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SetupStep(
    number: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            modifier = Modifier.size(22.dp),
            shape = CircleShape,
            color = ArcVioletTertiary.copy(alpha = 0.25f),
            border = androidx.compose.foundation.BorderStroke(1.dp, ArcVioletTertiary)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = number,
                    color = ArcVioletTertiary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            Text(
                text = title,
                color = TextPaleIce,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                color = TextMutedBlue,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}
