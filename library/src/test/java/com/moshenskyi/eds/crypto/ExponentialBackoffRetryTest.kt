package com.moshenskyi.eds.crypto

import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.security.GeneralSecurityException
import java.security.ProviderException
import javax.crypto.AEADBadTagException

class ExponentialBackoffRetryTest {

    @Test
    fun `success on first attempt - no sleeps`() = runTest {
        val retry = ExponentialBackoffRetry(
            maxAttempts = 3,
            baseDelayMs = 50,
            maxDelayMs = 400,
        )

        val result = retry.run { 42 }
        assertEquals(42, result)
    }

    @Test
    fun `transient failures then success - sleeps between attempts`() = runTest {
        val retry = ExponentialBackoffRetry(
            maxAttempts = 3,
            baseDelayMs = 50,
            maxDelayMs = 400,
        )

        var calls = 0
        val got = retry.run {
            if (++calls < 3) throw ProviderException("transient")
            "OK"
        }

        assertEquals("OK", got)
        assertEquals(3, calls)
    }

    @Test
    fun `transient GeneralSecurityException then success - sleeps between attempts`() = runTest {
        val retry = ExponentialBackoffRetry(
            maxAttempts = 3,
            baseDelayMs = 50,
            maxDelayMs = 400,
        )

        var calls = 0
        val got = retry.run {
            if (++calls < 3) throw GeneralSecurityException("transient")
            "OK"
        }

        assertEquals("OK", got)
        assertEquals(3, calls)
    }

    @Test(expected = GeneralSecurityException::class)
    fun `transient failure more than maxAttempts then failure - sleeps between attempts`() = runTest {
        val retry = ExponentialBackoffRetry(
            maxAttempts = 3,
            baseDelayMs = 50,
            maxDelayMs = 400,
        )

        var calls = 0
        retry.run {
            if (++calls < 4) throw GeneralSecurityException("transient")
            "OK"
        }
    }

    @Test(expected = AEADBadTagException::class)
    fun `AEADBadTagException - no retry`() = runTest {
        val retry = ExponentialBackoffRetry()

        retry.run { throw AEADBadTagException() }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `maxAttempts illegal`() = runTest {
        ExponentialBackoffRetry(maxAttempts = -1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `baseDelayMs illegal`() = runTest {
        ExponentialBackoffRetry(baseDelayMs = -1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `maxDelayMs illegal`() = runTest {
        ExponentialBackoffRetry(maxDelayMs = -1)
    }

}