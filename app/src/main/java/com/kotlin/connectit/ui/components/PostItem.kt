package com.kotlin.connectit.ui.home // Sesuaikan dengan package Anda

// import androidx.compose.foundation.Image // Tidak digunakan secara langsung
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.kotlin.connectit.R

// URL default untuk gambar profil jika tidak ada URL dari backend
private const val DEFAULT_PROFILE_IMAGE_URL = "https://i.pinimg.com/474x/81/8a/1b/818a1b89a57c2ee0fb7619b95e11aebd.jpg"

@Composable
fun PostImageItemView(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    if (!imageUrl.isNullOrBlank()) {
        AsyncImage(
            model = imageUrl, // URL gambar post
            contentDescription = contentDescription,
            placeholder = painterResource(id = R.drawable.bubble_login), // Placeholder untuk gambar post
            error = painterResource(id = R.drawable.bubble_login), // Fallback untuk gambar post
            modifier = modifier
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
            contentScale = ContentScale.FillWidth // Atau ContentScale.Crop sesuai kebutuhan
        )
    }
}

@Composable
fun PostItem(
    postData: UiDisplayPost,
    onMoreOptionsClick: (post: UiDisplayPost) -> Unit,
    onUsernameClick: (userId: String) -> Unit,
    currentLoggedInUserId: String?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF202125))
    ) {
        Column(modifier = Modifier.padding(bottom = 12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = if (postData.userProfileImageUrl.isNullOrBlank()) DEFAULT_PROFILE_IMAGE_URL else postData.userProfileImageUrl,
                    contentDescription = "${postData.username}'s profile picture",
                    placeholder = painterResource(id = R.drawable.bubble_profile),
                    error = painterResource(id = R.drawable.bubble_profile),
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, Color(0xFF8B5CF6), CircleShape)
                        .clickable { onUsernameClick(postData.userId) },
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onUsernameClick(postData.userId) }
                ) {
                    Text(
                        text = postData.username,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

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

                if (postData.userId == currentLoggedInUserId) {
                    IconButton(
                        onClick = { onMoreOptionsClick(postData) },
                        modifier = Modifier.size(32.dp).padding(start = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = Color.Gray
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(32.dp).padding(start = 4.dp))
                }
            }

            if (!postData.caption.isNullOrBlank()) {
                Text(
                    text = postData.caption,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White.copy(alpha = 0.90f),
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = if (postData.postImageUrl != null) 10.dp else 0.dp),
                    maxLines = 10,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                if (postData.postImageUrl != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            if (!postData.postImageUrl.isNullOrBlank()) {
                PostImageItemView(
                    imageUrl = postData.postImageUrl,
                    contentDescription = "Post image by ${postData.username}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp) // Padding agar gambar tidak terlalu mepet ke tepi card
                        .clip(RoundedCornerShape(12.dp)) // Bentuk rounded untuk gambar post
                        .aspectRatio(16f / 9f) // Contoh aspect ratio, sesuaikan jika perlu
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
        userProfileImageUrl = null, // Akan menggunakan DEFAULT_PROFILE_IMAGE_URL
        postImageUrl = "https://via.placeholder.com/600x400", // Contoh URL gambar post
        postCreatedAt = "2h ago",
        postUpdatedAt = "2h ago"
    )
    MaterialTheme {
        Column(Modifier.background(Color(0xFF191A1F)).padding(vertical=8.dp)) {
            PostItem(
                postData = dummyPostWithImage,
                onMoreOptionsClick = {},
                onUsernameClick = {},
                currentLoggedInUserId = "user001" // Asumsikan ini post milik user yang login
            )
        }
    }
}
