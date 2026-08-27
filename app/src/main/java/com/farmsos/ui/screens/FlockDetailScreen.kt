package com.farmsos.ui.screens

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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.farmsos.domain.model.FlockStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlockDetailScreen(
    navController: NavController,
    viewModel: FlockDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val flock = state.flock

    var code by remember { mutableStateOf("") }
    var breed by remember { mutableStateOf("") }
    var strain by remember { mutableStateOf("") }
    var placement by remember { mutableStateOf("") }
    var initialBirds by remember { mutableStateOf("") }
    var liveBirds by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(FlockStatus.PLANNED) }
    var target by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    LaunchedEffect(flock?.id) {
        if (flock != null) {
            code = flock.flockCode
            breed = flock.breed
            strain = flock.strain
            placement = flock.placementDate
            initialBirds = flock.initialBirds.toString()
            liveBirds = flock.currentLiveBirds.toString()
            status = flock.status
            target = flock.targetProduction
            notes = flock.notes
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(flock?.flockCode ?: "Flock") },
                navigationIcon = {
                    TextButton(onClick = { navController.popBackStack() }) { Text("Back") }
                }
            )
        }
    ) { padding ->
        if (state.isLoading && flock == null) {
            CircularProgressIndicator(modifier = Modifier.padding(padding).padding(24.dp))
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Text("Shed: ${state.shed?.name ?: flock?.shedId}", style = MaterialTheme.typography.bodyMedium)
            state.age?.let { age ->
                Text(
                    "Age: ${age.days} days · ${age.weeks} weeks · production week ${age.productionWeek}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            OutlinedTextField(code, { code = it }, label = { Text("Flock code") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(breed, { breed = it }, label = { Text("Breed") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(strain, { strain = it }, label = { Text("Strain") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(placement, { placement = it }, label = { Text("Placement date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(initialBirds, { initialBirds = it }, label = { Text("Initial birds") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(liveBirds, { liveBirds = it }, label = { Text("Current live birds") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(target, { target = it }, label = { Text("Target production") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(notes, { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())
            Text("Status", style = MaterialTheme.typography.labelLarge)
            Row {
                FlockStatus.entries.forEach { value ->
                    TextButton(onClick = { status = value }) {
                        Text(if (status == value) "[${value.name}]" else value.name)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    viewModel.save(
                        flockCode = code,
                        breed = breed,
                        strain = strain,
                        placementDate = placement,
                        initialBirds = initialBirds.toIntOrNull() ?: 0,
                        currentLiveBirds = liveBirds.toIntOrNull() ?: 0,
                        status = status,
                        targetProduction = target,
                        notes = notes
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save") }
            TextButton(onClick = { viewModel.archive() }, modifier = Modifier.fillMaxWidth()) {
                Text("Archive (close flock)")
            }
        }
    }
}
