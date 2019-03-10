package unittests.vulcan

import junit.framework.TestCase
import vulcan.Container
import vulcan.InjectionException
import vulcan.Lifecycle
import kotlin.test.assertFailsWith

@Suppress("UNUSED_PARAMETER")
class ContainerTestFixture: TestCase() {

    override fun setUp() {
        container = Container()
    }

    fun `test unique lifecycle`() {
        container.register<IDependencyBase, DependencyConcrete>(Lifecycle.Unique)
        assertNotSame(container.get<IDependencyBase>(), container.get<IDependencyBase>())
    }

    fun `test PerContainer lifecycle`() {
        container.register<IDependencyBase, DependencyConcrete>(Lifecycle.PerContainer)
        val nestedContainer = container.getNestedContainer()

        assertSame(container.get<IDependencyBase>(), container.get<IDependencyBase>())
        assertSame(nestedContainer.get<IDependencyBase>(), nestedContainer.get<IDependencyBase>())
        assertNotSame(container.get<IDependencyBase>(), nestedContainer.get<IDependencyBase>())
    }

    fun `test Singleton lifecycle`() {
        container.register<IDependencyBase, DependencyConcrete>(Lifecycle.Singleton)
        val nestedContainer = container.getNestedContainer()
        assertSame(container.get<IDependencyBase>(), nestedContainer.get<IDependencyBase>())
    }

    fun `test get unregistered dependency throws`() {
        assertFailsWith(InjectionException::class) { container.get<IDependencyBase>() }
    }

    fun `test get dependency with Int param throws`() {
        assertFailsWith(InjectionException::class) { container.get<DependencyWithIntParam>() }
    }

    fun `test get dependency with nullable String param throws`() {
        assertFailsWith(InjectionException::class) { container.get<DependencyWithNullableStringParam>() }
    }

    fun `test get Singleton with PerContainer dependency throws`() {
        container.register<IDependencyBase, DependencyConcrete>(Lifecycle.PerContainer)
        container.register<IThingy, ThingyConcrete>(Lifecycle.Singleton)
        assertFailsWith(InjectionException::class) { container.get<IThingy>() }
    }

    fun `test registering type against itself`() {
        container.register<IDependencyBase, DependencyConcrete>()
        container.register<DependencyConcrete, DependencyConcrete>()
        assertSame(container.get<IDependencyBase>(), container.get<DependencyConcrete>())
    }

    lateinit var container: Container

    interface IDependencyBase
    class DependencyConcrete: IDependencyBase

    interface IThingy
    class ThingyConcrete(dependency: IDependencyBase): IThingy

    class DependencyWithIntParam(x: Int)
    class DependencyWithNullableStringParam(x: String?)
}