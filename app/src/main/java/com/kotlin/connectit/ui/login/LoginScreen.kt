package com.kotlin.connectit.ui.login

import MainTextField
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kotlin.connectit.R
import com.kotlin.connectit.util.ResultWrapper
import com.kotlin.connectit_fe.ui.components.CustomButton

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onSuccessfulLogin: () -> Unit,
    onRegisterClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(key1 = uiState.loginResult) {
        when (val result = uiState.loginResult) {
            is ResultWrapper.Success -> {
                Toast.makeText(context, "Login Berhasil!", Toast.LENGTH_SHORT).show()
                viewModel.consumeLoginResult()
                onSuccessfulLogin()
            }
            is ResultWrapper.Error -> {
                Toast.makeText(context, result.message ?: "Login Gagal (Kode: ${result.code})", Toast.LENGTH_LONG).show()
                viewModel.consumeLoginResult() // Reset state
            }
            null -> { /* Initial state atau sudah dikonsumsi */ }
        }
    }

    // Handle general error messages (e.g., validation)
    LaunchedEffect(key1 = uiState.errorMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.consumeErrorMessage() // Reset state
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF191A1F))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.bubble_login), // Pastikan drawable ini ada
                    contentDescription = "Background Bubbles",
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(.9f)
                        .padding(end = 16.dp, bottom = 16.dp) // Kurangi padding bawah agar gambar lebih terlihat
                )

                Text(
                    text = "Login",
                    fontSize = 39.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(bottom = 30.dp) // Naikkan teks Login sedikit
                )
            }

            Column(
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                MainTextField(
                    label = "Email",
                    value = uiState.email,
                    onValueChange = { viewModel.onEmailChanged(it) }
                )

                MainTextField(
                    label = "Password",
                    value = uiState.password,
                    onValueChange = { viewModel.onPasswordChanged(it) },
                    isPassword = true
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Checkbox(
                        checked = uiState.rememberMe,
                        onCheckedChange = { viewModel.onRememberMeChanged(it) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF8B5CF6),
                            uncheckedColor = Color.Gray,
                            checkmarkColor = Color.White
                        )
                    )
                    Text("Remember me", color = Color.Gray, fontSize = 13.sp, fontWeight = FontWeight.Normal)
                }

                Spacer(modifier = Modifier.height(16.dp))
                CustomButton(
                    text = if (uiState.isLoading) "Logging in..." else "Login",
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        viewModel.attemptLogin()
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row {
                    Text(
                        "Don't have an account? ",
                        color = Color.Gray,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal
                    )
                    Text(
                        text = "Sign up",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF8B5CF6),
                        modifier = Modifier.clickable(onClick = onRegisterClick)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color(0xFF8B5CF6)
            )
        }
    }
}