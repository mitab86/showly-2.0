package com.michaldrabik.ui_my_shows.seelater.recycler

import androidx.recyclerview.widget.DiffUtil

class SeeLaterItemDiffCallback : DiffUtil.ItemCallback<SeeLaterListItem>() {

  override fun areItemsTheSame(oldItem: SeeLaterListItem, newItem: SeeLaterListItem) =
    oldItem.show.ids.trakt == newItem.show.ids.trakt

  override fun areContentsTheSame(oldItem: SeeLaterListItem, newItem: SeeLaterListItem) =
    oldItem.image == newItem.image &&
      oldItem.isLoading == newItem.isLoading &&
      oldItem.translation == newItem.translation
}
