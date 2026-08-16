package com.thanu.smarthome

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.thanu.smarthome.navigation.AppNavigation
import com.thanu.smarthome.ui.theme.SmartHomeMonitoringTheme
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
            SmartHomeMonitoringTheme {

                /*
                 * This app targets a modern SDK, which means the
                 * system draws content edge-to-edge by default —
                 * screens are responsible for their own padding
                 * around the status bar, display cutout (camera
                 * notch), and navigation bar, instead of the system
                 * reserving that space automatically. Without this,
                 * every screen's header renders partly underneath
                 * the status bar, which is why titles like "Rooms"
                 * looked cramped right at the very top of the
                 * screen. safeDrawingPadding() here applies that
                 * inset once, for every screen, instead of needing
                 * it added to each screen individually.
                 */
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}