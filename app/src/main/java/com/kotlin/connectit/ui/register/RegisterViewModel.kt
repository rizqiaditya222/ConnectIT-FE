package com.kotlin.connectit.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlin.connectit.data.api.request.RegisterRequest
import com.kotlin.connectit.data.api.response.RegisterResponse
import com.kotlin.connectit.domain.repository.AuthRepository
import com.kotlin.connectit.util.ResultWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterUiState(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val repeatPassword: String = "",
    val agreeToTerms: Boolean = false,
    val isLoading: Boolean = false,
    val registrationResult: ResultWrapper<RegisterResponse>? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onUsernameChanged(username: String) {
        _uiState.update { it.copy(username = username, errorMessage = null) }
    }

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, errorMessage = null) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun onRepeatPasswordChanged(repeatPassword: String) {
        _uiState.update { it.copy(repeatPassword = repeatPassword, errorMessage = null) }
    }

    fun onAgreeToTermsChanged(agreed: Boolean) {
        _uiState.update { it.copy(agreeToTerms = agreed, errorMessage = null) }
    }

    fun attemptRegistration() {
        val currentState = _uiState.value
        if (!validateInputs(currentState)) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, registrationResult = null) }
            val result = authRepository.registerUser(
                RegisterRequest(
                    username = currentState.username.trim(),
                    email = currentState.email.trim(),
                    password = currentState.password
                )
            )
            _uiState.update { it.copy(isLoading = false, registrationResult = result) }
        }
    }

    private fun validateInputs(state: RegisterUiState): Boolean {
        if (state.username.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Username tidak boleh kosong") }
            return false
        }
        if (state.email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            _uiState.update { it.copy(errorMessage = "Format email tidak valid") }
            return false
        }
        if (state.password.length < 6) { // Contoh validasi panjang password
            _uiState.update { it.copy(errorMessage = "Password minimal 6 karakter") }
            return false
        }
        if (state.password != state.repeatPassword) {
            _uiState.update { it.copy(errorMessage = "Password tidak cocok") }
            return false
        }
        if (!state.agreeToTerms) {
            _uiState.update { it.copy(errorMessage = "Anda harus menyetujui syarat dan ketentuan") }
            return false
        }
        _uiState.update { it.copy(errorMessage = null) }
        return true
    }

    fun consumeRegistrationResult() {
        _uiState.update { it.copy(registrationResult = null) }
    }

    fun consumeErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}