package com.project.cryptowallet.login.domain

import com.google.firebase.auth.FirebaseAuth
import kotlin.coroutines.resume


import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resumeWithException

class AuthorizationRepository {

    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val tag = "AuthRepository: "

    private suspend fun performAuthOperation(
        operation: (FirebaseAuth, (Boolean) -> Unit, (Exception) -> Unit) -> Unit
    ): Boolean {
        return suspendCancellableCoroutine { continuation ->
            operation(
                firebaseAuth,
                { continuation.resume(true) },
                { exception ->
                    continuation.resumeWithException(exception)
                }
            )
        }


    }

    suspend fun login(email: String, password: String): Boolean {
        return performAuthOperation { auth, onSuccess, onFailure ->
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { onSuccess(true) }
                .addOnFailureListener { onFailure(it) }
        }
    }

    suspend fun register(email: String, password: String): Boolean {
        return performAuthOperation { auth, onSuccess, onFailure ->
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { onSuccess(true) }
                .addOnFailureListener { onFailure(it) }
        }
    }

    fun logout() {
        firebaseAuth.signOut()
    }

    fun isLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }
}
