package ru.z8.louttsev.bkt_homeworks_mobile_auth_android_client

import android.Manifest
import android.accounts.Account
import android.accounts.AccountAuthenticatorActivity
import android.accounts.AccountManager
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.ktor.util.KtorExperimentalAPI
import kotlinx.android.synthetic.main.activity_login.*

private const val GET_ACCOUNTS_PERMISSION_REQUEST = 1000

@KtorExperimentalAPI
class LoginActivity : AccountAuthenticatorActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

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

    fun onTokenReceived(account: Account, password: String, token: String) {
        val accountManager = AccountManager.get(this)
        val result = Bundle()

        if (accountManager.addAccountExplicitly(account, password, Bundle())) {
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name)
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type)
            result.putString(AccountManager.KEY_AUTHTOKEN, token)

            accountManager.setAuthToken(account, account.type, token)

        } else {
            result.putString(AccountManager.KEY_ERROR_MESSAGE,
                getString(R.string.account_already_exist_error_message))
        }

        setAccountAuthenticatorResult(result)
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun login() {
        val login = loginEdt.text.toString()
        val password = passwordEdt.text.toString()

        if (isCorrectInputted(login, password)) {
            Account(login, ACCOUNT_TYPE).also {
                AccountManager.get(this).addAccountExplicitly(it, password, null)
            }
            finish()
            /*requestToken(login, password)*/
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

/*    private fun requestToken(login: String, password: String) {
        sNetworkService.authenticate(login, password, ::checkAuthentication)
    }*/

/*    private fun checkAuthentication(token: String?) {
        if (token != null) {
            sMyToken = token
            val accountManager = AccountManager.get(this)
            val accounts = accountManager.getAccountsByType(ACCOUNT_TYPE)
            if (accounts.isNotEmpty()) {
                val account = accounts[0]

            }
            finish()
            getUserAndStartMainActivity()

        } else {
            makeToast(this, R.string.authentication_error_message)
            clearFields()
        }
    }*/

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
}