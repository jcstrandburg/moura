package extensions

import java.time.LocalTime
import java.time.temporal.TemporalAmount

interface ICachedValue<out T> {
    val value: T
}

fun <T> cached(lifetime: TemporalAmount, getter: () -> T): ICachedValue<T> =
    CachedValue(lifetime, getter)

private class CachedValue<out T>(private val lifetime: TemporalAmount, private val getter: () -> T): ICachedValue<T> {
    private var expiration = LocalTime.now().minus(lifetime)
    private var cachedValue: Any? = null

    override val value: T
        get() = synchronized(this) {
            if (expiration <= LocalTime.now()) {
                cachedValue = getter()
                expiration = LocalTime.now().plus(lifetime)
            }

            return cachedValue as T
        }
}
