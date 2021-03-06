package com.michaldrabik.ui_base

import android.os.Bundle
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.michaldrabik.ui_model.DiscoverFilters
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.MyShowsSection
import com.michaldrabik.ui_model.Show
import java.util.Locale.ROOT

object Analytics {

  private val firebaseAnalytics by lazy { Firebase.analytics }

  fun logShowDetailsDisplay(show: Show) {
    firebaseAnalytics.logEvent("show_details_display") {
      param("show_id_trakt", show.traktId)
      param("show_title", show.title)
    }
  }

  fun logShowAddToMyShows(show: Show) {
    firebaseAnalytics.logEvent("show_add_to_my_shows") {
      param("show_id_trakt", show.traktId)
      param("show_title", show.title)
    }
  }

  fun logShowAddToSeeLater(show: Show) {
    firebaseAnalytics.logEvent("show_add_to_see_later") {
      param("show_id_trakt", show.traktId)
      param("show_title", show.title)
    }
  }

  fun logShowAddToArchive(show: Show) {
    firebaseAnalytics.logEvent("show_add_to_archive") {
      param("show_id_trakt", show.traktId)
      param("show_title", show.title)
    }
  }

  fun logShowTrailerClick(show: Show) {
    firebaseAnalytics.logEvent("show_click_trailer") {
      param("show_id_trakt", show.traktId)
      param("show_title", show.title)
    }
  }

  fun logShowCommentsClick(show: Show) {
    firebaseAnalytics.logEvent("show_click_comments") {
      param("show_id_trakt", show.traktId)
      param("show_title", show.title)
    }
  }

  fun logShowImdbClick(show: Show) {
    firebaseAnalytics.logEvent("show_click_imdb") {
      param("show_id_trakt", show.traktId)
      param("show_title", show.title)
    }
  }

  fun logShowShareClick(show: Show) {
    firebaseAnalytics.logEvent("show_click_share") {
      param("show_id_trakt", show.traktId)
      param("show_title", show.title)
    }
  }

  fun logShowGalleryClick(idTrakt: Long) {
    firebaseAnalytics.logEvent("show_click_gallery") {
      param("show_id_trakt", idTrakt)
    }
  }

  fun logShowQuickProgress(show: Show) {
    firebaseAnalytics.logEvent("show_quick_progress_set") {
      param("show_id_trakt", show.traktId)
      param("show_title", show.title)
    }
  }

  fun logShowRated(show: Show, rating: Int) {
    firebaseAnalytics.logEvent("show_rate") {
      param("show_id_trakt", show.traktId)
      param("show_title", show.title)
      param("show_rating", rating.toLong())
    }
  }

  fun logEpisodeRated(idTrakt: Long, episode: Episode, rating: Int) {
    firebaseAnalytics.logEvent("episode_rate") {
      param("show_id_trakt", idTrakt)
      param("episode_id_trakt", episode.ids.trakt.id)
      param("episode_title", episode.title)
      param("episode_rating", rating.toLong())
    }
  }

  fun logDiscoverFiltersApply(filters: DiscoverFilters) {
    firebaseAnalytics.logEvent("discover_filters_set") {
      param("filters_feed_order", filters.feedOrder.name.toLowerCase(ROOT))
      param("filters_hide_anticipated", filters.hideAnticipated.toString())
      param("filters_genres", filters.genres.map { it.slug }.toTypedArray().contentToString())
    }
  }

  fun logSearchQuery(searchQuery: String) {
    firebaseAnalytics.logEvent("search_query") {
      param("search_text", searchQuery)
    }
  }

  // In App Rate

  fun logInAppRateDisplayed() =
    firebaseAnalytics.logEvent("in_app_rate_display", Bundle.EMPTY)

  fun logInAppRateDecision(isYes: Boolean) {
    val decision = if (isYes) "yes" else "no"
    firebaseAnalytics.logEvent("in_app_rate_decision") {
      param("decision", decision)
    }
  }

  // Trakt

  fun logTraktLogin() = firebaseAnalytics.logEvent("trakt_login", null)

  fun logTraktLogout() = firebaseAnalytics.logEvent("trakt_logout", null)

  fun logTraktFullSyncSuccess(import: Boolean, export: Boolean) {
    firebaseAnalytics.logEvent("trakt_full_sync_success") {
      param("sync_type_import", import.toString())
      param("sync_type_export", export.toString())
    }
  }

  fun logTraktQuickSyncSuccess(count: Int) {
    firebaseAnalytics.logEvent("trakt_quick_sync_success") {
      param("items_count", count.toLong())
    }
  }

  // Settings

  fun logSettingsTraktQuickSync(enabled: Boolean) {
    firebaseAnalytics.logEvent("settings_trakt_quick_sync") {
      param("enabled", enabled.toString())
    }
  }

  fun logSettingsTraktQuickRemove(enabled: Boolean) {
    firebaseAnalytics.logEvent("settings_trakt_quick_remove") {
      param("enabled", enabled.toString())
    }
  }

  fun logSettingsRecentlyAddedAmount(amount: Long) {
    firebaseAnalytics.logEvent("settings_recently_added_amount") {
      param("amount", amount)
    }
  }

  fun logSettingsPushNotifications(enabled: Boolean) {
    firebaseAnalytics.logEvent("settings_push_notifications") {
      param("enabled", enabled.toString())
    }
  }

  fun logSettingsEpisodesAnnouncements(enabled: Boolean) {
    firebaseAnalytics.logEvent("settings_episodes_announcements") {
      param("enabled", enabled.toString())
    }
  }

  fun logSettingsArchivedStats(enabled: Boolean) {
    firebaseAnalytics.logEvent("settings_archived_stats") {
      param("enabled", enabled.toString())
    }
  }

  fun logSettingsSpecialSeasons(enabled: Boolean) {
    firebaseAnalytics.logEvent("settings_special_seasons") {
      param("enabled", enabled.toString())
    }
  }

  fun logSettingsWhenToNotify(value: String) {
    firebaseAnalytics.logEvent("settings_when_to_notify") {
      param("value", value.toLowerCase(ROOT))
    }
  }

  fun logSettingsLanguage(value: String) {
    firebaseAnalytics.logEvent("settings_language") {
      param("value", value.toLowerCase(ROOT))
    }
  }

  fun logSettingsMyShowsSection(section: MyShowsSection, enabled: Boolean) {
    firebaseAnalytics.logEvent("settings_my_shows_section") {
      param("section", section.name.toLowerCase(ROOT))
      param("enabled", enabled.toString())
    }
  }
}
