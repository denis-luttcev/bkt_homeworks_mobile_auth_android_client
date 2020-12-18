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
import ru.z8.louttsev.bkt_homeworks_mobile_auth_android_client.databinding.ActivitySignUpBinding

@KtorExperimentalAPI
class SignUpActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mBinding.signUpBtn.setOnClickListener {
            registration()
        }
    }

    private fun registration() {
        val username = mBinding.usernameEdt.text.toString()
        val login = mBinding.loginEdt.text.toString()
        val password = mBinding.passwordEdt.text.toString()
        val passwordConfirmation = mBinding.passwordConfirmationEdt.text.toString()

        if (isCorrectInputted(login, password, username, passwordConfirmation)) {
            mBinding.progressBarSignUp.visibility = View.VISIBLE

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
        mBinding.usernameEdt.text?.clear()
        mBinding.loginEdt.text?.clear()
        mBinding.passwordEdt.text?.clear()
        mBinding.passwordConfirmationEdt.text?.clear()
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
                mBinding.progressBarSignUp.visibility = View.GONE
                finish()

            } else {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                mBinding.progressBarSignUp.visibility = View.GONE
                clearFields()
            }
        }
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        mBinding.progressBarSignUp.visibility = View.GONE
        super.onBackPressed()
    }
}