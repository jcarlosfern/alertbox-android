package app.alertbox.io

import android.app.Application
import app.alertbox.io.core.auth.AuthRepository
import app.alertbox.io.core.network.ApiClient
import app.alertbox.io.core.notifications.NotificationChannels
import app.alertbox.io.data.AlertBoxRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class AlertBoxApplication : Application() {
    val firebaseConfigured: Boolean by lazy {
        FirebaseApp.getApps(this).isNotEmpty() || FirebaseApp.initializeApp(this) != null
    }

    val firebaseAuth: FirebaseAuth? by lazy {
        if (firebaseConfigured) FirebaseAuth.getInstance() else null
    }

    val repository: AlertBoxRepository by lazy {
        AlertBoxRepository(this, ApiClient.create(firebaseAuth), firebaseConfigured)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository(this, firebaseAuth, repository)
    }

    override fun onCreate() {
        super.onCreate()
        NotificationChannels.create(this)
        firebaseConfigured
    }
}
