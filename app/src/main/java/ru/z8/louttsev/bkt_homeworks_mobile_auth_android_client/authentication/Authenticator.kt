package ru.z8.louttsev.bkt_homeworks_mobile_auth_android_client.authentication

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import io.ktor.util.KtorExperimentalAPI
import ru.z8.louttsev.bkt_homeworks_mobile_auth_android_client.ACCOUNT_ID
import ru.z8.louttsev.bkt_homeworks_mobile_auth_android_client.LoginActivity

@KtorExperimentalAPI
class Authenticator(private val context: Context) : AbstractAccountAuthenticator(context) {
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
        account: Account?,
        authTokenType: String?,
        options: Bundle?
    ): Bundle {
        val accountManager = AccountManager.get(context)
        val token = accountManager.peekAuthToken(account, authTokenType)
        val bundle = Bundle()

        if (token.isNotEmpty()) {
            bundle.putString(AccountManager.KEY_ACCOUNT_NAME, account!!.name);
            bundle.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            bundle.putString(AccountManager.KEY_AUTHTOKEN, token);

        } else {
            val intent = Intent(context, LoginActivity::class.java)
            intent.apply {
                putExtra(ACCOUNT_ID, authTokenType)
                putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
            }
            bundle.putParcelable(AccountManager.KEY_INTENT, intent)
        }

        return bundle
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
        val intent = Intent(context, LoginActivity::class.java)
        intent.apply {
            putExtra(ACCOUNT_ID, accountType)
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