package pro.bartos.store.data.store

interface KeyValueStore {
    // Value operations
    suspend fun get(key: String): String?
    suspend fun set(key: String, value: String)
    suspend fun set(keyToValue: Pair<String, String>) {
        set(key = keyToValue.first, value = keyToValue.second)
    }
    suspend fun delete(key: String): String?
    suspend fun count(value: String): Int

    // Transaction operations
    suspend fun beginTransaction()

    @Throws(IllegalStateException::class)
    suspend fun commitTransaction()

    @Throws(IllegalStateException::class)
    suspend fun rollbackTransaction()
}