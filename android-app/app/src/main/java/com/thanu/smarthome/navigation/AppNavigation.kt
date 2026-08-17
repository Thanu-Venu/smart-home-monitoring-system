package com.thanu.smarthome.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.thanu.smarthome.ui.DeviceScreen
import com.thanu.smarthome.ui.FloorScreen
import com.thanu.smarthome.ui.HomeScreen
import com.thanu.smarthome.ui.LoginScreen
import com.thanu.smarthome.ui.ReportsScreen
import com.thanu.smarthome.ui.RoomScreen
import com.thanu.smarthome.ui.SignUpScreen
import java.net.URLDecoder
import java.net.URLEncoder

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    /*
     * Skip straight to the home screen if a user session already
     * exists (Firebase keeps the user signed in across app restarts).
     */
    val startDestination =
        if (FirebaseAuth.getInstance().currentUser != null) {
            "home"
        } else {
            "login"
        }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        /*
         * ==============================================
         * LOGIN SCREEN
         * ==============================================
         */

        composable("login") {

            LoginScreen(
                onLoginSuccess = {

                    navController.navigate("home") {
                        popUpTo("login") {
                            inclusive = true
                        }
                    }
                },

                onNavigateToSignUp = {

                    navController.navigate("signup")
                }
            )
        }


        /*
         * ==============================================
         * SIGN UP SCREEN
         * ==============================================
         */

        composable("signup") {

            SignUpScreen(
                onSignUpSuccess = {

                    navController.navigate("home") {
                        popUpTo("login") {
                            inclusive = true
                        }
                    }
                },

                onNavigateToLogin = {

                    navController.popBackStack()
                }
            )
        }


        /*
         * ==============================================
         * HOME SCREEN
         * ==============================================
         */

        composable("home") {

            HomeScreen(
                onOpenFloors = { homeId ->

                    navController.navigate(
                        "floors/$homeId"
                    )
                },

                onOpenReports = { homeId ->

                    navController.navigate(
                        "reports/$homeId"
                    )
                },

                onLogout = {

                    navController.navigate("login") {
                        popUpTo(0) {
                            inclusive = true
                        }
                    }
                }
            )
        }


        /*
         * ==============================================
         * REPORTS SCREEN
         * ==============================================
         */

        composable(
            route = "reports/{homeId}"
        ) { backStackEntry ->

            val homeId =
                backStackEntry.arguments
                    ?.getString("homeId")

            if (homeId != null) {

                ReportsScreen(
                    homeId = homeId,

                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }


        /*
         * ==============================================
         * FLOOR SCREEN
         * ==============================================
         */

        composable(
            route = "floors/{homeId}"
        ) { backStackEntry ->

            val homeId =
                backStackEntry.arguments
                    ?.getString("homeId")

            if (homeId != null) {

                FloorScreen(
                    homeId = homeId,

                    onOpenRooms = {
                            selectedHomeId,
                            selectedFloorId ->

                        navController.navigate(
                            "rooms/$selectedHomeId/$selectedFloorId"
                        )
                    },

                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }


        /*
         * ==============================================
         * ROOM SCREEN
         * ==============================================
         */

        composable(
            route = "rooms/{homeId}/{floorId}"
        ) { backStackEntry ->

            val homeId =
                backStackEntry.arguments
                    ?.getString("homeId")

            val floorId =
                backStackEntry.arguments
                    ?.getString("floorId")

            if (
                homeId != null &&
                floorId != null
            ) {

                RoomScreen(
                    homeId = homeId,
                    floorId = floorId,

                    /*
                     * ROOM → DEVICE
                     *
                     * Now we also pass roomName.
                     */

                    onOpenDevices = {
                            selectedHomeId,
                            selectedFloorId,
                            selectedRoomId,
                            selectedRoomName ->

                        /*
                         * URL-encode the room name before it becomes a
                         * route segment. Room names can be freely typed
                         * (the "Custom" room-name option), so a name
                         * containing "/" would otherwise be split into
                         * extra path segments and break this route's
                         * argument parsing.
                         */
                        val encodedRoomName =
                            URLEncoder.encode(selectedRoomName, "UTF-8")

                        navController.navigate(
                            "devices/" +
                                    "$selectedHomeId/" +
                                    "$selectedFloorId/" +
                                    "$selectedRoomId/" +
                                    encodedRoomName
                        )
                    },

                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }


        /*
         * ==============================================
         * DEVICE SCREEN
         * ==============================================
         */

        composable(
            route =
                "devices/{homeId}/{floorId}/{roomId}/{roomName}"
        ) { backStackEntry ->

            val homeId =
                backStackEntry.arguments
                    ?.getString("homeId")

            val floorId =
                backStackEntry.arguments
                    ?.getString("floorId")

            val roomId =
                backStackEntry.arguments
                    ?.getString("roomId")

            val roomName =
                backStackEntry.arguments
                    ?.getString("roomName")
                    ?.let { URLDecoder.decode(it, "UTF-8") }

            if (
                homeId != null &&
                floorId != null &&
                roomId != null &&
                roomName != null
            ) {

                DeviceScreen(
                    homeId = homeId,
                    floorId = floorId,
                    roomId = roomId,

                    /*
                     * Pass actual room name
                     * to DeviceScreen.
                     */

                    roomName = roomName,

                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}