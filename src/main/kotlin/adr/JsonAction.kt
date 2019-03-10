package adr

import io.javalin.BadRequestResponse
import io.javalin.Context
import services.AuthenticationService

abstract class JsonAction(
    protected val authenticationService: AuthenticationService
) : Action {

    protected val authenticatedUser by lazy { authenticationService.getLoggedInUser()!! }

    open fun before(): JsonResult? {
        return if (authenticationService.getLoggedInUser() == null)
            Unauthorized()
        else
            null
    }

    override fun handle(ctx: Context) {
        val result = before() ?: doHandle(ctx)

        ctx.status(result.status.httpCode)
        ctx.contentType("application/json")
        ctx.json(result.response)
    }

    abstract fun doHandle(ctx: Context): JsonResult

    protected inline fun <reified T:Any> fromBody(ctx: Context): T {
        try {
            return ctx.body<T>()
        } catch (e: Exception) {
            throw BadRequestResponse()
        }
    }

    companion object {
        private val nothing = mapOf<String, Any>()

        fun Ok(response: Any = nothing) = JsonResult(Status.OK, response)
        fun Created(response: Any = nothing) = JsonResult(Status.CREATED, response)
        fun BadRequest(response: Any = nothing) = JsonResult(Status.BAD_REQUEST, response)
        fun Unauthorized(response: Any = nothing) = JsonResult(Status.UNAUTHORIZED, response)
        fun Forbidden(response: Any = nothing) = JsonResult(Status.FORBIDDEN, response)
        fun NotFound(response: Any = nothing) = JsonResult(Status.NOT_FOUND, response)
        fun Conflict(response: Any = nothing) = JsonResult(Status.CONFLICT, response)
    }
}
