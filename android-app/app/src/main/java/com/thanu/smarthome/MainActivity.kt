package com.thanu.smarthome

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.thanu.smarthome.navigation.AppNavigation
import com.thanu.smarthome.worker.SafetyMonitor
import com.thanu.smarthome.worker.ScheduleMonitor

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*
         * Start the client-side automation workers. They keep
         * running for as long as the app process is alive, so
         * safety-critical devices (e.g. irons) get auto-switched
         * off, and scheduled lights turn on/off, even while the
         * user is on a different screen.
         */
        SafetyMonitor.start()
        ScheduleMonitor.start()

        setContent {
            AppNavigation()
        }
    }
}