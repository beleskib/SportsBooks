package com.example.sportsbook.ui.screens.auth

import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showPassword by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onLoginSuccess()
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
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // ── Top section with logo
            Column(
                modifier = Modifier.padding(top = 80.dp, bottom = 40.dp, start = 24.dp, end = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(GreenAccent, GreenDark))),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("⚽", fontSize = 36.sp)
                }
                Text(
                    "SportsBooks",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GreenAccent,
                    modifier = Modifier.padding(top = 16.dp),
                )
                Text(
                    "Welcome back",
                    fontSize = 14.sp,
                    color = DarkTextSecondary,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            // ── Form
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
            ) {
                // Email
                Text("Email", fontSize = 13.sp, color = DarkTextSecondary, modifier = Modifier.padding(bottom = 6.dp))
                AuthInputField(
                    value = uiState.email,
                    onValueChange = viewModel::onEmailChange,
                    placeholder = "your@email.com",
                    keyboardType = KeyboardType.Email,
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Password
                Text("Password", fontSize = 13.sp, color = DarkTextSecondary, modifier = Modifier.padding(bottom = 6.dp))
                Box {
                    AuthInputField(
                        value = uiState.password,
                        onValueChange = viewModel::onPasswordChange,
                        placeholder = "Enter password",
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

                // Forgot password
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Text(
                        "Forgot password?",
                        fontSize = 13.sp,
                        color = GreenAccent,
                        modifier = Modifier.clickable { },
                    )
                }

                // Error
                if (uiState.error != null) {
                    Text(
                        text = uiState.error!!,
                        fontSize = 13.sp,
                        color = Color(0xFFEF5350),
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }

                // Login button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.linearGradient(listOf(GreenAccent, GreenDark)))
                        .clickable(enabled = !uiState.isLoading) { viewModel.signIn() }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Log In", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            // ── Divider
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.weight(1f).height(1.dp).background(Color(0xFF333333)))
                Text("or continue with", fontSize = 13.sp, color = Color(0xFF555555), modifier = Modifier.padding(horizontal = 16.dp))
                Box(modifier = Modifier.weight(1f).height(1.dp).background(Color(0xFF333333)))
            }

            // ── Social buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SocialButton(emoji = "G", label = "Continue with Google", borderColor = Color(0xFF4285F4), onClick = {})
                SocialButton(emoji = "", label = "Continue with Apple", borderColor = Color.White, onClick = {})
            }

            // ── Sign up link
            Row(
                modifier = Modifier.padding(top = 32.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Don't have an account?", fontSize = 14.sp, color = DarkTextSecondary)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "Sign up",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GreenAccent,
                    modifier = Modifier.clickable(onClick = onNavigateToRegister),
                )
            }
        }
    }
}

// ── Reusable input field ──────────────────────────────────────────────────────

@Composable
fun AuthInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(color = DarkTextPrimary, fontSize = 15.sp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = visualTransformation,
        cursorBrush = SolidColor(GreenAccent),
        modifier = Modifier.fillMaxWidth(),
        decorationBox = { inner ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                if (value.isEmpty()) {
                    Text(placeholder, fontSize = 15.sp, color = Color(0xFF555555))
                }
                inner()
            }
        },
    )
}

// ── Social button ─────────────────────────────────────────────────────────────

@Composable
private fun SocialButton(emoji: String, label: String, borderColor: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(emoji.ifEmpty { "" }, fontSize = 20.sp, color = DarkTextPrimary)
            Text(label, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary)
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun LoginScreenPreview() {
    LoginScreen(onNavigateToRegister = {}, onLoginSuccess = {})
}
