package data.util

import java.util.*

class UuidMemoryRepository<T: Any>: MemoryRepository<UUID, T>() {
    override fun nextId() = UUID.randomUUID()
}