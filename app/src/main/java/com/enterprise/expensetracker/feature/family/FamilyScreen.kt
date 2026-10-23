package com.enterprise.expensetracker.feature.family

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import com.enterprise.expensetracker.core.designsystem.components.UserAvatarView
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.enterprise.expensetracker.core.designsystem.components.ExpenseTopBar
import com.enterprise.expensetracker.core.designsystem.theme.AmberWarning
import com.enterprise.expensetracker.core.designsystem.theme.BrandBlue
import com.enterprise.expensetracker.core.designsystem.theme.CardBackgroundDark
import com.enterprise.expensetracker.core.designsystem.theme.CardBorderDark
import com.enterprise.expensetracker.core.designsystem.theme.EmeraldSuccess
import com.enterprise.expensetracker.core.designsystem.theme.PurpleAi
import com.enterprise.expensetracker.core.designsystem.theme.RoseAlert
import com.enterprise.expensetracker.core.designsystem.theme.SlateDark700
import com.enterprise.expensetracker.core.designsystem.theme.SlateDark800
import com.enterprise.expensetracker.core.designsystem.theme.TextMutedDark
import com.enterprise.expensetracker.core.designsystem.theme.TextPrimaryDark
import com.enterprise.expensetracker.core.model.FamilyRelationship
import com.enterprise.expensetracker.core.model.UserProfile
import com.enterprise.expensetracker.core.model.UserRole

