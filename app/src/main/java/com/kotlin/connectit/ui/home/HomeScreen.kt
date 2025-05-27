package com.kotlin.connectit.ui.home

import android.widget.Toast
import androidx.compose.foundation.Image // Tetap diperlukan untuk header banner
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
// import androidx.compose.material3.SheetState // Tidak digunakan secara langsung di sini
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage // Import AsyncImage
import com.kotlin.connectit.R
import com.kotlin.connectit.util.ResultWrapper
import kotlinx.coroutines.launch

// URL default untuk gambar profil jika tidak ada URL dari backend
private const val DEFAULT_PROFILE_IMAGE_URL = "https://i.pinimg.com/474x/81/8a/1b/818a1b89a57c2ee0fb7619b95e11aebd.jpg"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToUserProfile: (userId: String) -> Unit,
    onNavigateToEditPost: (postId: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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


    Scaffold (
        topBar = {
            Surface (
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 4.dp,
                color = Color(0xFF191A1F)
            ) {
                Column {
                    Image(
                        painter = painterResource(id = R.drawable.header),
                        contentDescription = "Header Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Cari URL gambar profil pengguna yang login dari uiState.posts atau dari state user yang login jika ada
                        // Untuk sementara, kita asumsikan uiState.loggedInUser?.profileImageUrl ada, atau ambil dari post pertama jika ada
                        // Idealnya, HomeViewModel memiliki state sendiri untuk UserData yang login.
                        val loggedInUserFromPosts = uiState.posts.firstOrNull { it.userId == uiState.loggedInUserId }
                        val loggedInUserProfileImageUrl = loggedInUserFromPosts?.userProfileImageUrl // Ini sudah ditransformasi di ViewModel

                        AsyncImage( // Menggunakan AsyncImage untuk foto profil pengguna yang login
                            model = if (loggedInUserProfileImageUrl.isNullOrBlank()) DEFAULT_PROFILE_IMAGE_URL else loggedInUserProfileImageUrl,
                            placeholder = painterResource(R.drawable.bubble_profile),
                            error = painterResource(R.drawable.bubble_profile),
                            contentDescription = "Current User Profile Image",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, Color(0xFF8B5CF6), CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        OutlinedTextField(
                            value = uiState.searchText,
                            onValueChange = { viewModel.onSearchTextChanged(it) },
                            singleLine = true,
                            shape = RoundedCornerShape(25.dp),
                            placeholder = { Text("Search ConnectIT...", color = Color.Gray, fontSize = 14.sp) },
                            trailingIcon = {
                                Icon(Icons.Filled.Search, contentDescription = "Search", tint = Color(0xFF8B5CF6))
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0xFF2A2A2F),
                                unfocusedContainerColor = Color(0xFF2A2A2F),
                                cursorColor = Color(0xFF8B5CF6),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                            )
                        )
                    }
                }
            }
        },
        containerColor = Color(0xFF191A1F)
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (uiState.isLoading && uiState.posts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF8B5CF6))
                }
            } else if (uiState.posts.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
                ) {
                    items(uiState.posts, key = { feedItem -> feedItem.postId }) { feedItem ->
                        PostItem(
                            postData = feedItem,
                            onMoreOptionsClick = { selectedPost ->
                                viewModel.triggerPostOptions(selectedPost)
                            },
                            onUsernameClick = { userId ->
                                onNavigateToUserProfile(userId)
                            },
                            currentLoggedInUserId = uiState.loggedInUserId
                        )
                    }
                }
            } else if (!uiState.isLoading && uiState.errorMessage == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No posts to show yet. Be the first!", color = Color.Gray, fontSize = 16.sp)
                }
            }
        }
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
                            uiState.selectedPostForOptions?.postId?.let { postId ->
                                viewModel.attemptDeletePost(postId)
                            }
                        }
                    }
                }
            )
        }
    }
}
