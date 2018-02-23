package adr

import io.javalin.ApiBuilder
import io.javalin.Context
import io.javalin.Javalin
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

class Router(private val doGetAction: (Context, KClass<*>) -> Any) {

    fun routes(app: Javalin, register: Router.() -> Unit): Javalin = app.routes { register() }

    fun path(p: String, register: Router.() -> Unit) = ApiBuilder.path(p) { register() }

    inline fun <reified T: IGetAction> get(path: String = "") = get(T::class, path)

    fun get(kclass: KClass<*>, path: String = "") {
        assertIsSubclass(kclass, IGetAction::class)
        ApiBuilder.get(path) { ctx -> getAction<IGetAction>(ctx, kclass).get(ctx) }
    }

    inline fun <reified T: IPutAction> put(path: String = "") = put(T::class, path)

    fun put(kclass: KClass<*>, path: String = "") {
        assertIsSubclass(kclass, IPutAction::class)
        ApiBuilder.put(path) { ctx -> getAction<IPutAction>(ctx, kclass).put(ctx) }
    }

    inline fun <reified T: IDeleteAction> delete(path: String = "") = delete(T::class, path)

    fun delete(kclass: KClass<*>, path: String = "") {
        assertIsSubclass(kclass, IDeleteAction::class)
        ApiBuilder.delete(path) { ctx -> getAction<IDeleteAction>(ctx, kclass).delete(ctx) }
    }

    inline fun <reified T: IPostAction> post(path: String = "") = post(T::class, path)

    fun post(kclass: KClass<*>, path: String = "") {
        assertIsSubclass(kclass, IPostAction::class)
        ApiBuilder.post(path) { ctx -> getAction<IPostAction>(ctx, kclass).post(ctx) }
    }

    inline fun <reified T: IPatchAction> patch(path: String = "") = patch(T::class, path)

    fun patch(kclass: KClass<*>, path: String = "") {
        assertIsSubclass(kclass, IPatchAction::class)
        ApiBuilder.patch(path) { ctx -> getAction<IPatchAction>(ctx, kclass).patch(ctx) }
    }

    private fun <T: Any> getAction(ctx: Context, kclass: KClass<*>): T =
        doGetAction(ctx, kclass) as T

    private fun assertIsSubclass(actionClass: KClass<*>, iface: KClass<*>) {
        if (!actionClass.isSubclassOf(iface))
            throw IllegalArgumentException("Type ${actionClass.qualifiedName} mismatch does not implement ${iface.qualifiedName}")
    }
}
