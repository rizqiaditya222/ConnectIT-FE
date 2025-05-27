package com.kotlin.connectit.ui.addpost

import android.app.Application
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlin.connectit.data.api.TokenManager // Tidak secara langsung digunakan, tapi PostRepositoryImpl memakainya
import com.kotlin.connectit.data.api.response.CreatePostResponse
import com.kotlin.connectit.domain.repository.PostRepository //
import com.kotlin.connectit.util.DataRefreshTrigger // Import DataRefreshTrigger
import com.kotlin.connectit.util.ResultWrapper //
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject

data class AddPostUiState(
    val caption: String = "",
    val selectedImageUri: Uri? = null, // Menggunakan satu Uri, nullable
    val isLoading: Boolean = false,
    val createPostResult: ResultWrapper<CreatePostResponse>? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class AddPostViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val application: Application,
    private val dataRefreshTrigger: DataRefreshTrigger // Injeksi DataRefreshTrigger
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddPostUiState())
    val uiState: StateFlow<AddPostUiState> = _uiState.asStateFlow()

    fun onCaptionChanged(caption: String) {
        _uiState.update { it.copy(caption = caption, errorMessage = null, createPostResult = null) }
    }

    fun onImageSelected(uri: Uri?) {
        _uiState.update { it.copy(selectedImageUri = uri, errorMessage = null, createPostResult = null) }
    }

    private fun uriToFile(context: Context, uri: Uri, fileNamePrefix: String): File? {
        return try {
            val contentResolver = context.contentResolver
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            if (inputStream == null) {
                System.err.println("InputStream null untuk URI: $uri")
                return null
            }

            var fileName = "$fileNamePrefix.tmp"
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (displayNameIndex != -1) {
                        val displayName = cursor.getString(displayNameIndex)
                        if (!displayName.isNullOrEmpty()) {
                            fileName = displayName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
                        }
                    }
                }
            }

            val fileExtension = contentResolver.getType(uri)?.substringAfterLast('/') ?: "tmp"
            val saneFileName = fileName.substringBeforeLast('.', fileNamePrefix)

            val tempFile = File.createTempFile(
                saneFileName,
                ".$fileExtension",
                context.cacheDir
            )
            tempFile.deleteOnExit()

            FileOutputStream(tempFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            inputStream.close()
            tempFile
        } catch (e: Exception) {
            System.err.println("Error membuat file dari URI: $uri, Error: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    fun attemptCreatePost() {
        val currentState = _uiState.value
        if (currentState.caption.isBlank() && currentState.selectedImageUri == null) {
            _uiState.update { it.copy(errorMessage = "Caption atau gambar tidak boleh kosong") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, createPostResult = null) }

            val captionRequestBody = currentState.caption.trim()
                .toRequestBody("text/plain".toMediaTypeOrNull())

            var imagePart: MultipartBody.Part? = null
            currentState.selectedImageUri?.let { uri ->
                val imageFile = uriToFile(application.applicationContext, uri, "post_image")
                if (imageFile != null) {
                    val imageRequestBody = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                    imagePart = MultipartBody.Part.createFormData("image", imageFile.name, imageRequestBody)
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Gagal memproses gambar") }
                    return@launch
                }
            }

            val finalCaptionRequestBody = if (currentState.caption.isBlank() && imagePart != null) {
                "".toRequestBody("text/plain".toMediaTypeOrNull())
            } else {
                captionRequestBody
            }

            val result = postRepository.createPost(finalCaptionRequestBody, imagePart)
            if (result is ResultWrapper.Success) {
                dataRefreshTrigger.triggerRefresh() // Picu refresh setelah berhasil
            }
            _uiState.update { it.copy(isLoading = false, createPostResult = result) }
        }
    }

    fun clearForm() {
        _uiState.update {
            it.copy(
                caption = "",
                selectedImageUri = null,
                isLoading = false,
                errorMessage = null
                // createPostResult tidak di-reset di sini, tapi di consumeCreatePostResult
            )
        }
    }

    fun consumeCreatePostResult() {
        _uiState.update { it.copy(createPostResult = null) }
    }

    fun consumeErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
