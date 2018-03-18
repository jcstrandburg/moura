package web.actions

import adr.Action
import io.javalin.Context
import java.util.*

class ServeMock : Action {
    override fun handle(ctx: Context) {
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
        <div id="mock-viewer" class="app-container" />
        <script src="/index.js?$cacheBuster"></script>
    </body>
</html>
"""
        ctx.result(html)
        ctx.contentType("text/html")
    }
}