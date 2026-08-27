package com.farmsos.ui.screens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.farmsos.domain.model.*
import com.farmsos.domain.repository.FeedRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class FeedUiState(
    val items: List<FeedItem> = emptyList(),
    val stock: Map<String, FeedStock> = emptyMap(),
    val loading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class FeedViewModel @Inject constructor(state: SavedStateHandle, private val repo: FeedRepository) :
    ViewModel() {
    private val farmId = checkNotNull<String>(state["farmId"]);
    private val _state = MutableStateFlow(FeedUiState());
    val ui = _state.asStateFlow();

    init {
        refresh()
    };
    fun refresh() = viewModelScope.launch {
        val items = repo.items(farmId).getOrElse {
            _state.value = FeedUiState(error = it.message, loading = false); return@launch
        };
        val stocks = items.associate { item ->
            item.id to FeedCalculator.stock(
                item,
                repo.purchases(item.id).getOrDefault(emptyList()),
                repo.consumption(item.id).getOrDefault(emptyList()),
                repo.adjustments(item.id).getOrDefault(emptyList())
            )
        }; _state.value = FeedUiState(items, stocks, false)
    };

    fun addItem(name: String, type: FeedType, opening: Double, cost: Double?) {
        viewModelScope.launch {
            repo.addItem(
                FeedItem(
                    farmId = farmId,
                    name = name,
                    feedType = type,
                    openingQuantityKg = opening,
                    openingCostPerKg = cost,
                    openingDate = today()
                )
            ).onSuccess { refresh() }
                .onFailure { _state.update { s -> s.copy(error = it.message) } }
        }
    };
    fun purchase(item: FeedItem, q: Double, price: Double, supplier: String) {
        viewModelScope.launch {
            repo.addPurchase(
                FeedPurchase(
                    feedItemId = item.id,
                    farmId = farmId,
                    supplier = supplier,
                    quantityKg = q,
                    unit = item.unit,
                    pricePerKg = price,
                    batch = "",
                    purchaseDate = today()
                )
            ).onSuccess { refresh() }
                .onFailure { _state.update { s -> s.copy(error = it.message) } }
        }
    };
    fun consume(item: FeedItem, q: Double) {
        viewModelScope.launch {
            repo.addConsumption(
                FeedConsumption(
                    feedItemId = item.id,
                    farmId = farmId,
                    quantityKg = q,
                    consumedDate = today()
                )
            ).onSuccess { refresh() }
                .onFailure { _state.update { s -> s.copy(error = it.message) } }
        }
    };
    private fun today() = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedInventoryScreen(nav: NavController, vm: FeedViewModel = hiltViewModel()) {
    val s by vm.ui.collectAsStateWithLifecycle();
    var add by remember { mutableStateOf(false) };
    var selected by remember { mutableStateOf<FeedItem?>(null) }; Scaffold(topBar = {
        TopAppBar(
            title = { Text("Feed inventory") },
            navigationIcon = { TextButton({ nav.popBackStack() }) { Text("Back") } },
            actions = { TextButton({ add = true }) { Text("Feed item") } })
    }) { p ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(p)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            s.error?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error
                )
            }; if (s.loading) CircularProgressIndicator(); s.items.forEach { item ->
            val stock = s.stock[item.id]; Card(
            Modifier
                .fillMaxWidth()
                .clickable { selected = item }) {
            Column(
                Modifier.padding(16.dp)
            ) {
                Text(
                    item.name,
                    style = MaterialTheme.typography.titleMedium
                ); Text("${stock?.quantityKg ?: 0.0} ${item.unit} · cost/kg ${stock?.averageCostPerKg ?: "—"}")
            }
        }
        }
        }
    }; if (add) FeedItemDialog({ add = false }) { n, t, o, c ->
        vm.addItem(n, t, o, c); add = false
    }; selected?.let {
        FeedItemDialog(
            { selected = null },
            item = it
        ) { _, _, _, _ -> }; FeedTransactionDialog(it,
        { selected = null },
        { q, price, supplier, consumption ->
            if (consumption) vm.consume(it, q) else vm.purchase(
                it,
                q,
                price,
                supplier
            ); selected = null
        })
    }
}

@Composable
private fun FeedItemDialog(
    dismiss: () -> Unit,
    item: FeedItem? = null,
    save: (String, FeedType, Double, Double?) -> Unit
) {
    if (item != null) return;
    var n by remember { mutableStateOf("") };
    var o by remember { mutableStateOf("0") };
    var c by remember { mutableStateOf("") };
    var t by remember { mutableStateOf(FeedType.LAYER_FEED) }; AlertDialog(
        onDismissRequest = dismiss,
        title = { Text("Feed item") },
        text = {
            Column {
                OutlinedTextField(
                    n,
                    { n = it },
                    label = { Text("Name") }); OutlinedTextField(
                o,
                { o = it },
                label = { Text("Opening kg") }); OutlinedTextField(
                c,
                { c = it },
                label = { Text("Opening cost/kg (optional)") }); FeedType.entries.forEach {
                TextButton({ t = it }) { Text(it.name) }
            }
            }
        },
        confirmButton = {
            TextButton({
                save(
                    n,
                    t,
                    o.toDoubleOrNull() ?: -1,
                    c.toDoubleOrNull()
                );
            }) { Text("Save") }
        },
        dismissButton = { TextButton(dismiss) { Text("Cancel") } })
}

@Composable
private fun FeedTransactionDialog(
    item: FeedItem,
    dismiss: () -> Unit,
    save: (Double, Double, String, Boolean) -> Unit
) {
    var q by remember { mutableStateOf("") };
    var price by remember { mutableStateOf("") };
    var supplier by remember { mutableStateOf("") };
    var consume by remember { mutableStateOf(false) }; AlertDialog(
        onDismissRequest = dismiss,
        title = { Text(if (consume) "Consumption entry" else "Purchase entry") },
        text = {
            Column {
                TextButton({
                    consume = !consume
                }) { Text(if (consume) "Switch to purchase" else "Switch to consumption") }; OutlinedTextField(
                q,
                { q = it },
                label = { Text("Quantity kg") }); if (!consume) {
                OutlinedTextField(
                    price,
                    { price = it },
                    label = { Text("Price per kg") }); OutlinedTextField(
                    supplier,
                    { supplier = it },
                    label = { Text("Supplier") })
            }
            }
        },
        confirmButton = {
            TextButton({
                save(
                    q.toDoubleOrNull() ?: -1,
                    price.toDoubleOrNull() ?: -1,
                    supplier,
                    consume
                )
            }) { Text("Save") }
        },
        dismissButton = { TextButton(dismiss) { Text("Cancel") } })
}
