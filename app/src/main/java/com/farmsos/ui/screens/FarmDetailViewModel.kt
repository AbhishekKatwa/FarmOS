package com.farmsos.ui.screens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farmsos.domain.model.Farm
import com.farmsos.domain.model.Flock
import com.farmsos.domain.model.FlockStatus
import com.farmsos.domain.model.Shed
import com.farmsos.domain.usecase.farm.ArchiveFlockUseCase
import com.farmsos.domain.usecase.farm.ArchiveShedUseCase
import com.farmsos.domain.usecase.farm.CreateFlockUseCase
import com.farmsos.domain.usecase.farm.CreateShedUseCase
import com.farmsos.domain.usecase.farm.GetFarmUseCase
import com.farmsos.domain.usecase.farm.ListFlocksUseCase
import com.farmsos.domain.usecase.farm.ListShedsUseCase
import com.farmsos.domain.usecase.farm.UpdateFarmUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FarmDetailUiState(
    val farm: Farm? = null,
    val sheds: List<Shed> = emptyList(),
    val flocks: List<Flock> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class FarmDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getFarm: GetFarmUseCase,
    private val updateFarm: UpdateFarmUseCase,
    private val listSheds: ListShedsUseCase,
    private val createShed: CreateShedUseCase,
    private val archiveShed: ArchiveShedUseCase,
    private val listFlocks: ListFlocksUseCase,
    private val createFlock: CreateFlockUseCase,
    private val archiveFlock: ArchiveFlockUseCase
) : ViewModel() {

    private val farmId: String = checkNotNull(savedStateHandle["farmId"])

    private val _state = MutableStateFlow(FarmDetailUiState())
    val state: StateFlow<FarmDetailUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            val farm = getFarm(farmId).getOrElse { error ->
                _state.update { it.copy(isLoading = false, errorMessage = error.message ?: "Farm not found") }
                return@launch
            }
            val sheds = listSheds(farmId).getOrDefault(emptyList())
            val flocks = listFlocks(farmId).getOrDefault(emptyList())
            _state.update {
                it.copy(farm = farm, sheds = sheds, flocks = flocks, isLoading = false)
            }
        }
    }

    fun saveFarm(name: String, location: String) {
        val current = _state.value.farm ?: return
        viewModelScope.launch {
            updateFarm(current.copy(name = name, location = location)).fold(
                onSuccess = { refresh() },
                onFailure = { error -> _state.update { it.copy(errorMessage = error.message) } }
            )
        }
    }

    fun addShed(name: String, capacity: Int?, notes: String) {
        viewModelScope.launch {
            createShed(farmId, name, capacity, notes).fold(
                onSuccess = { refresh() },
                onFailure = { error -> _state.update { it.copy(errorMessage = error.message) } }
            )
        }
    }

    fun archiveSelectedShed(shed: Shed) {
        viewModelScope.launch {
            archiveShed(shed.id).fold(
                onSuccess = { refresh() },
                onFailure = { error -> _state.update { it.copy(errorMessage = error.message) } }
            )
        }
    }

    fun addFlock(
        shedId: String,
        flockCode: String,
        breed: String,
        strain: String,
        placementDate: String,
        initialBirds: Int,
        status: FlockStatus,
        targetProduction: String,
        notes: String
    ) {
        viewModelScope.launch {
            val flock = Flock(
                id = "",
                farmId = farmId,
                shedId = shedId,
                flockCode = flockCode,
                breed = breed,
                strain = strain,
                placementDate = placementDate,
                initialBirds = initialBirds,
                currentLiveBirds = initialBirds,
                status = status,
                targetProduction = targetProduction,
                notes = notes,
                createdAt = 0L,
                updatedAt = 0L
            )
            createFlock(flock).fold(
                onSuccess = { refresh() },
                onFailure = { error -> _state.update { it.copy(errorMessage = error.message) } }
            )
        }
    }

    fun archiveSelectedFlock(flock: Flock) {
        viewModelScope.launch {
            archiveFlock(flock.id).fold(
                onSuccess = { refresh() },
                onFailure = { error -> _state.update { it.copy(errorMessage = error.message) } }
            )
        }
    }
}
