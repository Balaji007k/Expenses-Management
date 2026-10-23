package com.enterprise.expensetracker.feature.expenses

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File
import java.io.FileOutputStream
import com.enterprise.expensetracker.core.designsystem.theme.BrandBlue
import com.enterprise.expensetracker.core.designsystem.theme.CardBackgroundDark
import com.enterprise.expensetracker.core.designsystem.theme.CardBorderDark
import com.enterprise.expensetracker.core.designsystem.theme.EmeraldSuccess
import com.enterprise.expensetracker.core.designsystem.theme.PurpleAi
import com.enterprise.expensetracker.core.designsystem.theme.SlateDark700
import com.enterprise.expensetracker.core.designsystem.theme.TextMutedDark
import com.enterprise.expensetracker.core.designsystem.theme.TextPrimaryDark
import com.enterprise.expensetracker.core.model.Category
import com.enterprise.expensetracker.core.model.Expense
import com.enterprise.expensetracker.core.model.ExpenseScope
import com.enterprise.expensetracker.core.model.ItemCatalog

@Composable
fun AddExpenseDialog(
    initialExpense: Expense? = null,
    catalogSuggestions: List<ItemCatalog> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        amount: Double,
        category: Category,
        quantity: Double,
        unit: String,
        scope: ExpenseScope,
        notes: String?,
        imageUrl: String?
    ) -> Unit
) {
    var title by remember { mutableStateOf(initialExpense?.title ?: "") }
    var amountText by remember { mutableStateOf(initialExpense?.amount?.let { if (it > 0) it.toString() else "" } ?: "") }
    var quantityText by remember { mutableStateOf(initialExpense?.quantity?.toString() ?: "1") }
    var unit by remember { mutableStateOf(initialExpense?.unit ?: "kg") }
    var selectedScope by remember { mutableStateOf(initialExpense?.scope ?: ExpenseScope.FAMILY) }
    var selectedCategory by remember { mutableStateOf(initialExpense?.category ?: Category.GROCERIES_AND_FOOD) }
    var notes by remember { mutableStateOf(initialExpense?.notes ?: "") }
    var imageUrl by remember { mutableStateOf(initialExpense?.receiptUrl ?: "") }

    val context = LocalContext.current

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            val saved = saveExpenseBitmap(context, bitmap)
            if (saved != null) {
                imageUrl = saved
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val saved = saveExpenseUri(context, uri)
            if (saved != null) {
                imageUrl = saved
            }
        }
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val commonUnits = listOf("kg", "g", "litre", "ml", "packet", "piece", "dozen", "box", "bundle", "item")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackgroundDark,
        title = {
            Text(
                text = if (initialExpense != null) "Edit Expense" else "Add Household Expense",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Scope Toggle: Family Level vs Individual Level
                Text(
                    text = "Expense Scope",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMutedDark
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedScope == ExpenseScope.FAMILY) BrandBlue else SlateDark700)
                            .clickable { selectedScope = ExpenseScope.FAMILY }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Family Shared",
                            fontSize = 12.sp,
                            fontWeight = if (selectedScope == ExpenseScope.FAMILY) FontWeight.Bold else FontWeight.Normal,
                            color = TextPrimaryDark
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedScope == ExpenseScope.INDIVIDUAL) PurpleAi else SlateDark700)
                            .clickable { selectedScope = ExpenseScope.INDIVIDUAL }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Personal Only",
                            fontSize = 12.sp,
                            fontWeight = if (selectedScope == ExpenseScope.INDIVIDUAL) FontWeight.Bold else FontWeight.Normal,
                            color = TextPrimaryDark
                        )
                    }
                }

                // Global Item Catalog Autocomplete Quick Chips
                if (initialExpense == null && catalogSuggestions.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Quick Select Global Catalog Item",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = EmeraldSuccess
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            catalogSuggestions.take(8).forEach { item ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SlateDark700.copy(alpha = 0.7f))
                                        .clickable {
                                            title = item.name
                                            unit = item.defaultUnit
                                            selectedCategory = Category.fromId(item.categoryId)
                                            if (item.lastPrice > 0) amountText = item.lastPrice.toInt().toString()
                                            if (item.lastQuantity > 0) quantityText = item.lastQuantity.toString()
                                            if (!item.imageUrl.isNullOrBlank()) imageUrl = item.imageUrl
                                        }
                                        .padding(horizontal = 8.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = item.name,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimaryDark
                                    )
                                }
                            }
                        }
                    }
                }

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Item / Expense Name (e.g. Tomato, Milk, Fuel)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark,
                        focusedBorderColor = BrandBlue,
                        unfocusedBorderColor = CardBorderDark
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Quantity & Unit Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it },
                        label = { Text("Quantity") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark,
                            focusedBorderColor = BrandBlue,
                            unfocusedBorderColor = CardBorderDark
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit (kg, L, etc.)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark,
                            focusedBorderColor = BrandBlue,
                            unfocusedBorderColor = CardBorderDark
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Quick Unit Pills
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    commonUnits.forEach { u ->
                        val isSel = unit.equals(u, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) BrandBlue.copy(alpha = 0.25f) else SlateDark700.copy(alpha = 0.4f))
                                .clickable { unit = u }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = u,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) BrandBlue else TextMutedDark
                            )
                        }
                    }
                }

                // Amount Input
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Total Amount (₹)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark,
                        focusedBorderColor = BrandBlue,
                        unfocusedBorderColor = CardBorderDark
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Item Photo / Receipt (Camera capture or Gallery upload)
                Text(
                    text = "Item Photo / Receipt (Optional)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMutedDark
                )

                val currentImageBitmap = remember(imageUrl) {
                    if (imageUrl.isNotBlank()) {
                        try {
                            val f = File(imageUrl)
                            if (f.exists()) {
                                BitmapFactory.decodeFile(f.absolutePath)?.asImageBitmap()
                            } else null
                        } catch (_: Throwable) {
                            null
                        }
                    } else null
                }

                if (currentImageBitmap != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CardBackgroundDark)
                            .border(1.dp, CardBorderDark, RoundedCornerShape(12.dp))
                    ) {
                        Image(
                            bitmap = currentImageBitmap,
                            contentDescription = "Selected Item Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        IconButton(
                            onClick = { imageUrl = "" },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.65f))
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove photo",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { cameraLauncher.launch(null) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandBlue)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Capture", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandBlue)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Upload", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                // Category Selection
                Text(
                    text = "Category",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMutedDark
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Category.entries.forEach { category ->
                        val isSelected = selectedCategory == category
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) BrandBlue else SlateDark700)
                                .clickable { selectedCategory = category }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = category.displayName,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = TextPrimaryDark
                            )
                        }
                    }
                }

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Store name (Optional)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark,
                        focusedBorderColor = BrandBlue,
                        unfocusedBorderColor = CardBorderDark
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = Color(0xFFF43F5E),
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    val quantity = quantityText.toDoubleOrNull() ?: 1.0

                    if (title.isBlank()) {
                        errorMessage = "Please enter an item title."
                        return@Button
                    }
                    if (amount == null || amount <= 0.0) {
                        errorMessage = "Please enter a valid amount greater than ₹0."
                        return@Button
                    }
                    if (quantity <= 0.0) {
                        errorMessage = "Quantity must be greater than 0."
                        return@Button
                    }

                    onConfirm(
                        title.trim(),
                        amount,
                        selectedCategory,
                        quantity,
                        unit.trim().ifBlank { "item" },
                        selectedScope,
                        notes.ifBlank { null },
                        imageUrl.ifBlank { null }
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
            ) {
                Text(if (initialExpense != null) "Update" else "Add Expense", color = TextPrimaryDark)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMutedDark)
            }
        }
    )
}

