package vulcan

typealias Instantiator = Container.() -> Any

abstract class Resolver(protected val instatiator: Instantiator, val lifecycle: Lifecycle) {

    abstract fun resolve(container: Container): Any

    abstract fun getResolverForNestedContainer(): Resolver

    fun instantiate(container: Container): Any = container.instatiator()
}

class AlwaysUniqueResolver(instatiator: Instantiator)
    : Resolver(instatiator, Lifecycle.Unique)
{
    override fun resolve(container: Container) = instantiate(container)

    override fun getResolverForNestedContainer() = this
}

abstract class CachingResolver(instatiator: Instantiator, lifecycle: Lifecycle)
    : Resolver(instatiator, lifecycle)
{
    override fun resolve(container: Container): Any {
        synchronized(this) {
            if (cachedValue == null)
                cachedValue = instantiate(container)

            return cachedValue!!
        }
    }

    private var cachedValue : Any? = null
}

class SingletonResolver(instatiator: Container.() -> Any)
    : CachingResolver(instatiator, Lifecycle.Singleton)
{
    override fun getResolverForNestedContainer() = this
}

class TransientResolver(instatiator: Container.() -> Any)
    : CachingResolver(instatiator, Lifecycle.PerContainer)
{
    override fun getResolverForNestedContainer() = TransientResolver(instatiator)
}
