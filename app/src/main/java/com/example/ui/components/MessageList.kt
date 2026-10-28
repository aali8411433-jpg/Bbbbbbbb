package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatMessage
import com.example.model.CommandCategory
import com.example.ui.theme.ArcBlueSecondary
import com.example.ui.theme.ArcCyanGlow
import com.example.ui.theme.ArcCyanPrimary
import com.example.ui.theme.ArcVioletTertiary
import com.example.ui.theme.SpaceNavyCard
import com.example.ui.theme.SpaceNavyCardBorder
import com.example.ui.theme.SpaceNavyInput
import com.example.ui.theme.StatusRed
import com.example.ui.theme.TextMutedBlue
import com.example.ui.theme.TextPaleIce
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MessageList(
    messages: List<ChatMessage>,
    onSpeakMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    if (messages.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = ArcCyanPrimary.copy(alpha = 0.5f),
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Awaiting command, sir.",
                    color = TextMutedBlue,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Say \"Hey Jarvis\", tap the microphone, or try a sample action below.",
                    color = TextMutedBlue.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    } else {
        LazyColumn(
            state = listState,
            modifier = modifier
                .fillMaxSize()
                .testTag("message_list"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                MessageItem(
                    message = msg,
                    onSpeak = { onSpeakMessage(msg.text) }
                )
            }
        }
    }
}

@Composable
private fun MessageItem(
    message: ChatMessage,
    onSpeak: () -> Unit
) {
    val isUser = message.isFromUser
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val bubbleColor = if (isUser) SpaceNavyInput else SpaceNavyCard
    val borderColor = if (isUser) ArcBlueSecondary.copy(alpha = 0.4f) else SpaceNavyCardBorder
    val shape = if (isUser) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    }

    val categoryColor = when (message.category) {
        CommandCategory.GREETING -> ArcCyanPrimary
        CommandCategory.ACCESSIBILITY -> ArcVioletTertiary
        CommandCategory.APP_LAUNCH -> ArcBlueSecondary
        CommandCategory.SYSTEM_INFO -> ArcCyanGlow
        CommandCategory.AI_QUERY -> ArcCyanPrimary
        CommandCategory.SYSTEM_ERROR -> StatusRed
        CommandCategory.CALL_MESSAGE -> ArcCyanGlow
        CommandCategory.SETTINGS -> ArcCyanPrimary
        CommandCategory.DEVICE_ACTION -> ArcBlueSecondary
    }

    val timeFormatted = rememberFormattedTime(message.timestamp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalAlignment = alignment
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            modifier = Modifier.padding(bottom = 3.dp)
        ) {
            if (!isUser) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = ArcCyanPrimary,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "JARVIS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ArcCyanPrimary,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(6.dp))
                CategoryBadge(category = message.category, color = categoryColor)
            } else {
                CategoryBadge(category = message.category, color = categoryColor)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "YOU",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ArcBlueSecondary,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = ArcBlueSecondary,
                    modifier = Modifier.size(13.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .clip(shape)
                .background(bubbleColor)
                .border(width = 1.dp, color = borderColor, shape = shape)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column {
                Text(
                    text = message.text,
                    color = TextPaleIce,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = timeFormatted,
                        fontSize = 10.sp,
                        color = TextMutedBlue.copy(alpha = 0.6f)
                    )

                    if (!isUser) {
                        IconButton(
                            onClick = onSpeak,
                            modifier = Modifier
                                .size(24.dp)
                                .testTag("speak_btn_${message.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Read aloud",
                                tint = ArcCyanPrimary.copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryBadge(
    category: CommandCategory,
    color: Color
) {
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(0.6.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = category.label,
            color = color,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
        )
    }
}

@Composable
private fun rememberFormattedTime(timestamp: Long): String {
    val formatter = SimpleDateFormat("h:mm a", Locale.getDefault())
    return formatter.format(Date(timestamp))
}
