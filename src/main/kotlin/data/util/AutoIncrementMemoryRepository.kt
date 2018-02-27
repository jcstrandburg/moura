package data.util

class AutoIncrementMemoryRepository<T: Any>(startingId: Int = 1000): MemoryRepository<Int, T>() {

    private var autoIncrementId = startingId

    override fun nextId() = autoIncrementId++
}