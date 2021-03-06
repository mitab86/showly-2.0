package com.michaldrabik.showly2.ui.main.cases

import android.content.SharedPreferences
import com.google.firebase.messaging.FirebaseMessaging
import com.michaldrabik.common.Config
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.showly2.BuildConfig
import com.michaldrabik.ui_base.fcm.NotificationChannel
import com.michaldrabik.ui_model.Settings
import com.michaldrabik.ui_repository.SettingsRepository
import javax.inject.Inject
import javax.inject.Named

@AppScope
class MainInitialsCase @Inject constructor(
  private val settingsRepository: SettingsRepository,
  @Named("miscPreferences") private var miscPreferences: SharedPreferences
) {

  suspend fun initSettings() {
    if (!settingsRepository.isInitialized()) {
      settingsRepository.update(Settings.createInitial())
    }
  }

  suspend fun setInitialRun(value: Boolean) {
    val settings = settingsRepository.load()
    settings.let {
      settingsRepository.update(it.copy(isInitialRun = value))
    }
  }

  suspend fun isInitialRun(): Boolean {
    val settings = settingsRepository.load()
    return settings.isInitialRun
  }

  suspend fun initFcm() {
    val isEnabled = settingsRepository.load().pushNotificationsEnabled
    FirebaseMessaging.getInstance().run {
      val suffix = if (BuildConfig.DEBUG) "-debug" else ""
      if (isEnabled) {
        subscribeToTopic(NotificationChannel.GENERAL_INFO.topicName + suffix)
        subscribeToTopic(NotificationChannel.SHOWS_INFO.topicName + suffix)
      } else {
        unsubscribeFromTopic(NotificationChannel.GENERAL_INFO.topicName + suffix)
        unsubscribeFromTopic(NotificationChannel.SHOWS_INFO.topicName + suffix)
      }
    }
  }

  fun showWhatsNew(isInitialRun: Boolean): Boolean {
    val version = miscPreferences.getInt("APP_VERSION", 0)
    miscPreferences.edit().putInt("APP_VERSION", BuildConfig.VERSION_CODE).apply()

    if (Config.SHOW_WHATS_NEW && BuildConfig.VERSION_CODE > version && !isInitialRun) {
      return true
    }
    return false
  }
}
