package com.kotlin.connectit.ui.home

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import com.kotlin.connectit.R // Pastikan ini mengarah ke folder res Anda

data class Post(
    val id: Int,
    val fullName: String,
    val username: String,
    val caption: String,
    val timestamp: String,
    val profileImageRes: Int,
    val postImageRes: List<Int>? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(key1 = uiState.errorMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.consumeErrorMessage()
        }
    }

    // Menggunakan Scaffold untuk struktur dengan TopAppBar (opsional, bisa dihilangkan jika TopAppBar diatur oleh NavHost utama)
    Scaffold (
        topBar = {
            Surface ( // Memberi background dan elevasi pada TopAppBar kustom
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 4.dp, // Elevasi untuk bayangan
                color = Color(0xFF191A1F) // Warna background TopAppBar
            ) {
                Column { // Kolom untuk header image dan search bar
                    // Header Image (jika ingin tetap ada di atas)
                    Image(
                        painter = painterResource(id = R.drawable.header), // Pastikan drawable ini ada
                        contentDescription = "Header Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp) // Tinggi header dikurangi
                    )
                    // Profile & Search Bar Section
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // TODO: Ganti dengan AsyncImage untuk foto profil pengguna yang login, dari ViewModel lain atau User Preferences
                        Image(
                            painter = painterResource(R.drawable.bubble_login),
                            contentDescription = "Current User Profile Image",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, Color(0xFF8B5CF6), CircleShape)
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
        containerColor = Color(0xFF191A1F) // Background utama Scaffold
    ) { innerPadding -> // innerPadding dari Scaffold

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF8B5CF6))
                }
            } else if (uiState.posts.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp) // Padding untuk list
                ) {
                    items(uiState.posts, key = { feedItem -> feedItem.postId }) { feedItem ->
                        PostItem(
                            postData = feedItem,
                            showMoreOptions = true,
                            onMoreOptionsClick = { postId ->
                                viewModel.handlePostMoreOptions(postId)
                            }
                        )
                    }
                }
            } else if (uiState.errorMessage == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No posts to show yet. Be the first!", color = Color.Gray, fontSize = 16.sp)
                }
            }
        }
    }
}