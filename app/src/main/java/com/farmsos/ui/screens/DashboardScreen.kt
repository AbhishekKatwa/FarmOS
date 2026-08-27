package com.farmsos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController, viewModel: DashboardViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Dashboard") })
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row { listOf(1 to "Today",7 to "7 days",30 to "30 days").forEach { (days,label)->TextButton(onClick={viewModel.load(days)}){Text(label)} } }
                when { state.loading -> Text("Loading farm data…"); state.error!=null -> Text(state.error!!,color=MaterialTheme.colorScheme.error); state.snapshot==null -> Text("No dashboard data for this period."); else -> DashboardContent(state.snapshot!!) }
            }
        }
    }
}

@Composable private fun DashboardContent(s: com.farmsos.domain.model.DashboardSnapshot) {
    val cards=listOf("Live Birds" to s.liveBirds,"Eggs Today" to s.eggsToday,"Hen-Day %" to s.henDay,"Mortality" to s.mortality,"Feed Used kg" to s.feedUsedKg,"Feed Cost" to s.feedCost,"Current Egg Rate" to s.currentEggRate,"Revenue" to s.revenue,"Expenses" to s.expenses,"Profit" to s.profit,"Buyer Outstanding" to s.buyerOutstanding,"Feed Stock Days" to s.feedStockDays)
    cards.forEach{(label,value)->Card(Modifier.fillMaxWidth()){Text("$label: ${value ?: "—"}",Modifier.padding(12.dp))}}
    Text("Critical alerts",style=MaterialTheme.typography.titleMedium);if(s.alerts.isEmpty())Text("No critical alerts.") else s.alerts.forEach{Text("${it.type}: ${it.message}")}
    DashboardChart("Egg production trend",s.production.map{it.eggs});DashboardChart("Hen-Day trend",s.production.mapNotNull{it.henDay});DashboardChart("Mortality trend",s.production.map{it.mortality});DashboardChart("Feed consumption",s.production.map{it.feedKg});DashboardChart("FCR trend",emptyList());DashboardChart("Egg grade distribution",s.grades.groupBy{it.grade}.values.map{it.sumOf{x->x.eggs}});DashboardChart("Market price trend",s.sales.mapNotNull{it.eggRate});DashboardChart("Revenue vs expenses",s.finance.flatMap{listOf(it.revenue,it.expenses)});DashboardChart("Profit trend",s.finance.map{it.profit})
}
@Composable private fun DashboardChart(title:String,values:List<Double>){Card(Modifier.fillMaxWidth()){Column(Modifier.padding(12.dp)){Text(title,style=MaterialTheme.typography.titleMedium);if(values.isEmpty())Text("No actual data for this period.") else Text(values.joinToString("  •  "){String.format(java.util.Locale.US,"%.2f",it)})}}}
