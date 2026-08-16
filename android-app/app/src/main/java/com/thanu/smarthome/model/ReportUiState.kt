package com.thanu.smarthome.model

data class ReportUiState(
    val isLoading: Boolean = false,
    val report: HomeReport = HomeReport(),
    val errorMessage: String? = null
)
