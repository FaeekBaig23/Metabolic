package com.faiqbaig.metabolic.feature.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.faiqbaig.metabolic.core.ui.theme.*

@Composable
fun LoginScreen(
    onLoginSuccess  : () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel       : AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    var email     by remember { mutableStateOf("") }
    var password  by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val isLoading = authState is AuthState.Loading

    // Navigate on success
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            viewModel.resetState()
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkBackground, Color(0xFF0D2A20))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(80.dp))

            // ── Header ───────────────────────────────────────────
            Text(
                text       = "Welcome Back",
                fontSize   = 32.sp,
                fontWeight = FontWeight.Bold,
                color      = DarkTextPrimary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text      = "Sign in to continue your journey",
                fontSize  = 15.sp,
                color     = DarkTextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(48.dp))

            // ── Email field ───────────────────────────────────────
            OutlinedTextField(
                value         = email,
                onValueChange = { email = it },
                label         = { Text("Email") },
                leadingIcon   = {
                    Icon(Icons.Default.Email, contentDescription = null,
                        tint = MetabolicGreen)
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction    = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true,
                modifier   = Modifier.fillMaxWidth(),
                shape      = RoundedCornerShape(14.dp),
                colors     = metabolicTextFieldColors()
            )

            Spacer(Modifier.height(16.dp))

            // ── Password field ────────────────────────────────────
            OutlinedTextField(
                value         = password,
                onValueChange = { password = it },
                label         = { Text("Password") },
                leadingIcon   = {
                    Icon(Icons.Default.Lock, contentDescription = null,
                        tint = MetabolicGreen)
                },
                trailingIcon  = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible)
                                Icons.Default.Visibility
                            else
                                Icons.Default.VisibilityOff,
                            contentDescription = "Toggle password",
                            tint = DarkTextSecondary
                        )
                    }
                },
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction    = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        viewModel.login(email, password)
                    }
                ),
                singleLine = true,
                modifier   = Modifier.fillMaxWidth(),
                shape      = RoundedCornerShape(14.dp),
                colors     = metabolicTextFieldColors()
            )

            Spacer(Modifier.height(8.dp))

            // ── Error message ─────────────────────────────────────
            AnimatedVisibility(visible = authState is AuthState.Error) {
                Text(
                    text      = (authState as? AuthState.Error)?.message ?: "",
                    color     = SemanticError,
                    fontSize  = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier  = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Login button ──────────────────────────────────────
            Button(
                onClick  = {
                    focusManager.clearFocus()
                    viewModel.login(email, password)
                },
                enabled  = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape  = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MetabolicGreen,
                    contentColor   = DarkBackground
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color    = DarkBackground,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text       = "Sign In",
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Divider ───────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color    = DarkBorder
                )
                Text(
                    text     = "  or  ",
                    color    = DarkTextSecondary,
                    fontSize = 13.sp
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color    = DarkBorder
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Google Sign-In button ─────────────────────────────
            OutlinedButton(
                onClick  = { /* wired in next step */ },
                enabled  = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape  = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, DarkBorder
                )
            ) {
                Text(
                    text       = "Continue with Google",
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color      = DarkTextPrimary
                )
            }

            Spacer(Modifier.height(32.dp))

            // ── Navigate to Register ──────────────────────────────
            Row(horizontalArrangement = Arrangement.Center) {
                Text(
                    text     = "Don't have an account? ",
                    color    = DarkTextSecondary,
                    fontSize = 14.sp
                )
                Text(
                    text      = "Sign Up",
                    color     = MetabolicGreen,
                    fontSize  = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier  = Modifier.clickable { onNavigateToRegister() }
                )
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

// ── Shared text field colors ──────────────────────────────────
@Composable
fun metabolicTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor   = MetabolicGreen,
    unfocusedBorderColor = DarkBorder,
    focusedLabelColor    = MetabolicGreen,
    unfocusedLabelColor  = DarkTextSecondary,
    cursorColor          = MetabolicGreen,
    focusedTextColor     = DarkTextPrimary,
    unfocusedTextColor   = DarkTextPrimary
)