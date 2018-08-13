package domain

import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.declaredMemberProperties

data class Change<out S>(val value: S)

open class ChangeSet<T: Any> {
    fun getChanges(): Map<String, Any?> {
        val kclass = this.javaClass.kotlin

        return kclass.declaredMemberProperties
            .mapNotNull { prop -> toChangeInfo(prop) }
            .associateBy({ it.name }, { it.value })
    }

    fun applyTo(src: T): T {
        val changes = getChanges()
        val copy = src.javaClass.kotlin.declaredMemberFunctions.single { it.name == "copy" }

        val paramsByName = copy.parameters
            .associateBy { p -> p.name }

        val params = changes.mapKeys { paramsByName[it.key]!! }.toMutableMap()
        params[paramsByName[null]!!] = src

        return copy.callBy(params) as T
    }

    private fun toChangeInfo(prop: KProperty1<ChangeSet<T>, *>): ChangeInfo? {
        val propertyType = prop.returnType

        if (!propertyType.isMarkedNullable)
            throw Exception("ChangeSet properties must be nullable")

        val value = prop.get(this)
        return when (value) {
            null -> null
            is Change<*> -> ChangeInfo(prop.name, value.value)
            else -> ChangeInfo(prop.name, value)
        }
    }

    data class ChangeInfo(val name: String, val value: Any?)
}
