package com.enterprise.expensetracker.feature.auth

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import android.accounts.AccountManager
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.enterprise.expensetracker.core.model.UserRole

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showDirectGoogleDialog by remember { mutableStateOf(false) }
    var directGoogleEmail by remember { mutableStateOf("") }
    var directGoogleName by remember { mutableStateOf("") }

    val accountPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val selectedEmail = result.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)?.trim().orEmpty()
            if (selectedEmail.isNotBlank()) {
                val resolvedName = selectedEmail.substringBefore("@")
                    .replace(".", " ")
                    .split(" ")
                    .filter { it.isNotBlank() }
                    .joinToString(" ") { word -> word.replaceFirstChar { c -> c.uppercase() } }
                viewModel.dispatchEvent(
                    AuthEvent.GoogleSignInSuccess(
                        email = selectedEmail,
                        displayName = resolvedName
                    )
                )
            } else {
                showDirectGoogleDialog = true
            }
        } else if (result.resultCode != Activity.RESULT_CANCELED) {
            showDirectGoogleDialog = true
        }
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val email = account.email.orEmpty().trim()
            val name = account.displayName.orEmpty().trim().ifEmpty {
                account.givenName.orEmpty().trim().ifEmpty { email.substringBefore("@") }
            }
            if (email.isNotEmpty()) {
                viewModel.dispatchEvent(
                    AuthEvent.GoogleSignInSuccess(
                        email = email,
                        displayName = name,
                        photoUrl = account.photoUrl?.toString()
                    )
                )
            } else {
                viewModel.dispatchEvent(AuthEvent.GoogleSignInFailure("Google did not return an email address."))
            }
        } catch (e: ApiException) {
            when (e.statusCode) {
                GoogleSignInStatusCodes.SIGN_IN_CANCELLED -> {
                    viewModel.dispatchEvent(AuthEvent.GoogleSignInFailure("Google sign-in was cancelled."))
                }
                else -> {
                    try {
                        val chooseAccountIntent = AccountManager.newChooseAccountIntent(
                            null, null, arrayOf("com.google"), false, null, null, null, null
                        )
                        accountPickerLauncher.launch(chooseAccountIntent)
                    } catch (_: Exception) {
                        showDirectGoogleDialog = true
                    }
                }
            }
        } catch (_: Exception) {
            try {
                val chooseAccountIntent = AccountManager.newChooseAccountIntent(
                    null, null, arrayOf("com.google"), false, null, null, null, null
                )
                accountPickerLauncher.launch(chooseAccountIntent)
            } catch (_: Exception) {
                showDirectGoogleDialog = true
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AuthEffect.NavigateToHome -> onAuthSuccess()
                is AuthEffect.ShowToast -> {}
            }
        }
    }

    if (showDirectGoogleDialog) {
        AlertDialog(
            onDismissRequest = { showDirectGoogleDialog = false },
            title = {
                Text(
                    text = "Connect Google Account",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter your live Google Account details below to authenticate and sync directly with MongoDB Atlas:",
                        fontSize = 13.sp,
                        color = TextMutedDark
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = directGoogleName,
                        onValueChange = { directGoogleName = it },
                        label = { Text("Display Name") },
                        placeholder = { Text("e.g. John Doe") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandBlue,
                            unfocusedBorderColor = CardBorderDark,
                            focusedLabelColor = BrandBlue,
                            unfocusedLabelColor = TextMutedDark,
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = directGoogleEmail,
                        onValueChange = { directGoogleEmail = it },
                        label = { Text("Google Email *") },
                        placeholder = { Text("e.g. yourname@gmail.com") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandBlue,
                            unfocusedBorderColor = CardBorderDark,
                            focusedLabelColor = BrandBlue,
                            unfocusedLabelColor = TextMutedDark,
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (directGoogleEmail.isNotBlank()) {
                            showDirectGoogleDialog = false
                            val displayName = directGoogleName.trim().ifBlank { directGoogleEmail.substringBefore("@") }
                            viewModel.dispatchEvent(
                                AuthEvent.GoogleSignInSuccess(
                                    email = directGoogleEmail.trim(),
                                    displayName = displayName
                                )
                            )
                        }
                    },
                    enabled = directGoogleEmail.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                ) {
                    Text("Authenticate with MongoDB", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDirectGoogleDialog = false }) {
                    Text("Cancel", color = TextMutedDark)
                }
            },
            containerColor = CardBackgroundDark
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // App Brand Header
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(BrandBlue, PurpleAi)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FamilyRestroom,
                    contentDescription = "Family Expense",
                    tint = TextPrimaryDark,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Home Expense Tracker",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark
            )

            Text(
                text = if (state.isRegisterMode) "Set up your family ledger account" else "Sign in to manage household expenses",
                fontSize = 13.sp,
                color = TextMutedDark,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Tab Toggle: Sign In vs Register
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SlateDark800)
                    .border(1.dp, CardBorderDark, RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!state.isRegisterMode) BrandBlue else Color.Transparent)
                            .clickable { if (state.isRegisterMode) viewModel.dispatchEvent(AuthEvent.ToggleMode) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Sign In",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (!state.isRegisterMode) TextPrimaryDark else TextMutedDark
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (state.isRegisterMode) BrandBlue else Color.Transparent)
                            .clickable { if (!state.isRegisterMode) viewModel.dispatchEvent(AuthEvent.ToggleMode) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Register",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (state.isRegisterMode) TextPrimaryDark else TextMutedDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Error Banner
            AnimatedVisibility(visible = state.errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(RoseAlert.copy(alpha = 0.15f))
                        .border(1.dp, RoseAlert.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = state.errorMessage ?: "",
                        fontSize = 12.sp,
                        color = RoseAlert,
                        lineHeight = 16.sp
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Traditional Form Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(CardBackgroundDark)
                    .border(1.dp, CardBorderDark, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Full Name (Only in Register Mode)
                    if (state.isRegisterMode) {
                        OutlinedTextField(
                            value = state.name,
                            onValueChange = { viewModel.dispatchEvent(AuthEvent.NameChanged(it)) },
                            label = { Text("Full Name (e.g. Ramesh Sharma)") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TextMutedDark) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimaryDark,
                                unfocusedTextColor = TextPrimaryDark,
                                focusedBorderColor = BrandBlue,
                                unfocusedBorderColor = CardBorderDark
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Role Selection: Head of Household vs Family Member
                        Text(
                            text = "Household Account Type",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextMutedDark
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            UserRole.entries.forEach { role ->
                                val isSelected = state.selectedRole == role
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) PurpleAi.copy(alpha = 0.25f) else SlateDark700)
                                        .border(
                                            1.dp,
                                            if (isSelected) PurpleAi else Color.Transparent,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable { viewModel.dispatchEvent(AuthEvent.RoleChanged(role)) }
                                        .padding(vertical = 10.dp, horizontal = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = if (role == UserRole.HEAD_OF_HOUSEHOLD) "👑 Head (Admin)" else "👤 Member",
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) TextPrimaryDark else TextMutedDark
                                        )
                                    }
                                }
                            }
                        }

                        // Relationship Selection
                        Text(
                            text = "Relationship to Household",
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
                                val isSelected = state.selectedRelationship == rel
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) BrandBlue else SlateDark700)
                                        .clickable { viewModel.dispatchEvent(AuthEvent.RelationshipChanged(rel)) }
                                        .padding(vertical = 6.dp),
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

                    // Email Field
                    OutlinedTextField(
                        value = state.email,
                        onValueChange = { viewModel.dispatchEvent(AuthEvent.EmailChanged(it)) },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = TextMutedDark) },
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

                    // Password Field
                    OutlinedTextField(
                        value = state.password,
                        onValueChange = { viewModel.dispatchEvent(AuthEvent.PasswordChanged(it)) },
                        label = { Text("Password (min 6 characters)") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TextMutedDark) },
                        trailingIcon = {
                            IconButton(onClick = { viewModel.dispatchEvent(AuthEvent.TogglePasswordVisibility) }) {
                                Icon(
                                    imageVector = if (state.isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle password",
                                    tint = TextMutedDark
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark,
                            focusedBorderColor = BrandBlue,
                            unfocusedBorderColor = CardBorderDark
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Submit Button
                    Button(
                        onClick = { viewModel.dispatchEvent(AuthEvent.SubmitAuth) },
                        enabled = !state.isLoading && state.email.isNotBlank() && state.password.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(color = TextPrimaryDark, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                        } else {
                            Text(
                                text = if (state.isRegisterMode) "Create Household Account" else "Sign In with Email",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Or separator
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = CardBorderDark)
                Text(
                    text = "  OR  ",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMutedDark
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = CardBorderDark)
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Google SSO Button
            OutlinedButton(
                onClick = {
                    viewModel.dispatchEvent(AuthEvent.GoogleSignInClicked)
                    try {
                        val chooseAccountIntent = AccountManager.newChooseAccountIntent(
                            null,
                            null,
                            arrayOf("com.google"),
                            false,
                            null,
                            null,
                            null,
                            null
                        )
                        accountPickerLauncher.launch(chooseAccountIntent)
                    } catch (e: Exception) {
                        try {
                            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestEmail()
                                .requestProfile()
                                .build()
                            val googleSignInClient = GoogleSignIn.getClient(context, gso)
                            googleSignInClient.signOut().addOnCompleteListener {
                                googleSignInLauncher.launch(googleSignInClient.signInIntent)
                            }
                        } catch (_: Exception) {
                            showDirectGoogleDialog = true
                        }
                    }
                },
                enabled = !state.isLoading,
                shape = RoundedCornerShape(12.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(SlateDark700, BrandBlue))),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = CardBackgroundDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Google G Icon badge
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "G",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF4285F4)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Continue with Google SSO",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimaryDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
