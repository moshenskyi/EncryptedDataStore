package com.moshenskyi.eds.crypto.encoding

import com.moshenskyi.eds.EncryptedStore
import kotlinx.coroutines.flow.Flow

interface Codec<T> {
    fun encode(value: T): String
    fun decode(value: String): T?
}

object DefaultCodecs {
    val string = object : Codec<String> {
        override fun encode(value: String) = value
        override fun decode(value: String) = value
    }
    val int = object : Codec<Int> {
        override fun encode(value: Int) = value.toString()
        override fun decode(value: String) = value.toIntOrNull()
    }
    val long = object : Codec<Long> {
        override fun encode(value: Long) = value.toString()
        override fun decode(value: String) = value.toLongOrNull()
    }
    val boolean = object : Codec<Boolean> {
        override fun encode(value: Boolean) = value.toString()
        override fun decode(value: String) = value.toBooleanStrictOrNull()
    }
    val double = object : Codec<Double> {
        override fun encode(value: Double) = value.toRawBits().toString()
        override fun decode(value: String) = value.toLongOrNull()?.let(Double::fromBits)
    }
    val float = object : Codec<Float> {
        override fun encode(value: Float) = value.toRawBits().toString()
        override fun decode(value: String) = value.toIntOrNull()?.let(Float::fromBits)
    }
    val bytes = object : Codec<ByteArray> {
        override fun encode(value: ByteArray) = value.encodeToString()
        override fun decode(value: String) = runCatching { value.decode() }.getOrNull()
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : Any> ofType(): Codec<T> = when (T::class) {
        String::class -> string as Codec<T>
        Int::class -> int as Codec<T>
        Long::class -> long as Codec<T>
        Boolean::class -> boolean as Codec<T>
        Double::class -> double as Codec<T>
        Float::class -> float as Codec<T>
        ByteArray::class -> bytes as Codec<T>
        else -> error("No default Codec for ${T::class}. Provide one explicitly.")
    }
}

suspend inline fun <reified T : Any> EncryptedStore.put(
    name: String,
    value: T,
) = put(name, value, DefaultCodecs.ofType())

inline fun <reified T : Any> EncryptedStore.flow(name: String): Flow<T?> =
    flow(name, DefaultCodecs.ofType())

suspend inline fun <reified T : Any> EncryptedStore.get(
    name: String,
): T? = get(name, DefaultCodecs.ofType())