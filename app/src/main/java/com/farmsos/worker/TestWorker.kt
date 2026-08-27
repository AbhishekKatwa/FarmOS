package com.farmsos.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

import com.farmsos.core.logging.AppLogger
import javax.inject.Inject

@HiltWorker
class TestWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val logger: AppLogger
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        logger.i("TestWorker executed")
        return Result.success()
    }
}
