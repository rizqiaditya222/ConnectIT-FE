package com.kotlin.connectit.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlin.connectit.domain.repository.PostRepository
import com.kotlin.connectit.domain.repository.UserRepository
import com.kotlin.connectit.util.ResultWrapper
import com.kotlin.connectit.data.api.response.PostItem as ApiPostItem
import com.kotlin.connectit.data.api.response.UserData as ApiUserData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import java.util.TimeZone
import javax.inject.Inject

data class HomeUiState(
    val posts: List<UiDisplayPost> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchText: String = ""
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val OLD_IMAGE_DOMAIN = "raion-battlepass.elginbrian.com"
    private val NEW_IMAGE_DOMAIN = "connect-it.elginbrian.com"

    init {
        loadPosts()
    }

    fun onSearchTextChanged(text: String) {
        _uiState.update { it.copy(searchText = text) }
        // TODO: Implement search logic
    }

    // Fungsi helper untuk transformasi URL
    private fun transformImageUrl(originalUrl: String?): String? {
        if (originalUrl.isNullOrBlank()) {
            return originalUrl
        }
        // Cek apakah URL mengandung domain lama dan ganti jika ada
        if (originalUrl.contains(OLD_IMAGE_DOMAIN)) {
            // Coba ganti dengan mempertahankan scheme (http/https)
            // dan default ke https untuk domain baru jika scheme lama adalah http
            return when {
                originalUrl.startsWith("https://$OLD_IMAGE_DOMAIN") ->
                    originalUrl.replaceFirst("https://$OLD_IMAGE_DOMAIN", "https://$NEW_IMAGE_DOMAIN")
                originalUrl.startsWith("http://$OLD_IMAGE_DOMAIN") ->
                    originalUrl.replaceFirst("http://$OLD_IMAGE_DOMAIN", "https://$NEW_IMAGE_DOMAIN") // Selalu ke HTTPS untuk domain baru
                else -> // Fallback jika scheme tidak standar atau tidak ada, tapi domain cocok
                    originalUrl.replaceFirst(OLD_IMAGE_DOMAIN, NEW_IMAGE_DOMAIN)
            }
        }
        return originalUrl
    }

    fun loadPosts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val postsResult = postRepository.getAllPosts()) {
                is ResultWrapper.Success -> {
                    val apiPosts = postsResult.data.data ?: emptyList()
                    val uiDisplayPostsDeferred = apiPosts.map { apiPost ->
                        async {
                            var userNameDisplay = "Pengguna"
                            var userEmailDisplay = "N/A"
                            var fetchedUserProfileUrl: String? = null

                            if (apiPost.userId.isNotBlank()) { // Menggunakan apiPost.userId sesuai struktur Anda
                                when (val userResult = userRepository.getUserById(apiPost.userId)) {
                                    is ResultWrapper.Success -> {
                                        userResult.data.data?.let { userData ->
                                            userNameDisplay = userData.username
                                            userEmailDisplay = userData.email
                                            // Jika UserData nantinya memiliki field profileImageUrl, ambil di sini:
                                            // fetchedUserProfileUrl = userData.profileImageUrl
                                        }
                                    }
                                    is ResultWrapper.Error -> {
                                        println("Error fetching user ${apiPost.userId} for post ${apiPost.id}: ${userResult.message}")
                                    }
                                }
                            }
                            mapToUiDisplayPost(
                                apiPost = apiPost,
                                username = userNameDisplay,
                                userEmail = userEmailDisplay,
                                fetchedUserProfileImageUrl = fetchedUserProfileUrl
                            )
                        }
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            posts = uiDisplayPostsDeferred.awaitAll()
                        )
                    }
                }
                is ResultWrapper.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = postsResult.message ?: "Gagal memuat posts"
                        )
                    }
                }
            }
        }
    }

    private fun mapToUiDisplayPost(
        apiPost: ApiPostItem,
        username: String,
        userEmail: String,
        fetchedUserProfileImageUrl: String?
    ): UiDisplayPost {
        // ✨ Terapkan transformasi URL di sini ✨
        val transformedUserProfileImageUrl = transformImageUrl(fetchedUserProfileImageUrl)
        val transformedPostImageUrl = transformImageUrl(apiPost.imageUrl) // Menggunakan apiPost.imageUrl sesuai struktur Anda

        return UiDisplayPost(
            postId = apiPost.id,
            userId = apiPost.userId, // Menggunakan apiPost.userId sesuai struktur Anda
            username = username,
            userEmail = userEmail,
            caption = apiPost.caption,
            userProfileImageUrl = transformedUserProfileImageUrl,
            postImageUrl = transformedPostImageUrl,
            postCreatedAt = formatApiTimestampToRelative(apiPost.createdAt),
            postUpdatedAt = if (apiPost.updatedAt != null && apiPost.createdAt != apiPost.updatedAt) {
                formatApiTimestampToRelative(apiPost.updatedAt)
            } else {
                formatApiTimestampToRelative(apiPost.createdAt) // Tampilkan createdAt jika updatedAt sama atau null
            }
        )
    }

    private fun formatApiTimestampToRelative(apiTimestamp: String?): String {
        if (apiTimestamp.isNullOrBlank()) return "Beberapa waktu lalu"
        return try {
            // Mendeteksi format timestamp yang mungkin berbeda
            val supportedFormats = listOf(
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault()),
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()) // Format tanpa microseconds
            )

            var date: Date? = null
            for (format in supportedFormats) {
                format.timeZone = TimeZone.getTimeZone("UTC") // Pastikan parsing sebagai UTC
                try {
                    date = format.parse(apiTimestamp)
                    if (date != null) break
                } catch (e: Exception) {
                    // Lanjutkan ke format berikutnya
                }
            }

            if (date == null) return apiTimestamp

            val now = System.currentTimeMillis()
            val diff = now - date.time

            val seconds = diff / 1000
            val minutes = seconds / 60
            val hours = minutes / 60
            val days = hours / 24
            val weeks = days / 7
            val months = days / 30 // Perkiraan
            val years = days / 365 // Perkiraan

            when {
                years > 0 -> "${years}y"
                months > 0 -> "${months}mo"
                weeks > 0 -> "${weeks}w"
                days > 1 -> "${days}d"
                days == 1L -> "1d"
                hours > 1 -> "${hours}h"
                hours == 1L -> "1h"
                minutes > 1 -> "${minutes}m"
                minutes == 1L -> "1m"
                seconds > 5 -> "${seconds}s"
                else -> "baru saja"
            }
        } catch (e: Exception) {
            apiTimestamp
        }
    }

    fun consumeErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun handlePostMoreOptions(postId: String) {
        println("More options clicked for post ID: $postId")
        _uiState.update { it.copy(errorMessage = "More options for $postId (Not implemented)") }
    }
}
