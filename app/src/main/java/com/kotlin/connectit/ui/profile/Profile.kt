package com.kotlin.connectit.ui.home

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
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.* // Import semua Material3 composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kotlin.connectit.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Profile() {
    var selectedTab by remember { mutableStateOf("Post") }

    val posts = listOf(
        Post(1, "Andreas Bagas", "@andreas", "Koding hari ini terasa seru!", "2d", R.drawable.illustration1, listOf(R.drawable.header)),
        Post(2, "Elgin Brian", "@elgin", "Menikmati secangkir kopi hangat.", "1d", R.drawable.illustration1),
        Post(3, "Budi Santoso", "@budi", "Ide-ide baru bermunculan!", "5h", R.drawable.illustration1, listOf(R.drawable.bubble_login, R.drawable.header)),
        Post(4, "Citra Dewi", "@citra", "Momen-momen terbaik di acara kemarin.", "3h", R.drawable.illustration1, listOf(R.drawable.bubble_login, R.drawable.header, R.drawable.illustration1)),
        Post(5, "Dian Pratama", "@dian", "Potret keindahan alam yang menenangkan.", "1h", R.drawable.illustration1, listOf(R.drawable.bubble_login, R.drawable.header, R.drawable.illustration1, R.drawable.bubble_login)),
        Post(6, "Eko Susilo", "@eko", "Kumpulan foto liburan musim panas!", "30m", R.drawable.illustration1, listOf(R.drawable.bubble_login, R.drawable.header, R.drawable.illustration1, R.drawable.bubble_login, R.drawable.header)),
        Post(7, "Fina Amelia", "@fina", "Akhirnya liburan tiba!", "10m", R.drawable.illustration1, listOf(R.drawable.bubble_login))
    )

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedPostForOptions by remember { mutableStateOf<Post?>(null) }

    Column(
        modifier = Modifier
            .background(Color(0xFF191A1F))
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF191A1F))
        ) {
            Image(
                painter = painterResource(id = R.drawable.bubble_profile),
                contentDescription = "Background Bubbles",
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 32.dp, vertical = 16.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Logout icon",
                        tint = Color(0xFF4C00A8),
                        modifier = Modifier
                            .size(32.dp)
                            .align(Alignment.CenterEnd)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Card(
                        shape = CircleShape,
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.Center)
                            .background(Color.Transparent)
                            .border(5.dp, Color.White, CircleShape)
                            .padding(4.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.illustration1),
                            contentDescription = "User Profile Picture",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    }

                    IconButton(
                        onClick = { /* Handle edit profile picture click */ },
                        modifier = Modifier
                            .size(48.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(Color(0xFF4C00A8))
                            .border(2.dp, Color.White, CircleShape)
                            .offset(x = (-4).dp, y = (-4).dp)
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

                Text(
                    "Elgin Brian",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Text(
                    "@elginbrian",
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { /* Handle edit profile click */ },
                    modifier = Modifier
                        .height(36.dp)
                        .align(Alignment.CenterHorizontally),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4C00A8)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = "Edit Profile",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.2f))
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(posts) { post ->
                PostItem(
                    fullName = post.fullName,
                    username = post.username,
                    caption = post.caption,
                    timestamp = post.timestamp,
                    profileImageRes = post.profileImageRes,
                    postImageRes = post.postImageRes,
                    showMoreOptions = true, // Di Profile Screen, tampilkan opsi titik tiga
                    onMoreOptionsClick = { clickedPost ->
                        selectedPostForOptions = clickedPost
                        showBottomSheet = true
                    }
                )
            }
        }
    }

    // Floating Action Button for Create Post
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        Button(
            onClick = { /* TODO: Aksi create post */ },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4C00A8)),
            shape = CircleShape,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Create Post",
                tint = Color.White,
            )
        }
    }

    // Modal Bottom Sheet
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            containerColor = Color(0xFF1F222A),
            contentColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = selectedPostForOptions?.caption ?: "Pilihan Postingan",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Divider(color = Color.White.copy(alpha = 0.1f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showBottomSheet = false
                                println("Edit Post: ${selectedPostForOptions?.id}")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Edit Post",
                        fontSize = 18.sp,
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showBottomSheet = false
                                println("Delete Post: ${selectedPostForOptions?.id}")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Delete Post",
                        fontSize = 18.sp,
                        color = Color.Red,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewProfile() {
    Profile()
}