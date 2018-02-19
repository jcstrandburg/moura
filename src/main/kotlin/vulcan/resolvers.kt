package vulcan

abstract class Resolver(
    protected val instatiator: Container.() -> Any,
    val lifecycle: Lifecycle
) {

    abstract fun resolve(container: Container): Any

    abstract fun getResolverForNestedContainer(): Resolver

    fun instantiate(container: Container): Any = container.instatiator()
}

class AlwaysUniqueResolver : Resolver {

    constructor(instatiator: Container.() -> Any) : super(instatiator, Lifecycle.Unique)

    override fun resolve(container: Container) = instantiate(container)

    override fun getResolverForNestedContainer() = this
}

abstract class CachingResolver : Resolver {

    constructor(instatiator: Container.() -> Any, lifecycle: Lifecycle) : super(instatiator, lifecycle)

    override fun resolve(container: Container): Any {
        synchronized(this) {
            if (cachedValue == null)
                cachedValue = instantiate(container)

            return cachedValue!!
        }
    }

    private var cachedValue : Any? = null
}

class SingletonResolver : CachingResolver {

    constructor(instatiator: Container.() -> Any) : super(instatiator, Lifecycle.Singleton)

    override fun getResolverForNestedContainer() = this
}

class TransientResolver : CachingResolver {

    constructor(instatiator: Container.() -> Any) : super(instatiator, Lifecycle.PerContainer)

    override fun getResolverForNestedContainer() = TransientResolver(instatiator)
}
