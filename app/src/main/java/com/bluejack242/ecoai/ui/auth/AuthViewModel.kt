package com.bluejack242.ecoai.ui.auth

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.bluejack242.ecoai.data.AuthRepository
import com.bluejack242.ecoai.utils.PasswordUtil
import com.bluejack242.ecoai.utils.ValidationUtil

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        val error = ValidationUtil.validateLogin(email, password)
        if (error != null) {
            errorMessage.value = error
            return
        }

        isLoading.value = true
        repository.login(email, password) { success, err ->
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
        fName: String,
        lName: String,
        email: String,
        confirmEmail: String,
        password: String,
        confirmPassword: String,
        onSuccess: () -> Unit
    ) {
        val error = ValidationUtil.validateRegistration(
            fName, lName, email, confirmEmail, password, confirmPassword
        )
        if (error != null) {
            errorMessage.value = error
            return
        }

        isLoading.value = true

        val hashedPassword = PasswordUtil.hash(password)

        repository.register(
            fName = fName,
            lName = lName,
            email = email,
            password = password,
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
        name: String,
        username: String,
        bio: String,
        isUsernameTaken: Boolean,
        onSuccess: () -> Unit
    ) {
        val error = ValidationUtil.validateProfile(name, username, bio, isUsernameTaken)
        if (error != null) {
            errorMessage.value = error
            return
        }

        // Lanjut simpan ke Firestore atau database
        onSuccess()
    }

}
