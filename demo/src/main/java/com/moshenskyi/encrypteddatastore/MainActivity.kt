package com.moshenskyi.encrypteddatastore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.moshenskyi.eds.EncryptedStore
import com.moshenskyi.eds.crypto.CryptoManager
import com.moshenskyi.eds.crypto.encoding.get
import com.moshenskyi.eds.crypto.encoding.put
import com.moshenskyi.encrypteddatastore.ui.theme.EncryptedDataStoreTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EncryptedDataStoreTheme {
                val cryptoManager = CryptoManager()
                val store = EncryptedStore(
                    LocalContext.current,
                    cryptoManager
                )

                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        var input by remember { mutableStateOf("") }
                        OutlinedTextField(
                            value = input,
                            onValueChange = { input = it },
                            placeholder = { Text("Enter password") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            modifier = Modifier.padding(16.dp),
                            onClick = {
                                lifecycleScope.launch { store.put("password", input) }
                            }) {
                            Text("encrypt")
                        }

                        var output by remember { mutableStateOf("") }
                        Text("Decrypted: $output")

                        Button(
                            modifier = Modifier.padding(16.dp),
                            onClick = {
                                lifecycleScope.launch {
                                    output = store.get<String>("password") ?: ""
                                }
                            }) {
                            Text("decrypt")
                        }
                    }
                }
            }
        }
    }
}
