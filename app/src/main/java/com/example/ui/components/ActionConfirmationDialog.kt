package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.ConfirmationAction
import com.example.ui.theme.ArcBlueSecondary
import com.example.ui.theme.ArcCyanPrimary
import com.example.ui.theme.SpaceNavyCard
import com.example.ui.theme.SpaceNavyCardBorder
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.TextMutedBlue
import com.example.ui.theme.TextPaleIce

@Composable
fun ActionConfirmationDialog(
    action: ConfirmationAction,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SpaceNavyCard)
                .border(1.dp, SpaceNavyCardBorder, RoundedCornerShape(16.dp))
                .padding(20.dp)
                .testTag("action_confirmation_dialog")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = when (action) {
                        is ConfirmationAction.PlaceCall -> Icons.Default.Call
                        is ConfirmationAction.SendSms -> Icons.Default.Message
                        is ConfirmationAction.OpenWhatsApp -> Icons.Default.Message
                    },
                    contentDescription = null,
                    tint = ArcCyanPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "CONFIRM ACTION",
                    color = ArcCyanPrimary,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 15.sp,
                    letterSpacing = 1.2.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = action.description,
                color = TextPaleIce,
                fontSize = 15.sp,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = StatusAmber,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "JARVIS requires explicit confirmation for phone and messaging actions.",
                    color = TextMutedBlue,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("cancel_action_btn"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMutedBlue)
                ) {
                    Text("Cancel")
                }

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    onClick = onConfirm,
                    modifier = Modifier.testTag("confirm_action_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ArcCyanPrimary,
                        contentColor = androidx.compose.ui.graphics.Color(0xFF00222B)
                    )
                ) {
                    Text("Confirm", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
