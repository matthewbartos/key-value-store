package pro.bartos.store.data

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.assertThrows
import pro.bartos.store.data.store.InMemoryKeyValueStoreImpl
import pro.bartos.store.data.store.KeyValueStore

class KeyValueStoreTest {

    private var store: KeyValueStore = InMemoryKeyValueStoreImpl()

    @Before
    fun setup() {
        store = InMemoryKeyValueStoreImpl()
    }

    //region Basic tests

    @Test
    fun `GET on missed key returns null`() = runTest {
        assertEquals(null, store.get(key = "0"))
    }

    @Test
    fun `SET and GET return correct value`() = runTest {
        store.set("0", "A")
        assertEquals("A", store.get(key = "0"))
    }

    @Test
    fun `A single transaction is commited correctly`() = runTest {
        store.set("0", "A")
        assertEquals("A", store.get(key = "0"))

        store.beginTransaction()
        store.set("0", "B")
        assertEquals("B", store.get(key = "0"))
        store.commitTransaction()

        assertEquals("B", store.get(key = "0"))
    }

    @Test
    fun `A single transaction is rolled back correctly`() = runTest {
        store.set("0", "A")
        assertEquals("A", store.get(key = "0"))

        store.beginTransaction()
        store.set("0", "B")
        assertEquals("B", store.get(key = "0"))
        store.rollbackTransaction()

        assertEquals("A", store.get(key = "0"))
    }

    //endregion

    //region Concurrency tests

    @Test
    fun `Concurrent transactions commit and rollback works correctly`() = runTest {
        store.set("foo", "123")
        store.set("bar", "456")

        (1..100).forEach { i ->
            launch(Dispatchers.Default) {
                store.beginTransaction()
                /**/ store.set("foo", "456$i")

                /**/ store.beginTransaction()
                /**//**/ assertEquals("456$i", store.get(key = "foo"))
                /**//**/ store.set("foo", "789$i")
                /**//**/ assertEquals("789$i", store.get(key = "foo"))
                /**/ store.rollbackTransaction()

                /**/ assertEquals("456$i", store.get(key = "foo"))
                /**/ store.delete(key = "foo")
                /**/ assertEquals(null, store.get(key = "foo"))
                store.rollbackTransaction()

                assertEquals("123", store.get(key = "foo"))
            }
        }
    }

    @Test
    fun `Concurrent reads and writes work`() = runTest {
        store.set("foo", "bar")
        val coroutineCount = 100
        val results = mutableListOf<String?>()
        val coroutines = mutableListOf<Deferred<Any>>()

        (1..coroutineCount).forEach { i ->
            coroutines.add(
                async(Dispatchers.Default) {
                    if (i % 2 == 0) {
                        store.set("foo", "new_$i")
                    } else {
                        results.add(store.get("foo"))
                    }
                }
            )
        }
        coroutines.awaitAll()
        assertTrue(results.contains(null).not()) // sometimes I was getting null reads while doing concurrent writes and reads
        assertTrue(results.size == coroutineCount / 2, "Result size is ${results.size}, should be ${coroutineCount / 2}")
    }

    @Test
    fun `Concurrent transactions commits may override each other`() = runTest {
        val results = mutableListOf<String?>()
        val coroutines = mutableListOf<Deferred<Boolean>>()
        store.set("foo", "123")

        (1..100).forEach { i ->
            coroutines.add(
                async(Dispatchers.Default) {
                    store.beginTransaction()
                    /**/ store.set("foo", "new_$i")
                    store.commitTransaction()
                    results.add(store.get("foo"))
                }
            )
        }
        coroutines.awaitAll()
        assertTrue(results.distinct().size > 1)
    }
    //endregion

    // region Tests from the official task description
    @Test
    fun `Set and get a value`() = runTest {
        store.set("foo", "123")
        assertEquals("123", store.get(key = "foo"))
    }

    @Test
    fun `Delete a value`() = runTest {
        store.set("foo", "123")
        store.delete(key = "foo")
        assertEquals(null, store.get(key = "foo"))
    }

    /**
     * > SET foo 123
     * > SET bar 456
     * > SET baz 123
     * > COUNT 123
     * 2
     * > COUNT 456
     * 1
     */
    @Test
    fun `Count the number of occurrences of a value`() = runTest {
        store.set("foo", "123")
        store.set("bar", "456")
        store.set("baz", "123")

        assertEquals(2, store.count("123"))
        assertEquals(1, store.count("456"))
    }

    /**
     * > SET bar 123
     * > GET bar
     * 123
     * > BEGIN
     * > SET foo 456
     * > GET bar
     * 123
     * > DELETE bar
     * > COMMIT
     * > GET bar
     * key not set
     * > ROLLBACK
     * no transaction
     * > GET foo
     * 456
     */
    @Test
    fun `Commit a transaction`() = runTest {
        store.set("bar", "123")
        assertEquals("123", store.get(key = "bar"))

        store.beginTransaction()
        /**/ store.set("foo", "456")
        /**/ assertEquals("123", store.get(key = "bar"))
        /**/ store.delete("bar")
        store.commitTransaction()

        assertEquals(null, store.get(key = "bar"))
        assertThrows<IllegalStateException> {
            store.rollbackTransaction()
        }
        assertEquals("456", store.get(key = "foo"))
    }

    /**
     * > SET foo 123
     * > SET bar abc
     * > BEGIN
     * > SET foo 456
     * > GET foo
     * 456
     * > SET bar def
     * > GET bar
     * def
     * > ROLLBACK
     * > GET foo
     * 123
     * > GET bar
     * abc
     * > COMMIT
     * no transaction
     */
    @Test
    fun `Rollback a transaction`() = runTest {
        store.set("foo", "123")
        store.set("bar", "abc")

        store.beginTransaction()
        /**/ store.set("foo", "456")
        /**/ assertEquals("456", store.get(key = "foo"))
        /**/ store.set("bar", "def")
        /**/ assertEquals("def", store.get(key = "bar"))
        store.rollbackTransaction()

        assertEquals("123", store.get(key = "foo"))
        assertEquals("abc", store.get(key = "bar"))

        assertThrows<IllegalStateException> {
            store.commitTransaction()
        }
    }

    /**
     * > SET foo 123
     * > SET bar 456
     * > BEGIN
     * > SET foo 456
     * > BEGIN
     * > COUNT 456
     * 2
     * > GET foo
     * 456
     * > SET foo 789
     * > GET foo
     * 789
     * > ROLLBACK
     * > GET foo
     * 456
     * > DELETE foo
     * > GET foo
     * key not set
     * > ROLLBACK
     * > GET foo
     * 123
     */
    @Test
    fun `Nested transactions`() = runTest {
        store.set("foo", "123")
        store.set("bar", "456")

        store.beginTransaction()
        /**/ store.set("foo", "456")

        /**/ store.beginTransaction()
        /**//**/ assertEquals(2, store.count(value = "456"))
        /**//**/ assertEquals("456", store.get(key = "foo"))
        /**//**/ store.set("foo", "789")
        /**//**/ assertEquals("789", store.get(key = "foo"))
        /**/ store.rollbackTransaction()

        /**/ assertEquals("456", store.get(key = "foo"))
        /**/ store.delete(key = "foo")
        /**/ assertEquals(null, store.get(key = "foo"))
        store.rollbackTransaction()

        assertEquals("123", store.get(key = "foo"))
    }

    //endregion
}
