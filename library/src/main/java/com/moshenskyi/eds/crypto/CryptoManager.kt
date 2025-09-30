package com.moshenskyi.eds.crypto

interface CryptoManager {
    suspend fun encrypt(plaintext: String, aad: String = ""): ByteArray
    suspend fun encryptBytes(plaintext: ByteArray, aad: ByteArray = ByteArray(0)): ByteArray
    suspend fun decrypt(ciphertext: String, aad: String = ""): String
    suspend fun decryptBytes(ciphertext: ByteArray, aad: ByteArray = ByteArray(0)): ByteArray
    fun hashKey(input: String): String
}

fun CryptoManager(
    keyId: String = "secure_data_store_master_key",
    retryPolicy: RetryPolicy = ExponentialBackoffRetry()
): CryptoManager = CryptoManagerImpl(keyId, retryPolicy)
