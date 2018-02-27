package web.actions

import adr.Action
import adr.HaltException
import adr.Status
import io.javalin.Context
import services.AuthenticationService

class PostSignInForm(
    private val authenticationService: AuthenticationService
) : Action {
    override fun handle(ctx: Context) {
        val username = ctx.formParam("username") ?: throw HaltException(Status.BAD_REQUEST)
        val password = ctx.formParam("password") ?: throw HaltException(Status.BAD_REQUEST)
        val authenticatedUser = authenticationService.logInUser(username, password)

        if (authenticatedUser != null) {
            ctx.redirect(authenticationService.getLogInSuccessRedirectUri() ?: "/app")
        } else {
            ctx.sessionAttribute("auth_error", "Unable to login with these credentials")
            ctx.redirect("/signin")
        }
    }
}
