package com.project.cryptowallet.login.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.project.cryptowallet.login.domain.AuthorizationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthorizationRepository
) : ViewModel() {

    private val _navigateToHome = MutableStateFlow(false)
    val navigateToHome: StateFlow<Boolean> = _navigateToHome.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null) // New state for error messages
    val authError: StateFlow<String?> = _authError.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                val result = authRepository.login(email, password)
                if (result) {
                    _navigateToHome.value = true
                } else {
                    _authError.value = "Login failed. Please try again."
                }
            } catch (e: FirebaseAuthInvalidUserException) {
                _authError.value = "No account found with this email."
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                _authError.value = "Invalid password. Please try again."
            } catch (e: Exception) {
                _authError.value = e.message
                println("AuthViewModel: register: ${e.message}")
            }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            try {
                val result = authRepository.register(email, password)
                if (result) {
                    _navigateToHome.value = true
                } else {
                    _authError.value = "Registration failed. Please try again."
                }
            } catch (e: FirebaseAuthWeakPasswordException) {
                _authError.value = "Password should be at least 6 characters."
            } catch (e: FirebaseAuthUserCollisionException) {
                _authError.value = "An account already exists with this email."
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                _authError.value = "Invalid email format."
            } catch (e: Exception) {
                _authError.value = e.message
                println("AuthViewModel: register: ${e.message}")
            }
        }
    }

    fun resetNavigation() {
        _navigateToHome.value = false
    }

    fun resetError() {
        _authError.value = null
    }
}



//sealed class AuthState {
//    object Idle : AuthState()
//    object Loading : AuthState()
//    object Success : AuthState()
//    data class Error(val message: String) : AuthState()
//}
