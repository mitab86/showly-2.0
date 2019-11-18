package com.michaldrabik.showly2.ui.settings

import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
import com.michaldrabik.showly2.BuildConfig
import com.michaldrabik.showly2.Config
import com.michaldrabik.showly2.common.notifications.AnnouncementManager
import com.michaldrabik.showly2.di.AppScope
import com.michaldrabik.showly2.fcm.NotificationChannel
import com.michaldrabik.showly2.model.Settings
import com.michaldrabik.showly2.repository.settings.SettingsRepository
import javax.inject.Inject

@AppScope
class SettingsInteractor @Inject constructor(
  private val settingsRepository: SettingsRepository,
  private val announcementManager: AnnouncementManager
) {

  suspend fun getSettings(): Settings = settingsRepository.load()!!

  suspend fun setRecentShowsAmount(amount: Int) {
    check(amount in Config.MY_SHOWS_RECENTS_OPTIONS)
    val settings = settingsRepository.load()
    settings?.let {
      val new = it.copy(myShowsRecentsAmount = amount)
      settingsRepository.update(new)
    }
  }

  suspend fun enablePushNotifications(enable: Boolean) {
    val settings = settingsRepository.load()
    settings?.let {
      val new = it.copy(pushNotificationsEnabled = enable)
      settingsRepository.update(new)
    }
    FirebaseMessaging.getInstance().run {
      val suffix = if (BuildConfig.DEBUG) "-debug" else ""
      if (enable) {
        subscribeToTopic(NotificationChannel.GENERAL_INFO.topicName + suffix)
        subscribeToTopic(NotificationChannel.SHOWS_INFO.topicName + suffix)
      } else {
        unsubscribeFromTopic(NotificationChannel.GENERAL_INFO.topicName + suffix)
        unsubscribeFromTopic(NotificationChannel.SHOWS_INFO.topicName + suffix)
      }
    }
  }

  suspend fun enableShowsNotifications(enable: Boolean, context: Context) {
    val settings = settingsRepository.load()
    settings?.let {
      val new = it.copy(episodesNotificationsEnabled = enable)
      settingsRepository.update(new)
      announcementManager.refreshEpisodesAnnouncements(context.applicationContext)
    }
  }
}