private fun saveExpenseBitmap(context: Context, bitmap: Bitmap): String? {
    return try {
        val dir = File(context.filesDir, "expense_images").apply { mkdirs() }
        val file = File(dir, "item_${System.currentTimeMillis()}.jpg")
        val out = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        out.flush()
        out.close()
        file.absolutePath
    } catch (_: Throwable) {
        null
    }
}

private fun saveExpenseUri(context: Context, uri: Uri): String? {
    return try {
        val stream = context.contentResolver.openInputStream(uri) ?: return null
        val original = BitmapFactory.decodeStream(stream)
        stream.close()
        if (original == null) return null

        val maxDim = 800
        val scale = minOf(maxDim.toFloat() / original.width, maxDim.toFloat() / original.height, 1.0f)
        val scaled = if (scale < 1.0f) {
            Bitmap.createScaledBitmap(original, (original.width * scale).toInt(), (original.height * scale).toInt(), true)
        } else original

        val dir = File(context.filesDir, "expense_images").apply { mkdirs() }
        val file = File(dir, "item_${System.currentTimeMillis()}.jpg")
        val out = FileOutputStream(file)
        scaled.compress(Bitmap.CompressFormat.JPEG, 85, out)
        out.flush()
        out.close()
        file.absolutePath
    } catch (_: Throwable) {
        null
    }
}

