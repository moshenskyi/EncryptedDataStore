package com.moshenskyi.library.eds.crypto

import com.google.crypto.tink.integration.android.AndroidKeystore

object KeystoreTestUtils {
    fun deleteAlias(alias: String) {
        AndroidKeystore.deleteKey(alias)
    }
}