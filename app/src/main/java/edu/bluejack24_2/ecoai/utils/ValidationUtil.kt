package edu.bluejack24_2.ecoai.utils

import android.util.Patterns
import edu.bluejack24_2.ecoai.model.LoginRequest
import edu.bluejack24_2.ecoai.model.ProfileForm
import edu.bluejack24_2.ecoai.model.RegisterRequest
import edu.bluejack24_2.ecoai.model.ResetPasswordRequest

object ValidationUtil {

    fun validateRegistration(request: RegisterRequest): String? {
        with(request) {
            if (firstName.isBlank() || lastName.isBlank() ||
                email.isBlank() || confirmEmail.isBlank() ||
                password.isBlank() || confirmPassword.isBlank()
            ) return "All fields must be filled out."

            if (firstName.length < 4) return "First name must be at least 4 characters long."
            if (lastName.length < 4) return "Last name must be at least 4 characters long."
            if (!email.endsWith("@gmail.com")) return "Email address must end with @gmail.com."
            if (email != confirmEmail) return "Confirm email must match the email."
            if (password.length < 8) return "Password must be at least 8 characters long."
            if (password != confirmPassword) return "Confirm password must match the password."
        }
        return null
    }

    fun validateLogin(request: LoginRequest): String? {
        with(request) {
            if (email.isBlank() || password.isBlank()) return "All fields must be filled out."
            if (!email.endsWith("@gmail.com")) return "Email address must end with @gmail.com."
            if (password.length < 8) return "Password must be at least 8 characters long."
        }
        return null
    }

    fun validateProfile(form: ProfileForm): String? {
        with(form) {
            if (name.length < 4) return "Name must be at least 4 characters long."
            if (isUsernameTaken) return "Username must be unique."
            if (bio.trim()
                    .split("\\s+".toRegex()).size < 3
            ) return "Bio must be at least 3 words long."
        }
        return null
    }

    fun validateEmailOnly(email: String): String? {
        return when {
            email.isBlank() -> "Email cannot be empty"
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Invalid email format"
            !email.endsWith("@gmail.com") -> "Email address must end with @gmail.com"
            else -> null
        }
    }

    fun validatePasswordReset(request: ResetPasswordRequest): String? {
        with(request) {
            if (newPassword.isBlank()) return "Password cannot be empty"
            if (newPassword.length < 8) return "Password must be at least 8 characters"
            if (newPassword != confirmPassword) return "Passwords do not match"
        }
        return null
    }

    fun validatePost(headline: String, caption: String, mediaCount: Int): String? {
        if (mediaCount < 1) return "At least 1 image is required."
        if (mediaCount > 10) return "Maximum 10 images allowed."
        if (headline.length > 50) return "Headline must be at most 50 characters."
        if (caption.isEmpty()) return "Caption must be filled."
        if (headline.isEmpty()) return "Headline must be filled."
        return null
    }

}
