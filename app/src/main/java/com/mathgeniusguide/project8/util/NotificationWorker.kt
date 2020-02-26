package com.mathgeniusguide.project8.util

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Context.JOB_SCHEDULER_SERVICE
import android.net.NetworkRequest
import android.os.PersistableBundle
import androidx.work.Worker
import androidx.work.WorkerParameters

class NotificationWorker (context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        val scheduler = applicationContext.getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
        val componentName = ComponentName(applicationContext, NotificationJobService::class.java)
        val username = inputData.getString("username")
        val bundle = PersistableBundle()
        bundle.putString("username", username)

        val jobInfo = JobInfo.Builder(0, componentName)
            .setExtras(bundle)
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .build()
        scheduler.schedule(jobInfo)

        return Result.success()
    }
}