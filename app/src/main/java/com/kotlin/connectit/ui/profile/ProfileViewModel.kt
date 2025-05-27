package com.kotlin.connectit.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlin.connectit.data.api.TokenManager
import com.kotlin.connectit.data.api.response.DeletePostResponse
import com.kotlin.connectit.domain.repository.AuthRepository
import com.kotlin.connectit.domain.repository.PostRepository
import com.kotlin.connectit.domain.repository.UserRepository
import com.kotlin.connectit.data.api.response.PostItem as ApiPostItem // Alias
import com.kotlin.connectit.data.api.response.UserData as ApiUserData // Alias
import com.kotlin.connectit.ui.home.UiDisplayPost // Impor UiDisplayPost
import com.kotlin.connectit.util.ResultWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val userRepository: UserRepository // Diperlukan untuk mengambil detail user pembuat post
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    // Konstanta untuk transformasi URL (jika masih relevan dan belum global)
    private val OLD_IMAGE_DOMAIN = "raion-battlepass.elginbrian.com"
    private val NEW_IMAGE_DOMAIN = "connect-it.elginbrian.com"


    init {
        loadUserProfileAndPosts()
    }

    fun loadUserProfileAndPosts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingUserProfile = true, errorMessage = null) }
            when (val result = authRepository.getCurrentUser()) {
                is ResultWrapper.Success -> {
                    val apiUserData = result.data.data
                    if (apiUserData != null) {
                        _uiState.update {
                            it.copy(
                                userProfileInfo = UserProfileHeaderInfo(
                                    userId = apiUserData.id,
                                    username = apiUserData.username,
                                    userEmail = apiUserData.email, // Atau format @username jika mau
                                    profileImageUrl = transformImageUrl(null) // API UserData belum ada profile image URL
                                ),
                                isLoadingUserProfile = false
                            )
                        }
                        loadUserPosts(apiUserData.id) // Muat post setelah mendapatkan userId
                    } else {
                        _uiState.update {
                            it.copy(isLoadingUserProfile = false, errorMessage = "Gagal mendapatkan data pengguna.")
                        }
                    }
                }
                is ResultWrapper.Error -> {
                    _uiState.update {
                        it.copy(isLoadingUserProfile = false, errorMessage = result.message ?: "Gagal memuat profil")
                    }
                }
            }
        }
    }

    private fun loadUserPosts(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingUserPosts = true, errorMessage = null) }
            when (val postsResult = postRepository.getPostsByUserId(userId)) {
                is ResultWrapper.Success -> {
                    val apiPosts = postsResult.data.data ?: emptyList()
                    // Untuk post di halaman profil, user pembuatnya adalah user yang sedang dilihat profilnya
                    // Jadi kita bisa langsung gunakan data user yang sudah ada.
                    val currentUserInfo = _uiState.value.userProfileInfo
                    val mappedPosts = apiPosts.map { apiPost ->
                        mapToUiDisplayPost(
                            apiPost = apiPost,
                            userId = currentUserInfo.userId, // Sudah pasti userId dari profil ini
                            username = currentUserInfo.username,
                            userEmail = currentUserInfo.userEmail,
                            fetchedUserProfileImageUrl = currentUserInfo.profileImageUrl // URL profil dari user saat ini
                        )
                    }
                    _uiState.update {
                        it.copy(isLoadingUserPosts = false, userPosts = mappedPosts)
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
        userId: String,
        username: String,
        userEmail: String,
        fetchedUserProfileImageUrl: String?
    ): UiDisplayPost {
        val transformedUserProfileImageUrl = transformImageUrl(fetchedUserProfileImageUrl)
        val transformedPostImageUrl = transformImageUrl(apiPost.imageUrl)

        return UiDisplayPost(
            postId = apiPost.id,
            userId = userId, // dari parameter
            username = username, // dari parameter
            userEmail = userEmail, // dari parameter
            caption = apiPost.caption,
            userProfileImageUrl = transformedUserProfileImageUrl,
            postImageUrl = transformedPostImageUrl,
            postCreatedAt = formatApiTimestampToRelative(apiPost.createdAt),
            postUpdatedAt = if (apiPost.updatedAt != null && apiPost.createdAt != apiPost.updatedAt) {
                formatApiTimestampToRelative(apiPost.updatedAt)
            } else {
                formatApiTimestampToRelative(apiPost.createdAt)
            }
        )
    }

    private fun formatApiTimestampToRelative(apiTimestamp: String?): String {
        if (apiTimestamp.isNullOrBlank()) return "Beberapa waktu lalu"
        return try {
            val supportedFormats = listOf(
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault()),
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            )
            var date: Date? = null
            for (format in supportedFormats) {
                format.timeZone = TimeZone.getTimeZone("UTC")
                try {
                    date = format.parse(apiTimestamp)
                    if (date != null) break
                } catch (e: Exception) { /* Lanjutkan */ }
            }
            if (date == null) return apiTimestamp
            val now = System.currentTimeMillis()
            val diff = now - date.time
            val seconds = diff / 1000; val minutes = seconds / 60; val hours = minutes / 60; val days = hours / 24
            val weeks = days / 7; val months = days / 30; val years = days / 365
            when {
                years > 0 -> "${years}y"; months > 0 -> "${months}mo"; weeks > 0 -> "${weeks}w"
                days > 1 -> "${days}d"; days == 1L -> "1d"; hours > 1 -> "${hours}h"
                hours == 1L -> "1h"; minutes > 1 -> "${minutes}m"; minutes == 1L -> "1m"
                seconds > 5 -> "${seconds}s"; else -> "baru saja"
            }
        } catch (e: Exception) { apiTimestamp }
    }


    fun onShowBottomSheet(post: UiDisplayPost) {
        _uiState.update { it.copy(showBottomSheet = true, selectedPostForOptions = post) }
    }

    fun onDismissBottomSheet() {
        _uiState.update { it.copy(showBottomSheet = false, selectedPostForOptions = null) }
    }

    fun attemptLogout(onLoggedOut: () -> Unit) {
        TokenManager.clearToken()
        // Di sini Anda bisa menambahkan logika tambahan jika ada, misal membersihkan cache
        _uiState.update { it.copy(userProfileInfo = UserProfileHeaderInfo(), userPosts = emptyList()) } // Reset state UI
        onLoggedOut() // Panggil callback untuk navigasi
    }

    fun attemptDeletePost(postId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingUserPosts = true) } // Bisa juga flag loading spesifik untuk delete
            val result = postRepository.deletePost(postId)
            _uiState.update { it.copy(deletePostResult = result) }
            if (result is ResultWrapper.Success) {
                // Muat ulang daftar post setelah berhasil hapus
                _uiState.value.userProfileInfo.userId.takeIf { it.isNotBlank() }?.let {
                    loadUserPosts(it)
                }
            } else if (result is ResultWrapper.Error) {
                _uiState.update { it.copy(isLoadingUserPosts = false, errorMessage = result.message ?: "Gagal menghapus post") }
            }
        }
    }

    fun consumeDeletePostResult() {
        _uiState.update { it.copy(deletePostResult = null) }
    }

    fun consumeErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}