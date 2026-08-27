package com.farmsos.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Background sync worker for FarmOS
 * Handles periodic data synchronization with Supabase backend
 */
@HiltWorker
class FarmOSWorkerManager @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val productionRepository: com.farmsos.domain.repository.ProductionRepository,
    private val feedRepository: com.farmsos.domain.repository.FeedRepository,
    private val salesRepository: com.farmsos.domain.repository.SalesRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val productionResult = productionRepository.syncPending()
        val feedResult = feedRepository.syncPending()
        val salesResult = salesRepository.syncPending()

        return if (productionResult.isSuccess && feedResult.isSuccess && salesResult.isSuccess) {
            Result.success()
        } else {
            Result.retry()
        }
    }
}
