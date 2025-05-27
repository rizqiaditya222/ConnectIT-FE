package com.kotlin.connectit.ui.addpost

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.kotlin.connectit.util.ResultWrapper //
import org.json.JSONObject // Untuk parsing errorBody

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPostScreen(
    viewModel: AddPostViewModel = hiltViewModel(),
    onPostCreatedSuccessfully: () -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            viewModel.onImageSelected(uri)
        }
    )

    LaunchedEffect(key1 = uiState.createPostResult) {
        when (val result = uiState.createPostResult) {
            is ResultWrapper.Success -> {
                Toast.makeText(context, result.data.status ?: "Post berhasil dibuat!", Toast.LENGTH_SHORT).show()
                viewModel.clearForm()
                onPostCreatedSuccessfully()
                viewModel.consumeCreatePostResult()
            }
            is ResultWrapper.Error -> {
                val errorMsg = result.message ?: "Gagal membuat post"
                var detailedError = result.errorBody?.let { body ->
                    try {
                        // Coba parse sebagai JSON jika backend mengirim error terstruktur
                        val jsonError = JSONObject(body)
                        // Cari field "message" atau "error" di JSON, atau tampilkan body mentah
                        jsonError.optString("message", null) ?: jsonError.optString("error", null) ?: body.take(100) // Batasi panjang body mentah
                    } catch (e: Exception) {
                        body.take(100) // Jika bukan JSON atau gagal parse, tampilkan body mentah (dibatasi)
                    }
                } ?: ""

                if (detailedError.equals(errorMsg, ignoreCase = true)) detailedError = ""


                val fullErrorMessage = "$errorMsg ${if(detailedError.isNotBlank()) "($detailedError)" else ""}"
                Toast.makeText(context, fullErrorMessage, Toast.LENGTH_LONG).show()
                viewModel.consumeCreatePostResult()
            }
            null -> {}
        }
    }

    LaunchedEffect(key1 = uiState.errorMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.consumeErrorMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Post", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    Button(
                        onClick = { viewModel.attemptCreatePost() },
                        enabled = !uiState.isLoading,
                        modifier = Modifier.heightIn(min = 36.dp).padding(end = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("Post", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF191A1F))
            )
        },
        containerColor = Color(0xFF191A1F)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            OutlinedTextField(
                value = uiState.caption,
                onValueChange = { viewModel.onCaptionChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 150.dp),
                placeholder = { Text("What's on your mind?", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
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

            uiState.selectedImageUri?.let { uri ->
                Text("Selected Image:", color = Color.White.copy(alpha = 0.7f), modifier = Modifier.padding(bottom = 8.dp))
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.DarkGray)
                ) {
                    AsyncImage(
                        model = uri,
                        contentDescription = "Selected image preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    IconButton(
                        onClick = { viewModel.onImageSelected(null) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove image",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            OutlinedButton(
                onClick = {
                    pickMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFF8B5CF6))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add Picture Icon",
                        tint = Color(0xFF8B5CF6),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = if (uiState.selectedImageUri == null) "Add Picture" else "Change Picture",
                        color = Color(0xFF8B5CF6),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF191A1F)
@Composable
fun PreviewAddPostScreen() {
    MaterialTheme {
        AddPostScreen(
            onPostCreatedSuccessfully = {},
            onBackClick = {}
        )
    }
}