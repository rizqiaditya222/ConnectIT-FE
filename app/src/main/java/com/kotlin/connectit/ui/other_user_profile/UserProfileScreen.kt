package com.kotlin.connectit.ui.other_user_profile

import android.widget.Toast
import androidx.compose.foundation.Image // Tetap diperlukan untuk background di header
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
// import androidx.compose.foundation.shape.RoundedCornerShape // Tidak digunakan secara langsung di sini
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import com.kotlin.connectit.R
import com.kotlin.connectit.ui.home.PostItem
import com.kotlin.connectit.ui.profile.UserProfileHeaderInfo

// URL default untuk gambar profil jika tidak ada URL dari backend
private const val DEFAULT_PROFILE_IMAGE_URL = "https://i.pinimg.com/474x/81/8a/1b/818a1b89a57c2ee0fb7619b95e11aebd.jpg"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    navController: NavController,
    viewModel: UserProfileViewModel = hiltViewModel(),
    onNavigateToPostDetail: (postId: String) -> Unit,
    onUsernameClickInPost: (userId: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(key1 = uiState.errorMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.consumeErrorMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.userProfileInfo?.username ?: "User Profile", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
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
        ) {
            if (uiState.isLoadingProfile && uiState.userProfileInfo == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF8B5CF6))
                }
            } else if (uiState.userProfileInfo != null) {
                OtherUserProfileHeader(userInfo = uiState.userProfileInfo!!)

                Divider(color = Color.White.copy(alpha = 0.2f), thickness = 1.dp)

                if (uiState.isLoadingPosts && uiState.userPosts.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF8B5CF6))
                    }
                } else if (uiState.userPosts.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().weight(1f),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(uiState.userPosts, key = { it.postId }) { post ->
                            PostItem(
                                postData = post,
                                currentLoggedInUserId = null, // Tidak relevan untuk profil orang lain (opsi edit/delete tidak muncul)
                                onMoreOptionsClick = { /* Tidak ada opsi untuk post orang lain di sini */ },
                                onUsernameClick = { userId -> onUsernameClickInPost(userId) },
                            )
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                        Text("This user hasn't posted anything yet.", color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
                    }
                }
            } else if (uiState.errorMessage != null && !uiState.isLoadingProfile){
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(uiState.errorMessage!!, color = Color.Red, textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}

@Composable
fun OtherUserProfileHeader(userInfo: UserProfileHeaderInfo) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF191A1F))
    ) {
        Image(
            painter = painterResource(id = R.drawable.bubble_profile),
            contentDescription = "Profile Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize().alpha(0.3f)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 24.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            AsyncImage(
                model = if (userInfo.profileImageUrl.isNullOrBlank()) DEFAULT_PROFILE_IMAGE_URL else userInfo.profileImageUrl,
                placeholder = painterResource(id = R.drawable.bubble_profile),
                error = painterResource(id = R.drawable.bubble_profile),
                contentDescription = "User Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(3.dp, Color.White, CircleShape)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = userInfo.username,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                text = "@${userInfo.username.takeIf { it.isNotEmpty() && it != "Username" } ?: userInfo.userEmail}",
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
