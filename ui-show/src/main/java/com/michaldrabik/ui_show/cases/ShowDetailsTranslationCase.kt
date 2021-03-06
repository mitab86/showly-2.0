package com.michaldrabik.ui_show.cases

import com.michaldrabik.common.Config.DEFAULT_LANGUAGE
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.Season
import com.michaldrabik.ui_model.SeasonTranslation
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_repository.SettingsRepository
import com.michaldrabik.ui_repository.TranslationsRepository
import javax.inject.Inject

@AppScope
class ShowDetailsTranslationCase @Inject constructor(
  private val translationsRepository: TranslationsRepository,
  private val settingsRepository: SettingsRepository
) {

  suspend fun loadTranslation(show: Show): Translation? {
    val language = settingsRepository.getLanguage()
    if (language == DEFAULT_LANGUAGE) return null
    return translationsRepository.loadTranslation(show, language)
  }

  suspend fun loadTranslation(episode: Episode, show: Show, onlyLocal: Boolean = false): Translation? {
    val language = settingsRepository.getLanguage()
    if (language == DEFAULT_LANGUAGE) return null
    return translationsRepository.loadTranslation(episode, show.ids.trakt, language, onlyLocal)
  }

  suspend fun loadTranslations(season: Season, show: Show, onlyLocal: Boolean = false): List<SeasonTranslation> {
    val language = settingsRepository.getLanguage()
    if (language == DEFAULT_LANGUAGE) return emptyList()
    return translationsRepository.loadTranslations(season, show.ids.trakt, language, onlyLocal)
  }
}
