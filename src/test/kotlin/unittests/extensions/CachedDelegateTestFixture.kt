package unittests.extensions

import extensions.cached
import junit.framework.TestCase
import org.junit.Test
import java.time.Duration
import kotlin.concurrent.thread

class CachedDelegateTestFixture: TestCase() {

    val lifeTime: Long = 100

    @Test
    fun testCacheExpiration() {
        val helper = TestHelper()

        val cached = cached(Duration.ofMillis(lifeTime)) {
            helper.nextValue()
        }

        assertEquals(1, cached.value)
        Thread.sleep(5)
        assertEquals(1, cached.value)
        Thread.sleep(lifeTime)
        assertEquals(2, cached.value)
        assertEquals(2, cached.value)
        Thread.sleep(lifeTime)
        assertEquals(3, cached.value)
    }

    @Test
    fun testCacheExpirationIsNotAffectedByLongRunningRequest() {
        val helper = TestHelper()

        val cached = cached(Duration.ofMillis(lifeTime)) {
            Thread.sleep(lifeTime + 50)
            helper.nextValue()
        }

        assertEquals(1, cached.value)
        Thread.sleep(5)
        assertEquals(1, cached.value)
        Thread.sleep(lifeTime)
        assertEquals(2, cached.value)
        assertEquals(2, cached.value)
        Thread.sleep(lifeTime)
        assertEquals(3, cached.value)
    }

    @Test
    fun testCacheSynchronizationWithLongRunningRequests() {
        val helper = TestHelper()

        val cached = cached(Duration.ofMillis(lifeTime)) {
            Thread.sleep(lifeTime + 50)
            helper.nextValue()
        }

        (1..4).map { thread { assertEquals(1, cached.value) } }.forEach { it.join() }
        assertEquals(1, helper.value)

        Thread.sleep(lifeTime)
        (1..4).map { thread { assertEquals(2, cached.value) } }.forEach { it.join() }
        assertEquals(2, helper.value)
    }

    private class TestHelper {
        var value: Int = 0
            private set

        fun nextValue(): Int {
            return ++value
        }
    }
}