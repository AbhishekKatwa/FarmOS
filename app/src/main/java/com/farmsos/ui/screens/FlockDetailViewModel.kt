package com.farmsos.ui.screens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farmsos.domain.model.Flock
import com.farmsos.domain.model.FlockAge
import com.farmsos.domain.model.FlockStatus
import com.farmsos.domain.model.Shed
import com.farmsos.domain.usecase.farm.ArchiveFlockUseCase
import com.farmsos.domain.usecase.farm.CalculateFlockAgeUseCase
import com.farmsos.domain.usecase.farm.GetFlockUseCase
import com.farmsos.domain.usecase.farm.ListShedsUseCase
import com.farmsos.domain.usecase.farm.UpdateFlockUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FlockDetailUiState(
    val flock: Flock? = null,
    val shed: Shed? = null,
    val age: FlockAge? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val saved: Boolean = false
)

@HiltViewModel
class FlockDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getFlock: GetFlockUseCase,
    private val updateFlock: UpdateFlockUseCase,
    private val archiveFlock: ArchiveFlockUseCase,
    private val listSheds: ListShedsUseCase,
    private val calculateAge: CalculateFlockAgeUseCase
) : ViewModel() {

    private val flockId: String = checkNotNull(savedStateHandle["flockId"])

    private val _state = MutableStateFlow(FlockDetailUiState())
    val state: StateFlow<FlockDetailUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null, saved = false) }
            val flock = getFlock(flockId).getOrElse { error ->
                _state.update { it.copy(isLoading = false, errorMessage = error.message) }
                return@launch
            }
            val shed = listSheds(flock.farmId).getOrDefault(emptyList()).firstOrNull { it.id == flock.shedId }
            _state.update {
                it.copy(
                    flock = flock,
                    shed = shed,
                    age = calculateAge(flock.placementDate),
                    isLoading = false
                )
            }
        }
    }

    fun save(
        flockCode: String,
        breed: String,
        strain: String,
        placementDate: String,
        initialBirds: Int,
        currentLiveBirds: Int,
        status: FlockStatus,
        targetProduction: String,
        notes: String
    ) {
        val current = _state.value.flock ?: return
        viewModelScope.launch {
            val updated = current.copy(
                flockCode = flockCode,
                breed = breed,
                strain = strain,
                placementDate = placementDate,
                initialBirds = initialBirds,
                currentLiveBirds = currentLiveBirds,
                status = status,
                targetProduction = targetProduction,
                notes = notes
            )
            updateFlock(updated).fold(
                onSuccess = { flock ->
                    _state.update {
                        it.copy(
                            flock = flock,
                            age = calculateAge(flock.placementDate),
                            saved = true,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { error -> _state.update { it.copy(errorMessage = error.message) } }
            )
        }
    }

    fun archive() {
        viewModelScope.launch {
            archiveFlock(flockId).fold(
                onSuccess = { refresh() },
                onFailure = { error -> _state.update { it.copy(errorMessage = error.message) } }
            )
        }
    }
}
