package adr

import io.javalin.Context

interface Action { fun handle(ctx: Context) }
