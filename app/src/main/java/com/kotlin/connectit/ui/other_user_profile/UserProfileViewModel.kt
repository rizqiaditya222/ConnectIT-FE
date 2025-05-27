package com.kotlin.connectit.ui.other_user_profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlin.connectit.domain.repository.PostRepository
import com.kotlin.connectit.domain.repository.UserRepository
import com.kotlin.connectit.data.api.response.PostItem as ApiPostItem
import com.kotlin.connectit.ui.home.UiDisplayPost
import com.kotlin.connectit.ui.profile.UserProfileHeaderInfo
import com.kotlin.connectit.util.DataRefreshTrigger // Import DataRefreshTrigger
import com.kotlin.connectit.util.ResultWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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

data class UserProfileScreenUiState(
    val userProfileInfo: UserProfileHeaderInfo? = null,
    val userPosts: List<UiDisplayPost> = emptyList(),
    val isLoadingProfile: Boolean = true,
    val isLoadingPosts: Boolean = false,
    val errorMessage: String? = null
    // isMyProfile tidak diperlukan karena ini ViewModel khusus untuk profil pengguna lain
)

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val postRepository: PostRepository,
    private val dataRefreshTrigger: DataRefreshTrigger, // Injeksi DataRefreshTrigger
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserProfileScreenUiState())
    val uiState: StateFlow<UserProfileScreenUiState> = _uiState.asStateFlow()

    private val userId: String = savedStateHandle.get<String>("userId") ?: ""

    private val OLD_IMAGE_DOMAIN = "raion-battlepass.elginbrian.com"
    private val NEW_IMAGE_DOMAIN = "connect-it.elginbrian.com"

    init {
        if (userId.isNotBlank()) {
            loadUserProfile(userId)
            // loadUserPosts(userId) akan dipanggil setelah loadUserProfile berhasil atau dari collector

            // Observasi perubahan data dari DataRefreshTrigger
            viewModelScope.launch {
                dataRefreshTrigger.onDataChanged.collectLatest {
                    // Hanya refresh jika userId yang ditampilkan saat ini adalah target
                    // Ini untuk memastikan bahwa jika ada perubahan pada post user lain,
                    // dan kita sedang melihat profil user tersebut, datanya akan diperbarui.
                    if (_uiState.value.userProfileInfo?.userId == this@UserProfileViewModel.userId && this@UserProfileViewModel.userId.isNotBlank()) {
                        loadUserPosts(this@UserProfileViewModel.userId) // Muat ulang hanya post, profil mungkin tidak berubah
                    }
                }
            }
        } else {
            _uiState.update { it.copy(isLoadingProfile = false, errorMessage = "User ID tidak valid.") }
        }
    }

    // Fungsi refreshData bisa digunakan untuk pull-to-refresh manual jika diimplementasikan di UI
    fun refreshData() {
        if (userId.isNotBlank()) {
            loadUserProfile(userId)
            // loadUserPosts(userId) akan dipanggil setelah loadUserProfile berhasil
        }
    }
    private fun loadUserProfile(userIdToLoad: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingProfile = true, errorMessage = null) }
            when (val result = userRepository.getUserById(userIdToLoad)) {
                is ResultWrapper.Success -> {
                    val apiUserData = result.data.data
                    val profileInfo = apiUserData?.let { ud ->
                        UserProfileHeaderInfo(
                            userId = ud.id,
                            username = ud.username,
                            userEmail = ud.email,
                            profileImageUrl = transformImageUrl(ud.profileImageUrl)
                        )
                    }
                    _uiState.update {
                        it.copy(
                            userProfileInfo = profileInfo,
                            isLoadingProfile = false
                        )
                    }
                    // Setelah profil berhasil dimuat, muat post pengguna
                    if (profileInfo != null) {
                        loadUserPosts(userIdToLoad)
                    } else if (apiUserData == null) { // Jika user tidak ditemukan
                        _uiState.update { it.copy(errorMessage = "Pengguna tidak ditemukan.") }
                    }
                }
                is ResultWrapper.Error -> {
                    _uiState.update {
                        it.copy(isLoadingProfile = false, errorMessage = result.message ?: "Gagal memuat profil pengguna.")
                    }
                }
            }
        }
    }

    private fun loadUserPosts(userIdToLoad: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingPosts = true, errorMessage = null) }
            // Ambil userProfileInfo dari state, karena mungkin sudah terisi dari loadUserProfile
            val profileInfo = _uiState.value.userProfileInfo
                ?: userRepository.getUserById(userIdToLoad).let { result -> // Fallback jika belum ada
                    if (result is ResultWrapper.Success && result.data.data != null) {
                        val ud = result.data.data!!
                        UserProfileHeaderInfo(
                            userId = ud.id, username = ud.username, userEmail = ud.email,
                            profileImageUrl = transformImageUrl(ud.profileImageUrl)
                        )
                    } else { UserProfileHeaderInfo(userId = userIdToLoad) } // Fallback minimal
                }

            when (val postsResult = postRepository.getPostsByUserId(userIdToLoad)) {
                is ResultWrapper.Success -> {
                    val apiPosts = postsResult.data.data ?: emptyList()
                    val mappedPosts = coroutineScope {
                        apiPosts.map { apiPost ->
                            async {
                                // Untuk post item di profil orang lain, username dan gambar profil
                                // harusnya dari data user yang profilnya sedang dilihat (profileInfo)
                                // karena semua post ini milik user tersebut.
                                mapToUiDisplayPost(
                                    apiPost = apiPost,
                                    userId = profileInfo.userId, // ID dari user yang profilnya dilihat
                                    username = profileInfo.username,
                                    userEmail = profileInfo.userEmail,
                                    fetchedUserProfileImageUrl = profileInfo.profileImageUrl
                                )
                            }
                        }.awaitAll()
                    }
                    _uiState.update {
                        it.copy(isLoadingPosts = false, userPosts = mappedPosts.sortedByDescending { it.postUpdatedAt })
                    }
                }
                is ResultWrapper.Error -> {
                    _uiState.update {
                        it.copy(isLoadingPosts = false, errorMessage = postsResult.message ?: "Gagal memuat post pengguna.")
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
        userId: String, // ID user pemilik profil
        username: String, // Username pemilik profil
        userEmail: String, // Email pemilik profil
        fetchedUserProfileImageUrl: String? // URL gambar profil pemilik profil
    ): UiDisplayPost {
        val transformedPostImageUrl = transformImageUrl(apiPost.imageUrl)
        // Karena ini adalah post dari user yang profilnya sedang dilihat,
        // maka userId, username, userEmail, fetchedUserProfileImageUrl adalah milik user tersebut.
        return UiDisplayPost(
            postId = apiPost.id,
            userId = apiPost.userId, // Ini seharusnya sama dengan 'userId' parameter
            username = username,
            userEmail = userEmail,
            caption = apiPost.caption,
            userProfileImageUrl = fetchedUserProfileImageUrl,
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
            if (date == null) { System.err.println("Gagal mem-parse tanggal (UserProfileVM): $apiTimestamp"); return apiTimestamp }
            val now = System.currentTimeMillis(); val diff = now - date.time
            if (diff < 0) return "baru saja"
            val seconds = diff / 1000; val minutes = seconds / 60; val hours = minutes / 60; val days = hours / 24
            val weeks = days / 7; val months = days / 30; val years = days / 365
            when {
                years > 0 -> "${years}y"; months > 0 -> "${months}mo"; weeks > 0 -> "${weeks}w"
                days > 1 -> "${days}d"; days == 1L -> "1d"; hours > 1 -> "${hours}h"; hours == 1L -> "1h"
                minutes > 1 -> "${minutes}m"; minutes == 1L -> "1m"; seconds > 5 -> "${seconds}s"; else -> "baru saja"
            }
        } catch (e: Exception) { System.err.println("Error format timestamp (UserProfileVM): $apiTimestamp, Error: ${e.message}"); apiTimestamp }
    }

    fun consumeErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
