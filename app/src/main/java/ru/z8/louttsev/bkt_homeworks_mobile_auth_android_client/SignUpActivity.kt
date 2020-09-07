package ru.z8.louttsev.bkt_homeworks_mobile_auth_android_client

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import io.ktor.util.KtorExperimentalAPI
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_sign_up.*

@KtorExperimentalAPI
class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        signUpBtn.setOnClickListener {
            registration()
        }
    }

    private fun registration() {
        val username = usernameEdt.text.toString()
        val login = loginEdt.text.toString()
        val password = passwordEdt.text.toString()
        val passwordConfirmation = passwordConfirmationEdt.text.toString()

        if (isCorrectInputted(login, password, username, passwordConfirmation)) {
            progressBarSignUp.visibility = View.VISIBLE

            requestRegistration(username, login, password)
        }
    }

    private fun isCorrectInputted(
        login: String,
        password: String,
        username: String,
        passwordConfirmation: String
    )
            = validateLoginAndPassword(login, password)
            && validateUsername(username)
            && checkConfirmation(password, passwordConfirmation)

    private fun validateLoginAndPassword(login: String, password: String): Boolean {
        val condition = login.isNotEmpty() && login.isNotBlank()
                && password.isNotEmpty() && password.isNotBlank()

        if (!condition) {
            makeToast(this, R.string.authentication_data_error_message)
            clearFields()
        }

        return condition
    }

    private fun clearFields() {
        usernameEdt.text.clear()
        loginEdt.text.clear()
        passwordEdt.text.clear()
        passwordConfirmationEdt.text.clear()
    }

    private fun validateUsername(username: String): Boolean {
        val condition = username.isNotEmpty() && username.isNotBlank()

        if (!condition) {
            makeToast(this, R.string.registration_data_error_message)
            clearFields()
        }

        return condition
    }

    private fun checkConfirmation(password: String, passwordConfirmation: String): Boolean {
        val condition = password == passwordConfirmation

        if (!condition) {
            makeToast(this, R.string.password_confirmation_error_message)
            clearFields()
        }

        return condition
    }

    private fun requestRegistration(username: String, login: String, password: String) {
        sNetworkService.signUp(username, login, password) { token: String?, message: String? ->
            if (token != null) {
                val accountManager = AccountManager.get(applicationContext)
                val account = Account(login, ACCOUNT_TYPE)

                val data = Bundle().apply {
                    putString(AccountManager.KEY_ACCOUNT_NAME, account.name)
                    putString(AccountManager.KEY_ACCOUNT_TYPE, account.type)
                    putString(AccountManager.KEY_AUTHTOKEN, token)
                }

                if (accountManager.addAccountExplicitly(account, password, null)) {
                    accountManager.setAuthToken(account, account.type, token)
                } else {
                    accountManager.setPassword(account, password)
                }

                setResult(Activity.RESULT_OK, Intent().putExtras(data))
                progressBarSignUp.visibility = View.GONE
                finish()

            } else {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                progressBarSignUp.visibility = View.GONE
                clearFields()
            }
        }
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        progressBarSignUp.visibility = View.GONE
        super.onBackPressed()
    }
}