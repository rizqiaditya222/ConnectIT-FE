package com.kotlin.connectit.ui.search // Atau package komponen UI Anda

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
// import androidx.compose.material3.ListItem // Tidak digunakan
// import androidx.compose.material3.ListItemDefaults // Tidak digunakan
// import androidx.compose.material3.MaterialTheme // Tidak digunakan
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kotlin.connectit.R // Pastikan R.drawable.bubble_profile ada
import com.kotlin.connectit.data.api.response.UserData // Impor UserData dari API

// URL default untuk gambar profil jika tidak ada URL dari backend
private const val DEFAULT_PROFILE_IMAGE_URL = "https://i.pinimg.com/474x/81/8a/1b/818a1b89a57c2ee0fb7619b95e11aebd.jpg"

@Composable
fun UserSearchResultItem(
    userData: UserData,
    onClick: (userId: String) -> Unit
) {
    Column(modifier = Modifier.clickable { onClick(userData.id) }) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                // Gunakan profileImageUrl dari UserData jika ada dan tidak kosong,
                // jika tidak, gunakan URL default dari internet.
                model = if (userData.profileImageUrl.isNullOrBlank()) DEFAULT_PROFILE_IMAGE_URL else userData.profileImageUrl,
                placeholder = painterResource(id = R.drawable.bubble_profile), // Menggunakan bubble_profile sebagai placeholder
                error = painterResource(id = R.drawable.bubble_profile), // Menggunakan bubble_profile sebagai error fallback
                contentDescription = "${userData.username}'s profile picture",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color.Gray.copy(alpha = 0.5f), CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = userData.username,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color.White
                )
                Text(
                    text = userData.email, // Atau @username jika lebih disukai
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
        HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.3f), thickness = 0.5.dp, modifier = Modifier.padding(start = 76.dp))
    }
}