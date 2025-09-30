package com.moshenskyi.eds.crypto

import kotlinx.coroutines.delay
import java.security.GeneralSecurityException
import java.security.ProviderException
import javax.crypto.AEADBadTagException
import kotlin.random.Random

interface RetryPolicy {
    suspend fun <T> run(block: suspend () -> T): T
}

object NoRetry: RetryPolicy {
    override suspend fun <T> run(block: suspend () -> T) = block()
}

class ExponentialBackoffRetry(
    private val maxAttempts: Int = 3,
    private val baseDelayMs: Long = 40,
    private val maxDelayMs: Long = 400
): RetryPolicy {

    init {
        require(maxAttempts > 0) { "maxAttempts should be positive number" }
        require(baseDelayMs > 0) { "baseDelayMs should be positive number" }
        require(maxDelayMs > 0) { "maxDelayMs should be positive number" }
    }

    override suspend fun <T> run(block: suspend () -> T): T {
        var last: Exception? = null

        repeat(maxAttempts) { attempt ->
            try {
                return block()
            } catch (e: AEADBadTagException) {
                throw e
            } catch (e: ProviderException) {
                last = e
            } catch (e: GeneralSecurityException) {
                last = e
            }
            if (attempt < maxAttempts - 1) {
                val backoff = computeBackoff(attempt)
                delay(backoff)
            }
        }
        throw last ?: IllegalStateException("Operation failed without exception")
    }

    private fun computeBackoff(attempt: Int): Long {
        val exp = (baseDelayMs * (1L shl attempt)).coerceAtMost(maxDelayMs)
        return Random.nextLong(0, exp + 1)
    }

}