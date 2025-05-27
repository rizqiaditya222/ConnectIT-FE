package com.kotlin.connectit.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlin.connectit.data.api.response.DeletePostResponse
import com.kotlin.connectit.data.api.response.GetAllPostsResponse
import com.kotlin.connectit.data.api.response.GetAllUsersResponse
import com.kotlin.connectit.data.api.response.PostItem
import com.kotlin.connectit.data.api.response.UserData
import com.kotlin.connectit.domain.repository.AuthRepository
import com.kotlin.connectit.domain.repository.PostRepository
import com.kotlin.connectit.domain.repository.SearchRepository
import com.kotlin.connectit.domain.repository.UserRepository
import com.kotlin.connectit.ui.home.UiDisplayPost
import com.kotlin.connectit.util.DataRefreshTrigger // Import DataRefreshTrigger
import com.kotlin.connectit.util.ResultWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest // Menggunakan collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

enum class SearchTab {
    POSTS, USERS
}

data class SearchUiState(
    val searchText: String = "",
    val selectedTab: SearchTab = SearchTab.POSTS,
    val postSearchResult: ResultWrapper<List<UiDisplayPost>>? = null,
    val userSearchResult: ResultWrapper<List<UserData>>? = null,
    val isLoading: Boolean = false,
    val searchPerformed: Boolean = false, // Menandakan apakah pencarian pernah dilakukan
    val errorMessage: String? = null,
    val loggedInUserId: String? = null,
    val showBottomSheet: Boolean = false,
    val selectedPostForOptions: UiDisplayPost? = null,
    val deletePostResult: ResultWrapper<DeletePostResponse>? = null
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val postRepository: PostRepository,
    private val dataRefreshTrigger: DataRefreshTrigger // Injeksi DataRefreshTrigger
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    private val OLD_IMAGE_DOMAIN = "raion-battlepass.elginbrian.com"
    private val NEW_IMAGE_DOMAIN = "connect-it.elginbrian.com"

    init {
        fetchLoggedInUserId()

        // Observasi perubahan data dari DataRefreshTrigger
        viewModelScope.launch {
            dataRefreshTrigger.onDataChanged.collectLatest {
                // Hanya refresh jika pencarian sudah pernah dilakukan dan ada query
                // atau jika tab saat ini adalah POSTS (karena CUD post bisa mempengaruhi hasil umum)
                if (uiState.value.searchPerformed && uiState.value.searchText.isNotEmpty()) {
                    performSearch()
                } else if (uiState.value.selectedTab == SearchTab.POSTS && uiState.value.searchText.isNotEmpty()){
                    // Jika tab post aktif dan ada query, refresh
                    performSearch()
                }
                // Tidak refresh user search secara otomatis kecuali ada event spesifik untuk user
            }
        }

        // Debounce search text changes
        viewModelScope.launch {
            _uiState.debounce(300).collectLatest { state ->
                if (state.searchText.isNotEmpty() && state.searchPerformed) { // Hanya trigger jika searchPerformed true (artinya user sudah menekan search atau enter)
                    // Atau jika ingin live search, hapus kondisi searchPerformed
                    // performSearch()
                } else if (state.searchText.isEmpty() && state.searchPerformed) {
                    // Jika teks dihapus setelah search, reset hasil
                    _uiState.update { it.copy(postSearchResult = null, userSearchResult = null, isLoading = false) }
                }
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
                    _uiState.update { it.copy(loggedInUserId = null) }
                }
            }
        }
    }

    fun onSearchTextChanged(text: String) {
        _uiState.update {
            it.copy(
                searchText = text,
                // searchPerformed tidak diubah di sini, hanya saat performSearch dipanggil
                errorMessage = null,
                // Reset hasil pencarian saat teks berubah agar tidak menampilkan hasil lama
                postSearchResult = if (text.isEmpty()) null else it.postSearchResult,
                userSearchResult = if (text.isEmpty()) null else it.userSearchResult
            )
        }
        // Logika debounce akan menangani pemanggilan performSearch jika diperlukan
        // Jika ingin search otomatis saat mengetik (live search):
        // searchJob?.cancel()
        // if (text.length >= 2) { // Contoh: mulai search jika panjang teks >= 2
        //     searchJob = viewModelScope.launch {
        //         kotlinx.coroutines.delay(300) // Debounce manual sederhana
        //         _uiState.update { it.copy(searchPerformed = true) } // Tandai search dilakukan
        //         performSearch()
        //     }
        // } else if (text.isEmpty()) {
        //     _uiState.update { it.copy(postSearchResult = null, userSearchResult = null, searchPerformed = false, isLoading = false) }
        // }
    }


    fun onTabSelected(tab: SearchTab) {
        val currentSearchText = _uiState.value.searchText
        _uiState.update {
            it.copy(
                selectedTab = tab,
                postSearchResult = null, // Reset hasil saat ganti tab
                userSearchResult = null, // Reset hasil saat ganti tab
                errorMessage = null,
                // searchPerformed dipertahankan jika ada teks, agar search bisa langsung dijalankan
                searchPerformed = currentSearchText.isNotEmpty()
            )
        }
        // Jika ada teks pencarian, langsung lakukan pencarian di tab baru
        if (currentSearchText.isNotEmpty()) {
            performSearch()
        }
    }

    private fun validateSearchQuery(): Boolean {
        val query = _uiState.value.searchText
        if (query.isBlank()) {
            // Tidak set error message di sini, biarkan UI menampilkan prompt awal
            _uiState.update { it.copy(searchPerformed = true, isLoading = false, postSearchResult = null, userSearchResult = null) }
            return false
        }
        _uiState.update { it.copy(errorMessage = null) }
        return true
    }

    fun performSearch() { // Dijadikan public agar bisa dipanggil dari UI (misal tombol search di keyboard)
        if (!validateSearchQuery()) {
            return
        }
        _uiState.update { it.copy(searchPerformed = true) } // Tandai bahwa pencarian telah dilakukan

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val query = _uiState.value.searchText // Ambil query terbaru dari state

            when (_uiState.value.selectedTab) {
                SearchTab.POSTS -> {
                    val postsResult = executePostSearch(query)
                    _uiState.update { it.copy(postSearchResult = postsResult, isLoading = false) }
                }
                SearchTab.USERS -> {
                    val usersResult = executeUserSearch(query)
                    _uiState.update { it.copy(userSearchResult = usersResult, isLoading = false) }
                }
            }
        }
    }

    private suspend fun executePostSearch(query: String): ResultWrapper<List<UiDisplayPost>> {
        return when (val result: ResultWrapper<GetAllPostsResponse> = searchRepository.searchPosts(query)) {
            is ResultWrapper.Success -> {
                val allApiPosts: List<PostItem> = result.data.data ?: emptyList()
                val mappedUiPosts: List<UiDisplayPost> = coroutineScope {
                    allApiPosts.map { apiPost ->
                        async(Dispatchers.IO) {
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
                                        System.err.println("Error fetching user ${apiPost.userId} for post ${apiPost.id}: ${userResult.message}")
                                    }
                                }
                            }
                            mapToUiDisplayPost(apiPost, userNameDisplay, userEmailDisplay, fetchedUserProfileUrl)
                        }
                    }.awaitAll()
                }
                ResultWrapper.Success(mappedUiPosts.sortedByDescending { it.postUpdatedAt })
            }
            is ResultWrapper.Error -> {
                ResultWrapper.Error(result.code, result.message ?: "Gagal mencari post", result.errorBody, result.exception)
            }
        }
    }

    private suspend fun executeUserSearch(query: String): ResultWrapper<List<UserData>> {
        return when (val result: ResultWrapper<GetAllUsersResponse> = searchRepository.searchUsers(query)) {
            is ResultWrapper.Success -> {
                val users: List<UserData> = result.data.data ?: emptyList()
                ResultWrapper.Success(users)
            }
            is ResultWrapper.Error -> {
                ResultWrapper.Error(result.code, result.message ?: "Gagal mencari pengguna", result.errorBody, result.exception)
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
        apiPost: PostItem,
        username: String,
        userEmail: String,
        fetchedUserProfileImageUrl: String?
    ): UiDisplayPost {
        val transformedUserProfileImageUrl = transformImageUrl(fetchedUserProfileImageUrl)
        val transformedPostImageUrl = transformImageUrl(apiPost.imageUrl)
        return UiDisplayPost(
            postId = apiPost.id, userId = apiPost.userId, username = username, userEmail = userEmail,
            caption = apiPost.caption, userProfileImageUrl = transformedUserProfileImageUrl,
            postImageUrl = transformedPostImageUrl, postCreatedAt = formatApiTimestampToRelative(apiPost.createdAt),
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
            if (date == null) { System.err.println("Gagal mem-parse tanggal (SearchVM): $apiTimestamp"); return apiTimestamp }
            val now = System.currentTimeMillis(); val diff = now - date.time
            if (diff < 0) return "baru saja"
            val seconds = diff / 1000; val minutes = seconds / 60; val hours = minutes / 60; val days = hours / 24
            val weeks = days / 7; val months = days / 30; val years = days / 365
            when {
                years > 0 -> "${years}y"; months > 0 -> "${months}mo"; weeks > 0 -> "${weeks}w"
                days > 1 -> "${days}d"; days == 1L -> "1d"; hours > 1 -> "${hours}h"; hours == 1L -> "1h"
                minutes > 1 -> "${minutes}m"; minutes == 1L -> "1m"; seconds > 5 -> "${seconds}s"; else -> "baru saja"
            }
        } catch (e: Exception) { System.err.println("Error format timestamp (SearchVM): $apiTimestamp, Error: ${e.message}"); apiTimestamp }
    }


    fun onShowBottomSheet(post: UiDisplayPost) {
        _uiState.update { it.copy(showBottomSheet = true, selectedPostForOptions = post) }
    }

    fun onDismissBottomSheet() {
        _uiState.update { it.copy(showBottomSheet = false, selectedPostForOptions = null) }
    }

    fun attemptDeletePost(postId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = postRepository.deletePost(postId)
            if (result is ResultWrapper.Success) {
                dataRefreshTrigger.triggerRefresh() // Picu refresh global
            } else if (result is ResultWrapper.Error) {
                _uiState.update { it.copy(errorMessage = result.message ?: "Gagal menghapus post") }
            }
            _uiState.update { it.copy(deletePostResult = result, isLoading = false) }
        }
    }

    fun consumePostSearchResult() {
        _uiState.update { it.copy(postSearchResult = null) }
    }

    fun consumeUserSearchResult() {
        _uiState.update { it.copy(userSearchResult = null) }
    }

    fun consumeDeletePostResult() {
        _uiState.update { it.copy(deletePostResult = null) }
    }

    fun consumeErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
