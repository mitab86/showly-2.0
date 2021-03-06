package com.michaldrabik.ui_widgets.progress

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.michaldrabik.ui_base.common.WidgetsProvider
import com.michaldrabik.ui_base.trakt.quicksync.QuickSyncManager
import com.michaldrabik.ui_episodes.EpisodesManager
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_widgets.di.UiWidgetsComponentProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class ProgressWidgetEpisodeCheckService : JobIntentService(), CoroutineScope {

  companion object {
    private const val JOB_ID = 1001
    private const val EXTRA_EPISODE_ID = "EXTRA_EPISODE_ID"
    private const val EXTRA_SEASON_ID = "EXTRA_SEASON_ID"
    private const val EXTRA_SHOW_ID = "EXTRA_SHOW_ID"

    fun initialize(
      context: Context,
      episodeId: Long,
      seasonId: Long,
      showId: IdTrakt
    ) {
      val intent = Intent().apply {
        putExtra(EXTRA_EPISODE_ID, episodeId)
        putExtra(EXTRA_SEASON_ID, seasonId)
        putExtra(EXTRA_SHOW_ID, showId.id)
      }
      enqueueWork(
        context, ProgressWidgetEpisodeCheckService::class.java,
        JOB_ID, intent
      )
    }
  }

  override val coroutineContext = Job() + Dispatchers.Main

  @Inject
  lateinit var episodesManager: EpisodesManager

  @Inject
  lateinit var quickSyncManager: QuickSyncManager

  override fun onHandleWork(intent: Intent) {
    (application as UiWidgetsComponentProvider).provideWidgetsComponent().inject(this)

    val episodeId = intent.getLongExtra(EXTRA_EPISODE_ID, -1)
    val seasonId = intent.getLongExtra(EXTRA_SEASON_ID, -1)
    val showId = intent.getLongExtra(EXTRA_SHOW_ID, -1)

    if (episodeId == -1L || seasonId == -1L || showId == -1L) {
      FirebaseCrashlytics
        .getInstance()
        .recordException(Throwable("WatchlistWidgetEpisodeCheckService error. One of the IDs is invalid."))
      return
    }

    runBlocking {
      episodesManager.setEpisodeWatched(episodeId, seasonId, IdTrakt(showId))
      quickSyncManager.scheduleEpisodes(applicationContext, listOf(episodeId))
      (applicationContext as WidgetsProvider).requestWidgetsUpdate()
    }
  }

  override fun onDestroy() {
    coroutineContext.cancelChildren()
    super.onDestroy()
  }
}
