package com.clearance.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.clearance.app.data.local.SessionManager
import com.clearance.app.navigation.ClearanceNavGraph
import com.clearance.app.ui.theme.ClearanceSystemTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STAGE 3B addition: must run before any network call or
        // Compose UI is shown — RetrofitClient's AuthInterceptor and
        // the login flow both read/write through SessionManager.
        SessionManager.init(applicationContext)

        enableEdgeToEdge()
        setContent {
            ClearanceSystemTheme {
                ClearanceNavGraph()
            }
        }
    }
}