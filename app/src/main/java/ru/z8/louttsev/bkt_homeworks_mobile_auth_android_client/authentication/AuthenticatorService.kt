package ru.z8.louttsev.bkt_homeworks_mobile_auth_android_client.authentication

import android.app.Service
import android.content.Intent
import android.os.IBinder
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
class AuthenticatorService : Service() {
    private lateinit var mAuthenticator: Authenticator

    override fun onCreate() {
        super.onCreate()

        mAuthenticator = Authenticator(applicationContext)
    }

    override fun onBind(intent: Intent?): IBinder? = mAuthenticator.iBinder
}