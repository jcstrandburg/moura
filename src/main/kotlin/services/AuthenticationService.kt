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

        val userId = context.cookie(USER_ID_COOKIE)?.toIntOrNull()
        val authToken = context.cookie(AUTH_TOKEN_COOKIE)
        if (userId == null || authToken == null)
            return null

        val user = accountRepository.getUserById(userId) ?: return null

        authenticatedUser = if (user.authToken == authToken)
            user
        else
            null

        return authenticatedUser
    }

    fun logInUser(email: String, plainPassword: String): User? {
        authenticatedUser = null
        val user = accountRepository.getUserByEmail(email) ?: return null

        if (passwordHasher.checkPassword(plainPassword, user.password)) {
            val changeSet = UserChangeSet(authToken = Change(UUID.randomUUID().toString()))
            val userWithAuthToken = accountRepository.updateUser(user.id, changeSet)!!
            setCookie(USER_ID_COOKIE, userWithAuthToken.id.toString())
            setCookie(AUTH_TOKEN_COOKIE, userWithAuthToken.authToken!!)
            authenticatedUser = userWithAuthToken
        }

        return authenticatedUser
    }

    fun hashPassword(plainPassword: String) = passwordHasher.hashPassword(plainPassword)

    fun logOutUser() {
        val authenticatedUserId = authenticatedUser?.id ?: return
        accountRepository.updateUser(authenticatedUserId, UserChangeSet(authToken = Change(null)))
        unsetCookie(USER_ID_COOKIE)
        unsetCookie(AUTH_TOKEN_COOKIE)
        authenticatedUser = null
    }

    fun setLogInSuccessRedirectUri(uri: String) = setCookie(LOG_IN_REDIRECT_COOKIE, uri)

    fun getLogInSuccessRedirectUri(): String? = context.cookie(LOG_IN_REDIRECT_COOKIE)

    fun clearLogInSuccessRedirectUri() = unsetCookie(LOG_IN_REDIRECT_COOKIE)

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
        private const val USER_ID_COOKIE = "username"
        private const val AUTH_TOKEN_COOKIE = "authToken"
        private const val LOG_IN_REDIRECT_COOKIE = "logInSuccessRedirect"
    }
}
