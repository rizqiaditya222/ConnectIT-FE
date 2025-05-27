package com.kotlin.connectit.ui.editProfile // Renamed package

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlin.connectit.data.api.request.UpdateUsernameRequest
import com.kotlin.connectit.data.api.response.UpdateUserResponse
import com.kotlin.connectit.domain.repository.AuthRepository
import com.kotlin.connectit.domain.repository.UserRepository
import com.kotlin.connectit.util.DataRefreshTrigger // Import DataRefreshTrigger
import com.kotlin.connectit.util.ResultWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// Renamed UiState
data class EditProfileUiState(
    val username: String = "",
    val email: String = "", // Email will be displayed but not editable
    val profileImageUrl: String? = null, // To hold current profile image
    val isLoading: Boolean = false,
    val isProfileLoaded: Boolean = false,
    val updateProfileResult: ResultWrapper<UpdateUserResponse>? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class EditProfileViewModel @Inject constructor( // Renamed ViewModel
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val dataRefreshTrigger: DataRefreshTrigger // Injeksi DataRefreshTrigger
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        loadCurrentUser()
    }

    fun onUsernameChanged(username: String) {
        _uiState.update { it.copy(username = username, errorMessage = null, updateProfileResult = null) }
    }

    fun loadCurrentUser() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = authRepository.getCurrentUser()) {
                is ResultWrapper.Success -> {
                    val userData = result.data.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            username = userData?.username ?: "",
                            email = userData?.email ?: "",
                            profileImageUrl = userData?.profileImageUrl,
                            isProfileLoaded = true
                        )
                    }
                }
                is ResultWrapper.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message ?: "Gagal memuat data pengguna",
                            isProfileLoaded = true // Tetap true agar UI tidak stuck di loading jika hanya error message
                        )
                    }
                }
            }
        }
    }

    fun attemptUpdateProfile() {
        val currentState = _uiState.value
        if (currentState.username.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Username tidak boleh kosong") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, updateProfileResult = null) }
            // TODO: Handle image update if functionality is added
            // For now, only username is updated via UpdateUsernameRequest
            val result = userRepository.updateAuthenticatedUser(
                UpdateUsernameRequest(username = currentState.username.trim())
            )

            if (result is ResultWrapper.Success) {
                result.data.data?.let { updatedUserData ->
                    _uiState.update {
                        it.copy(
                            username = updatedUserData.username,
                            // profileImageUrl = updatedUserData.profileImageUrl // Jika API mengembalikan URL gambar baru
                        )
                    }
                }
                dataRefreshTrigger.triggerRefresh() // Picu refresh setelah berhasil
            }
            _uiState.update { it.copy(isLoading = false, updateProfileResult = result) }
        }
    }

    fun consumeUpdateResult() {
        _uiState.update { it.copy(updateProfileResult = null) }
    }

    fun consumeErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
