package com.farmsos.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.farmsos.domain.model.SyncStatus

@Composable
fun SyncStatusIndicator(
    status: SyncStatus,
    modifier: Modifier = Modifier
) {
    val (icon, tint, text) = when (status) {
        SyncStatus.PENDING -> Triple(Icons.Default.CloudOff, Color.Gray, "Pending Sync")
        SyncStatus.SYNCING -> Triple(Icons.Default.Sync, MaterialTheme.colorScheme.primary, "Syncing...")
        SyncStatus.SYNCED -> Triple(Icons.Default.CloudDone, Color.Green, "Synced")
        SyncStatus.FAILED -> Triple(Icons.Default.SyncProblem, MaterialTheme.colorScheme.error, "Sync Failed")
    }

    Row(
        modifier = modifier.padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = tint,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = tint
        )
    }
}
