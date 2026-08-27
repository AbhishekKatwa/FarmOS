package com.farmsos.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.farmsos.domain.model.Flock
import com.farmsos.domain.model.FlockStatus
import com.farmsos.domain.model.Shed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmDetailScreen(
    navController: NavController,
    viewModel: FarmDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showEditFarm by remember { mutableStateOf(false) }
    var showAddShed by remember { mutableStateOf(false) }
    var showAddFlock by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.farm?.name ?: "Farm") },
                navigationIcon = {
                    TextButton(onClick = { navController.popBackStack() }) { Text("Back") }
                }
            )
        }
    ) { padding ->
        if (state.isLoading && state.farm == null) {
            CircularProgressIndicator(modifier = Modifier.padding(padding).padding(24.dp))
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Text(state.farm?.location.orEmpty(), style = MaterialTheme.typography.bodyLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { showEditFarm = true }) { Text("Edit farm") }
                Button(onClick = { showAddShed = true }) { Text("Add shed") }
                Button(onClick = { showAddFlock = true }, enabled = state.sheds.isNotEmpty()) {
                    Text("Add flock")
                }
            }
            Text("Sheds", style = MaterialTheme.typography.titleMedium)
            if (state.sheds.isEmpty()) {
                Text("No sheds yet. Create a shed before adding flocks.")
            } else {
                state.sheds.forEach { shed ->
                    ShedCard(
                        shed = shed,
                        flocks = state.flocks.filter { it.shedId == shed.id },
                        onArchive = { viewModel.archiveSelectedShed(shed) },
                        onFlockClick = { flock ->
                            navController.navigate("farm/${shed.farmId}/flock/${flock.id}")
                        },
                        onArchiveFlock = { viewModel.archiveSelectedFlock(it) }
                    )
                }
            }
        }
    }

    if (showEditFarm) {
        val farm = state.farm
        if (farm != null) {
            EditFarmDialog(
                name = farm.name,
                location = farm.location,
                onDismiss = { showEditFarm = false },
                onConfirm = { name, location ->
                    viewModel.saveFarm(name, location)
                    showEditFarm = false
                }
            )
        }
    }
    if (showAddShed) {
        AddShedDialog(
            onDismiss = { showAddShed = false },
            onConfirm = { name, capacity, notes ->
                viewModel.addShed(name, capacity, notes)
                showAddShed = false
            }
        )
    }
    if (showAddFlock) {
        AddFlockDialog(
            sheds = state.sheds,
            onDismiss = { showAddFlock = false },
            onConfirm = { shedId, code, breed, strain, date, birds, status, target, notes ->
                viewModel.addFlock(shedId, code, breed, strain, date, birds, status, target, notes)
                showAddFlock = false
            }
        )
    }
}

@Composable
private fun ShedCard(
    shed: Shed,
    flocks: List<Flock>,
    onArchive: () -> Unit,
    onFlockClick: (Flock) -> Unit,
    onArchiveFlock: (Flock) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(shed.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Capacity: ${shed.capacity?.toString() ?: "—"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                TextButton(onClick = onArchive) { Text("Archive") }
            }
            Text("Flocks", style = MaterialTheme.typography.labelLarge)
            if (flocks.isEmpty()) {
                Text("No flocks in this shed.", style = MaterialTheme.typography.bodySmall)
            } else {
                flocks.forEach { flock ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onFlockClick(flock) }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(flock.flockCode, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "${flock.status} · ${flock.currentLiveBirds} birds",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        TextButton(onClick = { onArchiveFlock(flock) }) { Text("Close") }
                    }
                }
            }
        }
    }
}

@Composable
private fun EditFarmDialog(
    name: String,
    location: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var nameValue by remember { mutableStateOf(name) }
    var locationValue by remember { mutableStateOf(location) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit farm") },
        text = {
            Column {
                OutlinedTextField(nameValue, { nameValue = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(locationValue, { locationValue = it }, label = { Text("Location") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(nameValue, locationValue) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun AddShedDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Int?, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var capacity by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add shed") },
        text = {
            Column {
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                OutlinedTextField(name, { name = it }, label = { Text("Shed name") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(capacity, { capacity = it }, label = { Text("Capacity (optional)") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(notes, { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val cap = capacity.trim().takeIf { it.isNotBlank() }?.toIntOrNull()
                when {
                    name.isBlank() -> error = "Shed name is required"
                    capacity.isNotBlank() && cap == null -> error = "Capacity must be a number"
                    cap != null && cap <= 0 -> error = "Capacity must be greater than 0"
                    else -> onConfirm(name, cap, notes)
                }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddFlockDialog(
    sheds: List<Shed>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String, Int, FlockStatus, String, String) -> Unit
) {
    var shedExpanded by remember { mutableStateOf(false) }
    var shedId by remember { mutableStateOf(sheds.firstOrNull()?.id.orEmpty()) }
    var code by remember { mutableStateOf("") }
    var breed by remember { mutableStateOf("") }
    var strain by remember { mutableStateOf("") }
    var placement by remember { mutableStateOf("") }
    var birds by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(FlockStatus.PLANNED) }
    var target by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val selectedShed = sheds.firstOrNull { it.id == shedId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add flock") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                ExposedDropdownMenuBox(expanded = shedExpanded, onExpandedChange = { shedExpanded = it }) {
                    OutlinedTextField(
                        value = selectedShed?.name ?: "Select shed",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Shed") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(shedExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = shedExpanded, onDismissRequest = { shedExpanded = false }) {
                        sheds.forEach { shed ->
                            DropdownMenuItem(
                                text = { Text(shed.name) },
                                onClick = {
                                    shedId = shed.id
                                    shedExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(code, { code = it }, label = { Text("Flock code") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(breed, { breed = it }, label = { Text("Breed") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(strain, { strain = it }, label = { Text("Strain") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(placement, { placement = it }, label = { Text("Placement date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(birds, { birds = it }, label = { Text("Initial birds") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(target, { target = it }, label = { Text("Target production") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(notes, { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Text("Status: ${status.name}", style = MaterialTheme.typography.bodySmall)
                Row {
                    FlockStatus.entries.forEach { value ->
                        TextButton(onClick = { status = value }) { Text(value.name) }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val count = birds.toIntOrNull()
                error = when {
                    shedId.isBlank() -> "Flock must belong to a valid shed"
                    code.isBlank() -> "Flock code is required"
                    count == null || count <= 0 -> "Initial birds must be greater than 0"
                    else -> null
                }
                if (error == null) {
                    onConfirm(shedId, code, breed, strain, placement, count!!, status, target, notes)
                }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
