package com.kotlin.connectit.ui.home // Atau com.kotlin.connectit.ui.profile jika sudah dipindah

import android.widget.Toast
import androidx.compose.foundation.Image // Tetap diperlukan untuk background di header
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create // Untuk tombol edit foto profil
import androidx.compose.material.icons.filled.Edit // Digunakan di FAB
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage // Import AsyncImage
import com.kotlin.connectit.R
import com.kotlin.connectit.ui.profile.ProfileViewModel
import com.kotlin.connectit.ui.profile.UserProfileHeaderInfo
import com.kotlin.connectit.util.ResultWrapper
import kotlinx.coroutines.launch

// URL default untuk gambar profil jika tidak ada URL dari backend
private const val DEFAULT_PROFILE_IMAGE_URL = "https://i.pinimg.com/474x/81/8a/1b/818a1b89a57c2ee0fb7619b95e11aebd.jpg"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Profile(
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit,
    onNavigateToEditProfile: (userId: String) -> Unit,
    onNavigateToCreatePost: () -> Unit,
    onNavigateToEditPost: (postId: String) -> Unit,
    onNavigateToUserProfile: (userId: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var postToDeleteId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(key1 = uiState.errorMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.consumeErrorMessage()
        }
    }

    LaunchedEffect(key1 = uiState.deletePostResult) {
        when (val result = uiState.deletePostResult) {
            is ResultWrapper.Success -> {
                Toast.makeText(context, result.data.data?.message ?: "Post berhasil dihapus", Toast.LENGTH_SHORT).show()
            }
            is ResultWrapper.Error -> {
                Toast.makeText(context, result.message ?: "Gagal menghapus post", Toast.LENGTH_LONG).show()
            }
            null -> {}
        }
        viewModel.consumeDeletePostResult()
    }


    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF191A1F))
        ) {
            ProfileHeader(
                userInfo = uiState.userProfileInfo,
                isLoading = uiState.isLoadingUserProfile,
                onEditProfileClick = { onNavigateToEditProfile(uiState.userProfileInfo.userId) },
                onEditProfilePicClick = {
                    // Arahkan ke EditProfileScreen juga, atau buat layar khusus edit foto
                    onNavigateToEditProfile(uiState.userProfileInfo.userId)
                    Toast.makeText(context, "Edit profile picture clicked (navigates to edit profile)", Toast.LENGTH_SHORT).show()
                },
                onLogoutClick = {
                    viewModel.attemptLogout {
                        onNavigateToLogin()
                    }
                }
            )

            Divider(color = Color.White.copy(alpha = 0.2f), thickness = 1.dp)

            if (uiState.isLoadingUserPosts && uiState.userPosts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF8B5CF6))
                }
            } else if (uiState.userPosts.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(uiState.userPosts, key = { it.postId }) { post ->
                        PostItem(
                            postData = post,
                            onMoreOptionsClick = { selectedPost -> viewModel.onShowBottomSheet(selectedPost) },
                            onUsernameClick = { userId ->
                                if (userId == uiState.userProfileInfo.userId) {
                                    // Tidak lakukan apa-apa atau refresh jika klik username sendiri
                                } else {
                                    onNavigateToUserProfile(userId)
                                }
                            },
                            currentLoggedInUserId = uiState.userProfileInfo.userId
                        )
                    }
                }
            } else if (!uiState.isLoadingUserProfile && !uiState.isLoadingUserPosts) {
                Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                    Text("No posts yet. Start sharing your moments!", color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
                }
            }
        }

        FloatingActionButton(
            onClick = onNavigateToCreatePost,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = Color(0xFF4C00A8),
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Default.Edit, // Sebaiknya Icons.Filled.Add atau Create
                contentDescription = "Create Post"
            )
        }

        if (uiState.showBottomSheet && uiState.selectedPostForOptions != null) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.onDismissBottomSheet() },
                sheetState = sheetState,
                containerColor = Color(0xFF2A2A2F),
                contentColor = Color.White
            ) {
                PostOptionsBottomSheetContent(
                    selectedPostCaption = uiState.selectedPostForOptions?.caption ?: "Selected Post",
                    onEditClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                viewModel.onDismissBottomSheet()
                                uiState.selectedPostForOptions?.postId?.let { postId ->
                                    onNavigateToEditPost(postId)
                                }
                            }
                        }
                    },
                    onDeleteClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                viewModel.onDismissBottomSheet()
                                postToDeleteId = uiState.selectedPostForOptions?.postId
                                showDeleteConfirmDialog = true
                            }
                        }
                    }
                )
            }
        }

        if (showDeleteConfirmDialog && postToDeleteId != null) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteConfirmDialog = false
                    postToDeleteId = null
                },
                title = { Text("Confirm Delete", color = Color.White) },
                text = { Text("Are you sure you want to delete this post? This action cannot be undone.", color = Color.White.copy(alpha = 0.8f)) },
                confirmButton = {
                    Button(
                        onClick = {
                            postToDeleteId?.let { viewModel.attemptDeletePost(it) }
                            showDeleteConfirmDialog = false
                            postToDeleteId = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4C00A8))
                    ) {
                        Text("Delete", color = Color.White)
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            showDeleteConfirmDialog = false
                            postToDeleteId = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray.copy(alpha = 0.5f))
                    ) {
                        Text("Cancel", color = Color.White)
                    }
                },
                containerColor = Color(0xFF2A2A2F)
            )
        }
    }
}

@Composable
fun ProfileHeader(
    userInfo: UserProfileHeaderInfo,
    isLoading: Boolean,
    onEditProfileClick: () -> Unit,
    onEditProfilePicClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onLogoutClick) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Logout",
                        tint = Color(0xFFBB86FC)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier.size(120.dp)
            ) {
                AsyncImage(
                    model = if (userInfo.profileImageUrl.isNullOrBlank()) DEFAULT_PROFILE_IMAGE_URL else userInfo.profileImageUrl,
                    placeholder = painterResource(id = R.drawable.bubble_profile),
                    error = painterResource(id = R.drawable.bubble_profile),
                    contentDescription = "User Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .border(3.dp, Color.White, CircleShape)
                )
                IconButton(
                    onClick = onEditProfilePicClick,
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(Color(0xFF8B5CF6))
                        .border(1.5.dp, Color.White, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Create,
                        contentDescription = "Edit Profile Picture",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator(color = Color(0xFF8B5CF6), modifier = Modifier.size(24.dp))
            } else {
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
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onEditProfileClick,
                modifier = Modifier.height(40.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Edit Profile", color = Color.White, fontSize = 14.sp)
            }
        }
    }
}

// PostOptionsBottomSheetContent (jika belum ada di file terpisah)
@Composable
fun PostOptionsBottomSheetContent(
    selectedPostCaption: String,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Options for: \"${selectedPostCaption.take(50)}${if (selectedPostCaption.length > 50) "..." else ""}\"",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 12.dp, start = 8.dp, end = 8.dp),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Divider(color = Color.White.copy(alpha = 0.2f), thickness = 1.dp)

        TextButton(onClick = onEditClick, modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Edit Post", fontSize = 16.sp, color = Color.White)
            }
        }
        Divider(color = Color.White.copy(alpha = 0.1f), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
        TextButton(onClick = onDeleteClick, modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Delete Post", fontSize = 16.sp, color = Color.Red.copy(alpha = 0.8f))
            }
        }
    }
}
