package com.kotlin.connectit.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlin.connectit.data.api.TokenManager
import com.kotlin.connectit.data.api.response.DeletePostResponse
import com.kotlin.connectit.domain.repository.AuthRepository
import com.kotlin.connectit.domain.repository.PostRepository
import com.kotlin.connectit.data.api.response.PostItem as ApiPostItem
import com.kotlin.connectit.ui.home.UiDisplayPost
import com.kotlin.connectit.util.DataRefreshTrigger // Import DataRefreshTrigger
import com.kotlin.connectit.util.ResultWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest // Menggunakan collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

data class UserProfileHeaderInfo(
    val userId: String = "",
    val username: String = "Username",
    val userEmail: String = "email@example.com",
    val profileImageUrl: String? = null
)

data class ProfileUiState(
    val userProfileInfo: UserProfileHeaderInfo = UserProfileHeaderInfo(),
    val userPosts: List<UiDisplayPost> = emptyList(),
    val isLoadingUserProfile: Boolean = true,
    val isLoadingUserPosts: Boolean = false,
    val errorMessage: String? = null,
    val showBottomSheet: Boolean = false,
    val selectedPostForOptions: UiDisplayPost? = null,
    val deletePostResult: ResultWrapper<DeletePostResponse>? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val postRepository: PostRepository,
    private val dataRefreshTrigger: DataRefreshTrigger // Injeksi DataRefreshTrigger
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val OLD_IMAGE_DOMAIN = "raion-battlepass.elginbrian.com"
    private val NEW_IMAGE_DOMAIN = "connect-it.elginbrian.com"

    init {
        loadUserProfileAndPosts() // Muat data saat inisialisasi

        // Observasi perubahan data dari DataRefreshTrigger
        viewModelScope.launch {
            dataRefreshTrigger.onDataChanged.collectLatest {
                // Cek apakah ID user saat ini masih valid sebelum refresh
                // Ini penting jika user logout dan ProfileViewModel masih aktif sesaat
                if (_uiState.value.userProfileInfo.userId.isNotBlank()) {
                    loadUserProfileAndPosts()
                }
            }
        }
    }

    fun loadUserProfileAndPosts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingUserProfile = true, errorMessage = null) }
            when (val result = authRepository.getCurrentUser()) {
                is ResultWrapper.Success -> {
                    val apiUserData = result.data.data
                    if (apiUserData != null) {
                        val currentUserProfileInfo = UserProfileHeaderInfo(
                            userId = apiUserData.id,
                            username = apiUserData.username,
                            userEmail = apiUserData.email,
                            profileImageUrl = transformImageUrl(apiUserData.profileImageUrl)
                        )
                        _uiState.update {
                            it.copy(
                                userProfileInfo = currentUserProfileInfo,
                                isLoadingUserProfile = false
                            )
                        }
                        loadUserPosts(apiUserData.id, currentUserProfileInfo) // Kirim info profil saat ini
                    } else {
                        _uiState.update {
                            it.copy(isLoadingUserProfile = false, errorMessage = "Gagal mendapatkan data pengguna.")
                        }
                    }
                }
                is ResultWrapper.Error -> {
                    val message = if (result.code == 401) {
                        "Sesi tidak valid atau telah berakhir. Silakan login kembali."
                    } else {
                        result.message ?: "Gagal memuat profil pengguna."
                    }
                    _uiState.update {
                        it.copy(isLoadingUserProfile = false, errorMessage = message)
                    }
                }
            }
        }
    }

    private fun loadUserPosts(userId: String, currentUserInfo: UserProfileHeaderInfo) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingUserPosts = true, errorMessage = null) }
            when (val postsResult = postRepository.getPostsByUserId(userId)) {
                is ResultWrapper.Success -> {
                    val apiPosts = postsResult.data.data ?: emptyList()
                    val mappedPosts = apiPosts.map { apiPost ->
                        mapToUiDisplayPost(
                            apiPost = apiPost,
                            // Gunakan currentUserInfo yang sudah ditransformasi
                            userId = currentUserInfo.userId, // Ini adalah ID pemilik profil (user yang login)
                            username = currentUserInfo.username,
                            userEmail = currentUserInfo.userEmail,
                            fetchedUserProfileImageUrl = currentUserInfo.profileImageUrl
                        )
                    }
                    _uiState.update {
                        it.copy(isLoadingUserPosts = false, userPosts = mappedPosts.sortedByDescending { post -> post.postUpdatedAt })
                    }
                }
                is ResultWrapper.Error -> {
                    _uiState.update {
                        it.copy(isLoadingUserPosts = false, errorMessage = postsResult.message ?: "Gagal memuat post pengguna")
                    }
                }
            }
        }
    }

    private fun transformImageUrl(originalUrl: String?): String? {
        if (originalUrl.isNullOrBlank()) return originalUrl
        if (originalUrl.contains(OLD_IMAGE_DOMAIN)) {
            return when {
                originalUrl.startsWith("https://$OLD_IMAGE_DOMAIN") ->
                    originalUrl.replaceFirst("https://$OLD_IMAGE_DOMAIN", "https://$NEW_IMAGE_DOMAIN")
                originalUrl.startsWith("http://$OLD_IMAGE_DOMAIN") ->
                    originalUrl.replaceFirst("http://$OLD_IMAGE_DOMAIN", "https://$NEW_IMAGE_DOMAIN")
                else -> originalUrl.replaceFirst(OLD_IMAGE_DOMAIN, NEW_IMAGE_DOMAIN)
            }
        }
        return originalUrl
    }

    private fun mapToUiDisplayPost(
        apiPost: ApiPostItem,
        userId: String, // Ini adalah ID user pemilik profil (yang sedang dilihat profilnya)
        username: String, // Username pemilik profil
        userEmail: String, // Email pemilik profil
        fetchedUserProfileImageUrl: String? // URL gambar profil pemilik profil
    ): UiDisplayPost {
        val transformedPostImageUrl = transformImageUrl(apiPost.imageUrl)
        // Untuk post item, username dan profile image harusnya dari post.userId, bukan dari profil yang sedang dilihat
        // Namun, karena kita memanggil getPostsByUserId, semua post ini adalah milik 'userId'
        // Jadi, username, userEmail, dan fetchedUserProfileImageUrl di sini adalah milik pembuat post (yaitu user yang profilnya sedang dilihat)

        return UiDisplayPost(
            postId = apiPost.id,
            userId = apiPost.userId, // Seharusnya ini sama dengan 'userId' parameter
            username = username, // Username dari pemilik post (yang profilnya dilihat)
            userEmail = userEmail, // Email dari pemilik post
            caption = apiPost.caption,
            userProfileImageUrl = fetchedUserProfileImageUrl, // Gambar profil pemilik post
            postImageUrl = transformedPostImageUrl,
            postCreatedAt = formatApiTimestampToRelative(apiPost.createdAt),
            postUpdatedAt = formatApiTimestampToRelative(apiPost.updatedAt ?: apiPost.createdAt)
        )
    }

    private fun formatApiTimestampToRelative(apiTimestamp: String?): String {
        if (apiTimestamp.isNullOrBlank()) return "Beberapa waktu lalu"
        return try {
            val supportedFormats = listOf(
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()),
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault()),
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            )
            var date: Date? = null
            for (format in supportedFormats) {
                format.timeZone = TimeZone.getTimeZone("UTC")
                try { date = format.parse(apiTimestamp); if (date != null) break } catch (e: Exception) { }
            }
            if (date == null) { System.err.println("Gagal mem-parse tanggal (ProfileVM): $apiTimestamp"); return apiTimestamp }
            val now = System.currentTimeMillis(); val diff = now - date.time
            if (diff < 0) return "baru saja"
            val seconds = diff / 1000; val minutes = seconds / 60; val hours = minutes / 60; val days = hours / 24
            val weeks = days / 7; val months = days / 30; val years = days / 365
            when {
                years > 0 -> "${years}y"; months > 0 -> "${months}mo"; weeks > 0 -> "${weeks}w"
                days > 1 -> "${days}d"; days == 1L -> "1d"; hours > 1 -> "${hours}h"; hours == 1L -> "1h"
                minutes > 1 -> "${minutes}m"; minutes == 1L -> "1m"; seconds > 5 -> "${seconds}s"; else -> "baru saja"
            }
        } catch (e: Exception) { System.err.println("Error format timestamp (ProfileVM): $apiTimestamp, Error: ${e.message}"); apiTimestamp }
    }


    fun onShowBottomSheet(post: UiDisplayPost) {
        _uiState.update { it.copy(showBottomSheet = true, selectedPostForOptions = post) }
    }

    fun onDismissBottomSheet() {
        _uiState.update { it.copy(showBottomSheet = false, selectedPostForOptions = null) }
    }

    fun attemptLogout(onLoggedOut: () -> Unit) {
        TokenManager.clearToken()
        _uiState.update { ProfileUiState() } // Reset state ke default
        onLoggedOut()
        viewModelScope.launch { // Picu refresh agar layar lain (jika ada) tahu user sudah logout
            dataRefreshTrigger.triggerRefresh()
        }
    }

    fun attemptDeletePost(postId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingUserPosts = true) } // Bisa tambahkan loading state untuk delete
            val result = postRepository.deletePost(postId)
            if (result is ResultWrapper.Success) {
                dataRefreshTrigger.triggerRefresh() // Picu refresh global
            } else if (result is ResultWrapper.Error) {
                _uiState.update { it.copy(errorMessage = result.message ?: "Gagal menghapus post") }
            }
            // Hasil spesifik (sukses/gagal) untuk UI Toast/Snackbar
            _uiState.update { it.copy(deletePostResult = result, isLoadingUserPosts = false) }
        }
    }

    fun consumeDeletePostResult() {
        _uiState.update { it.copy(deletePostResult = null) }
    }

    fun consumeErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
