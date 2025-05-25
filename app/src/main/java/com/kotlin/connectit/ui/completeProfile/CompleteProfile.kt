package com.kotlin.connectit.ui.profile

import MainTextField
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kotlin.connectit.R
import com.kotlin.connectit.ui.completeProfile.CompleteProfileViewModel
import com.kotlin.connectit.util.ResultWrapper
import com.kotlin.connectit_fe.ui.components.CustomButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompleteProfileScreen(
    viewModel: CompleteProfileViewModel = hiltViewModel(),
    onProfileCompleted: () -> Unit,
    onSkipOrNavigate: () -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(key1 = uiState.updateProfileResult) {
        when (val result = uiState.updateProfileResult) {
            is ResultWrapper.Success -> {
                Toast.makeText(context, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                viewModel.consumeUpdateResult()
                onProfileCompleted()
            }
            is ResultWrapper.Error -> {
                Toast.makeText(context, result.message ?: "Update profil gagal", Toast.LENGTH_LONG).show()
                viewModel.consumeUpdateResult()
            }
            null -> { /* Initial or consumed */ }
        }
    }

    // Handle general error messages
    LaunchedEffect(key1 = uiState.errorMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.consumeErrorMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Complete your profile", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    TextButton(onClick = onSkipOrNavigate) {
                        Text("Skip", color = Color(0xFFBB86FC)) // Warna ungu muda untuk "Skip"
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF191A1F) // Warna background TopAppBar
                )
            )
        },
        containerColor = Color(0xFF191A1F) // Warna background utama
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (uiState.isLoading && !uiState.isProfileLoaded) { // Loading data awal
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF8B5CF6)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .padding(top = 24.dp), // Padding atas setelah TopAppBar
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Placeholder Foto Profil
                    Box(
                        contentAlignment = Alignment.BottomEnd,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color.DarkGray) // Warna placeholder
                            .border(2.dp, Color(0xFF8B5CF6), CircleShape)
                            .clickable { /* TODO: Handle ganti foto */ }
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.bubble_login),
                            contentDescription = "Profile Picture Placeholder",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Change Photo",
                            tint = Color.White,
                            modifier = Modifier
                                .size(30.dp)
                                .background(Color(0xFF8B5CF6), CircleShape)
                                .padding(4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .background(
                                Color(0xFF1F222A),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email Icon",
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uiState.email.ifEmpty { "Email tidak tersedia" },
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    MainTextField(
                        label = "Username",
                        value = uiState.username,
                        onValueChange = { viewModel.onUsernameChanged(it) },
                        leadingIcon = Icons.Default.Person
                    )

                    Text(
                        text = "Field lain seperti nama lengkap, nomor telepon, dll. akan dapat diisi pada versi mendatang setelah pembaruan sistem.",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    CustomButton(
                        text = if (uiState.isLoading && uiState.isProfileLoaded) "Saving..." else "Finish",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        onClick = {
                            viewModel.attemptSaveProfile()
                        }
                    )
                }
            }

            if (uiState.isLoading && uiState.isProfileLoaded) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF8B5CF6)
                )
            }
        }
    }
}
