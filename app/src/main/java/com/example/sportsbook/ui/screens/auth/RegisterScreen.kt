package com.example.sportsbook.ui.screens.auth

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sportsbook.ui.theme.DarkBg
import com.example.sportsbook.ui.theme.DarkBorder
import com.example.sportsbook.ui.theme.DarkSurface
import com.example.sportsbook.ui.theme.DarkTextPrimary
import com.example.sportsbook.ui.theme.DarkTextSecondary
import com.example.sportsbook.ui.theme.GreenAccent
import com.example.sportsbook.ui.theme.GreenDark

// ── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedRole by remember { mutableIntStateOf(0) } // 0=Player, 1=Venue Owner, 2=Coach
    var showPassword by remember { mutableStateOf(false) }
    var phone by remember { mutableStateOf("") }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onRegisterSuccess()
    }

    // Calculate password strength (0-4)
    val passwordStrength = when {
        uiState.password.length >= 8 && uiState.password.any { it.isDigit() } && uiState.password.any { !it.isLetterOrDigit() } -> 4
        uiState.password.length >= 8 && uiState.password.any { it.isDigit() } -> 3
        uiState.password.length >= 6 -> 2
        uiState.password.isNotEmpty() -> 1
        else -> 0
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 40.dp),
        ) {
            // ── Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 56.dp, bottom = 0.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(DarkSurface)
                        .clickable(onClick = onNavigateToLogin),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DarkTextPrimary, modifier = Modifier.size(20.dp))
                }
                Text("Create Account", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
            }

            Text(
                "Join the community",
                fontSize = 14.sp,
                color = DarkTextSecondary,
                modifier = Modifier.padding(start = 24.dp, top = 4.dp, bottom = 0.dp),
            )

            // ── Role selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                listOf(
                    Triple("🏃", "Player", "Book & play"),
                    Triple("🏟", "Venue Owner", "List your venue"),
                    Triple("🏅", "Coach", "Offer sessions"),
                ).forEachIndexed { index, (emoji, name, desc) ->
                    val isActive = selectedRole == index
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isActive) GreenAccent.copy(alpha = 0.1f) else DarkSurface)
                            .border(2.dp, if (isActive) GreenAccent else DarkBorder, RoundedCornerShape(16.dp))
                            .clickable { selectedRole = index }
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(emoji, fontSize = 32.sp, modifier = Modifier.padding(bottom = 8.dp))
                        Text(name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                        Text(desc, fontSize = 11.sp, color = DarkTextSecondary, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }

            // ── Form
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // Display name (used as first name for now)
                Column {
                    Text("Display Name", fontSize = 13.sp, color = DarkTextSecondary, modifier = Modifier.padding(bottom = 6.dp))
                    AuthInputField(
                        value = uiState.displayName,
                        onValueChange = viewModel::onDisplayNameChange,
                        placeholder = "Your name",
                    )
                }

                // Email
                Column {
                    Text("Email", fontSize = 13.sp, color = DarkTextSecondary, modifier = Modifier.padding(bottom = 6.dp))
                    AuthInputField(
                        value = uiState.email,
                        onValueChange = viewModel::onEmailChange,
                        placeholder = "your@email.com",
                        keyboardType = KeyboardType.Email,
                    )
                }

                // Password
                Column {
                    Text("Password", fontSize = 13.sp, color = DarkTextSecondary, modifier = Modifier.padding(bottom = 6.dp))
                    Box {
                        AuthInputField(
                            value = uiState.password,
                            onValueChange = viewModel::onPasswordChange,
                            placeholder = "Min. 8 characters",
                            keyboardType = KeyboardType.Password,
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        )
                        Text(
                            text = if (showPassword) "🙈" else "👁",
                            fontSize = 16.sp,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 16.dp)
                                .clickable { showPassword = !showPassword },
                        )
                    }
                    // Password strength bars
                    if (uiState.password.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            for (i in 1..4) {
                                val barColor = when {
                                    i > passwordStrength -> Color(0xFF333333)
                                    passwordStrength <= 2 -> Color(0xFFFF9800)
                                    else -> GreenAccent
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(3.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(barColor),
                                )
                            }
                        }
                    }
                }

                // Phone (optional)
                Column {
                    Text("Phone (optional)", fontSize = 13.sp, color = DarkTextSecondary, modifier = Modifier.padding(bottom = 6.dp))
                    AuthInputField(
                        value = phone,
                        onValueChange = { phone = it },
                        placeholder = "+1 (555) 000-0000",
                        keyboardType = KeyboardType.Phone,
                    )
                }
            }

            // ── Error
            if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    fontSize = 13.sp,
                    color = Color(0xFFEF5350),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                )
            }

            // ── Terms
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = DarkTextSecondary, fontSize = 12.sp)) {
                        append("By creating an account, you agree to our ")
                    }
                    withStyle(SpanStyle(color = GreenAccent, fontSize = 12.sp)) {
                        append("Terms of Service")
                    }
                    withStyle(SpanStyle(color = DarkTextSecondary, fontSize = 12.sp)) {
                        append(" and ")
                    }
                    withStyle(SpanStyle(color = GreenAccent, fontSize = 12.sp)) {
                        append("Privacy Policy")
                    }
                },
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            )

            // ── Create account button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(listOf(GreenAccent, GreenDark)))
                    .clickable(enabled = !uiState.isLoading) { viewModel.signUp() }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                } else {
                    Text("Create Account", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            // ── Divider
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.weight(1f).height(1.dp).background(Color(0xFF333333)))
                Text("or", fontSize = 13.sp, color = Color(0xFF555555), modifier = Modifier.padding(horizontal = 16.dp))
                Box(modifier = Modifier.weight(1f).height(1.dp).background(Color(0xFF333333)))
            }

            // ── Social buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurface)
                        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                        .clickable { }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("G", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                        Text("Google", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary)
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurface)
                        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                        .clickable { }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("", fontSize = 14.sp, color = DarkTextPrimary)
                        Text("Apple", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary)
                    }
                }
            }

            // ── Login link
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Already have an account?", fontSize = 14.sp, color = DarkTextSecondary)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "Log in",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GreenAccent,
                    modifier = Modifier.clickable(onClick = onNavigateToLogin),
                )
            }
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun RegisterScreenPreview() {
    RegisterScreen(onNavigateToLogin = {}, onRegisterSuccess = {})
}
