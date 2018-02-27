package adr

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import io.javalin.Context
import services.AuthenticationService
import kotlin.reflect.KClass

abstract class JsonAction(
    protected val authenticationService: AuthenticationService
) : Action {

    protected val authenticatedUser by lazy { authenticationService.getLoggedInUser()!! }

    override fun handle(ctx: Context) {

        val result = authenticationService.getLoggedInUser()?.let { doHandle(ctx) } ?: Unauthorized()
        ctx.status(result.status.httpCode)
        ctx.contentType("application/json")
        ctx.result(gson.toJson(result.response))
    }

    abstract fun doHandle(ctx: Context): JsonResult

    protected inline fun <reified T:Any> fromBody(ctx: Context) = fromBody(ctx, T::class)

    protected fun <T:Any> fromBody(ctx: Context, kclass: KClass<T>): T {
        try {
            return gson.fromJson(ctx.body(), kclass.java)
        } catch (e: JsonSyntaxException) {
            throw HaltException(Status.BAD_REQUEST)
        }
    }

    companion object {
        val gson = Gson()

        fun Ok(response: Any = Unit) = JsonResult(Status.OK, response)
        fun Created(response: Any = Unit) = JsonResult(Status.CREATED, response)
        fun BadRequest(response: Any = Unit) = JsonResult(Status.BAD_REQUEST, response)
        fun Unauthorized(response: Any = Unit) = JsonResult(Status.UNAUTHORIZED, response)
        fun Forbidden(response: Any = Unit) = JsonResult(Status.FORBIDDEN, response)
        fun NotFound(response: Any = Unit) = JsonResult(Status.NOT_FOUND, response)
        fun Conflict(response: Any = Unit) = JsonResult(Status.CONFLICT, response)
    }
}