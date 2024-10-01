package pro.bartos.store.data.store

import java.util.concurrent.ConcurrentHashMap

// ConcurrentHashMap ensures atomic operations on items
typealias KeyValueDb = ConcurrentHashMap<String, String>
typealias Stack<T> = MutableList<T>

class InMemoryKeyValueStoreImpl : KeyValueStore {
    // source of truth
    private val db = KeyValueDb()

    /**
     * Thread to Transactions db snapshot Stack ConcurrentHashMap
     * key = thread name, value = db snapshot stack
     * using a separate stack for each thread - better time performance (less locks), worse memory performance (db snapshots)
     * ensures transaction isolation
     *
     * Alternatively, transactions could be implemented as a list of operations to save memory
     */
    private val transactionStackMap = ConcurrentHashMap<String, Stack<KeyValueDb>>()

    private val thread
        get() = Thread.currentThread().name

    private val currentTransaction
        get() = transactionStackMap[thread]?.lastOrNull()

    private val dbSnapshot
        get() = (currentTransaction ?: db)

    override suspend fun get(key: String): String? =
        dbSnapshot[key]

    override suspend fun set(key: String, value: String) {
        dbSnapshot[key] = value
    }

    override suspend fun delete(key: String) =
        dbSnapshot.remove(key)

    override suspend fun count(value: String): Int =
        dbSnapshot.values.count { it == value }

    override suspend fun beginTransaction() {
        transactionStackMap
            .computeIfAbsent(thread) { mutableListOf() }
            .add(ConcurrentHashMap(dbSnapshot))
    }

    @Throws(IllegalStateException::class)
    override suspend fun commitTransaction() {
        // thread-safe way to replace db with current transaction snapshot
        // multiple concurrent transactions will override each other
        currentTransaction?.let { transactionSnapshot ->
            (db.keys + transactionSnapshot.keys)
                .distinct()
                .forEach { key ->
                    transactionSnapshot[key]?.let { db[key] = it } ?: db.remove(key)
                }
            transactionStackMap[thread]?.remove(transactionSnapshot)
        } ?: error("No transaction to commit")
    }

    @Throws(IllegalStateException::class)
    override suspend fun rollbackTransaction() {
        transactionStackMap[thread]?.let { stack ->
            stack.lastOrNull()?.let { stack.remove(it) }
        } ?: error("No transaction to rollback")
    }

    // fun cleanup() {
    //      TODO: Maybe it's a good idea to clean up TransactionStackMap periodically?
    // }
}