package com.thanu.smarthome.model

data class HomeUiState(
    val homes: List<Home> = emptyList(),
    val selectedHome: Home? = null,
    val summary: DashboardSummary = DashboardSummary(),
    val isLoading: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = null
)