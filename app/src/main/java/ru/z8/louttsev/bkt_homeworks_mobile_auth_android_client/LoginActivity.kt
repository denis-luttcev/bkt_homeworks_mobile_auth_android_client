package ru.z8.louttsev.bkt_homeworks_mobile_auth_android_client

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import io.ktor.util.KtorExperimentalAPI
import kotlinx.android.synthetic.main.activity_login.*

@KtorExperimentalAPI
class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (User.isAuthenticated()) {
            getUserAndStartMainActivity()
            finish()

        } else {
            setContentView(R.layout.activity_login)

            loginBtn.setOnClickListener {
                login()
            }

            registrationBtn.setOnClickListener {
                startRegistrationActivity()
            }
        }
    }

    private fun login() {
        val login = loginEdt.text.toString()
        val password = passwordEdt.text.toString()

        if (isCorrectInputted(login, password)) {
            requestToken(login, password)
        }
    }

    private fun isCorrectInputted(login: String, password: String): Boolean {
        val condition = login.isNotEmpty() && login.isNotBlank()
                && password.isNotEmpty() && password.isNotBlank()

        if (!condition) {
            makeToast(this, R.string.authentication_data_error_message)
            clearFields()
        }

        return condition
    }

    private fun clearFields() {
        loginEdt.text.clear()
        passwordEdt.text.clear()
    }

    private fun requestToken(login: String, password: String) {
        sNetworkService.authenticate(login, password, ::checkAuthentication)
    }

    private fun checkAuthentication(token: String?) {
        if (token != null) {
            sMyToken = token
            getSharedPreferences(SECURITY, MODE_PRIVATE).edit().putString(TOKEN, sMyToken).apply()

            getUserAndStartMainActivity()

        } else {
            makeToast(this, R.string.authentication_error_message)
            clearFields()
        }
    }

    private fun getUserAndStartMainActivity() {
        sNetworkService.getMe { user ->
            if (user != null) {
                sMyself = user
                startMainActivity()

            } else {
                sMyToken = null
                getSharedPreferences(SECURITY, MODE_PRIVATE).edit().remove(TOKEN).apply()
            }
        }
    }

    private fun startRegistrationActivity() {
        startActivity(Intent(this@LoginActivity, RegistrationActivity::class.java))
    }

    private fun startMainActivity() {
        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
    }

    override fun onStop() {
        super.onStop()

        if (!User.isAuthenticated()) {
            cancelRequests()
        }
    }

    private fun cancelRequests() {
        sNetworkService.cancellation()
    }
}