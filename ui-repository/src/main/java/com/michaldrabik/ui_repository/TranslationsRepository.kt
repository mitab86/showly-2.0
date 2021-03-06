package com.michaldrabik.ui_repository

import com.michaldrabik.common.Config
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.network.Cloud
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.model.EpisodeTranslation
import com.michaldrabik.storage.database.model.ShowTranslation
import com.michaldrabik.storage.database.model.TranslationsSyncLog
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Season
import com.michaldrabik.ui_model.SeasonTranslation
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_repository.mappers.Mappers
import javax.inject.Inject

@AppScope
class TranslationsRepository @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val mappers: Mappers
) {

  suspend fun loadTranslation(
    show: Show,
    language: String = Config.DEFAULT_LANGUAGE,
    onlyLocal: Boolean = false
  ): Translation? {
    val local = database.showTranslationsDao().getById(show.traktId, language)
    local?.let {
      return mappers.translation.fromDatabase(it).copy(isLocal = true)
    }
    if (onlyLocal) return null

    val remoteTranslation = cloud.traktApi.fetchShowTranslations(show.traktId, language).firstOrNull()
    val translation = mappers.translation.fromNetwork(remoteTranslation)
    val translationDb = ShowTranslation.fromTraktId(
      show.traktId,
      translation.title,
      translation.language,
      translation.overview,
      nowUtcMillis()
    )

    if (translationDb.overview.isNotBlank()) {
      database.showTranslationsDao().insert(translationDb)
    }

    return translation
  }

  suspend fun loadTranslation(
    episode: Episode,
    showId: IdTrakt,
    language: String = Config.DEFAULT_LANGUAGE,
    onlyLocal: Boolean = false
  ): Translation? {
    val local = database.episodeTranslationsDao().getById(episode.ids.trakt.id, showId.id, language)
    local?.let {
      return mappers.translation.fromDatabase(it).copy(isLocal = true)
    }

    if (onlyLocal) return null

    val remoteTranslation = cloud.traktApi.fetchSeasonTranslations(showId.id, episode.season, language)
      .map { mappers.translation.fromNetwork(it) }
    val targetTranslation = remoteTranslation.find { it.ids.trakt == episode.ids.trakt }

    remoteTranslation
      .filter { it.overview.isNotBlank() }
      .forEach { item ->
        val dbItem = EpisodeTranslation.fromTraktId(
          item.ids.trakt.id,
          showId.id,
          item.title,
          item.language,
          item.overview,
          nowUtcMillis()
        )
        database.episodeTranslationsDao().insert(dbItem)
      }

    if (targetTranslation != null) {
      return Translation(
        targetTranslation.title,
        targetTranslation.overview,
        targetTranslation.language
      )
    }
    return null
  }

  suspend fun loadTranslations(
    season: Season,
    showId: IdTrakt,
    language: String = Config.DEFAULT_LANGUAGE,
    onlyLocal: Boolean = false
  ): List<SeasonTranslation> {
    val episodes = season.episodes.toList()
    val episodesIds = season.episodes.map { it.ids.trakt.id }
    val local = database.episodeTranslationsDao().getByIds(episodesIds, showId.id, language)

    if (local.isNotEmpty()) {
      return episodes.map { episode ->
        val translation = local.find { it.idTrakt == episode.ids.trakt.id }
        SeasonTranslation(
          ids = episode.ids.copy(),
          title = translation?.title ?: "",
          seasonNumber = season.number,
          episodeNumber = episode.number,
          overview = translation?.overview ?: "",
          language = translation?.language ?: language,
          isLocal = true
        )
      }
    }

    if (onlyLocal) return emptyList()

    val remoteTranslation = cloud.traktApi.fetchSeasonTranslations(showId.id, season.number, language)
      .map { mappers.translation.fromNetwork(it) }

    remoteTranslation
      .filter { it.overview.isNotBlank() }
      .forEach { item ->
        val dbItem = EpisodeTranslation.fromTraktId(
          item.ids.trakt.id,
          showId.id,
          item.title,
          item.language,
          item.overview,
          nowUtcMillis()
        )
        database.episodeTranslationsDao().insert(dbItem)
      }

    return episodes.map { episode ->
      val translation = remoteTranslation.find { it.ids.trakt.id == episode.ids.trakt.id }
      SeasonTranslation(
        ids = episode.ids.copy(),
        title = translation?.title ?: "",
        seasonNumber = season.number,
        episodeNumber = episode.number,
        overview = translation?.overview ?: "",
        language = translation?.language ?: language,
        isLocal = true
      )
    }
  }

  suspend fun updateLocalShowTranslation(show: Show, language: String = Config.DEFAULT_LANGUAGE) {
    val localTranslation = database.showTranslationsDao().getById(show.traktId, language)
    val remoteTranslation = cloud.traktApi.fetchShowTranslations(show.traktId, language).firstOrNull()

    val translationDb = ShowTranslation.fromTraktId(
      show.traktId,
      remoteTranslation?.title ?: "",
      remoteTranslation?.language ?: "",
      remoteTranslation?.overview ?: "",
      nowUtcMillis()
    ).copy(id = localTranslation?.id ?: 0)

    if (translationDb.overview.isNotBlank()) {
      database.showTranslationsDao().insert(translationDb)
    }

    database.translationsSyncLogDao().upsert(TranslationsSyncLog(show.ids.trakt.id, nowUtcMillis()))
  }
}
