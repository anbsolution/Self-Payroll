package com.selfattendance.salaryslip.repository

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.selfattendance.salaryslip.data.local.AppDatabase

class SyncWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val db = AppDatabase.get(applicationContext)
        val api: SyncApi = NoOpSecureApi()
        val states = db.syncDao().pending()
        for (state in states) {
            try {
                when (state.entityType) {
                    "PROFILE" -> db.employeeDao().get()?.let { api.uploadProfile(it) }
                    "ATTENDANCE" -> db.attendanceDao().pending().firstOrNull { it.id == state.recordId }?.let { api.uploadAttendance(it) }
                    "SALARY" -> { /* Salary records are local until a configured API is supplied. */ }
                }
                db.syncDao().update(state.recordId, "SYNCED", System.currentTimeMillis(), null)
            } catch (e: Exception) {
                db.syncDao().update(state.recordId, "FAILED", System.currentTimeMillis(), e.message)
                return Result.retry()
            }
        }
        return Result.success()
    }
}
