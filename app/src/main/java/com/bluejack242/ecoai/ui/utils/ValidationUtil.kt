package com.bluejack242.ecoai.utils

object ValidationUtil {

    fun validateRegistration(
        fName: String,
        lName: String,
        email: String,
        confirmEmail: String,
        password: String,
        confirmPassword: String
    ): String? {
        if (email.isBlank() || confirmEmail.isBlank() ||
            password.isBlank() || confirmPassword.isBlank() ||
            fName.isBlank() || lName.isBlank()
        ) return "All fields must be filled out."
        if (fName.length < 4) return "First name must be at least 4 characters long."
        if (lName.length < 4) return "Last name must be at least 4 characters long."
        if (!email.endsWith("@gmail.com")) return "Email address must end with @gmail.com."
        if (email != confirmEmail) return "Confirm email must match the email."
        if (password.length < 8) return "Password must be at least 8 characters long."
        if (password != confirmPassword) return "Confirm password must match the password."

        return null
    }

    fun validateLogin(email: String, password: String): String? {
        if (email.isBlank() || password.isBlank()) return "All fields must be filled out."
        if (!email.endsWith("@gmail.com")) return "Email address must end with @gmail.com."
        if (password.length < 8) return "Password must be at least 8 characters long."
        return null
    }

    fun validateProfile(
        name: String,
        username: String,
        bio: String,
        isUsernameTaken: Boolean
    ): String? {
        if (name.length < 4) return "Name must be at least 4 characters long."
        if (isUsernameTaken) return "Username must be unique."
        if (bio.trim().split("\\s+".toRegex()).size < 3) return "Bio must be at least 3 words long."
        return null
    }
}
