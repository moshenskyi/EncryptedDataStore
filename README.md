# Encrypted Data Store

A type-safe, coroutine-friendly wrapper around **Android DataStore** that adds **automatic encryption** using the Android **Keystore**.  
This library makes it simple to persist sensitive data (tokens, credentials, preferences) without worrying about encryption details.

## ✨ Features

- 🔐 **Secure by default**: encryption keys are stored in **Android Keystore** and never leave the device  
- 📦 Backed by **DataStore Preferences**  
- 🧩 **Codec system** for type-safe serialization (supports `String`, `Int`, `Long`, `Double`, `Boolean`, `ByteArray` out of the box)  
- 🔄 Easy to extend with **custom codecs** (e.g. `UUID`, `LocalDate`, `BigDecimal`)  
- ⚡ Coroutine/Flow support for reactive data access  
- 🛡️ AEAD with key-bound **AAD** (associated data) for integrity and tamper protection
- All keys are hashed internally - prevents leaking sensitive key names or exposing your app's data structure

---

## 🚀 Installation

Coming soon to Maven Central / Gradle.

## 📝 Usage

```kotlin
val cryptoManager = CryptoManager() // add keyId or retryPolicy if you want
val store = EncryptedStore(context, cryptoManager)

// Save a value
store.put("username", "alice")

// Subscribe for a value change
val username: Flow<String?> = store.flow("username")

// Get a single value
val idToken: String? = store.get("idToken")

// Remove value
store.remove("idToken")

// Clear storage
store.clear()
```

## Supported default types

- String
- Int
- Long
- Double
- Float
- Boolean
- ByteArray

## Custom codecs

You can provide your own codec for custom types:

```kotlin
object UuidCodec : Codec<UUID> {
    override fun encode(value: UUID): String = value.toString()
    override fun decode(str: String): UUID = UUID.fromString(str)
}

store.put("session", UUID.randomUUID(), UuidCodec)
val session: Flow<UUID?> = store.get("session", UuidCodec)
```

## 🔧 How it works

1. Values are serialized via PlainCodec<T> → UTF-8 bytes
2. Bytes are encrypted with a key stored in Android Keystore (CryptoManager)
3. Ciphertext is Base64-encoded and stored in DataStore
4. On read: reverse the process

➡️ Because keys are bound to the Keystore, even if DataStore files are extracted, they cannot be decrypted on another device.

## 🤝 Contributing

Pull requests are welcome!
If you’d like to improve the library, add new codecs, or fix issues — feel free to open a PR.
And if you find this useful, give it a ⭐ on GitHub.
