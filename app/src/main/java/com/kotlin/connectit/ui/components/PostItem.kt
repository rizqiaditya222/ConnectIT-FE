package com.kotlin.connectit.ui.home // Sesuaikan dengan package Anda

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kotlin.connectit.R // Pastikan import R sudah benar

// Diasumsikan UiDisplayPost data class sudah ada:
// data class UiDisplayPost(
//    val postId: String,
//    val userId: String,
//    val username: String,
//    val userEmail: String,
//    val caption: String?,
//    val userProfileImageUrl: String?,
//    val postImageUrl: String?,
//    val postCreatedAt: String,
//    val postUpdatedAt: String
// )

@Composable
fun PostImageItemView(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier // Modifier akan datang dari PostItem
) {
    if (!imageUrl.isNullOrBlank()) {
        AsyncImage(
            model = imageUrl,
            contentDescription = contentDescription,
            placeholder = painterResource(id = R.drawable.bubble_login), // Placeholder yang lebih sesuai
            error = painterResource(id = R.drawable.bubble_login),       // Placeholder yang lebih sesuai
            modifier = modifier // Modifier ini sekarang TIDAK akan memiliki batasan tinggi dari PostItem
                // .clip(RoundedCornerShape(12.dp)) // Kliping bisa dilakukan di sini atau di pemanggil
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
            contentScale = ContentScale.FillWidth // ✨ KUNCI: Sesuaikan tinggi berdasarkan lebar & aspect ratio
        )
    }
}

@Composable
fun PostItem(
    postData: UiDisplayPost,
    showMoreOptions: Boolean = true,
    onMoreOptionsClick: (postId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp), // Padding antar card sedikit ditambah
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF202125))
    ) {
        Column(modifier = Modifier.padding(bottom = 12.dp)) { // Padding bawah untuk konten dalam card
            // Header: Foto Profil, Nama, Timestamp, Tombol Opsi
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 8.dp), // Sesuaikan padding header
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = postData.userProfileImageUrl,
                    contentDescription = "${postData.username}'s profile picture",
                    placeholder = painterResource(id = R.drawable.bubble_profile), // Placeholder Avatar
                    error = painterResource(id = R.drawable.bubble_profile),       // Placeholder Avatar
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, Color(0xFF8B5CF6), CircleShape),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = postData.username,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    // Email tidak ditampilkan lagi di sini untuk tampilan yang lebih bersih
                }

                // Column untuk Timestamp dan Tombol Opsi agar bisa bertumpuk jika perlu
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = postData.postCreatedAt,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.Gray
                    )
                    if (postData.postUpdatedAt != postData.postCreatedAt && postData.postUpdatedAt.contains(Regex("\\d"))) {
                        Text(
                            text = "(edited)",
                            fontSize = 9.sp,
                            color = Color.Gray.copy(alpha = 0.7f)
                        )
                    }
                }

                if (showMoreOptions) {
                    IconButton(
                        onClick = { onMoreOptionsClick(postData.postId) },
                        modifier = Modifier.size(32.dp).padding(start = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = Color.Gray
                        )
                    }
                }
            }

            // Caption
            if (!postData.caption.isNullOrBlank()) {
                Text(
                    text = postData.caption,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White.copy(alpha = 0.90f),
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = if (postData.postImageUrl != null) 10.dp else 0.dp), // Padding bawah sebelum gambar
                    maxLines = 10,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                // Jika tidak ada caption tapi ada gambar, beri sedikit jarak atas untuk gambar
                if (postData.postImageUrl != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            // Gambar Post
            if (!postData.postImageUrl.isNullOrBlank()) {
                PostImageItemView(
                    imageUrl = postData.postImageUrl,
                    contentDescription = "Post image by ${postData.username}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp) // Padding kecil agar clip RoundedCorner terlihat
                        .clip(RoundedCornerShape(12.dp)) // Kliping bentuk sudut di sini
                    // HAPUS .heightIn() atau .height() dari sini agar aspect ratio terjaga
                )
            }
        }
    }
}


@Preview(showBackground = true, backgroundColor = 0xFF191A1F)
@Composable
fun PreviewPostItem() {
    val dummyPostWithImage = UiDisplayPost(
        postId = "123",
        userId = "user001",
        username = "Elgin Brian",
        userEmail = "elgin.brian@example.com",
        caption = "Ini adalah contoh caption yang cukup panjang untuk melihat bagaimana teks akan ditampilkan. Aspect ratio gambar seharusnya terjaga sekarang.",
        userProfileImageUrl = null,
        postImageUrl = "https://via.placeholder.com/600x400", // Ganti dengan URL gambar aspek ratio berbeda untuk tes
        postCreatedAt = "2h ago",
        postUpdatedAt = "2h ago"
    )
    val dummyPostWithoutImage = UiDisplayPost(
        postId = "124",
        userId = "user002",
        username = "Jane Doe",
        userEmail = "jane.doe@example.com",
        caption = "Contoh post kedua tanpa gambar.",
        userProfileImageUrl = "https://via.placeholder.com/100",
        postImageUrl = null,
        postCreatedAt = "3h ago",
        postUpdatedAt = "3h ago"
    )
    MaterialTheme {
        Column(Modifier.background(Color(0xFF191A1F)).padding(vertical=8.dp)) {
            PostItem(
                postData = dummyPostWithImage,
                onMoreOptionsClick = {}
            )
            PostItem(
                postData = dummyPostWithoutImage,
                onMoreOptionsClick = {}
            )
        }
    }
}