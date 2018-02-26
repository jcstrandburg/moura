package vulcan

import kotlin.reflect.KClass
import kotlin.reflect.KVisibility
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.primaryConstructor

class Container(private val resolvers: HashMap<KClass<*>, Resolver>) {

    val registeredTypes get() = resolvers.keys

    constructor() : this(HashMap<KClass<*>, Resolver>())

    fun getNestedContainer(): Container {
        return Container(HashMap(resolvers.map({ it.key to it.value.getResolverForNestedContainer() }).toMap()))
    }

    inline fun <reified T: Any> register(noinline instantiator: Container.() -> T) =
        register(T::class, instantiator, Lifecycle.Singleton)

    inline fun <reified T: Any> register(noinline instantiator: Container.() -> T, lifecycle: Lifecycle = Lifecycle.Singleton) =
        register(T::class, instantiator, lifecycle)

    inline fun <reified T: Any, reified U: T> register(lifecycle: Lifecycle = Lifecycle.Singleton) =
        register(T::class, U::class, lifecycle)

    fun register(kclass: KClass<*>, kclass2: KClass<*>, lifecycle: Lifecycle = Lifecycle.Singleton) {
        if (!kclass2.isSubclassOf(kclass))
            throw IllegalArgumentException()

        if (kclass == kclass2)
            register(kclass, { getImplicit(kclass2, lifecycle) }, lifecycle)
        else
            register(kclass, { get(kclass2, lifecycle) }, lifecycle)
    }

    inline fun <reified T: Any> register(instance: T, lifecycle: Lifecycle = Lifecycle.Singleton) =
        register(T::class, { instance }, lifecycle)

    fun register(kclass: KClass<*>, instantiator: Container.() -> Any, lifecycle: Lifecycle = Lifecycle.Singleton): Container {
        if (resolvers.containsKey(kclass))
            throw InjectionException("Duplicate registration for type ${kclass.qualifiedName}")

        resolvers[kclass] = when(lifecycle) {
            Lifecycle.Singleton -> SingletonResolver(instantiator)
            Lifecycle.PerContainer -> TransientResolver(instantiator)
            Lifecycle.Unique -> AlwaysUniqueResolver(instantiator)
        }

        return this
    }

    inline fun <reified T: Any> get(lifecycle: Lifecycle = Lifecycle.Unique): T =
        get(T::class, lifecycle) as T

    fun get(kclass: KClass<*>, lifecycle: Lifecycle = Lifecycle.Unique): Any {
        val resolver = resolvers[kclass]
        if (resolver != null) {
            if (lifecycle < resolver.lifecycle)
                throw InjectionException("Unsupported lifecycle $lifecycle for type ${kclass.qualifiedName}")

            return resolver.resolve(this)
        }

        return getImplicit(kclass, lifecycle)
    }

    private fun getImplicit(kclass: KClass<*>, lifecycle: Lifecycle) : Any {

        if (kclass == String::class)
            throw InjectionException("Cannot instantiate ${kclass.qualifiedName} via implicit registration")
        if (kclass.isAbstract)
            throw InjectionException("Cannot instantiate abstract class ${kclass.qualifiedName} via implicit registration")
        if (kclass.javaPrimitiveType != null)
            throw InjectionException("Cannot instantiate class ${kclass.qualifiedName} with javaPrimitiveType ${kclass.qualifiedName} via implicit registration")
        if (kclass.constructors.size > 1)
            throw InjectionException("Cannot instantiate class ${kclass.qualifiedName} with multiple constructors via implicit registration")

        val primaryConstructor = kclass.primaryConstructor ?:
            throw InjectionException("Cannot instantiate class ${kclass.qualifiedName} with no primary constructor via implicit registration")
        if (primaryConstructor.visibility != KVisibility.PUBLIC)
            throw InjectionException("Cannot instantiate a class with a non-public primary constructor via implicit registration")

        return primaryConstructor.callBy(primaryConstructor
            .parameters
            .associate { it to get(it.type.classifier as KClass<*>, lifecycle) })
    }
}
