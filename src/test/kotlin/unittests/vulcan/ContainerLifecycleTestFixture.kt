package unittests.vulcan

import junit.framework.TestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import vulcan.Container
import vulcan.InjectionException
import vulcan.Lifecycle
import kotlin.test.assertFailsWith

@RunWith(Parameterized::class)
class ContainerLifecycleTestFixture(
    val registerLifecycle: Lifecycle,
    val getLifecycle: Lifecycle,
    val shouldFail: Boolean
): TestCase() {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "Register with {0} and get with {1}")
        fun testData(): Collection<Array<Any>> {
            return listOf(
                arrayOf(Lifecycle.Singleton, Lifecycle.Singleton, false),
                arrayOf(Lifecycle.Singleton, Lifecycle.PerContainer, false),
                arrayOf(Lifecycle.Singleton, Lifecycle.Unique, false),
                arrayOf(Lifecycle.PerContainer, Lifecycle.Singleton, true),
                arrayOf(Lifecycle.PerContainer, Lifecycle.PerContainer, false),
                arrayOf(Lifecycle.PerContainer, Lifecycle.Unique, false),
                arrayOf(Lifecycle.Unique, Lifecycle.Singleton, true),
                arrayOf(Lifecycle.Unique, Lifecycle.PerContainer, true),
                arrayOf(Lifecycle.Unique, Lifecycle.Unique, false)
            )
        }
    }

    @Test
    fun testGetDependencyWithLifecycle() {
        val container = Container()
        container.register<IFace, Implementation>(registerLifecycle)

        if (shouldFail)
            assertFailsWith(InjectionException::class) { container.get<IFace>(getLifecycle) }
        else
            assertNotNull(container.get<IFace>(getLifecycle))
    }

    interface IFace
    class Implementation: IFace
}