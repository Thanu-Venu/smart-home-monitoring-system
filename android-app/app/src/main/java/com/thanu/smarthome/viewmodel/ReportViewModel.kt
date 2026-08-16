package com.thanu.smarthome.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.database.ValueEventListener
import com.thanu.smarthome.model.ReportUiState
import com.thanu.smarthome.repository.ReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ReportViewModel : ViewModel() {

    private val repository = ReportRepository()

    private val _uiState = MutableStateFlow(ReportUiState())

    val uiState: StateFlow<ReportUiState> =
        _uiState.asStateFlow()

    private var reportListener: ValueEventListener? = null
    private var listeningHomeId: String? = null


    fun startListening(homeId: String) {

        if (
            reportListener != null &&
            listeningHomeId == homeId
        ) {
            return
        }

        stopListening()

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null
        )

        listeningHomeId = homeId

        reportListener = repository.observeHomeReport(
            homeId = homeId,

            onReport = { report ->

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    report = report,
                    errorMessage = null
                )
            },

            onError = { message ->

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = message
                )
            }
        )
    }


    fun stopListening() {

        val listener = reportListener
        val homeId = listeningHomeId

        if (listener != null && homeId != null) {

            repository.removeHomeReportListener(
                homeId = homeId,
                listener = listener
            )
        }

        reportListener = null
    }


    override fun onCleared() {
        super.onCleared()
        stopListening()
    }
}
