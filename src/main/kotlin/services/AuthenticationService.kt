package services

import domain.accounts.IAccountsRepository
import domain.accounts.User
import io.javalin.Context
import io.javalin.builder.CookieBuilder
import org.mindrot.jbcrypt.BCrypt
import java.util.*

class AuthenticationService(val context: Context, val accountRepository: IAccountsRepository) {
    /**
     * Gets the currently authenticated user via cookies
     */
    fun getLoggedInUser(): User? {
        val username = context.cookie(USERNAME_COOKIE)
        val authToken = context.cookie(AUTH_TOKEN_COOKIE)
        if (username == null || authToken == null)
            return null

        val user = accountRepository.getUser(username) ?: return null
        return if (user.authToken == authToken) user else null
    }

    fun logInUser(name: String, plainPassword: String): User? {
        val user = accountRepository.getUser(name) ?: return null

        if (BCrypt.checkpw(plainPassword, user.password)) {
            val authenticatedUser = accountRepository.setUserAuthToken(user.id, UUID.randomUUID().toString())

            setCookie(USERNAME_COOKIE, authenticatedUser.name)
            setCookie(AUTH_TOKEN_COOKIE, authenticatedUser.authToken)

            return authenticatedUser
        }
        return null
    }

    fun hashPassword(plainPassword: String) =
        BCrypt.hashpw(plainPassword, BCrypt.gensalt(12))!!

    fun logOutUser() {
        unsetCookie(USERNAME_COOKIE)
        unsetCookie(AUTH_TOKEN_COOKIE)
    }

    fun setLogInSuccessRedirectUri(uri: String) =
        setCookie(LOG_IN_REDIRECT_COOKIE, uri)

    fun getLogInSuccessRedirectUri(): String? =
        context.cookie(LOG_IN_REDIRECT_COOKIE)

    fun clearLogInSuccessRedirectUri() =
        unsetCookie(LOG_IN_REDIRECT_COOKIE)

    private fun setCookie(name: String, value: String) {
        context.cookie(CookieBuilder(
            path = "/",
            name = name, // root path so that the cookie gets submitted for all pages
            value = value,
            maxAge = -1, // no expiration
            secure = false,
            httpOnly =  false))
    }

    private fun unsetCookie(name: String) = context.removeCookie("/", name)

    companion object {
        private const val USERNAME_COOKIE = "username"
        private const val AUTH_TOKEN_COOKIE = "authToken"
        private const val LOG_IN_REDIRECT_COOKIE = "logInSuccessRedirect"
    }
}
