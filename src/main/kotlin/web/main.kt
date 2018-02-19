package web

import io.javalin.Context
import io.javalin.Javalin
import vulcan.Container
import vulcan.Lifecycle

class HelloAction(private val ctx: Context) {
    fun get() {
        ctx.result("hey dere hi dere ho dere")
    }
}

class Whatever(val x: Int?)

val container = Container()

inline fun <reified T: Any> getAction(ctx: Context): T
    = container.getNestedContainer().register(ctx, Lifecycle.PerContainer).get()

fun main(args: Array<String>) {
    val app = Javalin.start(7000)

    val container = Container()

    val whatever = container.get<Whatever>()

    app.get("/") { ctx -> getAction<HelloAction>(ctx).get() }
}