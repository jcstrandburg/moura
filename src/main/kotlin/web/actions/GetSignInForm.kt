package web.actions

import adr.Action
import io.javalin.Context
import services.AuthenticationService

class GetSignInForm(
    private val authenticationService: AuthenticationService
) : Action {

    override fun handle(ctx: Context) {
        val user = authenticationService.getLoggedInUser()
        if (user != null)
            return ctx.redirect("/app")

        val authError: String? = ctx.sessionAttribute("authError")

        val err = if (authError != null) {
            ctx.sessionAttribute("authError", Unit)
            "<div>$authError</div>";
        } else {
            ""
        }

        // todo: make this a real template
        val html = """
<html>
    <head>
    </head>
    <body>
        $err
        <form method="POST">
            Email: <input type="text" name="email"></input><br>
            Password: <input type="password" name="password"></input><br>
            <input type="submit" />
        </form>
    </body>
</html>
"""
        ctx.result(html)
        ctx.contentType("text/html")
    }
}