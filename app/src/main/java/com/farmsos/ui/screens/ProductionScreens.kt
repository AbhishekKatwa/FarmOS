package com.farmsos.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.farmsos.domain.model.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun ProductionHistoryScreen(navController: NavController, viewModel: ProductionHistoryViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(topBar = { TopAppBar(title = { Text("Production history") }, navigationIcon = { TextButton({ navController.popBackStack() }) { Text("Back") } }, actions = { TextButton(enabled = state.flock != null, onClick = { state.flock?.let { navController.navigate("farm/${it.farmId}/flock/${it.id}/production/new") } }) { Text("Daily entry") } }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            if (state.loading) CircularProgressIndicator()
            if (!state.loading && state.productions.isEmpty()) Text("No daily production has been recorded for this flock.")
            state.productions.forEach { production -> Card(Modifier.fillMaxWidth().clickable { navController.navigate("farm/${production.farmId}/flock/${production.flockId}/production/${production.id}") }) { Column(Modifier.padding(16.dp)) { Text(production.date, style = MaterialTheme.typography.titleMedium); Text("Eggs ${production.eggsCollected} · Closing birds ${production.closingLiveBirds}") } } }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun ProductionDetailScreen(navController: NavController, viewModel: ProductionEditorViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle(); val production = state.production
    Scaffold(topBar = { TopAppBar(title = { Text("Production detail") }, navigationIcon = { TextButton({ navController.popBackStack() }) { Text("Back") } }, actions = { if (production != null) TextButton({ navController.navigate("farm/${production.farmId}/flock/${production.flockId}/production/${production.id}/edit") }) { Text("Edit") } }) }) { padding ->
        if (state.loading) { CircularProgressIndicator(Modifier.padding(padding).padding(24.dp)); return@Scaffold }
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }; production?.let { p -> val m = ProductionCalculator.metrics(p); Text(p.date, style = MaterialTheme.typography.titleLarge); Text("Opening / closing birds: ${p.openingLiveBirds} / ${p.closingLiveBirds}"); Text("Mortality: ${p.mortality} · Culls: ${p.culls}"); Text("Eggs collected: ${p.eggsCollected}"); Text("Feed consumed: ${p.feedConsumedKg} kg"); Text("Hen-Day %: ${m.henDayPercent?.let(::format) ?: "—"}"); Text("Egg grades: ${p.eggGrades.sumOf { it.quantity }} eggs") } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun ProductionEditorScreen(navController: NavController, viewModel: ProductionEditorViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle(); val existing = state.production
    var date by remember(existing?.id) { mutableStateOf(existing?.date ?: SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())) }
    var opening by remember(existing?.id) { mutableStateOf(existing?.openingLiveBirds?.toString() ?: state.flock?.currentLiveBirds?.toString().orEmpty()) }
    var mortality by remember(existing?.id) { mutableStateOf(existing?.mortality?.toString() ?: "0") }; var culls by remember(existing?.id) { mutableStateOf(existing?.culls?.toString() ?: "0") }
    var eggs by remember(existing?.id) { mutableStateOf(existing?.eggsCollected?.toString() ?: "0") }; var broken by remember(existing?.id) { mutableStateOf(existing?.brokenEggs?.toString() ?: "0") }; var dirty by remember(existing?.id) { mutableStateOf(existing?.dirtyEggs?.toString() ?: "0") }; var usable by remember(existing?.id) { mutableStateOf(existing?.usableEggs?.toString() ?: "0") }; var rejected by remember(existing?.id) { mutableStateOf(existing?.rejectedEggs?.toString() ?: "0") }; var feed by remember(existing?.id) { mutableStateOf(existing?.feedConsumedKg?.toString() ?: "0") }; var remarks by remember(existing?.id) { mutableStateOf(existing?.remarks ?: "") }
    var cause by remember(existing?.id) { mutableStateOf(existing?.mortalityRecord?.cause ?: "") }; var mortalityRemarks by remember(existing?.id) { mutableStateOf(existing?.mortalityRecord?.remarks ?: "") }
    val gradeValues = remember(state.grades, existing?.id) { mutableStateMapOf<String, String>().apply { state.grades.forEach { grade -> put(grade.id, existing?.eggGrades?.firstOrNull { it.eggGradeId == grade.id }?.quantity?.toString() ?: "0") } } }
    val draft = DailyProduction(farmId = state.flock?.farmId.orEmpty(), shedId = state.flock?.shedId.orEmpty(), flockId = state.flock?.id.orEmpty(), date = date, openingLiveBirds = opening.toIntOrNull() ?: 0, mortality = mortality.toIntOrNull() ?: 0, culls = culls.toIntOrNull() ?: 0, eggsCollected = eggs.toIntOrNull() ?: 0, brokenEggs = broken.toIntOrNull() ?: 0, dirtyEggs = dirty.toIntOrNull() ?: 0, usableEggs = usable.toIntOrNull() ?: 0, rejectedEggs = rejected.toIntOrNull() ?: 0, feedConsumedKg = feed.toDoubleOrNull() ?: 0.0)
    val metrics = ProductionCalculator.metrics(draft)
    Scaffold(topBar = { TopAppBar(title = { Text(if (existing == null) "Daily production" else "Edit production") }, navigationIcon = { TextButton({ navController.popBackStack() }) { Text("Back") } }) }) { padding ->
        if (state.loading) { CircularProgressIndicator(Modifier.padding(padding).padding(24.dp)); return@Scaffold }
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }; Text(state.flock?.flockCode.orEmpty(), style = MaterialTheme.typography.titleMedium)
            NumberField("Date (YYYY-MM-DD)", date) { date = it }; NumberField("Opening live birds", opening) { opening = it }; NumberField("Mortality", mortality) { mortality = it }; NumberField("Culls", culls) { culls = it }
            Text("Closing live birds: ${draft.closingLiveBirds}", style = MaterialTheme.typography.titleMedium)
            NumberField("Eggs collected", eggs) { eggs = it }; NumberField("Broken eggs", broken) { broken = it }; NumberField("Dirty eggs", dirty) { dirty = it }; NumberField("Usable eggs", usable) { usable = it }; NumberField("Rejected eggs", rejected) { rejected = it }; NumberField("Feed consumed (kg)", feed) { feed = it }
            Text("Egg grade entry", style = MaterialTheme.typography.titleMedium); state.grades.forEach { grade -> NumberField(grade.displayName, gradeValues[grade.id].orEmpty()) { gradeValues[grade.id] = it } }
            Text("Mortality entry", style = MaterialTheme.typography.titleMedium); OutlinedTextField(cause, { cause = it }, label = { Text("Cause") }, modifier = Modifier.fillMaxWidth()); OutlinedTextField(mortalityRemarks, { mortalityRemarks = it }, label = { Text("Mortality remarks") }, modifier = Modifier.fillMaxWidth())
            Text("Calculated metrics", style = MaterialTheme.typography.titleMedium); Text("Average live birds: ${format(metrics.averageLiveBirds)}"); Text("Hen-Day %: ${metrics.henDayPercent?.let(::format) ?: "—"}"); Text("Eggs per bird: ${metrics.eggsPerBird?.let(::format) ?: "—"}"); Text("Feed per bird (kg): ${metrics.feedPerBirdKg?.let(::format) ?: "—"}"); Text("Feed per egg (kg): ${metrics.feedPerEggKg?.let(::format) ?: "—"}")
            OutlinedTextField(remarks, { remarks = it }, label = { Text("Remarks") }, modifier = Modifier.fillMaxWidth()); Button(enabled = !state.saving, onClick = { viewModel.save(date, opening.toIntOrNull() ?: -1, mortality.toIntOrNull() ?: -1, culls.toIntOrNull() ?: -1, eggs.toIntOrNull() ?: -1, broken.toIntOrNull() ?: -1, dirty.toIntOrNull() ?: -1, usable.toIntOrNull() ?: -1, rejected.toIntOrNull() ?: -1, feed.toDoubleOrNull() ?: -1.0, remarks, gradeValues.mapValues { it.value.toIntOrNull() ?: -1 }, cause, mortalityRemarks) }, modifier = Modifier.fillMaxWidth()) { Text(if (state.saving) "Saving…" else "Save production") }
            if (state.saved) Text("Saved", color = MaterialTheme.colorScheme.primary)
        }
    }
}
@Composable private fun NumberField(label: String, value: String, changed: (String) -> Unit) = OutlinedTextField(value, changed, label = { Text(label) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
private fun format(value: Double) = String.format(Locale.US, "%.2f", value)
