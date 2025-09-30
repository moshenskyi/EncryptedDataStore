package com.moshenskyi.eds.crypto.encoding

import android.util.Base64

fun ByteArray.safeToString(): String = toString(Charsets.UTF_8)

fun String.toBytes(): ByteArray = toByteArray(Charsets.UTF_8)

fun ByteArray.encodeToString(): String = Base64.encodeToString(this, Base64.NO_WRAP)

fun String.decode(): ByteArray = Base64.decode(this, Base64.NO_WRAP)