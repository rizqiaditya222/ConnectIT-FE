package com.kotlin.connectit.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kotlin.connectit.R

@Composable
fun SearchScreen() { // <--- Ubah nama fungsi dari Seach menjadi SearchScreen
    var selectedTab by remember { mutableStateOf("Post") }
    val posts = listOf(
        Post(1, "Andreas Bagas", "@andreas", "Koding hari ini terasa seru!", "2d", R.drawable.illustration1, listOf(R.drawable.header, R.drawable.bubble_login)),
        Post(2, "Elgin Brian", "@elgin", "Menikmati secangkir kopi hangat.", "1d", R.drawable.bubble_login),
        Post(3, "Budi Santoso", "@budi", "Ide-ide baru bermunculan!", "5h", R.drawable.bubble_login),
        Post(4, "Citra Dewi", "@citra", "Desain UI/UX memang menantang.", "3h", R.drawable.bubble_login),
        Post(5, "Dian Pratama", "@dian", "Belajar Compose itu asyik!", "1h", R.drawable.bubble_login),
        Post(6, "Eko Susilo", "@eko", "Jangan menyerah pada tantangan.", "30m", R.drawable.bubble_login),
        Post(7, "Fina Amelia", "@fina", "Akhirnya liburan tiba!", "10m", R.drawable.bubble_login)
    )

    Column(
        modifier = Modifier
            .background(Color(0xFF191A1F))
            .fillMaxSize()
        // Hapus padding(top = 24.dp) di sini.
        // Padding untuk status bar akan ditangani oleh Scaffold secara otomatis
        // melalui paddingValues yang diteruskan ke NavHost di AppNavGraph.
    ) {
        OutlinedTextField(
            value = "",
            onValueChange = {},
            singleLine = true,
            shape = RoundedCornerShape(32.dp),
            placeholder = {
                Text(
                    "Search...",
                    color = Color.Gray,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search icon",
                    tint = Color(0xFF8B5CF6),
                    modifier = Modifier.padding(end = 8.dp)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 16.dp), // Tambahkan padding top di sini jika ingin ada ruang di atas search bar
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0xFF1F222A),
                unfocusedContainerColor = Color(0xFF1F222A),
                disabledContainerColor = Color(0xFF2A2A2F),
                cursorColor = Color(0xFF8B5CF6),
                focusedIndicatorColor = Color(0xFF8B5CF6),
                unfocusedIndicatorColor = Color(0xFF1F222A),
            )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp).padding(top = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { selectedTab = "User" },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "User",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selectedTab == "User") Color(0xFF8B5CF6) else Color.White,
                )
                if (selectedTab == "User") {
                    // Indikator di bawah teks User
                    Box(
                        modifier = Modifier
                            .height(3.dp)
                            .width(96.dp)
                            .background(Color(0xFF8B5CF6))
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { selectedTab = "Post" },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Post",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selectedTab == "Post") Color(0xFF8B5CF6) else Color.White,
                )
                if (selectedTab == "Post") {
                    // Indikator di bawah teks Post
                    Box(
                        modifier = Modifier
                            // .padding(top = 16.dp) // Hapus padding top di sini, agar tidak terlalu jauh
                            .height(3.dp)
                            .width(96.dp)
                            .background(Color(0xFF8B5CF6))
                    )
                }
            }
        }

        Box( // Pembatas di bawah tab filter
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
                    showMoreOptions = false, // Biasanya tidak ada opsi di Search result
                    onMoreOptionsClick = {}
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSearchScreen() { // <--- Ubah nama preview juga
    SearchScreen()
}
