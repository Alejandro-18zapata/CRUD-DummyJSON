package com.sena.crud_dummyjson

import android.app.Application
import com.sena.crud_dummyjson.data.auth.FirebaseAuthDataSource
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class Application : Application() {

    @Inject
    lateinit var authDataSource: FirebaseAuthDataSource

    private val appScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO + CoroutineExceptionHandler { _, throwable ->
            throwable.printStackTrace()
        }
    )

    override fun onCreate() {
        super.onCreate()
        appScope.launch {
            runCatching { authDataSource.ensureSignedIn() }
        }
    }
}
