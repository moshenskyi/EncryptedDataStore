package com.moshenskyi.eds

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.moshenskyi.eds.crypto.CryptoManager
import com.moshenskyi.eds.crypto.encoding.Codec
import com.moshenskyi.eds.crypto.encoding.DefaultCodecs
import com.moshenskyi.eds.crypto.encoding.decode
import com.moshenskyi.eds.crypto.encoding.safeToString
import com.moshenskyi.eds.crypto.encoding.toBytes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

// TODO: Package name + store name
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class EncryptedStore(
    context: Context,
    private val cryptoManager: CryptoManager
) {
    private val store = context.dataStore

    private fun keyOf(name: String): Preferences.Key<String> {
        val hashedKey = cryptoManager.hashKey(name)
        return stringPreferencesKey(hashedKey)
    }

    suspend fun <T : Any> put(
        name: String,
        value: T,
        codec: Codec<T>
    ) {
        val prefKey = keyOf(name)
        val aad = prefKey.name.toBytes()

        val serialized = codec.encode(value)
        val plainBytes = serialized.toBytes()
        val cipherBytes = cryptoManager.encryptBytes(plainBytes, aad)

        store.edit {
            it[prefKey] = DefaultCodecs.bytes.encode(cipherBytes)
        }
    }

    fun <T : Any> flow(
        name: String,
        codec: Codec<T>
    ): Flow<T?> {
        val key = keyOf(name)

        return store.data.map { pref ->
            pref[key]?.let { enc ->
                val cipherText = enc.decode()
                val aad = key.name.toBytes()
                val encoded = cryptoManager.decryptBytes(cipherText, aad).safeToString()
                codec.decode(encoded)
            }
        }
    }

    suspend fun <T : Any> get(
        name: String,
        codec: Codec<T>
    ): T? {
        val prefKey = keyOf(name)

        return store.data.map { it[prefKey] }.firstOrNull()?.let {
            val encoded = cryptoManager.decrypt(it, prefKey.name)
            codec.decode(encoded)
        }
    }

    suspend fun contains(name: String): Boolean = store.data
        .map { preferences -> preferences.asMap().containsKey(keyOf(name)) }
        .firstOrNull() ?: false

    suspend fun remove(name: String) = store.edit { it.remove(keyOf(name)) }

    suspend fun clear() = store.edit { it.clear() }
}
