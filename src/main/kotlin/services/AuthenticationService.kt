package services

import domain.Change
import domain.accounts.IAccountsRepository
import domain.accounts.User
import domain.accounts.UserChangeSet
import io.javalin.Context
import java.util.*
import javax.servlet.http.Cookie

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
        val cookie = Cookie(name, value)
        cookie.path = "/"
        cookie.maxAge = -1
        cookie.secure = false
        cookie.isHttpOnly = false

        context.cookie(cookie)
    }

    private fun unsetCookie(name: String) = context.removeCookie(name, path ="/")

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
