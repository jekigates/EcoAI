package edu.bluejack24_2.ecoai.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    fun login(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                callback(task.isSuccessful, task.exception?.message)
            }
    }

    fun register(
        fullName: String,
        email: String,
        password: String,
        hashedPassword: String,
        callback: (Boolean, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.updateProfile(
                        UserProfileChangeRequest.Builder()
                            .setDisplayName(fullName)
                            .build()
                    )?.addOnCompleteListener {
                        user.sendEmailVerification()
                            .addOnSuccessListener {
                                callback(true, null)
                            }
                            .addOnFailureListener {
                                callback(false, it.message)
                            }
                    }

                    val userMap = hashMapOf(
                        "uid" to user?.uid,
                        "email" to email,
                        "fullName" to fullName,
                        "password_hash" to hashedPassword
                    )

                    db.collection("users").document(user!!.uid)
                        .set(userMap)
                        .addOnSuccessListener {
                            callback(true, null)
                        }
                        .addOnFailureListener { err ->
                            callback(false, err.message)
                        }
                } else {
                    callback(false, task.exception?.message)
                }
            }
    }

    fun sendPasswordResetEmail(email: String, callback: (Boolean, String?) -> Unit) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)
                } else {
                    callback(false, task.exception?.localizedMessage ?: "Failed to send reset email.")
                }
            }
    }

    fun updatePassword(newPassword: String, callback: (Boolean, String?) -> Unit) {
        val user = auth.currentUser
        if (user != null) {
            user.updatePassword(newPassword)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        callback(true, null)
                    } else {
                        callback(false, task.exception?.localizedMessage ?: "Failed to update password.")
                    }
                }
        } else {
            callback(false, "User not logged in.")
        }
    }

    fun checkEmailExists(email: String, callback: (Boolean) -> Unit) {
        @Suppress("DEPRECATION")
        auth.fetchSignInMethodsForEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val signInMethods = task.result?.signInMethods
                    callback(!signInMethods.isNullOrEmpty())
                } else {
                    callback(false)
                }
            }
    }


}
