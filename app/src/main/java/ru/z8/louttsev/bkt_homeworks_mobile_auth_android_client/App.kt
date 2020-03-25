package ru.z8.louttsev.bkt_homeworks_mobile_auth_android_client

import android.app.Application
import android.content.Context
import android.widget.Toast
import io.ktor.util.KtorExperimentalAPI
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.eagerSingleton
import org.kodein.di.generic.instance
import ru.z8.louttsev.bkt_homeworks_mobile_auth_android_client.services.NetworkService
import ru.z8.louttsev.bkt_homeworks_mobile_auth_android_client.services.NetworkServiceWithKtorHttpClientImpl
import ru.z8.louttsev.bkt_homeworks_mobile_auth_android_client.services.PostRepository
import ru.z8.louttsev.bkt_homeworks_mobile_auth_android_client.services.PostRepositoryInMemoryImpl
import java.util.UUID

@KtorExperimentalAPI
val sKodein = Kodein{
    bind<NetworkService>() with eagerSingleton {
        NetworkServiceWithKtorHttpClientImpl()
    }
    bind<PostRepository>() with eagerSingleton {
        PostRepositoryInMemoryImpl()
    }
}

@KtorExperimentalAPI
val sNetworkService by sKodein.instance<NetworkService>()
@KtorExperimentalAPI
val sRepository by sKodein.instance<PostRepository>()

var sMyToken: String? = null
var sMyself: User? = null

const val SECURITY = "security"
const val TOKEN = "token"

@KtorExperimentalAPI
class App : Application() {
    override fun onCreate() {
        super.onCreate()

        val sharedPreferences = getSharedPreferences(SECURITY, MODE_PRIVATE)
        sMyToken = sharedPreferences.getString(TOKEN, null)
    }
}

data class User(val id: UUID, val username: String) {
    companion object {
        fun isAuthenticated() = sMyToken != null
    }

    data class RegistrationRequestDto(val username: String, val login: String, val password: String)
    data class AuthenticationRequestDto(val login: String, val password: String)
    data class AuthenticationResponseDto(val token: String)
}

fun makeToast(context: Context, stringID: Int) {
    Toast.makeText(context, context.getString(stringID), Toast.LENGTH_SHORT).show()
}