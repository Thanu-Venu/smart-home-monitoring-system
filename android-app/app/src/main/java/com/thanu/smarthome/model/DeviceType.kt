package com.thanu.smarthome.model

enum class DeviceType(
    val displayName: String
) {

    LIGHT(
        displayName = "Light"
    ),

    OUTLET(
        displayName = "Electrical Outlet"
    ),

    MULTI_SWITCH(
        displayName = "Multi-Switch"
    ),

    IRON(
        displayName = "Iron"
    ),

    CAMERA(
        displayName = "Security Camera"
    ),

    OTHER(
        displayName = "Other"
    )
}