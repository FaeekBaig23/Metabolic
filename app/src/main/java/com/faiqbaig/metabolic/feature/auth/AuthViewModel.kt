package com.faiqbaig.metabolic.feature.auth

import com.faiqbaig.metabolic.feature.auth.AuthState

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _authState: MutableStateFlow<AuthState> = MutableStateFlow(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // ── Check if user is already logged in ───────────────
    val isLoggedIn: Boolean
        get() = auth.currentUser != null

    // ── Login with email + password ───────────────────────
    fun login(email: String, password: String) {
        if (!validateInputs(email, password)) return
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(
                    e.message ?: "Login failed. Please try again."
                )
            }
        }
    }

    // ── Register with email + password ────────────────────
    fun register(
        name    : String,
        email   : String,
        password: String,
        confirm : String
    ) {
        if (password != confirm) {
            _authState.value = AuthState.Error("Passwords do not match.")
            return
        }
        if (!validateInputs(email, password)) return

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = auth
                    .createUserWithEmailAndPassword(email, password)
                    .await()
                // Update display name
                result.user?.updateProfile(
                    com.google.firebase.auth.UserProfileChangeRequest
                        .Builder()
                        .setDisplayName(name)
                        .build()
                )?.await()
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(
                    e.message ?: "Registration failed. Please try again."
                )
            }
        }
    }

    // ── Google Sign-In ────────────────────────────────────
    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(credential).await()
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(
                    e.message ?: "Google sign-in failed."
                )
            }
        }
    }

    // ── Sign out ──────────────────────────────────────────
    fun signOut() {
        auth.signOut()
        _authState.value = AuthState.Idle
    }

    // ── Reset state (e.g. after showing error) ────────────
    fun resetState() {
        _authState.value = AuthState.Idle
    }

    // ── Input validation ──────────────────────────────────
    private fun validateInputs(email: String, password: String): Boolean {
        return when {
            email.isBlank() -> {
                _authState.value = AuthState.Error("Email cannot be empty.")
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                _authState.value = AuthState.Error("Please enter a valid email.")
                false
            }
            password.length < 6 -> {
                _authState.value = AuthState.Error("Password must be at least 6 characters.")
                false
            }
            else -> true
        }
    }
}