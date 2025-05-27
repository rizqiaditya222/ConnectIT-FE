package com.kotlin.connectit.ui.editpost

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.kotlin.connectit.R // Pastikan R diimport dengan benar
import com.kotlin.connectit.util.ResultWrapper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPostScreen(
    navController: NavController,
    viewModel: EditPostViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(key1 = uiState.loadError) {
        uiState.loadError?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.consumeLoadError()
            // Pertimbangkan untuk navigasi kembali jika load gagal total
            if (!uiState.isPostLoaded) navController.popBackStack()
        }
    }
    LaunchedEffect(key1 = uiState.updateError) {
        uiState.updateError?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.consumeUpdateError()
        }
    }

    LaunchedEffect(key1 = uiState.updateResult) {
        when (val result = uiState.updateResult) {
            is ResultWrapper.Success -> {
                Toast.makeText(context, result.data.status ?: "Post berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                viewModel.consumeUpdateResult()
                navController.popBackStack() // Kembali setelah berhasil update
            }
            is ResultWrapper.Error -> {
                Toast.makeText(context, result.message ?: "Gagal memperbarui post", Toast.LENGTH_LONG).show()
                viewModel.consumeUpdateResult()
            }
            null -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Post", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    Button(
                        onClick = { viewModel.attemptUpdatePost() },
                        enabled = !uiState.isLoading && uiState.isPostLoaded,
                        modifier = Modifier.heightIn(min = 36.dp).padding(end = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        if (uiState.isLoading && uiState.updateResult == null) { // Loading untuk update
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("Save", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF191A1F))
            )
        },
        containerColor = Color(0xFF191A1F)
    ) { paddingValues ->
        if (uiState.isLoading && !uiState.isPostLoaded) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF8B5CF6))
            }
        } else if (uiState.isPostLoaded) {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                OutlinedTextField( //
                    value = uiState.currentCaption,
                    onValueChange = { viewModel.onCaptionChanged(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 150.dp),
                    placeholder = { Text("What's on your mind?", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors( //
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF2A2A2F),
                        unfocusedContainerColor = Color(0xFF2A2A2F),
                        cursorColor = Color(0xFF8B5CF6),
                        focusedBorderColor = Color(0xFF8B5CF6).copy(alpha = 0.8f),
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                    ),
                    shape = RoundedCornerShape(8.dp),
                    maxLines = 10
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (!uiState.currentImageUrl.isNullOrBlank()) {
                    Text("Current Image (cannot be changed):", color = Color.White.copy(alpha = 0.7f), modifier = Modifier.padding(bottom = 8.dp))
                    AsyncImage(
                        model = uiState.currentImageUrl,
                        contentDescription = "Current post image",
                        placeholder = painterResource(id = R.drawable.bubble_login), // Ganti dengan placeholder yang sesuai
                        error = painterResource(id = R.drawable.bubble_login), // Ganti dengan placeholder yang sesuai
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f) // Sesuaikan aspect ratio jika perlu
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.DarkGray),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                // Tombol untuk mengganti gambar bisa ditambahkan di sini jika API mendukung
            }
        } else if (uiState.loadError != null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text(uiState.loadError!!, color = Color.Red, textAlign = TextAlign.Center)
            }
        }
    }
}