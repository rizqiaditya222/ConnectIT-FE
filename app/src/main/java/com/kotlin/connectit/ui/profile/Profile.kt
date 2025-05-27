package com.kotlin.connectit.ui.home // Sesuaikan package Anda

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create // Untuk edit profile pic
import androidx.compose.material.icons.filled.Edit // Untuk FAB create post
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage // Untuk memuat gambar dari URL
import com.kotlin.connectit.R
import com.kotlin.connectit.ui.profile.ProfileUiState // Impor UiState
import com.kotlin.connectit.ui.profile.ProfileViewModel // Impor ViewModel
import com.kotlin.connectit.ui.profile.UserProfileHeaderInfo
import com.kotlin.connectit.util.ResultWrapper
// Impor PostItem dan UiDisplayPost jika berada di file/package berbeda
// import com.kotlin.connectit.ui.home.PostItem
// import com.kotlin.connectit.ui.home.UiDisplayPost
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Profile( // Ubah nama Composable menjadi ProfileScreen atau sesuai konvensi Anda
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit,
    onNavigateToEditProfile: (userId: String) -> Unit, // Kirim userId jika perlu
    onNavigateToCreatePost: () -> Unit
    // Tambahkan callback lain jika perlu, misal onNavigateToEditPost(postId: String)
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true) // Agar langsung full expand atau hide

    // Handle Error Messages
    LaunchedEffect(key1 = uiState.errorMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.consumeErrorMessage()
        }
    }

    // Handle Delete Post Result
    LaunchedEffect(key1 = uiState.deletePostResult) {
        when (val result = uiState.deletePostResult) {
            is ResultWrapper.Success -> {
                Toast.makeText(context, result.data.data?.message ?: "Post berhasil dihapus", Toast.LENGTH_SHORT).show()
                viewModel.onDismissBottomSheet() // Tutup bottom sheet setelah aksi
            }
            is ResultWrapper.Error -> {
                Toast.makeText(context, result.message ?: "Gagal menghapus post", Toast.LENGTH_LONG).show()
            }
            null -> {}
        }
        viewModel.consumeDeletePostResult() // Selalu consume setelah dihandle
    }


    Box(modifier = Modifier.fillMaxSize()) { // Box utama untuk FAB dan BottomSheet
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF191A1F))
        ) {
            // Header Profile Section
            ProfileHeader(
                userInfo = uiState.userProfileInfo,
                isLoading = uiState.isLoadingUserProfile,
                onEditProfileClick = { onNavigateToEditProfile(uiState.userProfileInfo.userId) },
                onEditProfilePicClick = { /* TODO: Handle edit profile pic */ },
                onLogoutClick = {
                    viewModel.attemptLogout {
                        onNavigateToLogin()
                    }
                }
            )

            Divider(color = Color.White.copy(alpha = 0.2f), thickness = 1.dp)

            // User Posts Section
            if (uiState.isLoadingUserPosts && uiState.userPosts.isEmpty()) { // Loading awal untuk post
                Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF8B5CF6))
                }
            } else if (uiState.userPosts.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().weight(1f), // Beri weight agar LazyColumn mengisi sisa ruang
                    contentPadding = PaddingValues(bottom = 80.dp) // Padding untuk FAB
                ) {
                    items(uiState.userPosts, key = { it.postId }) { post ->
                        PostItem( // Menggunakan PostItem yang sudah disesuaikan untuk UiDisplayPost & URL
                            postData = post,
                            showMoreOptions = true,
                            onMoreOptionsClick = { viewModel.onShowBottomSheet(post) }
                        )
                    }
                }
            } else if (!uiState.isLoadingUserProfile && !uiState.isLoadingUserPosts) { // Tidak loading & tidak ada post
                Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                    Text("No posts yet. Start sharing your moments!", color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
                }
            }
        } // Akhir Column utama

        // Floating Action Button for Create Post
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
                imageVector = Icons.Default.Edit, // Ikon pensil untuk membuat/edit
                contentDescription = "Create Post"
            )
        }

        // Modal Bottom Sheet untuk Opsi Post
        if (uiState.showBottomSheet && uiState.selectedPostForOptions != null) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.onDismissBottomSheet() },
                sheetState = sheetState,
                containerColor = Color(0xFF2A2A2F), // Warna sedikit beda untuk sheet
                contentColor = Color.White
            ) {
                PostOptionsBottomSheetContent(
                    selectedPostCaption = uiState.selectedPostForOptions?.caption ?: "Selected Post",
                    onEditClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                viewModel.onDismissBottomSheet()
                                // TODO: Navigasi ke layar Edit Post dengan uiState.selectedPostForOptions?.postId
                                Toast.makeText(context, "Edit: ${uiState.selectedPostForOptions?.postId}", Toast.LENGTH_SHORT).show()
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
    } // Akhir Box utama
}

