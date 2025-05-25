package com.kotlin.connectit.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kotlin.connectit.R // Pastikan ini mengarah ke folder res Anda

data class Post(
    val id: Int,
    val fullName: String,
    val username: String,
    val caption: String,
    val timestamp: String,
    val profileImageRes: Int, // Mengubah nama agar lebih jelas
    val postImageRes: List<Int>? = null
)

@Composable
fun HomeScreenContent() { // Ubah nama Composable ini menjadi HomeScreenContent
    var selectedTab by remember { mutableStateOf("Post") } // Catatan: state ini mungkin tidak lagi relevan jika ada bottom nav utama

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
    ) {
        Image(
            painter = painterResource(id = R.drawable.header),
            contentDescription = "Header Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth().offset(y = -8.dp)
        )
        Row (modifier = Modifier.padding(horizontal = 24.dp).offset(y = -32.dp), verticalAlignment = Alignment.CenterVertically) {
            Card(modifier = Modifier.size(48.dp).clip(CircleShape)) {
                Image(
                    painter = painterResource(R.drawable.bubble_register),
                    contentDescription = "profile image"
                )
            }
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
                    .fillMaxWidth().padding(start = 8.dp),
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
                    profileImageRes = post.profileImageRes, // Gunakan profileImageRes
                    postImageRes = post.postImageRes,
                    showMoreOptions = false, // Di Home Screen, biasanya tidak ada opsi edit/hapus
                    onMoreOptionsClick = {}
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHomeScreenContent() { // Ubah nama preview juga
    HomeScreenContent()
}
