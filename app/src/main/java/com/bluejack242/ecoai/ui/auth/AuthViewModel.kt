package com.bluejack242.ecoai.ui.auth

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.bluejack242.ecoai.data.AuthRepository
import com.bluejack242.ecoai.model.LoginRequest
import com.bluejack242.ecoai.model.ProfileForm
import com.bluejack242.ecoai.model.RegisterRequest
import com.bluejack242.ecoai.model.ResetPasswordRequest
import com.bluejack242.ecoai.utils.PasswordUtil
import com.bluejack242.ecoai.utils.ValidationUtil

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
        repository.login(request.email, request.password) { success, err ->
            isLoading.value = false
            if (success) {
                errorMessage.value = null
                onSuccess()
            } else {
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

        val hashedPassword = PasswordUtil.hash(request.password)

        repository.register(
            fName = request.firstName,
            lName = request.lastName,
            email = request.email,
            password = request.password,
            hashedPassword = hashedPassword
        ) { success, err ->
            isLoading.value = false
            if (success) {
                errorMessage.value = null
                onSuccess()
            } else {
                errorMessage.value = "Failed to register account!"
            }
        }
    }

    fun saveProfile(
        form: ProfileForm,
        onSuccess: () -> Unit
    ) {
        val error = ValidationUtil.validateProfile(form)
        if (error != null) {
            errorMessage.value = error
            return
        }

        // Lanjut simpan ke Firestore atau database
        onSuccess()
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

    fun resetPassword(
        request: ResetPasswordRequest,
        onSuccess: () -> Unit
    ) {
        val error = ValidationUtil.validatePasswordReset(request)
        if (error != null) {
            errorMessage.value = error
            return
        }

        isLoading.value = true
        repository.updatePassword(request.newPassword) { success, err ->
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
