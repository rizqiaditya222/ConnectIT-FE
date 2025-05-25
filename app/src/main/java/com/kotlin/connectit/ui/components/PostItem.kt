package com.kotlin.connectit.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert // Import ikon MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton // Import IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kotlin.connectit.R // Pastikan import R sudah benar

@Composable
fun PostItem(
    fullName: String,
    username: String,
    caption: String,
    timestamp: String,
    profileImageRes: Int,
    postImageRes: List<Int>? = null,
    // Parameter baru untuk menampilkan tombol titik tiga dan menangani kliknya
    showMoreOptions: Boolean = false, // Secara default tidak ditampilkan
    onMoreOptionsClick: (Post) -> Unit = {} // Callback saat tombol titik tiga ditekan
) {
    Column {
        Row(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
            Card(modifier = Modifier.size(36.dp).clip(CircleShape)) {
                Image(
                    painter = painterResource(id = profileImageRes),
                    contentDescription = "profile image"
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = fullName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White
                        )
                        Text(
                            text = username,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Light,
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) { // Row untuk waktu dan ikon
                        Text(
                            text = timestamp,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.Gray
                        )
                        if (showMoreOptions) { // Tampilkan ikon hanya jika showMoreOptions true
                            IconButton(
                                onClick = {
                                    // Panggil callback dengan data Post yang relevan
                                    // Di sini kita perlu meneruskan objek Post yang sedang ditampilkan
                                    // Namun, PostItem tidak menerima objek Post secara langsung,
                                    // sehingga kita perlu mengubah PostItem agar menerima objek Post
                                    // Atau, kita bisa membuat callback onMoreOptionsClick menerima id Post.
                                    // Untuk sementara, kita akan mengubah Profile() agar meneruskan PostItem yang lebih lengkap
                                    // dan menggunakan Post di parameter callback onMoreOptionsClick.
                                    // Untuk sekarang, kita akan menganggap PostItem ini ada di dalam LazyColumn yang meneruskan Post.
                                },
                                modifier = Modifier.size(24.dp) // Ukuran tombol ikon
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More options",
                                    tint = Color.Gray
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = caption,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White
                )

                if (!postImageRes.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))

                    when (postImageRes.size) {
                        1 -> {
                            PostImageCard(imageRes = postImageRes[0], modifier = Modifier.fillMaxWidth().height(200.dp))
                        }
                        2 -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                PostImageCard(imageRes = postImageRes[0], modifier = Modifier.weight(1f).height(180.dp))
                                PostImageCard(imageRes = postImageRes[1], modifier = Modifier.weight(1f).height(180.dp))
                            }
                        }
                        3 -> {
                            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                PostImageCard(imageRes = postImageRes[0], modifier = Modifier.fillMaxWidth().height(150.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    PostImageCard(imageRes = postImageRes[1], modifier = Modifier.weight(1f).height(150.dp))
                                    PostImageCard(imageRes = postImageRes[2], modifier = Modifier.weight(1f).height(150.dp))
                                }
                            }
                        }
                        4 -> {
                            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    PostImageCard(imageRes = postImageRes[0], modifier = Modifier.weight(1f).height(150.dp))
                                    PostImageCard(imageRes = postImageRes[1], modifier = Modifier.weight(1f).height(150.dp))
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    PostImageCard(imageRes = postImageRes[2], modifier = Modifier.weight(1f).height(150.dp))
                                    PostImageCard(imageRes = postImageRes[3], modifier = Modifier.weight(1f).height(150.dp))
                                }
                            }
                        }
                        else -> { // Tambahkan kembali LazyRow untuk kasus > 4 gambar
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(postImageRes) { image ->
                                    PostImageCard(imageRes = image, modifier = Modifier.width(200.dp).height(150.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.2f))
        )
    }
}

@Composable
fun PostImageCard(imageRes: Int, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "post image",
            modifier = Modifier.fillMaxSize(),
        )
    }
}