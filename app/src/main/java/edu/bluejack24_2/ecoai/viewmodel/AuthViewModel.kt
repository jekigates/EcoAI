package edu.bluejack24_2.ecoai.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import edu.bluejack24_2.ecoai.data.AuthRepository
import edu.bluejack24_2.ecoai.model.LoginRequest
import edu.bluejack24_2.ecoai.model.RegisterRequest
import edu.bluejack24_2.ecoai.utils.PasswordUtil
import edu.bluejack24_2.ecoai.utils.ValidationUtil
import com.google.firebase.auth.FirebaseAuth

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)
    val email = mutableStateOf<String?>("")

    fun login(request: LoginRequest, onSuccess: () -> Unit) {
        val error = ValidationUtil.validateLogin(request)
        if (error != null) {
            errorMessage.value = error
            return
        }

        isLoading.value = true
        repository.login(request.email, request.password) { success, _ ->
            if (success) {
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null && user.isEmailVerified) {
                    isLoading.value = false
                    errorMessage.value = null
                    onSuccess()
                } else {
                    isLoading.value = false
                    errorMessage.value = "Please verify your email before logging in."
                }
            } else {
                isLoading.value = false
                errorMessage.value = "Invalid credentials!"
            }
        }
    }


    fun register(
        request: RegisterRequest,
        onSuccess: () -> Unit
    ) {
        val error = ValidationUtil.validateRegistration(
            request
        )
        if (error != null) {
            errorMessage.value = error
            return
        }

        isLoading.value = true

        repository.checkEmailExists(request.email) { exists ->
            if (exists) {
                isLoading.value = false
                errorMessage.value = "Email is already registered. Please use another email or log in."
            } else {
                val fullName = "${request.firstName} ${request.lastName}"
                val hashedPassword = PasswordUtil.hash(request.password)
                repository.register(
                    fullName = fullName,
                    email = request.email,
                    password = request.password,
                    hashedPassword = hashedPassword
                ) { success, err ->
                    isLoading.value = false
                    if (success) {
                        errorMessage.value = null
                        onSuccess()
                    } else {
                        errorMessage.value = err ?: "Failed to register account!"
                    }
                }
            }
        }
    }
    
    fun sendResetEmail(email: String, onSuccess: () -> Unit) {
        val error = ValidationUtil.validateEmailOnly(email)
        if (error != null) {
            errorMessage.value = error
            return
        }

        isLoading.value = true
        repository.sendPasswordResetEmail(email) { success, err ->
            isLoading.value = false
            if (success) {
                errorMessage.value = null
                onSuccess()
            } else {
                errorMessage.value = err
            }
        }
    }
}
