package com.bluejack242.ecoai.data

import com.bluejack242.ecoai.utils.PasswordUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
            private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun login(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                callback(task.isSuccessful, task.exception?.message)
            }
    }

    fun register(
        fName: String,
        lName: String,
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
                            .setDisplayName("$fName $lName")
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
                        "displayName" to "$fName $lName",
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
}
