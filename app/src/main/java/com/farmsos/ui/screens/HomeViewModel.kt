package com.farmsos.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farmsos.domain.model.Farm
import com.farmsos.domain.repository.AuthRepository
import com.farmsos.domain.repository.FarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val farmRepository: FarmRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val farms: StateFlow<List<Farm>> = farmRepository.getAllFarms()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun addFarm(name: String, location: String) {
        if (name.isBlank() || location.isBlank()) return
        viewModelScope.launch {
            val ownerId = authRepository.getSession()?.id ?: return@launch
            val now = System.currentTimeMillis()
            val farm = Farm(
                id = UUID.randomUUID().toString(),
                name = name.trim(),
                location = location.trim(),
                ownerId = ownerId,
                createdAt = now,
                updatedAt = now
            )
            farmRepository.insertFarm(farm)
        }
    }

    fun deleteFarm(farm: Farm) {
        viewModelScope.launch {
            farmRepository.deleteFarm(farm.id)
        }
    }
}
