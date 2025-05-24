package com.kotlin.connectit.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlin.connectit.data.api.TokenManager
import com.kotlin.connectit.data.api.request.LoginRequest
import com.kotlin.connectit.data.api.response.LoginResponse
import com.kotlin.connectit.domain.repository.AuthRepository
import com.kotlin.connectit.util.ResultWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val rememberMe: Boolean = false,
    val isLoading: Boolean = false,
    val loginResult: ResultWrapper<LoginResponse>? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, errorMessage = null) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun onRememberMeChanged(isChecked: Boolean) {
        _uiState.update { it.copy(rememberMe = isChecked) }
    }

    fun attemptLogin() {
        val currentState = _uiState.value
        if (!validateInputs(currentState)) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, loginResult = null) }
            val result = authRepository.loginUser(
                LoginRequest(
                    email = currentState.email.trim(),
                    password = currentState.password
                )
            )

            if (result is ResultWrapper.Success) {
                result.data.data?.token?.let { token ->
                    TokenManager.saveToken(token)
                }
            }
            _uiState.update { it.copy(isLoading = false, loginResult = result) }
        }
    }

    private fun validateInputs(state: LoginUiState): Boolean {
        if (state.email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            _uiState.update { it.copy(errorMessage = "Format email tidak valid") }
            return false
        }
        if (state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Password tidak boleh kosong") }
            return false
        }
        _uiState.update { it.copy(errorMessage = null) }
        return true
    }

    fun consumeLoginResult() {
        _uiState.update { it.copy(loginResult = null) }
    }

    fun consumeErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}