package ru.z8.louttsev.bkt_homeworks_mobile_auth_android_client

import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.accounts.AccountManager.*
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.ktor.util.KtorExperimentalAPI
import kotlinx.android.synthetic.main.activity_login.*

private const val GET_ACCOUNTS_PERMISSION_REQUEST = 1000

/**
 * Implementation borrowed from deprecated AccountAuthenticatorActivity
 */
@KtorExperimentalAPI
class LoginActivity : AppCompatActivity() {
    private var mAccountAuthenticatorResponse: AccountAuthenticatorResponse? = null
    private var mResultBundle: Bundle? = null

    fun setAccountAuthenticatorResult(result: Bundle) {
        mResultBundle = result
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mAccountAuthenticatorResponse =
            intent.getParcelableExtra(KEY_ACCOUNT_AUTHENTICATOR_RESPONSE)
        mAccountAuthenticatorResponse?.onRequestContinued()

        loginBtn.setOnClickListener {
            login()
        }

        registrationBtn.setOnClickListener {
            startRegistrationActivity()
        }

/*        if (ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.GET_ACCOUNTS),
                GET_ACCOUNTS_PERMISSION_REQUEST)
        } else {
            getAccount()
        }

        getAccount()*/
    }

/*    private fun getAccount() {

        val account = accountManager.getAccountsByType(ACCOUNT_ID)[0]
        val token = accountManager.peekAuthToken(account, ACCOUNT_TYPE)

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
    }*/

    /*override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>,
                                            grantResults: IntArray)
    {
        when(requestCode) {
            GET_ACCOUNTS_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getAccount()
                } else {
                    makeToast(this, R.string.permission_error_message)
                }
                return
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }*/

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
        sNetworkService.authenticate(login, password) { token: String? ->
            if (null != token) {
                val accountManager = get(applicationContext)
                val account = Account(login, ACCOUNT_TYPE)
/*                val accounts = accountManager.getAccountsByType(ACCOUNT_TYPE)
                val account = if (accounts.isNotEmpty()) {
                    accounts[0]
                } else {
                    Account(login, ACCOUNT_TYPE)
                }*/

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

/*    private fun getUserAndStartMainActivity() {
        sNetworkService.getMe { user ->
            if (user != null) {
                sMyself = user
                startMainActivity()

            } else {
                sMyToken = null
                getSharedPreferences(SECURITY, MODE_PRIVATE).edit().remove(TOKEN).apply()
            }
        }
    }*/

    private fun startRegistrationActivity() {
        startActivity(Intent(this@LoginActivity, RegistrationActivity::class.java))
    }

/*    private fun startMainActivity() {
        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
    }*/

/*    override fun onStop() {
        super.onStop()

        if (!User.isAuthenticated()) {
            cancelRequests()
        }
    }*/

/*    private fun cancelRequests() {
        sNetworkService.cancellation()
    }*/

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        super.onBackPressed()
    }

/*    override fun finish() {
        mAccountAuthenticatorResponse?.let { response: AccountAuthenticatorResponse ->
            mResultBundle?.let { bundle: Bundle ->
                if (!bundle.containsKey(AccountManager.KEY_ERROR_MESSAGE)) {
                    response.onResult(bundle)
                } else {
                    response.onError(
                        AccountManager.ERROR_CODE_CANCELED,
                        bundle.getString(AccountManager.KEY_ERROR_MESSAGE))
                }
                mAccountAuthenticatorResponse = null
            }
        }
        super.finish()
    }*/

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