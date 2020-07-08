package ru.z8.louttsev.bkt_homeworks_mobile_auth_android_client

import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager.*
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.ktor.util.KtorExperimentalAPI
import kotlinx.android.synthetic.main.activity_sign_in.*

/**
 * Implementation borrowed from deprecated AccountAuthenticatorActivity
 */
@KtorExperimentalAPI
class SignInActivity : AppCompatActivity() {
    private var mAccountAuthenticatorResponse: AccountAuthenticatorResponse? = null
    private var mResultBundle: Bundle? = null

    private fun setAccountAuthenticatorResult(result: Bundle) {
        mResultBundle = result
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        mAccountAuthenticatorResponse =
            intent.getParcelableExtra(KEY_ACCOUNT_AUTHENTICATOR_RESPONSE)
        mAccountAuthenticatorResponse?.onRequestContinued()

        signInBtn.setOnClickListener {
            login()
        }

        registrationBtn.setOnClickListener {
            startRegistrationActivity()
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
        sNetworkService.signIn(login, password) { token: String? ->
            if (null != token) {
                val accountManager = get(applicationContext)
                val account = Account(login, ACCOUNT_TYPE)

                val data = Bundle().apply {
                    putString(KEY_ACCOUNT_NAME, account.name)
                    putString(KEY_ACCOUNT_TYPE, account.type)
                    putString(KEY_AUTHTOKEN, token)
                }

                if (accountManager.addAccountExplicitly(account, password, null)) {
                    accountManager.setAuthToken(account, account.type, token)
                } else {
                    accountManager.setPassword(account, password)
                }

                setAccountAuthenticatorResult(data)
                setResult(Activity.RESULT_OK, Intent().putExtras(data))

                finish()

            } else {
                makeToast(this, R.string.authentication_error_message)
                clearFields()
            }
        }
    }

    private fun startRegistrationActivity() {
        startActivity(Intent(this@SignInActivity, SignUpActivity::class.java))
    }

    override fun onStop() {
        super.onStop()

        cancelRequests()
    }

    private fun cancelRequests() {
        sNetworkService.cancellation()
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        super.onBackPressed()
    }

    override fun finish() {
        mAccountAuthenticatorResponse?.let {
            mResultBundle?.let {
                mAccountAuthenticatorResponse!!.onResult(mResultBundle)
            } ?: mAccountAuthenticatorResponse!!.onError(ERROR_CODE_CANCELED, "canceled")
            mAccountAuthenticatorResponse = null
        }
        super.finish()
    }
}