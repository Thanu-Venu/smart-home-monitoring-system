package com.thanu.smarthome

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.thanu.smarthome.navigation.AppNavigation
import com.thanu.smarthome.worker.SafetyMonitor

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*
         * Start the client-side safety cutoff worker. It keeps
         * running for as long as the app process is alive, so
         * safety-critical devices (e.g. irons) get auto-switched
         * off even while the user is on a different screen.
         */
        SafetyMonitor.start()

        setContent {
            AppNavigation()
        }
    }
}