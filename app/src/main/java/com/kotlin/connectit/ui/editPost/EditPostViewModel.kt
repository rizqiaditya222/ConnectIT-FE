package com.kotlin.connectit.ui.editpost

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlin.connectit.data.api.request.UpdateCaptionRequest
import com.kotlin.connectit.data.api.response.UpdatePostResponse
import com.kotlin.connectit.domain.repository.PostRepository
import com.kotlin.connectit.util.DataRefreshTrigger // Import DataRefreshTrigger
import com.kotlin.connectit.util.ResultWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditPostUiState(
    val isLoading: Boolean = false,
    val postId: String = "",
    val currentCaption: String = "",
    val currentImageUrl: String? = null,
    val isPostLoaded: Boolean = false,
    val updateResult: ResultWrapper<UpdatePostResponse>? = null,
    val loadError: String? = null,
    val updateError: String? = null
)

@HiltViewModel
class EditPostViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val dataRefreshTrigger: DataRefreshTrigger, // Injeksi DataRefreshTrigger
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditPostUiState())
    val uiState: StateFlow<EditPostUiState> = _uiState.asStateFlow()

    private val postId: String = savedStateHandle.get<String>("postId") ?: ""

    private val OLD_IMAGE_DOMAIN = "raion-battlepass.elginbrian.com"
    private val NEW_IMAGE_DOMAIN = "connect-it.elginbrian.com"

    init {
        if (postId.isNotBlank()) {
            _uiState.update { it.copy(postId = postId) }
            loadPostDetails()
        } else {
            _uiState.update { it.copy(loadError = "Post ID tidak valid.") }
        }
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
                else -> originalUrl.replaceFirst(OLD_IMAGE_DOMAIN, NEW_IMAGE_DOMAIN)
            }
        }
        return originalUrl
    }


    fun loadPostDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadError = null) }
            when (val result = postRepository.getPostById(postId)) {
                is ResultWrapper.Success -> {
                    val postData = result.data.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            currentCaption = postData?.caption ?: "",
                            currentImageUrl = transformImageUrl(postData?.imageUrl),
                            isPostLoaded = true
                        )
                    }
                }
                is ResultWrapper.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loadError = result.message ?: "Gagal memuat detail post."
                        )
                    }
                }
            }
        }
    }

    fun onCaptionChanged(caption: String) {
        _uiState.update { it.copy(currentCaption = caption, updateError = null, updateResult = null) }
    }

    fun attemptUpdatePost() {
        val currentState = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, updateError = null, updateResult = null) }
            val result = postRepository.updatePostCaption(
                postId = postId,
                updateCaptionRequest = UpdateCaptionRequest(caption = currentState.currentCaption.trim())
            )
            if (result is ResultWrapper.Success) {
                dataRefreshTrigger.triggerRefresh() // Picu refresh setelah berhasil
            }
            _uiState.update { it.copy(isLoading = false, updateResult = result) }
        }
    }

    fun consumeUpdateResult() {
        _uiState.update { it.copy(updateResult = null) }
    }

    fun consumeLoadError() {
        _uiState.update { it.copy(loadError = null) }
    }
    fun consumeUpdateError() {
        _uiState.update { it.copy(updateError = null) }
    }
}