@Composable
fun ProfileHeader(
    userInfo: UserProfileHeaderInfo,
    isLoading: Boolean,
    onEditProfileClick: () -> Unit,
    onEditProfilePicClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Box( // Menggunakan Box agar Image bisa jadi background
        modifier = Modifier
            .fillMaxWidth()
            // .height(300.dp) // Atur tinggi header jika perlu
            .background(Color(0xFF191A1F)) // Background dasar jika gambar tidak full
    ) {
        Image( // Gambar bubble background
            painter = painterResource(id = R.drawable.bubble_profile), // Pastikan drawable ini ada
            contentDescription = "Profile Background",
            contentScale = ContentScale.Crop, // Crop agar mengisi tanpa distorsi
            modifier = Modifier.matchParentSize().alpha(0.3f) // Isi seluruh Box dan buat transparan
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 24.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End // Posisikan ikon logout di kanan atas
            ) {
                IconButton(onClick = onLogoutClick) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Logout",
                        tint = Color(0xFFBB86FC) // Warna ungu muda
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp)) // Jarak setelah ikon logout

            Box( // Box untuk foto profil dan tombol editnya
                modifier = Modifier.size(120.dp) // Ukuran area foto profil
            ) {
                AsyncImage( // Menggunakan AsyncImage untuk memuat URL
                    model = userInfo.profileImageUrl,
                    placeholder = painterResource(id = R.drawable.bubble_profile),
                    error = painterResource(id = R.drawable.bubble_profile),
                    contentDescription = "User Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .border(3.dp, Color.White, CircleShape) // Border putih
                )
                IconButton( // Tombol untuk edit foto profil
                    onClick = onEditProfilePicClick,
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(Color(0xFF8B5CF6)) // Warna ungu
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
                Text( // Menampilkan email atau @username
                    text = "@${userInfo.username}", // atau userInfo.userEmail
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button( // Tombol Edit Profile
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
        horizontalAlignment = Alignment.Start // Ubah alignment ke start
    ) {
        Text(
            text = "Options for: \"${selectedPostCaption.take(50)}${if (selectedPostCaption.length > 50) "..." else ""}\"",
            fontSize = 14.sp, // Ukuran font lebih kecil untuk judul
            fontWeight = FontWeight.SemiBold, // Sedikit bold
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 12.dp, start = 8.dp, end = 8.dp),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Divider(color = Color.White.copy(alpha = 0.2f), thickness = 1.dp)

        TextButton(onClick = onEditClick, modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                // Icon(Icons.Outlined.Edit, contentDescription = "Edit", tint = Color.White, modifier = Modifier.padding(end = 8.dp)) // Contoh dengan ikon
                Text("Edit Post", fontSize = 16.sp, color = Color.White)
            }
        }
        Divider(color = Color.White.copy(alpha = 0.1f), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
        TextButton(onClick = onDeleteClick, modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                // Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.8f), modifier = Modifier.padding(end = 8.dp)) // Contoh dengan ikon
                Text("Delete Post", fontSize = 16.sp, color = Color.Red.copy(alpha = 0.8f))
            }
        }
    }
}


@Preview(showBackground = true, backgroundColor = 0xFF191A1F)
@Composable
fun PreviewProfileScreen() { // Ganti nama preview agar sesuai
    MaterialTheme { // Pastikan ada MaterialTheme wrapper
        // Untuk preview, Anda bisa menggunakan mock ViewModel atau data statis.
        // Karena ViewModel melakukan panggilan jaringan di init, preview langsung mungkin kompleks.
        // Membuat Composable preview khusus dengan data statis adalah ide bagus.
        MockProfileScreen()
    }
}

@Composable
fun MockProfileScreen() {
    val sampleUserInfo = UserProfileHeaderInfo("user123", "Elgin Brian", "elgin@example.com", null)
    val samplePosts = listOf(
        UiDisplayPost("1", "user123", "Elgin Brian", "elgin@example.com", "Ini caption post pertama di profil.", null, "https://example.com/image1.jpg", "2h ago", "2h ago"),
        UiDisplayPost("2", "user123", "Elgin Brian", "elgin@example.com", "Post kedua tanpa gambar.", null, null, "1d ago", "1d ago")
    )
    val mockUiState = ProfileUiState(
        userProfileInfo = sampleUserInfo,
        userPosts = samplePosts,
        isLoadingUserProfile = false,
        isLoadingUserPosts = false
    )

    // Implementasi UI dasar seperti di ProfileScreen tapi dengan data statis
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().background(Color(0xFF191A1F))) {
            ProfileHeader(
                userInfo = mockUiState.userProfileInfo,
                isLoading = mockUiState.isLoadingUserProfile,
                onEditProfileClick = { },
                onEditProfilePicClick = { },
                onLogoutClick = { }
            )
            Divider(color = Color.White.copy(alpha = 0.2f), thickness = 1.dp)
            if (mockUiState.userPosts.isNotEmpty()) {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(mockUiState.userPosts) { post ->
                        PostItem(postData = post, onMoreOptionsClick = {})
                    }
                }
            } else {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No posts yet.", color = Color.Gray)
                }
            }
        }
        FloatingActionButton(
            onClick = { /* mock create post */ },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            containerColor = Color(0xFF4C00A8),
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Edit, contentDescription = "Create Post")
        }
    }
}