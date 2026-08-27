package com.farmsos.ui.screens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farmsos.domain.model.*
import com.farmsos.domain.usecase.farm.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ProductionHistoryUiState(val flock: Flock? = null, val productions: List<DailyProduction> = emptyList(), val loading: Boolean = true, val error: String? = null)
@HiltViewModel class ProductionHistoryViewModel @Inject constructor(
    state: SavedStateHandle,
    private val getFlock: GetFlockUseCase,
    private val observeProduction: ObserveProductionUseCase,
    private val listProduction: ListProductionUseCase
) : ViewModel() {
    private val flockId = checkNotNull<String>(state["flockId"])
    private val _state = MutableStateFlow(ProductionHistoryUiState())
    val uiState: StateFlow<ProductionHistoryUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            val flockResult = getFlock(flockId)
            flockResult.onSuccess { flock ->
                observeProduction(flockId).collectLatest { productions ->
                    _state.update { it.copy(flock = flock, productions = productions, loading = false) }
                }
            }.onFailure { error ->
                _state.update { it.copy(loading = false, error = error.message) }
            }
        }
        refresh()
    }

    fun refresh() = viewModelScope.launch {
        listProduction(flockId)
    }
}

data class ProductionEditorUiState(val flock: Flock? = null, val grades: List<EggGrade> = emptyList(), val production: DailyProduction? = null, val loading: Boolean = true, val saving: Boolean = false, val error: String? = null, val saved: Boolean = false)
@HiltViewModel class ProductionEditorViewModel @Inject constructor(
    state: SavedStateHandle, private val getFlock: GetFlockUseCase, private val getProduction: GetProductionUseCase,
    private val grades: ListEggGradesUseCase, private val create: CreateProductionUseCase, private val update: UpdateProductionUseCase
) : ViewModel() {
    private val flockId = checkNotNull<String>(state["flockId"]); private val productionId: String? = state["productionId"]
    private val _state = MutableStateFlow(ProductionEditorUiState()); val uiState = _state.asStateFlow()
    init { load() }
    private fun load() = viewModelScope.launch {
        val flock = getFlock(flockId).getOrElse { _state.update { s -> s.copy(loading = false, error = it.message) }; return@launch }
        val eggGrades = grades().getOrElse { _state.update { s -> s.copy(loading = false, error = it.message) }; return@launch }
        val production = productionId?.let { getProduction(it).getOrElse { error -> _state.update { s -> s.copy(loading = false, error = error.message) }; return@launch } }
        _state.value = ProductionEditorUiState(flock, eggGrades, production, loading = false)
    }
    fun save(date: String, opening: Int, mortality: Int, culls: Int, eggs: Int, broken: Int, dirty: Int, usable: Int, rejected: Int, feed: Double, remarks: String, gradeValues: Map<String, Int>, mortalityCause: String, mortalityRemarks: String) = viewModelScope.launch {
        val current = _state.value; val flock = current.flock ?: return@launch
        val localId = current.production?.localId ?: UUID.randomUUID().toString()
        val mortalityRecord = if (mortality > 0 || mortalityCause.isNotBlank() || mortalityRemarks.isNotBlank()) MortalityRecord(
            localId = UUID.randomUUID().toString(),
            productionDailyId = current.production?.id.orEmpty(),
            farmId = flock.farmId, flockId = flock.id, date = date, mortalityCount = mortality, cause = mortalityCause, remarks = mortalityRemarks,
            idempotencyKey = generateIdempotencyKey(), createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis()
        ) else null
        val item = DailyProduction(
            localId = localId,
            id = current.production?.id.orEmpty(), farmId = flock.farmId, shedId = flock.shedId, flockId = flock.id, date = date,
            openingLiveBirds = opening, mortality = mortality, culls = culls,
            closingLiveBirds = ProductionCalculator.closingLiveBirds(opening, mortality, culls),
            eggsCollected = eggs, brokenEggs = broken, dirtyEggs = dirty, usableEggs = usable, rejectedEggs = rejected,
            feedConsumedKg = feed, remarks = remarks, enteredBy = current.production?.enteredBy.orEmpty(),
            idempotencyKey = current.production?.idempotencyKey ?: generateIdempotencyKey(),
            createdAt = current.production?.createdAt ?: System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        ).apply {
            this.eggGrades = gradeValues.map { EggGradeEntry(productionDailyLocalId = localId, eggGradeId = it.key, quantity = it.value) }
            this.mortalityRecord = mortalityRecord
        }
        _state.update { it.copy(saving = true, error = null) }
        val result = if (current.production == null) create(item) else update(item)
        result.fold({ saved -> _state.update { it.copy(production = saved, saving = false, saved = true) } }, { error -> _state.update { it.copy(saving = false, error = error.message) } })
    }
}
