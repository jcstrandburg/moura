package data.util

import kotlin.collections.set

abstract class MemoryRepository<TId: Any, TEntity: Any> {

    val entities: Collection<TEntity> get() = entitiesById.values

    private val entitiesById = HashMap<TId, TEntity>()

    abstract fun nextId(): TId

    fun insert(mapper: (TId) -> TEntity): TEntity = synchronized(entities) {
        val id = nextId()

        if (entitiesById.containsKey(id))
            throw Exception("Duplicate userId: $id")

        entitiesById[id] = mapper(id)
        return entitiesById[id]!!
    }

    fun containsId(id: TId) = entitiesById.containsKey(id)

    fun get(id: TId): TEntity? = entitiesById[id]

    fun get(ids: Collection<TId>): List<TEntity> = ids
        .map { id -> entitiesById[id] }
        .filterNotNull()

    fun replace(id: TId, entity: TEntity) {
        entitiesById[id] = entity
    }

    fun delete(id: TId) {
        entitiesById.remove(id)
    }
}