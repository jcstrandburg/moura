package unittests.vulcan

import junit.framework.TestCase
import vulcan.Container
import vulcan.InjectionException
import vulcan.Lifecycle
import kotlin.test.assertFailsWith

class ContainerTestFixture: TestCase() {

    override fun setUp() {
        container = Container()
    }

    fun testUniqueLifecycle() {
        container.register<IDependencyBase, DependencyConcrete>(Lifecycle.Unique)
        assertNotSame(container.get<IDependencyBase>(), container.get<IDependencyBase>())
    }

    fun testPerContainerLifecycle() {
        container.register<IDependencyBase, DependencyConcrete>(Lifecycle.PerContainer)
        val nestedContainer = container.getNestedContainer()

        assertSame(container.get<IDependencyBase>(), container.get<IDependencyBase>())
        assertSame(nestedContainer.get<IDependencyBase>(), nestedContainer.get<IDependencyBase>())
        assertNotSame(container.get<IDependencyBase>(), nestedContainer.get<IDependencyBase>())
    }

    fun testSingletonLifecycle() {
        container.register<IDependencyBase, DependencyConcrete>(Lifecycle.Singleton)
        val nestedContainer = container.getNestedContainer()
        assertSame(container.get<IDependencyBase>(), container.get<IDependencyBase>())
    }

    fun testGetUnregisteredDependencyThrows() {
        assertFailsWith(InjectionException::class) { container.get<IDependencyBase>() }
    }

    fun testGetDependencyWithIntParamThrows() {
        assertFailsWith(InjectionException::class) { container.get<DependencyWithIntParam>() }
    }

    fun testGetDependencyWithNullableStringParamThrows() {
        assertFailsWith(InjectionException::class) { container.get<DependencyWithNullableStringParam>() }
    }

    fun testGetSingletonWithPerContainerDependencyThrows() {
        container.register<IDependencyBase, DependencyConcrete>(Lifecycle.PerContainer)
        container.register<IThingy, ThingyConcrete>(Lifecycle.Singleton)
        assertFailsWith(InjectionException::class) { container.get<IThingy>() }
    }

    lateinit var container: Container

    interface IDependencyBase
    class DependencyConcrete: IDependencyBase

    interface IThingy
    class ThingyConcrete(dependency: IDependencyBase): IThingy

    class DependencyWithIntParam(x: Int)
    class DependencyWithNullableStringParam(x: String)
}