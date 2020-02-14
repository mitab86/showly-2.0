package com.michaldrabik.showly2.common.trakt

import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.crashlytics.android.Crashlytics
import com.michaldrabik.network.trakt.model.Show
import com.michaldrabik.showly2.common.events.EventsManager
import com.michaldrabik.showly2.common.events.TraktSyncAuthError
import com.michaldrabik.showly2.common.events.TraktSyncError
import com.michaldrabik.showly2.common.events.TraktSyncProgress
import com.michaldrabik.showly2.common.events.TraktSyncStart
import com.michaldrabik.showly2.common.events.TraktSyncSuccess
import com.michaldrabik.showly2.common.trakt.exports.TraktExportWatchedRunner
import com.michaldrabik.showly2.common.trakt.exports.TraktExportWatchlistRunner
import com.michaldrabik.showly2.common.trakt.imports.TraktImportWatchedRunner
import com.michaldrabik.showly2.common.trakt.imports.TraktImportWatchlistRunner
import com.michaldrabik.showly2.model.error.TraktAuthError
import com.michaldrabik.showly2.serviceComponent
import com.michaldrabik.showly2.utilities.extensions.notificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import javax.inject.Inject

class TraktSyncService : TraktNotificationsService(), CoroutineScope {

  companion object {
    private const val TAG = "TraktSyncService"
    private const val SYNC_NOTIFICATION_PROGRESS_ID = 826
    private const val SYNC_NOTIFICATION_COMPLETE_SUCCESS_ID = 827
    private const val SYNC_NOTIFICATION_COMPLETE_ERROR_ID = 828
  }

  override val coroutineContext = Job() + Dispatchers.IO
  private val runners = mutableListOf<TraktSyncRunner>()

  @Inject lateinit var importWatchedRunner: TraktImportWatchedRunner
  @Inject lateinit var importWatchlistRunner: TraktImportWatchlistRunner

  @Inject lateinit var exportWatchedRunner: TraktExportWatchedRunner
  @Inject lateinit var exportWatchlistRunner: TraktExportWatchlistRunner

  override fun onCreate() {
    super.onCreate()
    serviceComponent().inject(this)
    runners.addAll(listOf(importWatchedRunner, importWatchlistRunner, exportWatchedRunner, exportWatchlistRunner))
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    Log.d(TAG, "Service initialized.")
    if (runners.any { it.isRunning }) {
      Log.d(TAG, "Already running. Skipping...")
      return START_NOT_STICKY
    }
    startForeground(SYNC_NOTIFICATION_PROGRESS_ID, createProgressNotification().build())

    Log.d(TAG, "Sync started.")
    launch {
      try {
        EventsManager.sendEvent(TraktSyncStart)

        val resultCount = runImportWatched()
        runImportWatchlist(resultCount)
        runExportWatched(resultCount)
        runExportWatchlist(resultCount)

        EventsManager.sendEvent(TraktSyncSuccess)
        notificationManager().notify(SYNC_NOTIFICATION_COMPLETE_SUCCESS_ID, createSuccessNotification())
      } catch (t: Throwable) {
        if (t is TraktAuthError) EventsManager.sendEvent(TraktSyncAuthError)
        EventsManager.sendEvent(TraktSyncError)
        notificationManager().notify(SYNC_NOTIFICATION_COMPLETE_ERROR_ID, createErrorNotification())
        Crashlytics.logException(t)
      } finally {
        Log.d(TAG, "Sync completed.")
        notificationManager().cancel(SYNC_NOTIFICATION_PROGRESS_ID)
        clear()
        stopSelf()
      }
    }
    return START_NOT_STICKY
  }

  private suspend fun runImportWatched(): Int {
    importWatchedRunner.progressListener = { show: Show, progress: Int, total: Int ->
      val notification = createProgressNotification().run {
        setContentText("Importing \'${show.title}\'...")
        setProgress(total, progress, false)
      }
      notificationManager().notify(SYNC_NOTIFICATION_PROGRESS_ID, notification.build())
      EventsManager.sendEvent(TraktSyncProgress)
    }
    return importWatchedRunner.run()
  }

  private suspend fun runImportWatchlist(totalProgress: Int) {
    importWatchlistRunner.progressListener = { show: Show, progress: Int, total: Int ->
      val notification = createProgressNotification().run {
        setContentText("Importing \'${show.title}\'...")
        setProgress(totalProgress + total, totalProgress + progress, false)
      }
      notificationManager().notify(SYNC_NOTIFICATION_PROGRESS_ID, notification.build())
      EventsManager.sendEvent(TraktSyncProgress)
    }
    importWatchlistRunner.run()
  }

  private suspend fun runExportWatched(totalProgress: Int) {
    val notification = createProgressNotification().run {
      setContentText("Exporting progress...")
      setProgress(totalProgress, totalProgress, false)
    }
    notificationManager().notify(SYNC_NOTIFICATION_PROGRESS_ID, notification.build())
    EventsManager.sendEvent(TraktSyncProgress)
    exportWatchedRunner.run()
  }

  private suspend fun runExportWatchlist(totalProgress: Int) {
    val notification = createProgressNotification().run {
      setContentText("Exporting watchlist...")
      setProgress(totalProgress, totalProgress, false)
    }
    notificationManager().notify(SYNC_NOTIFICATION_PROGRESS_ID, notification.build())
    EventsManager.sendEvent(TraktSyncProgress)
    exportWatchlistRunner.run()
  }

  override fun onDestroy() {
    coroutineContext.cancelChildren()
    super.onDestroy()
  }

  private fun clear() = runners.forEach { it.isRunning = false }

  override fun onBind(intent: Intent?): IBinder? = null
}