@Composable
fun FamilyScreen(
    viewModel: FamilyViewModel,
    onNavigateToAuth: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showSignOutConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is FamilyEffect.NavigateToAuth -> onNavigateToAuth()
                is FamilyEffect.ShowToast -> {}
            }
        }
    }

    val isHead = state.currentUser?.isHeadOfHousehold == true

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ExpenseTopBar(
            title = if (isHead) "Family Management" else "My Family Profile",
            subtitle = if (isHead) "Manage household members & permissions" else "Household member profile",
            aiProviderName = null,
            onRefreshClick = { viewModel.dispatchEvent(FamilyEvent.Refresh) }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Active User Profile Banner
            item {
                state.currentUser?.let { user ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(CardBackgroundDark, if (isHead) PurpleAi.copy(alpha = 0.12f) else BrandBlue.copy(alpha = 0.12f))
                                )
                            )
                            .border(
                                1.dp,
                                if (isHead) PurpleAi.copy(alpha = 0.35f) else BrandBlue.copy(alpha = 0.35f),
                                RoundedCornerShape(20.dp)
                            )
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    UserAvatarView(
                                        name = user.name,
                                        avatarUrl = user.avatarUrl,
                                        isHeadOfHousehold = isHead,
                                        size = 46.dp
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = user.name,
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimaryDark
                                        )
                                        Text(
                                            text = user.email,
                                            fontSize = 12.sp,
                                            color = TextMutedDark
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { viewModel.dispatchEvent(FamilyEvent.OpenEditProfileDialog) }) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Profile",
                                            tint = TextMutedDark
                                        )
                                    }
                                    IconButton(onClick = { showSignOutConfirm = true }) {
                                        Icon(
                                            imageVector = Icons.Default.Logout,
                                            contentDescription = "Sign Out",
                                            tint = RoseAlert
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isHead) PurpleAi.copy(alpha = 0.25f) else BrandBlue.copy(alpha = 0.25f))
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = if (isHead) "👑 Head of Household (Full Access)" else "👤 Family Member",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isHead) PurpleAi else BrandBlue
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SlateDark700)
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = user.relationship.displayName,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimaryDark
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.dispatchEvent(FamilyEvent.OpenEditProfileDialog) },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Edit Profile", fontSize = 12.sp)
                                }
                                OutlinedButton(
                                    onClick = { showSignOutConfirm = true },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseAlert),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(14.dp), tint = RoseAlert)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Sign Out", fontSize = 12.sp, color = RoseAlert, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Head of Household: Family Members Section
            if (isHead) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Household Members",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                            Text(
                                text = "${state.familyMembers.size} registered family members",
                                fontSize = 12.sp,
                                color = TextMutedDark
                            )
                        }

                        Button(
                            onClick = { viewModel.dispatchEvent(FamilyEvent.OpenAddMemberDialog) },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Member", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                items(state.familyMembers, key = { it.id }) { member ->
                    FamilyMemberCard(
                        member = member,
                        isCurrentUser = member.id == state.currentUser?.id,
                        expenseCount = state.memberExpenseCounts[member.id] ?: 0,
                        onDelete = { viewModel.dispatchEvent(FamilyEvent.DeleteMember(member.id)) }
                    )
                }
            } else {
                // Family Member Notice Card
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(CardBackgroundDark)
                            .border(1.dp, CardBorderDark, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = AmberWarning,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Family Member Access",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryDark
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "As a family member, you have personal access to record and edit your own home expenses and manage your profile. Overall household settings and family member rosters are managed by your Head of Household.",
                                    fontSize = 12.sp,
                                    color = TextMutedDark,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            // Sign Out / Switch User Action
            item {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { viewModel.dispatchEvent(FamilyEvent.SignOut) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseAlert),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(RoseAlert.copy(alpha = 0.5f), RoseAlert))),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp), tint = RoseAlert)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign Out / Switch Account", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = RoseAlert)
                }
            }
        }
    }

    // Add Family Member Dialog
    if (state.isAddMemberDialogOpen) {
        AlertDialog(
            onDismissRequest = { viewModel.dispatchEvent(FamilyEvent.CloseAddMemberDialog) },
            containerColor = CardBackgroundDark,
            title = {
                Text(
                    text = "Add Family Member",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Invite a spouse, child, or family relative to log their household expenses:",
                        fontSize = 12.sp,
                        color = TextMutedDark
                    )

                    OutlinedTextField(
                        value = state.newMemberName,
                        onValueChange = { viewModel.dispatchEvent(FamilyEvent.NewMemberNameChanged(it)) },
                        label = { Text("Member Full Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark,
                            focusedBorderColor = BrandBlue,
                            unfocusedBorderColor = CardBorderDark
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = state.newMemberEmail,
                        onValueChange = { viewModel.dispatchEvent(FamilyEvent.NewMemberEmailChanged(it)) },
                        label = { Text("Member Email Address") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark,
                            focusedBorderColor = BrandBlue,
                            unfocusedBorderColor = CardBorderDark
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Relationship",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextMutedDark
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            FamilyRelationship.SPOUSE,
                            FamilyRelationship.CHILD,
                            FamilyRelationship.PARENT,
                            FamilyRelationship.SIBLING
                        ).forEach { rel ->
                            val isSelected = state.newMemberRelationship == rel
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) BrandBlue else SlateDark700)
                                    .clickable { viewModel.dispatchEvent(FamilyEvent.NewMemberRelationshipChanged(rel)) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = rel.name.lowercase().replaceFirstChar { it.uppercase() },
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) TextPrimaryDark else TextMutedDark
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dispatchEvent(FamilyEvent.SubmitAddMember) },
                    enabled = state.newMemberName.isNotBlank() && state.newMemberEmail.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                ) {
                    Text("Add Member", color = TextPrimaryDark)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dispatchEvent(FamilyEvent.CloseAddMemberDialog) }) {
                    Text("Cancel", color = TextMutedDark)
                }
            }
        )
    }

    // Edit Profile Dialog
    if (state.isEditProfileDialogOpen) {
        AlertDialog(
            onDismissRequest = { viewModel.dispatchEvent(FamilyEvent.CloseEditProfileDialog) },
            containerColor = CardBackgroundDark,
            title = {
                Text(
                    text = "Edit Profile",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = state.editNameText,
                        onValueChange = { viewModel.dispatchEvent(FamilyEvent.EditNameChanged(it)) },
                        label = { Text("Display Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark,
                            focusedBorderColor = BrandBlue,
                            unfocusedBorderColor = CardBorderDark
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Relationship",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextMutedDark
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            FamilyRelationship.HEAD,
                            FamilyRelationship.SPOUSE,
                            FamilyRelationship.CHILD,
                            FamilyRelationship.PARENT
                        ).forEach { rel ->
                            val isSelected = state.editRelationship == rel
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) BrandBlue else SlateDark700)
                                    .clickable { viewModel.dispatchEvent(FamilyEvent.EditRelationshipChanged(rel)) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = rel.name.lowercase().replaceFirstChar { it.uppercase() },
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) TextPrimaryDark else TextMutedDark
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dispatchEvent(FamilyEvent.SubmitEditProfile) },
                    enabled = state.editNameText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                ) {
                    Text("Save Changes", color = TextPrimaryDark)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dispatchEvent(FamilyEvent.CloseEditProfileDialog) }) {
                    Text("Cancel", color = TextMutedDark)
                }
            }
        )
    }

    if (showSignOutConfirm) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirm = false },
            containerColor = CardBackgroundDark,
            title = {
                Text(
                    text = "Sign Out of Account?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to sign out from this device? You can log back in at any time.",
                    fontSize = 14.sp,
                    color = TextMutedDark
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutConfirm = false
                        viewModel.dispatchEvent(FamilyEvent.SignOut)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseAlert),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Sign Out", color = TextPrimaryDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutConfirm = false }) {
                    Text("Cancel", color = TextMutedDark)
                }
            }
        )
    }
}

@Composable
private fun FamilyMemberCard(
    member: UserProfile,
    isCurrentUser: Boolean,
    expenseCount: Int,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackgroundDark)
            .border(1.dp, CardBorderDark, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                UserAvatarView(
                    name = member.name,
                    avatarUrl = member.avatarUrl,
                    isHeadOfHousehold = member.isHeadOfHousehold,
                    size = 42.dp
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = member.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                        if (isCurrentUser) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "(You)",
                                fontSize = 11.sp,
                                color = BrandBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${member.relationship.displayName} • $expenseCount expenses recorded",
                        fontSize = 12.sp,
                        color = TextMutedDark
                    )

                    Text(
                        text = member.email,
                        fontSize = 11.sp,
                        color = SlateDark700.copy(alpha = 0.9f)
                    )
                }
            }

            if (!member.isHeadOfHousehold) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove Member",
                        tint = RoseAlert.copy(alpha = 0.8f)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(PurpleAi.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "👑 Head",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PurpleAi
                    )
                }
            }
        }
    }
}
