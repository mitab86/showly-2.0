package com.michaldrabik.network.trakt.api

import com.michaldrabik.network.Config.TRAKT_CLIENT_ID
import com.michaldrabik.network.Config.TRAKT_CLIENT_SECRET
import com.michaldrabik.network.Config.TRAKT_REDIRECT_URL
import com.michaldrabik.network.trakt.model.Comment
import com.michaldrabik.network.trakt.model.Episode
import com.michaldrabik.network.trakt.model.OAuthResponse
import com.michaldrabik.network.trakt.model.Show
import com.michaldrabik.network.trakt.model.SyncExportRequest
import com.michaldrabik.network.trakt.model.request.OAuthRefreshRequest
import com.michaldrabik.network.trakt.model.request.OAuthRequest
import com.michaldrabik.network.trakt.model.request.OAuthRevokeRequest
import com.michaldrabik.network.trakt.model.request.RatingRequest
import com.michaldrabik.network.trakt.model.request.RatingRequestValue

class TraktApi(private val service: TraktService) {

  suspend fun fetchShow(traktId: Long) = service.fetchShow(traktId)

  suspend fun fetchPopularShows(genres: String) = service.fetchPopularShows(genres)

  suspend fun fetchTrendingShows(genres: String) = service.fetchTrendingShows(genres).map { it.show!! }

  suspend fun fetchAnticipatedShows(genres: String) = service.fetchAnticipatedShows(genres).map { it.show!! }

  suspend fun fetchRelatedShows(traktId: Long) = service.fetchRelatedShows(traktId)

  suspend fun fetchNextEpisode(traktId: Long): Episode? {
    val response = service.fetchNextEpisode(traktId)
    if (response.isSuccessful && response.code() == 204) return null
    return response.body()
  }

  suspend fun fetchShowsSearch(query: String) =
    service.fetchSearchResults(query)
      .sortedWith(compareBy({ it.score }, { it.show?.votes ?: 0 }))
      .reversed()
      .map { it.show!! }

  suspend fun fetchSeasons(traktId: Long) =
    service.fetchSeasons(traktId)
      .sortedByDescending { it.number }

  suspend fun fetchShowComments(traktId: Long, limit: Int) =
    service.fetchShowComments(traktId, limit)

  suspend fun fetchShowTranslations(traktId: Long, code: String) =
    service.fetchShowTranslations(traktId, code)

  suspend fun fetchSeasonTranslations(showTraktId: Long, seasonNumber: Int, code: String) =
    service.fetchSeasonTranslations(showTraktId, seasonNumber, code)

  suspend fun fetchEpisodeTranslations(showTraktId: Long, seasonNumber: Int, episodeNumber: Int, code: String) =
    service.fetchEpisodeTranslations(showTraktId, seasonNumber, episodeNumber, code)

  suspend fun fetchEpisodeComments(
    traktId: Long,
    seasonNumber: Int,
    episodeNumber: Int
  ): List<Comment> = try {
    service.fetchEpisodeComments(traktId, seasonNumber, episodeNumber)
  } catch (t: Throwable) {
    emptyList()
  }

  suspend fun fetchShowActors(traktId: Long) =
    service.fetchShowPeople(traktId).cast ?: emptyList()

  suspend fun fetchAuthTokens(code: String): OAuthResponse {
    val request = OAuthRequest(
      code,
      TRAKT_CLIENT_ID,
      TRAKT_CLIENT_SECRET,
      TRAKT_REDIRECT_URL
    )
    return service.fetchOAuthToken(request)
  }

  suspend fun refreshAuthTokens(refreshToken: String): OAuthResponse {
    val request = OAuthRefreshRequest(
      refreshToken,
      TRAKT_CLIENT_ID,
      TRAKT_CLIENT_SECRET,
      TRAKT_REDIRECT_URL
    )
    return service.refreshOAuthToken(request)
  }

  suspend fun revokeAuthTokens(token: String) {
    val request = OAuthRevokeRequest(
      token,
      TRAKT_CLIENT_ID,
      TRAKT_CLIENT_SECRET
    )
    service.revokeOAuthToken(request)
  }

  suspend fun fetchMyProfile(token: String) =
    service.fetchMyProfile("Bearer $token")

  suspend fun fetchHiddenShows(token: String) =
    service.fetchHiddenShows("Bearer $token", pageLimit = 100)

  suspend fun fetchSyncWatched(token: String, extended: String? = null) =
    service.fetchSyncWatched("Bearer $token", extended)

  suspend fun fetchSyncWatchlist(token: String) =
    service.fetchSyncWatchlist("Bearer $token")

  suspend fun postSyncWatchlist(token: String, request: SyncExportRequest) =
    service.postSyncWatchlist("Bearer $token", request)

  suspend fun postSyncWatched(token: String, request: SyncExportRequest) =
    service.postSyncWatched("Bearer $token", request)

  suspend fun postDeleteProgress(token: String, request: SyncExportRequest) =
    service.deleteHistory("Bearer $token", request)

  suspend fun postDeleteWatchlist(token: String, request: SyncExportRequest) =
    service.deleteWatchlist("Bearer $token", request)

  suspend fun deleteRating(token: String, show: Show) {
    val requestValue = RatingRequestValue(0, show.ids)
    val body = RatingRequest(shows = listOf(requestValue))
    service.postRemoveRating("Bearer $token", body)
  }

  suspend fun deleteRating(token: String, episode: Episode) {
    val requestValue = RatingRequestValue(0, episode.ids)
    val body = RatingRequest(episodes = listOf(requestValue))
    service.postRemoveRating("Bearer $token", body)
  }

  suspend fun postRating(token: String, show: Show, rating: Int) {
    val requestValue = RatingRequestValue(rating, show.ids)
    val body = RatingRequest(shows = listOf(requestValue))
    service.postRating("Bearer $token", body)
  }

  suspend fun postRating(token: String, episode: Episode, rating: Int) {
    val requestValue = RatingRequestValue(rating, episode.ids)
    val body = RatingRequest(episodes = listOf(requestValue))
    service.postRating("Bearer $token", body)
  }

  suspend fun fetchShowsRatings(token: String) =
    service.fetchShowsRatings("Bearer $token")

  suspend fun fetchEpisodesRatings(token: String) =
    service.fetchEpisodesRatings("Bearer $token")
}
