package web.actions

import adr.Action
import io.javalin.Context
import services.AuthenticationService
import java.util.*

class ServeApp(
    private val authenticationService: AuthenticationService
) : Action {
    override fun handle(ctx: Context) {
        val user = authenticationService.getLoggedInUser()

        if (user == null) {
            authenticationService.setLogInSuccessRedirectUri(ctx.path())
            return ctx.redirect("/signin")
        }
        else {
            authenticationService.clearLogInSuccessRedirectUri()
        }

        // TODO: smarter cache buster
        val cacheBuster = UUID.randomUUID()
        val html = """
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <title>React App</title>
        <link rel="stylesheet" href="/styles.css?$cacheBuster">
    </head>
    <body>
        <div id="app" class="app-container">
            Hello world
            <a href="/signout">Log Out</a>
        </div>
        <script src="/index.js?$cacheBuster"></script>
    </body>
</html>
"""

        ctx.result(html)
        ctx.contentType("text/html")
    }
}
