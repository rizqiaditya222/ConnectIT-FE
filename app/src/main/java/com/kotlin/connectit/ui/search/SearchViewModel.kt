package com.kotlin.connectit.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlin.connectit.data.api.response.GetAllPostsResponse
import com.kotlin.connectit.data.api.response.GetAllUsersResponse
import com.kotlin.connectit.data.api.response.PostItem
import com.kotlin.connectit.data.api.response.UserData
import com.kotlin.connectit.domain.repository.SearchRepository
import com.kotlin.connectit.domain.repository.UserRepository
import com.kotlin.connectit.ui.home.UiDisplayPost
import com.kotlin.connectit.util.ResultWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
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

enum class SearchTab {
    POSTS, USERS
}

data class SearchUiState(
    val searchText: String = "",
    val selectedTab: SearchTab = SearchTab.POSTS,
    // Mengganti List langsung dengan ResultWrapper untuk hasil pencarian
    val postSearchResult: ResultWrapper<List<UiDisplayPost>>? = null,
    val userSearchResult: ResultWrapper<List<UserData>>? = null,
    val isLoading: Boolean = false,
    val searchPerformed: Boolean = false, // Tetap berguna untuk UI (misal tampilkan "tidak ada hasil")
    val errorMessage: String? = null // Untuk error validasi atau error umum dari ResultWrapper
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    private val OLD_IMAGE_DOMAIN = "raion-battlepass.elginbrian.com"
    private val NEW_IMAGE_DOMAIN = "connect-it.elginbrian.com"

    fun onSearchTextChanged(text: String) {
        _uiState.update {
            it.copy(
                searchText = text,
                searchPerformed = false,
                errorMessage = null, // Hapus error validasi sebelumnya
                postSearchResult = null, // Hapus hasil pencarian sebelumnya
                userSearchResult = null
            )
        }
        searchJob?.cancel()
        if (text.isNotEmpty()) {
            searchJob = viewModelScope.launch {
                performSearch()
            }
        } else {
            _uiState.update { it.copy(postSearchResult = null, userSearchResult = null) }
        }
    }

    fun onTabSelected(tab: SearchTab) {
        _uiState.update {
            it.copy(
                selectedTab = tab,
                postSearchResult = null, // Hapus hasil dari tab sebelumnya
                userSearchResult = null,
                searchPerformed = false,
                errorMessage = null
            )
        }
        if (_uiState.value.searchText.length > 2) {
            performSearch()
        }
    }

    private fun validateSearchQuery(): Boolean {
        val query = _uiState.value.searchText
        if (query.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Kata kunci pencarian tidak boleh kosong", searchPerformed = true) }
            return false
        }
        _uiState.update { it.copy(errorMessage = null) }
        return true
    }

    fun performSearch() {
        if (!validateSearchQuery()) {
            _uiState.update { it.copy(isLoading = false, postSearchResult = null, userSearchResult = null) } // Pastikan loading false
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, postSearchResult = null, userSearchResult = null) }
            val query = _uiState.value.searchText // Ambil query terbaru

            when (_uiState.value.selectedTab) {
                SearchTab.POSTS -> {
                    val postsResult = executePostSearch(query)
                    _uiState.update { it.copy(postSearchResult = postsResult, searchPerformed = true) }
                }
                SearchTab.USERS -> {
                    val usersResult = executeUserSearch(query)
                    _uiState.update { it.copy(userSearchResult = usersResult, searchPerformed = true) }
                }
            }
            _uiState.update { it.copy(isLoading = false) }
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
                            val fetchedUserProfileUrl: String? = null
                            if (apiPost.userId.isNotBlank()) {
                                when (val userResult = userRepository.getUserById(apiPost.userId)) {
                                    is ResultWrapper.Success -> {
                                        userResult.data.data?.let { userData ->
                                            userNameDisplay = userData.username
                                            userEmailDisplay = userData.email
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
                ResultWrapper.Success(mappedUiPosts)
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
                // Tidak ada transformasi URL profil karena UserData tidak memiliki field profileImageUrl
                // Jika ada transformasi lain pada UserData, lakukan di sini sebelum return Success
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
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()),
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault()),
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            )
            var date: Date? = null
            for (format in supportedFormats) {
                format.timeZone = TimeZone.getTimeZone("UTC")
                try { date = format.parse(apiTimestamp); if (date != null) break } catch (e: Exception) { /* Lanjutkan */ }
            }
            if (date == null) { System.err.println("Gagal mem-parse tanggal (semua format): $apiTimestamp"); return apiTimestamp }
            val now = System.currentTimeMillis(); val diff = now - date.time
            if (diff < 0) return "baru saja"
            val seconds = diff / 1000; val minutes = seconds / 60; val hours = minutes / 60; val days = hours / 24
            val weeks = days / 7; val months = days / 30; val years = days / 365
            when {
                years > 0 -> "${years}y"; months > 0 -> "${months}mo"; weeks > 0 -> "${weeks}w"
                days > 1 -> "${days}d"; days == 1L -> "1d"; hours > 1 -> "${hours}h"; hours == 1L -> "1h"
                minutes > 1 -> "${minutes}m"; minutes == 1L -> "1m"; seconds > 5 -> "${seconds}s"; else -> "baru saja"
            }
        } catch (e: Exception) { System.err.println("Error format timestamp: $apiTimestamp, Error: ${e.message}"); apiTimestamp }
    }

    fun consumePostSearchResult() {
        _uiState.update { it.copy(postSearchResult = null) }
    }

    fun consumeUserSearchResult() {
        _uiState.update { it.copy(userSearchResult = null) }
    }

    fun consumeErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}