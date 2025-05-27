package com.kotlin.connectit.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlin.connectit.data.api.response.DeletePostResponse
import com.kotlin.connectit.domain.repository.AuthRepository
import com.kotlin.connectit.domain.repository.PostRepository
import com.kotlin.connectit.domain.repository.UserRepository
import com.kotlin.connectit.util.DataRefreshTrigger // Import DataRefreshTrigger
import com.kotlin.connectit.util.ResultWrapper
import com.kotlin.connectit.data.api.response.PostItem as ApiPostItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest // Menggunakan collectLatest untuk re-subscribe jika perlu
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
    val searchText: String = "", // Mungkin tidak digunakan aktif di Home, lebih ke SearchScreen
    val loggedInUserId: String? = null,
    val showBottomSheet: Boolean = false,
    val selectedPostForOptions: UiDisplayPost? = null,
    val deletePostResult: ResultWrapper<DeletePostResponse>? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val dataRefreshTrigger: DataRefreshTrigger // Injeksi DataRefreshTrigger
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val OLD_IMAGE_DOMAIN = "raion-battlepass.elginbrian.com"
    private val NEW_IMAGE_DOMAIN = "connect-it.elginbrian.com"

    init {
        fetchLoggedInUserId() // Ambil ID user yang login
        loadPosts() // Muat post saat inisialisasi

        // Observasi perubahan data dari DataRefreshTrigger
        viewModelScope.launch {
            dataRefreshTrigger.onDataChanged.collectLatest {
                loadPosts() // Muat ulang post ketika ada trigger
            }
        }
    }

    private fun fetchLoggedInUserId() {
        viewModelScope.launch {
            when (val result = authRepository.getCurrentUser()) {
                is ResultWrapper.Success -> {
                    _uiState.update { it.copy(loggedInUserId = result.data.data?.id) }
                }
                is ResultWrapper.Error -> {
                    System.err.println("HomeViewModel: Failed to get loggedInUserId: ${result.message}")
                    _uiState.update { it.copy(loggedInUserId = null, errorMessage = if (result.code == 401) "Sesi berakhir, silakan login kembali." else result.message) }
                }
            }
        }
    }

    fun onSearchTextChanged(text: String) {
        _uiState.update { it.copy(searchText = text) }
        // Logika filter atau search bisa ditambahkan di sini jika search bar di Home aktif
    }

    private fun transformImageUrl(originalUrl: String?): String? {
        if (originalUrl.isNullOrBlank()) {
            return originalUrl
        }
        if (originalUrl.contains(OLD_IMAGE_DOMAIN)) {
            return when {
                originalUrl.startsWith("https://$OLD_IMAGE_DOMAIN") ->
                    originalUrl.replaceFirst("https://$OLD_IMAGE_DOMAIN", "https://$NEW_IMAGE_DOMAIN")
                originalUrl.startsWith("http://$OLD_IMAGE_DOMAIN") ->
                    originalUrl.replaceFirst("http://$OLD_IMAGE_DOMAIN", "https://$NEW_IMAGE_DOMAIN")
                else ->
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

                            if (apiPost.userId.isNotBlank()) {
                                when (val userResult = userRepository.getUserById(apiPost.userId)) {
                                    is ResultWrapper.Success -> {
                                        userResult.data.data?.let { userData ->
                                            userNameDisplay = userData.username
                                            userEmailDisplay = userData.email
                                            fetchedUserProfileUrl = userData.profileImageUrl
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
                            posts = uiDisplayPostsDeferred.awaitAll().sortedByDescending { post -> post.postUpdatedAt }
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
        val transformedUserProfileImageUrl = transformImageUrl(fetchedUserProfileImageUrl)
        val transformedPostImageUrl = transformImageUrl(apiPost.imageUrl)

        return UiDisplayPost(
            postId = apiPost.id,
            userId = apiPost.userId,
            username = username,
            userEmail = userEmail,
            caption = apiPost.caption,
            userProfileImageUrl = transformedUserProfileImageUrl,
            postImageUrl = transformedPostImageUrl,
            postCreatedAt = formatApiTimestampToRelative(apiPost.createdAt),
            postUpdatedAt = formatApiTimestampToRelative(apiPost.updatedAt ?: apiPost.createdAt) // Pastikan ada fallback dan format
        )
    }

    private fun formatApiTimestampToRelative(apiTimestamp: String?): String {
        if (apiTimestamp.isNullOrBlank()) return "Beberapa waktu lalu"
        return try {
            val supportedFormats = listOf(
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault()),
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()),
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            )
            var date: Date? = null
            for (format in supportedFormats) {
                format.timeZone = TimeZone.getTimeZone("UTC")
                try { date = format.parse(apiTimestamp); if (date != null) break } catch (e: Exception) { }
            }
            if (date == null) { System.err.println("Gagal mem-parse tanggal (HomeVM): $apiTimestamp"); return apiTimestamp }
            val now = System.currentTimeMillis(); val diff = now - date.time
            if (diff < 0) return "baru saja"
            val seconds = diff / 1000; val minutes = seconds / 60; val hours = minutes / 60; val days = hours / 24
            val weeks = days / 7; val months = days / 30; val years = days / 365
            when {
                years > 0 -> "${years}y"; months > 0 -> "${months}mo"; weeks > 0 -> "${weeks}w"
                days > 1 -> "${days}d"; days == 1L -> "1d"; hours > 1 -> "${hours}h"; hours == 1L -> "1h"
                minutes > 1 -> "${minutes}m"; minutes == 1L -> "1m"; seconds > 5 -> "${seconds}s"; else -> "baru saja"
            }
        } catch (e: Exception) { System.err.println("Error format timestamp (HomeVM): $apiTimestamp, Error: ${e.message}"); apiTimestamp }
    }

    fun onShowBottomSheet(post: UiDisplayPost) {
        _uiState.update { it.copy(showBottomSheet = true, selectedPostForOptions = post) }
    }

    fun onDismissBottomSheet() {
        _uiState.update { it.copy(showBottomSheet = false, selectedPostForOptions = null) }
    }

    fun attemptDeletePost(postId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) } // Bisa tambahkan loading state untuk delete
            val result = postRepository.deletePost(postId)
            if (result is ResultWrapper.Success) {
                dataRefreshTrigger.triggerRefresh() // Picu refresh global
            } else if (result is ResultWrapper.Error) {
                _uiState.update { it.copy(errorMessage = result.message ?: "Gagal menghapus post") }
            }
            // Hasil spesifik (sukses/gagal) untuk UI Toast/Snackbar
            _uiState.update { it.copy(deletePostResult = result, isLoading = false) }
        }
    }

    fun consumeDeletePostResult() {
        _uiState.update { it.copy(deletePostResult = null) }
    }

    fun consumeErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun triggerPostOptions(post: UiDisplayPost) {
        if (post.userId == _uiState.value.loggedInUserId) {
            onShowBottomSheet(post)
        } else {
            _uiState.update { it.copy(errorMessage = "Anda hanya dapat mengelola post Anda sendiri.") }
        }
    }
}
