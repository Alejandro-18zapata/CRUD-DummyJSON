package com.sena.crud_dummyjson.data.auth

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthDataSource @Inject constructor(
    private val auth: FirebaseAuth
) {
    private val signInMutex = Mutex()

    suspend fun ensureSignedIn(): String = signInMutex.withLock {
        auth.currentUser?.uid?.let { return@withLock it }

        val result = auth.signInAnonymously().await()
        result.user?.uid
            ?: error("No se pudo iniciar sesión anónima en Firebase")
    }
}
