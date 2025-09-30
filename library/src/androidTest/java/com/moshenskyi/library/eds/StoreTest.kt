package com.moshenskyi.library.eds

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.moshenskyi.eds.EncryptedStore
import com.moshenskyi.eds.crypto.CryptoManager
import com.moshenskyi.eds.crypto.encoding.flow
import com.moshenskyi.eds.crypto.encoding.get
import com.moshenskyi.eds.crypto.encoding.put
import com.moshenskyi.eds.crypto.encoding.safeToString
import com.moshenskyi.eds.crypto.encoding.toBytes
import com.moshenskyi.library.eds.crypto.KeystoreTestUtils
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StoreTest {

    private lateinit var context: Context
    private lateinit var store: EncryptedStore

    private lateinit var manager: CryptoManager

    @Before
    fun setup() {
        KeystoreTestUtils.deleteAlias("secure_data_store_master_key")

        context = ApplicationProvider.getApplicationContext()
        manager = CryptoManager()

        store = EncryptedStore(context, manager)
    }

    @After
    fun tearDown() {
        KeystoreTestUtils.deleteAlias("secure_data_store_master_key")
    }

    @Test
    fun testPutInEncryptedStorageAndReadback() = runTest {
        store.clear()

        val key = "test_key"
        val originalValue = "SensitiveData123"

        store.put(key, originalValue)

        val decrypted = store.get<String>(key)

        assertEquals(originalValue, decrypted)
    }

    @Test
    fun testPutIntInEncryptedStorageAndReadback() = runTest {
        store.clear()

        val key = "test_key"
        val originalValue = 123

        store.put(key, originalValue)

        val decrypted = store.get<Int>(key)

        assertEquals(originalValue, decrypted)
    }

    @Test
    fun testPutFloatInEncryptedStorageAndReadback() = runTest {
        store.clear()

        val key = "test_key"
        val originalValue = 234.4F

        store.put(key, originalValue)

        val decrypted = store.get<Float>(key)

        assertEquals(originalValue, decrypted)
    }

    @Test
    fun testPutDoubleInEncryptedStorageAndReadback() = runTest {
        store.clear()

        val key = "test_key"
        val originalValue = 234.4

        store.put(key, originalValue)

        val decrypted = store.get<Double>(key)

        assertEquals(originalValue, decrypted)
    }

    @Test
    fun testPutLongInEncryptedStorageAndReadback() = runTest {
        store.clear()

        val key = "test_key"
        val originalValue = 23434535345L

        store.put(key, originalValue)

        val decrypted = store.get<Long>(key)

        assertEquals(originalValue, decrypted)
    }

    @Test
    fun testPutBytesInEncryptedStorageAndReadback() = runTest {
        store.clear()

        val key = "test_key"
        val originalValue = "some value"
        val plainBytes = originalValue.toBytes()

        store.put(key, plainBytes)

        val decrypted = store.get<ByteArray>(key)?.safeToString()

        assertEquals(originalValue, decrypted)
    }

    @Test
    fun testSubscribeOnEncrypted() = runTest {
        val key = "test_key"
        val originalValue = "SensitiveData123"

        store.put(key, originalValue.toByteArray())

        store.flow<ByteArray>(key).test {
            val item = awaitItem()
            assertEquals(originalValue, item?.safeToString())
        }
    }

    @Test
    fun testSubscribeOnEncryptedString() = runTest {
        val key = "test_key"
        val originalValue = "SensitiveData123"

        store.put(key, originalValue)

        store.flow<String>(key).test {
            val item = awaitItem()
            assertEquals(originalValue, item)
        }
    }

    @Test
    fun testPutValueContainsTrue() = runTest {
        store.clear()

        val key = "test_key"
        val originalValue = 23434535345L

        store.put(key, originalValue)

        val isValuePresent = store.contains(key)
        assertTrue(isValuePresent)
    }

    @Test
    fun testRemoveValueContainsFalse() = runTest {
        store.clear()

        val key = "test_key"
        val originalValue = 23434535345L

        store.put(key, originalValue)

        var isValuePresent = store.contains(key)
        assertTrue(isValuePresent)

        store.remove(key)
        isValuePresent = store.contains(key)
        assertFalse(isValuePresent)
    }

}