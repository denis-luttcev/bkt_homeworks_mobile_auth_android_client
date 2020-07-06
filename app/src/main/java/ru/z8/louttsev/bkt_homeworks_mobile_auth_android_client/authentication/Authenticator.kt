package ru.z8.louttsev.bkt_homeworks_mobile_auth_android_client.authentication

import android.accounts.*
import android.content.Context
import android.content.Intent
import android.os.Bundle
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.*
import ru.z8.louttsev.bkt_homeworks_mobile_auth_android_client.*

@KtorExperimentalAPI
class Authenticator(private val mContext: Context) : AbstractAccountAuthenticator(mContext) {
    override fun getAuthTokenLabel(p0: String?): String? = null

    override fun confirmCredentials(
        p0: AccountAuthenticatorResponse?,
        p1: Account?,
        p2: Bundle?
    ): Bundle? = null

    override fun updateCredentials(
        p0: AccountAuthenticatorResponse?,
        p1: Account?,
        p2: String?,
        p3: Bundle?
    ): Bundle? = null

    override fun getAuthToken(
        response: AccountAuthenticatorResponse?,
        account: Account,
        authTokenType: String?,
        options: Bundle?
    ): Bundle {
        val result = Bundle()

        fun handleToken(token: String?) {
            if (null != token) {
                result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name)
                result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type)
                result.putString(AccountManager.KEY_AUTHTOKEN, token)
            } else {
                val intent = Intent(mContext, LoginActivity::class.java).apply {
                    putExtra(TOKEN_TYPE, authTokenType)
                    putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
                }
                result.putParcelable(AccountManager.KEY_INTENT, intent)
            }
        }

        val accountManager = AccountManager.get(mContext)
        val token = accountManager.peekAuthToken(account, authTokenType)

        if (null != token) {
            handleToken(token)
        } else {
            val password = accountManager.getPassword(account)
            val actualToken = runBlocking {
                sNetworkService.authenticate(account.name, password)
            }
            handleToken(actualToken)
        }

        return result
    }

    override fun hasFeatures(
        p0: AccountAuthenticatorResponse?,
        p1: Account?,
        p2: Array<out String>?
    ): Bundle? = null

    override fun editProperties(p0: AccountAuthenticatorResponse?, p1: String?): Bundle? = null

    override fun addAccount(
        response: AccountAuthenticatorResponse?,
        accountType: String?,
        authTokenType: String?,
        requiredFeatures: Array<out String>?,
        options: Bundle?
    ): Bundle {
        val intent = Intent(mContext, LoginActivity::class.java)
        intent.apply {
            putExtra(ACCOUNT_TYPE, accountType)
            putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
        }

        val bundle = Bundle()
        if (options != null) {
            bundle.putAll(options)
        }
        bundle.putParcelable(AccountManager.KEY_INTENT, intent)

        return bundle
    }
}