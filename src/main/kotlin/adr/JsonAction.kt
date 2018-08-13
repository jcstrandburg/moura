package adr

import io.javalin.Context
import services.AuthenticationService
import kotlin.reflect.KClass

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
        ctx.json(result.response)
    }

    abstract fun doHandle(ctx: Context): JsonResult

    protected inline fun <reified T:Any> fromBody(ctx: Context) = fromBody(ctx, T::class)

    protected fun <T:Any> fromBody(ctx: Context, kclass: KClass<T>): T {
        try {
            return ctx.bodyAsClass(kclass.java)
        } catch (e: Exception) {
            throw HaltException(Status.BAD_REQUEST)
        }
    }

    companion object {

        fun Ok(response: Any = Unit) = JsonResult(Status.OK, response)
        fun Created(response: Any = Unit) = JsonResult(Status.CREATED, response)
        fun BadRequest(response: Any = Unit) = JsonResult(Status.BAD_REQUEST, response)
        fun Unauthorized(response: Any = Unit) = JsonResult(Status.UNAUTHORIZED, response)
        fun Forbidden(response: Any = Unit) = JsonResult(Status.FORBIDDEN, response)
        fun NotFound(response: Any = Unit) = JsonResult(Status.NOT_FOUND, response)
        fun Conflict(response: Any = Unit) = JsonResult(Status.CONFLICT, response)
    }
}
