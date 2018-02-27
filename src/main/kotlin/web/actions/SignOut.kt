package web.actions

import adr.Action
import io.javalin.Context
import services.AuthenticationService

class SignOut(
    private val authenticationService: AuthenticationService
) : Action {
    override fun handle(ctx: Context) {
        authenticationService.logOutUser()
        ctx.redirect("/signin")
    }
}

