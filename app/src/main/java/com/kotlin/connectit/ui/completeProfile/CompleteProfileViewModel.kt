package com.kotlin.connectit.ui.completeProfile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlin.connectit.data.api.request.UpdateUsernameRequest
import com.kotlin.connectit.data.api.response.UpdateUserResponse
import com.kotlin.connectit.domain.repository.AuthRepository
import com.kotlin.connectit.domain.repository.UserRepository
import com.kotlin.connectit.util.ResultWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CompleteProfileUiState(
    val username: String = "",
    val email: String = "",
    val isLoading: Boolean = false,
    val isProfileLoaded: Boolean = false,
    val updateProfileResult: ResultWrapper<UpdateUserResponse>? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class CompleteProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompleteProfileUiState())
    val uiState: StateFlow<CompleteProfileUiState> = _uiState.asStateFlow()

    init {
        loadCurrentUser()
    }

    fun onUsernameChanged(username: String) {
        _uiState.update { it.copy(username = username, errorMessage = null) }
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
                            isProfileLoaded = true
                        )
                    }
                }
                is ResultWrapper.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message ?: "Gagal memuat data pengguna",
                            isProfileLoaded = true // Anggap selesai loading meski error
                        )
                    }
                }
            }
        }
    }

    fun attemptSaveProfile() {
        val currentState = _uiState.value
        if (currentState.username.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Username tidak boleh kosong") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, updateProfileResult = null) }
            val result = userRepository.updateAuthenticatedUser(
                UpdateUsernameRequest(username = currentState.username.trim())
            )
            if (result is ResultWrapper.Success) {
                result.data.data?.username?.let { updatedUsername ->
                    _uiState.update { it.copy(username = updatedUsername) }
                }
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