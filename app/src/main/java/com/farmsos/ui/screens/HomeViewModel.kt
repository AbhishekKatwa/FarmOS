package com.farmsos.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farmsos.domain.model.Farm
import com.farmsos.domain.repository.AuthRepository
import com.farmsos.domain.usecase.farm.ArchiveFarmUseCase
import com.farmsos.domain.usecase.farm.CreateFarmUseCase
import com.farmsos.domain.usecase.farm.ListFarmsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    listFarms: ListFarmsUseCase,
    private val createFarm: CreateFarmUseCase,
    private val archiveFarm: ArchiveFarmUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    val farms: StateFlow<List<Farm>> = listFarms()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        viewModelScope.launch {
            listFarms.refresh().onFailure { _error.value = it.message }
        }
    }

    fun addFarm(name: String, location: String) {
        viewModelScope.launch {
            val ownerId = authRepository.getSession()?.id
            if (ownerId == null) {
                _error.value = "You must be signed in to create a farm"
                return@launch
            }
            createFarm(name, location, ownerId).onFailure { _error.value = it.message }
        }
    }

    fun archiveSelectedFarm(farm: Farm) {
        viewModelScope.launch {
            archiveFarm(farm.id).onFailure { _error.value = it.message }
        }
    }
}
