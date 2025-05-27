package com.kotlin.connectit.ui.editProfile // Renamed package

import MainTextField // Keep existing import if MainTextField is in root
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
// import androidx.compose.foundation.Image // Tidak digunakan secara langsung, AsyncImage menggantikan
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.kotlin.connectit.R
import com.kotlin.connectit.util.ResultWrapper
import com.kotlin.connectit_fe.ui.components.CustomButton

private const val DEFAULT_PROFILE_IMAGE_URL = "https://i.pinimg.com/474x/81/8a/1b/818a1b89a57c2ee0fb7619b95e11aebd.jpg"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen( // Renamed Composable
    viewModel: EditProfileViewModel = hiltViewModel(),
    onProfileUpdated: () -> Unit, // Renamed callback
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            selectedImageUri = uri
            // TODO: Implement actual image upload logic and update uiState.profileImageUrl
            // For now, this only updates local URI for preview.
            // viewModel.onProfileImageCandidateSelected(uri) // Anda perlu fungsi ini di ViewModel jika ingin mengunggah
        }
    )

    LaunchedEffect(key1 = uiState.updateProfileResult) {
        when (val result = uiState.updateProfileResult) {
            is ResultWrapper.Success -> {
                Toast.makeText(context, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                viewModel.consumeUpdateResult()
                onProfileUpdated()
            }
            is ResultWrapper.Error -> {
                Toast.makeText(context, result.message ?: "Update profil gagal", Toast.LENGTH_LONG).show()
                viewModel.consumeUpdateResult()
            }
            null -> {  }
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
                title = { Text("Edit Profile", color = Color.White) }, // Changed title
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF191A1F)
                )
            )
        },
        containerColor = Color(0xFF191A1F)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (uiState.isLoading && !uiState.isProfileLoaded) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF8B5CF6)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp)
                        .padding(top = 24.dp, bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        contentAlignment = Alignment.BottomEnd,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color.DarkGray)
                            .border(2.dp, Color(0xFF8B5CF6), CircleShape)
                            .clickable { pickMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                    ) {
                        AsyncImage(
                            model = selectedImageUri ?: (if (uiState.profileImageUrl.isNullOrBlank()) DEFAULT_PROFILE_IMAGE_URL else uiState.profileImageUrl),
                            contentDescription = "Profile Picture",
                            placeholder = painterResource(id = R.drawable.bubble_profile),
                            error = painterResource(id = R.drawable.bubble_profile),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Change Photo",
                            tint = Color.White,
                            modifier = Modifier
                                .size(30.dp)
                                .background(Color(0xFF8B5CF6), CircleShape)
                                .padding(6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Email",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Color(0xFF2A2A2F),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email Icon",
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = uiState.email.ifEmpty { "Tidak tersedia" },
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Username",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                    )
                    MainTextField(
                        label = "Enter your username",
                        value = uiState.username,
                        onValueChange = { viewModel.onUsernameChanged(it) },
                        leadingIcon = Icons.Default.Person
                    )

                    Text(
                        text = "Informasi lain seperti bio, nama lengkap, dll. mungkin akan ditambahkan pada versi mendatang.",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
                    )

                    Spacer(modifier = Modifier.weight(1f, fill = false))

                    CustomButton(
                        text = if (uiState.isLoading && uiState.isProfileLoaded) "Menyimpan..." else "Simpan Perubahan",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 24.dp),
                        onClick = {
                            viewModel.attemptUpdateProfile()
                            // TODO: Jika selectedImageUri ada, panggil fungsi ViewModel untuk mengunggahnya
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
