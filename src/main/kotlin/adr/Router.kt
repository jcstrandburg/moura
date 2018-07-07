package adr

import io.javalin.ApiBuilder
import io.javalin.Context
import io.javalin.Javalin
import kotlin.reflect.KClass

class Router(private val doGetAction: (Context, KClass<*>) -> Action) {

    fun routes(app: Javalin, register: Router.() -> Unit): Javalin = app.routes { register() }

    fun path(p: String, register: Router.() -> Unit) = ApiBuilder.path(p) { register() }

    inline fun <reified T: Action> get(path: String = "") = get(T::class, path)
    fun <T: Action> get(kclass: KClass<T>, path: String = "")  = ApiBuilder.get(path, doAction(kclass))

    inline fun <reified T: Action> put(path: String = "") = put(T::class, path)
    fun <T: Action> put(kclass: KClass<T>, path: String = "") = ApiBuilder.put(path, doAction(kclass))

    inline fun <reified T: Action> delete(path: String = "") = delete(T::class, path)
    fun <T: Action> delete(kclass: KClass<T>, path: String = "") = ApiBuilder.delete(path, doAction(kclass))

    inline fun <reified T: Action> post(path: String = "") = post(T::class, path)
    fun <T: Action> post(kclass: KClass<T>, path: String = "") = ApiBuilder.post(path, doAction(kclass))

    inline fun <reified T: Action> patch(path: String = "") = patch(T::class, path)
    fun <T: Action> patch(kclass: KClass<T>, path: String = "") = ApiBuilder.patch(path, doAction(kclass))

    private fun doAction(kclass: KClass<*>): (Context) -> Unit = { ctx ->
        doGetAction(ctx, kclass).handle(ctx)
    }
}
