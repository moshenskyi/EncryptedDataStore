package com.moshenskyi.eds.crypto

import android.util.Base64
import com.google.crypto.tink.Aead
import com.google.crypto.tink.integration.android.AndroidKeystore
import com.moshenskyi.eds.crypto.encoding.safeToString
import com.moshenskyi.eds.crypto.encoding.toBytes
import java.security.KeyStore
import java.security.MessageDigest

private const val HASHING_ALGORITHM = "SHA-256"

internal class CryptoManagerImpl(
    keyId: String,
    private val retryPolicy: RetryPolicy
) : CryptoManager {

    private val aead: Aead = getOrCreateAead(keyId)

    private fun getOrCreateAead(alias: String): Aead {
        val keystore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        if (!keystore.containsAlias(alias)) {
            AndroidKeystore.generateNewAes256GcmKey(alias)
        }

        return AndroidKeystore.getAead(alias)
    }

    override suspend fun encrypt(plaintext: String, aad: String): ByteArray {
        val cipherText = encryptBytes(
            plaintext.toBytes(),
            aad.toBytes()
        )
        return cipherText
    }

    override suspend fun encryptBytes(plaintext: ByteArray, aad: ByteArray): ByteArray =
        retryPolicy.run { aead.encrypt(plaintext, aad) }

    override suspend fun decrypt(ciphertext: String, aad: String): String {
        return decryptBytes(
            Base64.decode(ciphertext, Base64.NO_WRAP),
            aad.toBytes()
        ).safeToString()
    }

    override suspend fun decryptBytes(ciphertext: ByteArray, aad: ByteArray): ByteArray =
        retryPolicy.run { aead.decrypt(ciphertext, aad) }

    override fun hashKey(input: String): String {
        val digest = MessageDigest.getInstance(HASHING_ALGORITHM)
        val hash = digest.digest(input.toBytes())
        return hash.joinToString("") { "%02x".format(it) }
    }
}