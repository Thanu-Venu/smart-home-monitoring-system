package com.thanu.smarthome.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.thanu.smarthome.ui.DeviceScreen
import com.thanu.smarthome.ui.FloorScreen
import com.thanu.smarthome.ui.HomeScreen
import com.thanu.smarthome.ui.RoomScreen

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {

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
                }
            )
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

                        navController.navigate(
                            "devices/" +
                                    "$selectedHomeId/" +
                                    "$selectedFloorId/" +
                                    "$selectedRoomId/" +
                                    selectedRoomName
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