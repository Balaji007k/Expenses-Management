package com.enterprise.expensetracker.core.designsystem.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.RemoveShoppingCart
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.enterprise.expensetracker.core.designsystem.theme.AmberWarning
import com.enterprise.expensetracker.core.designsystem.theme.BrandBlue
import com.enterprise.expensetracker.core.designsystem.theme.CardBackgroundDark
import com.enterprise.expensetracker.core.designsystem.theme.CardBorderDark
import com.enterprise.expensetracker.core.designsystem.theme.EmeraldSuccess
import com.enterprise.expensetracker.core.designsystem.theme.PurpleAi
import com.enterprise.expensetracker.core.designsystem.theme.RoseAlert
import com.enterprise.expensetracker.core.designsystem.theme.SlateDark700
import com.enterprise.expensetracker.core.designsystem.theme.TextMutedDark
import com.enterprise.expensetracker.core.designsystem.theme.TextPrimaryDark
import com.enterprise.expensetracker.core.model.Category
import com.enterprise.expensetracker.core.model.Expense
import com.enterprise.expensetracker.core.model.ExpenseScope
import com.enterprise.expensetracker.core.model.StockStatus
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExpenseListItem(
    expense: Expense,
    canDelete: Boolean = false,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onStockEnded: (() -> Unit)? = null,
    onRestock: (() -> Unit)? = null,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val dateTimeFormat = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
    val formattedDateTime = dateTimeFormat.format(Date(expense.timestamp))

    val categoryColor = runCatching {
        Color(android.graphics.Color.parseColor(expense.category.defaultColorHex))
    }.getOrDefault(Color.Gray)

    val categoryIcon = getCategoryIcon(expense.category)

    val itemBitmap = androidx.compose.runtime.remember(expense.receiptUrl) {
        if (!expense.receiptUrl.isNullOrBlank()) {
            try {
                val f = File(expense.receiptUrl)
                if (f.exists()) {
                    BitmapFactory.decodeFile(f.absolutePath)?.asImageBitmap()
                } else null
            } catch (_: Throwable) {
                null
            }
        } else null
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackgroundDark)
            .border(1.dp, CardBorderDark, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Main Row: Icon, Title, Quantity, Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Category Icon / Item Photo
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(categoryColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (itemBitmap != null) {
                            Image(
                                bitmap = itemBitmap,
                                contentDescription = expense.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = categoryIcon,
                                contentDescription = expense.category.displayName,
                                tint = categoryColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = expense.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // Scope Badge (Family vs Personal)
                            val isFamily = expense.scope == ExpenseScope.FAMILY
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isFamily) BrandBlue.copy(alpha = 0.15f) else PurpleAi.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isFamily) "Family" else "Personal",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isFamily) BrandBlue else PurpleAi
                                )
                            }
                        }

                        // Quantity & Category
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val qtyText = if (expense.quantity % 1.0 == 0.0) "${expense.quantity.toInt()} ${expense.unit}" else "${expense.quantity} ${expense.unit}"
                            Text(
                                text = qtyText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = EmeraldSuccess
                            )
                            Text(text = "•", fontSize = 12.sp, color = TextMutedDark)
                            Text(
                                text = expense.category.displayName,
                                fontSize = 12.sp,
                                color = TextMutedDark
                            )
                        }
                    }
                }

                // Amount
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹%,.2f".format(expense.amount),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                    if (expense.quantity > 0) {
                        val unitPrice = expense.amount / expense.quantity
                        Text(
                            text = "₹%,.1f / %s".format(unitPrice, expense.unit),
                            fontSize = 11.sp,
                            color = TextMutedDark
                        )
                    }
                }
            }

            // Sub-Row: When added & Creator info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Added: $formattedDateTime",
                    fontSize = 11.sp,
                    color = TextMutedDark
                )

                if (expense.createdByUserName.isNotBlank()) {
                    Text(
                        text = "by ${expense.createdByUserName} (${expense.createdByRelationship})",
                        fontSize = 11.sp,
                        color = BrandBlue,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Inventory Stock & Restock Status Row + Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SlateDark700.copy(alpha = 0.35f))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stock Status Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    when (expense.stockStatus) {
                        StockStatus.IN_STOCK -> {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(13.dp))
                            Text("In Stock", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = EmeraldSuccess)
                        }
                        StockStatus.STOCK_ENDED -> {
                            Icon(Icons.Default.RemoveShoppingCart, contentDescription = null, tint = RoseAlert, modifier = Modifier.size(13.dp))
                            Text("Stock Ended", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = RoseAlert)
                        }
                        StockStatus.RESTOCKED -> {
                            Icon(Icons.Default.Replay, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(13.dp))
                            val reasonText = if (expense.restockReason?.name == "IN_BETWEEN") {
                                "Restocked (Left: ${expense.quantityRemainingBeforeRestock ?: 0} ${expense.unit})"
                            } else "Restocked (Emptied)"
                            Text(reasonText, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = BrandBlue)
                        }
                    }
                }

                // Action Buttons: Stock Ended, Restock, Edit (all members), Delete (Head only)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Update Stock Actions (All Members can update)
                    if (onStockEnded != null && expense.stockStatus != StockStatus.STOCK_ENDED) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(RoseAlert.copy(alpha = 0.15f))
                                .clickable { onStockEnded() }
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text("Empty", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = RoseAlert)
                        }
                    }

                    if (onRestock != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(EmeraldSuccess.copy(alpha = 0.15f))
                                .clickable { onRestock() }
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text("Restock", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                        }
                    }

                    // Edit button (All Members have access)
                    if (onEdit != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(BrandBlue.copy(alpha = 0.15f))
                                .clickable { onEdit() }
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = BrandBlue, modifier = Modifier.size(12.dp))
                        }
                    }

                    // Delete button (Strictly Family Head Only)
                    if (canDelete && onDelete != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(RoseAlert.copy(alpha = 0.15f))
                                .clickable { onDelete() }
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete (Head Only)", tint = RoseAlert, modifier = Modifier.size(12.dp))
                        }
                    }
                }
            }
        }
    }
}

private fun getCategoryIcon(category: Category): ImageVector {
    return when (category) {
        Category.GROCERIES_AND_FOOD -> Icons.Default.ShoppingBasket
        Category.RENT_AND_HOUSING -> Icons.Default.Home
        Category.UTILITIES_AND_BILLS -> Icons.Default.Bolt
        Category.INTERNET_AND_MOBILE -> Icons.Default.Wifi
        Category.KIDS_AND_EDUCATION -> Icons.Default.School
        Category.HEALTH_AND_MEDICINE -> Icons.Default.LocalHospital
        Category.HOME_MAINTENANCE -> Icons.Default.Build
        Category.SHOPPING_AND_CLOTHING -> Icons.Default.ShoppingBag
        Category.TRAVEL_AND_FUEL -> Icons.Default.DirectionsCar
        Category.DINING_AND_OUTING -> Icons.Default.Restaurant
        Category.MISCELLANEOUS -> Icons.Default.Category
    }
}
