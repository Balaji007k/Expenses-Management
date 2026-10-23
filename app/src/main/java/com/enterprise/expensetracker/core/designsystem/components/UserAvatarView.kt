package com.enterprise.expensetracker.core.designsystem.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.enterprise.expensetracker.core.designsystem.theme.BrandBlue
import com.enterprise.expensetracker.core.designsystem.theme.PurpleAi
import com.enterprise.expensetracker.core.designsystem.theme.TextPrimaryDark
import java.io.File

@Composable
fun UserAvatarView(
    name: String,
    avatarUrl: String?,
    isHeadOfHousehold: Boolean = false,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    val bitmap = remember(avatarUrl) {
        if (!avatarUrl.isNullOrBlank() && !avatarUrl.startsWith("preset:")) {
            try {
                val file = File(avatarUrl)
                if (file.exists()) {
                    BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
                } else null
            } catch (_: Throwable) {
                null
            }
        } else null
    }

    val isPresetEmoji = avatarUrl?.startsWith("preset:") == true
    val presetEmoji = if (isPresetEmoji) avatarUrl?.removePrefix("preset:") ?: "" else ""

    val bgGradient = if (isHeadOfHousehold) {
        Brush.linearGradient(listOf(PurpleAi, Color(0xFF9333EA)))
    } else {
        Brush.linearGradient(listOf(BrandBlue, Color(0xFF2563EB)))
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(bgGradient)
            .border(2.dp, Color.White.copy(alpha = 0.2f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = "Avatar for $name",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else if (isPresetEmoji && presetEmoji.isNotBlank()) {
            Text(
                text = presetEmoji,
                fontSize = (size.value * 0.48f).sp
            )
        } else {
            val initial = name.trim().take(1).uppercase()
            if (initial.isNotBlank()) {
                Text(
                    text = initial,
                    fontSize = (size.value * 0.42f).sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
            } else {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = TextPrimaryDark,
                    modifier = Modifier.size(size * 0.6f)
                )
            }
        }
    }
}
