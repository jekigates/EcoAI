package com.bluejack242.ecoai.ui.auth

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.bluejack242.ecoai.data.AuthRepository

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        isLoading.value = true
        repository.login(email, password) { success, error ->
            isLoading.value = false
            if (success) {
                onSuccess()
            } else {
                errorMessage.value = error
            }
        }
    }

    fun register(
        fName: String, lName: String, email: String, password: String,
        onSuccess: () -> Unit
    ) {
        isLoading.value = true
//       TODO: hash password tp di firebase udah otomatis, tanya perlu bikin lg g
        repository.register(fName, lName, email, password) { success, error ->
            isLoading.value = false
            if (success) {
                onSuccess()
            } else {
                errorMessage.value = error
            }
        }
    }
}
