package com.enterprise.expensetracker.feature.settings

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.enterprise.expensetracker.core.designsystem.components.UserAvatarView
import com.enterprise.expensetracker.core.designsystem.theme.BrandBlue
import com.enterprise.expensetracker.core.designsystem.theme.CardBackgroundDark
import com.enterprise.expensetracker.core.designsystem.theme.CardBorderDark
import com.enterprise.expensetracker.core.designsystem.theme.EmeraldSuccess
import com.enterprise.expensetracker.core.designsystem.theme.RoseAlert
import com.enterprise.expensetracker.core.designsystem.theme.SlateDark700
import com.enterprise.expensetracker.core.designsystem.theme.TextMutedDark
import com.enterprise.expensetracker.core.designsystem.theme.TextPrimaryDark
import java.io.File
import java.io.FileOutputStream

@Composable
fun AvatarPickerDialog(
    currentName: String,
    currentAvatarUrl: String?,
    isHeadOfHousehold: Boolean,
    userId: String,
    onDismiss: () -> Unit,
    onSaveAvatar: (avatarUrl: String) -> Unit
) {
    val context = LocalContext.current
    var previewAvatar by remember { mutableStateOf(currentAvatarUrl) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    val presetEmojis = listOf(
        "👑", "👨‍💼", "👩‍💼", "👨‍💻", "👩‍💻",
        "👨‍🍳", "👩‍⚕️", "🚀", "🦁", "💎",
        "🌟", "⚡", "🍀", "🔥", "🎯", "🛡️"
    )

    // Gallery Photo Picker
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = saveImageLocally(context, uri, userId)
            if (savedPath != null) {
                previewAvatar = savedPath
                statusMessage = "Photo selected from gallery!"
            } else {
                statusMessage = "Could not load selected photo"
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackgroundDark,
        title = {
            Text(
                text = "Choose Profile Avatar",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Live Avatar Preview
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    UserAvatarView(
                        name = currentName,
                        avatarUrl = previewAvatar,
                        isHeadOfHousehold = isHeadOfHousehold,
                        size = 80.dp
                    )
                }

                if (statusMessage != null) {
                    Text(
                        text = statusMessage!!,
                        fontSize = 12.sp,
                        color = EmeraldSuccess,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                // Upload from Device Gallery Button
                Button(
                    onClick = { galleryLauncher.launch("image/*") },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Upload Photo from Device", fontWeight = FontWeight.SemiBold)
                }

                Text(
                    text = "Or Pick a Character / Emoji Preset",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMutedDark
                )

                // Grid of Presets
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(presetEmojis) { emoji ->
                        val isSelected = previewAvatar == "preset:$emoji"
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) BrandBlue else SlateDark700.copy(alpha = 0.5f))
                                .border(if (isSelected) 2.dp else 1.dp, if (isSelected) EmeraldSuccess else CardBorderDark, CircleShape)
                                .clickable {
                                    previewAvatar = "preset:$emoji"
                                    statusMessage = null
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 22.sp)
                        }
                    }
                }

                // Reset to Default Monogram
                OutlinedButton(
                    onClick = {
                        previewAvatar = ""
                        statusMessage = "Reset to default initial monogram"
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = RoseAlert, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Remove Photo (Use Initials)", color = RoseAlert, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveAvatar(previewAvatar ?: "")
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
            ) {
                Text("Save Avatar", color = TextPrimaryDark)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMutedDark)
            }
        }
    )
}

private fun saveImageLocally(context: Context, uri: Uri, userId: String): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        if (originalBitmap == null) return null

        // Scale down to max 512x512 for smooth rendering and lightweight storage
        val maxDimension = 512
        val scale = minOf(maxDimension.toFloat() / originalBitmap.width, maxDimension.toFloat() / originalBitmap.height, 1.0f)
        val scaledBitmap = if (scale < 1.0f) {
            Bitmap.createScaledBitmap(
                originalBitmap,
                (originalBitmap.width * scale).toInt(),
                (originalBitmap.height * scale).toInt(),
                true
            )
        } else originalBitmap

        val avatarsDir = File(context.filesDir, "avatars").apply { mkdirs() }
        val targetFile = File(avatarsDir, "avatar_${userId.replace("-", "_")}.jpg")
        val outStream = FileOutputStream(targetFile)
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outStream)
        outStream.flush()
        outStream.close()

        targetFile.absolutePath
    } catch (_: Throwable) {
        null
    }
}
