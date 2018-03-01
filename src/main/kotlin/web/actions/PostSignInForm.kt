package web.actions

import adr.Action
import io.javalin.Context
import services.AuthenticationService

class PostSignInForm(
    private val authenticationService: AuthenticationService
) : Action {
    override fun handle(ctx: Context) {
        val (isSuccess, error) = trySignIn(ctx.formParam("email"), ctx.formParam("password"))
        if (isSuccess) {
            ctx.redirect(authenticationService.getLogInSuccessRedirectUri() ?: "/app")
        } else {
            ctx.sessionAttribute("auth_error", error!!)
            ctx.redirect("/signin")
        }
    }

    private fun trySignIn(email: String?, password: String?): Pair<Boolean, String?> {
        val authenticatedUser = authenticationService.logInUser(
            email ?: return Pair(false, "Missing email"),
            password ?: return Pair(false, "Missing password"))

        return if (authenticatedUser != null) {
            Pair(true, null)
        } else {
            Pair(false, "Unable to login with these credentials")
        }
    }
}
