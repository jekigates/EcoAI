package com.bluejack242.ecoai.ui.auth

import androidx.compose.runtime.mutableStateOf

object LanguageManager {
    val currentLanguage = mutableStateOf("EN")
    
    private val translations = mapOf(
        "EN" to mapOf(
            // Landing Screen
            "waste_tracking_made_easy" to "Waste tracking made easy",
            "get_started" to "Get Started",
            "have_account" to "Have an account?",
            "log_in" to "Log In",
            
            // Login Screen
            "welcome_back" to "Welcome Back!",
            "email_address" to "Email address",
            "password_hint" to "Password (8+ characters)",
            "forgotten_password" to "Forgotten your password?",
            "dont_have_account" to "Don't have an account?",
            "register" to "Register",
            "login_success" to "Login success",
            
            // Register Screen
            "first_name" to "First name",
            "last_name" to "Last name",
            "confirm_email" to "Confirm email address",
            "confirm_password" to "Confirm password (8+ characters)",
            "create_account" to "Create account",
            "password_mismatch" to "Password doesn't match",
            "email_mismatch" to "Email doesn't match",
            "registration_successful" to "Registration successful",
            // Added for dialog
            "registration_success" to "Account Created",
            "verification_link_sent" to "Your account has been successfully created and a verification link has been sent to your email.",
            "go_to_login" to "Go to Login"
        ),
        "ID" to mapOf(
            // Landing Screen
            "waste_tracking_made_easy" to "Pelacakan sampah jadi mudah",
            "get_started" to "Mulai",
            "have_account" to "Sudah punya akun?",
            "log_in" to "Masuk",
            
            // Login Screen
            "welcome_back" to "Selamat Datang Kembali!",
            "email_address" to "Alamat email",
            "password_hint" to "Kata sandi (8+ karakter)",
            "forgotten_password" to "Lupa kata sandi?",
            "dont_have_account" to "Belum punya akun?",
            "register" to "Daftar",
            "login_success" to "Login berhasil",
            
            // Register Screen
            "first_name" to "Nama depan",
            "last_name" to "Nama belakang",
            "confirm_email" to "Konfirmasi alamat email",
            "confirm_password" to "Konfirmasi kata sandi (8+ karakter)",
            "create_account" to "Buat akun",
            "password_mismatch" to "Kata sandi tidak cocok",
            "email_mismatch" to "Email tidak cocok",
            "registration_successful" to "Registrasi berhasil",
            // Added for dialog
            "registration_success" to "Akun Berhasil Dibuat",
            "verification_link_sent" to "Akun Anda berhasil dibuat dan tautan verifikasi telah dikirim ke email Anda.",
            "go_to_login" to "Ke Halaman Masuk"
        )
    )
    
    fun getString(key: String): String {
        return translations[currentLanguage.value]?.get(key) ?: key
    }
    
    fun setLanguage(language: String) {
        currentLanguage.value = language
    }
}
