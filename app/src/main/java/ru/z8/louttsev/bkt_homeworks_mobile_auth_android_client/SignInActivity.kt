package ru.z8.louttsev.bkt_homeworks_mobile_auth_android_client

import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager.*
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import io.ktor.util.KtorExperimentalAPI
import ru.z8.louttsev.bkt_homeworks_mobile_auth_android_client.databinding.ActivitySignInBinding

/**
 * Implementation borrowed from deprecated AccountAuthenticatorActivity
 */
@KtorExperimentalAPI
class SignInActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivitySignInBinding

    private var mAccountAuthenticatorResponse: AccountAuthenticatorResponse? = null
    private var mResultBundle: Bundle? = null

    private fun setAccountAuthenticatorResult(result: Bundle) {
        mResultBundle = result
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mAccountAuthenticatorResponse =
            intent.getParcelableExtra(KEY_ACCOUNT_AUTHENTICATOR_RESPONSE)
        mAccountAuthenticatorResponse?.onRequestContinued()

        mBinding.signInBtn.setOnClickListener {
            login()
        }

        mBinding.registrationBtn.setOnClickListener {
            signUp()
        }
    }

    private fun login() {
        val login = mBinding.loginEdt.text.toString()
        val password = mBinding.passwordEdt.text.toString()

        if (isCorrectInputted(login, password)) {
            mBinding.progressBarSignIn.visibility = View.VISIBLE

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
        mBinding.loginEdt.text?.clear()
        mBinding.passwordEdt.text?.clear()
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

                finishSignIn(data)

            } else {
                makeToast(this, R.string.authentication_error_message)
                mBinding.progressBarSignIn.visibility = View.GONE
                clearFields()
            }
        }
    }

    private fun finishSignIn(data: Bundle) {
        setAccountAuthenticatorResult(data)
        setResult(Activity.RESULT_OK, Intent().putExtras(data))
        mBinding.progressBarSignIn.visibility = View.GONE
        finish()
    }

    private fun signUp() {
        startActivityForResult(Intent(this, SignUpActivity::class.java), SIGN_UP_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, result: Intent?) {
        if (SIGN_UP_REQUEST == requestCode && Activity.RESULT_OK == resultCode) {
            val data = result?.extras
            data?.let {
                finishSignIn(data)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, result)
        }
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