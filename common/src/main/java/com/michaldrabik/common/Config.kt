package com.michaldrabik.common

import org.threeten.bp.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

object Config {
  private const val TVDB_IMAGE_PERSON_BASE_URL = "https://artworks.thetvdb.com"
  const val TVDB_IMAGE_BASE_BANNERS_URL = "$TVDB_IMAGE_PERSON_BASE_URL/banners/"
  const val TVDB_IMAGE_BASE_POSTER_URL = "${TVDB_IMAGE_BASE_BANNERS_URL}posters/"
  const val TVDB_IMAGE_BASE_FANART_URL = "${TVDB_IMAGE_BASE_BANNERS_URL}fanart/original/"
  const val SHOW_WHATS_NEW = false

  const val DEVELOPER_MAIL = "showlyapp@gmail.com"
  const val PLAYSTORE_URL = "https://play.google.com/store/apps/details?id=com.michaldrabik.showly2"

  const val MAIN_GRID_SPAN = 3
  const val IMAGE_FADE_DURATION_MS = 200
  const val SEARCH_RECENTS_AMOUNT = 5
  const val FANART_GALLERY_IMAGES_LIMIT = 30
  const val PULL_TO_REFRESH_COOLDOWN_MS = 10_000
  const val INITIAL_RATING = 0
  const val DEFAULT_LANGUAGE = "en"

  val MY_SHOWS_RECENTS_OPTIONS = arrayOf(2, 4, 6, 8)
  val DISCOVER_SHOWS_CACHE_DURATION by lazy { TimeUnit.HOURS.toMillis(12) }
  val RELATED_CACHE_DURATION by lazy { TimeUnit.DAYS.toMillis(7) }
  val SHOW_DETAILS_CACHE_DURATION by lazy { TimeUnit.DAYS.toMillis(3) }
  val ACTORS_CACHE_DURATION by lazy { TimeUnit.DAYS.toMillis(3) }
  val NEW_BADGE_DURATION by lazy { TimeUnit.HOURS.toMillis(30) }
  val SHOW_SYNC_COOLDOWN by lazy {
    if (BuildConfig.DEBUG) TimeUnit.MINUTES.toMillis(5) else TimeUnit.HOURS.toMillis(8)
  }
  val TRANSLATION_SYNC_COOLDOWN by lazy {
    if (BuildConfig.DEBUG) TimeUnit.MINUTES.toMillis(5) else TimeUnit.DAYS.toMillis(3)
  }

  val DISPLAY_DATE_FORMAT: DateTimeFormatter by lazy { DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy, HH:mm") }
  val DISPLAY_DATE_DAY_ONLY_FORMAT: DateTimeFormatter by lazy { DateTimeFormatter.ofPattern("dd MMM yyyy") }

  const val HOST_ACTIVITY_NAME = "com.michaldrabik.showly2.ui.main.MainActivity"
}
