package services

import domain.Change
import domain.accounts.IAccountsRepository
import domain.accounts.User
import domain.accounts.UserChangeSet
import io.javalin.Context
import io.javalin.builder.CookieBuilder
import java.util.*

class AuthenticationService(val context: Context, val accountRepository: IAccountsRepository, val passwordHasher: BcryptPasswordHasher) {

    private var authenticatedUser: User? = null

    /**
     * Gets the currently authenticated user via cookies
     */
    fun getLoggedInUser(): User? {
        if (authenticatedUser != null)
            return authenticatedUser

        val authHeader = context.header(AUTHORIZATION_HEADER)

        val authToken = (if (authHeader != null)
            extractTokenFromHeader(authHeader)
        else
            context.cookie(AUTH_TOKEN_COOKIE))
        ?: return null

        return accountRepository.getUserByAuthToken(authToken) ?: return null
    }

    fun logInUser(email: String, plainPassword: String): User? {
        authenticatedUser = null
        val user = accountRepository.getUserByEmail(email) ?: return null

        if (passwordHasher.checkPassword(plainPassword, user.password)) {
            val changeSet = UserChangeSet(authToken = Change(UUID.randomUUID().toString()))
            val userWithAuthToken = accountRepository.updateUser(user.id, changeSet)!!
            setCookie(AUTH_TOKEN_COOKIE, userWithAuthToken.authToken!!)
            authenticatedUser = userWithAuthToken
        }

        return authenticatedUser
    }

    fun logOutUser() {
        val authenticatedUserId = authenticatedUser?.id ?: return
        accountRepository.updateUser(authenticatedUserId, UserChangeSet(authToken = Change(null)))
        unsetCookie(AUTH_TOKEN_COOKIE)
        authenticatedUser = null
    }

    fun setLogInSuccessRedirectUri(uri: String) = setCookie(LOG_IN_REDIRECT_COOKIE, uri)

    fun getLogInSuccessRedirectUri(): String? = context.cookie(LOG_IN_REDIRECT_COOKIE)

    fun clearLogInSuccessRedirectUri() = unsetCookie(LOG_IN_REDIRECT_COOKIE)

    private fun setCookie(name: String, value: String) {
        context.cookie(CookieBuilder(
            path = "/", // root path so that the cookie gets submitted for all pages
            name = name,
            value = value,
            maxAge = -1, // no expiration
            secure = false,
            httpOnly =  false))
    }

    private fun unsetCookie(name: String) = context.removeCookie("/", name)

    companion object {
        private const val AUTHORIZATION_HEADER = "X-Authorization"
        private const val AUTH_TOKEN_COOKIE = "authToken"
        private const val LOG_IN_REDIRECT_COOKIE = "logInSuccessRedirect"

        private val authHeaderRegex = Regex("TOKEN (.+)")

        fun extractTokenFromHeader(header: String): String? {
            val match = authHeaderRegex.find(header) ?: return null
            return match.groups[1]?.value
        }
    }
}
