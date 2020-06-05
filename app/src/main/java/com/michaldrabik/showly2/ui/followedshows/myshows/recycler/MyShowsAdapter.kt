package com.michaldrabik.showly2.ui.followedshows.myshows.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.showly2.model.MyShowsSection
import com.michaldrabik.showly2.model.SortOrder
import com.michaldrabik.showly2.ui.common.base.BaseAdapter
import com.michaldrabik.showly2.ui.followedshows.myshows.recycler.MyShowsItem.Type
import com.michaldrabik.showly2.ui.followedshows.myshows.views.MyShowAllView
import com.michaldrabik.showly2.ui.followedshows.myshows.views.MyShowHeaderView
import com.michaldrabik.showly2.ui.followedshows.myshows.views.MyShowsRecentsView
import com.michaldrabik.showly2.ui.followedshows.myshows.views.section.MyShowsSectionView

class MyShowsAdapter : BaseAdapter<MyShowsItem>() {

  companion object {
    private const val VIEW_TYPE_HEADER = 1
    private const val VIEW_TYPE_SHOW_ITEM = 2
    private const val VIEW_TYPE_RECENTS_SECTION = 3
    private const val VIEW_TYPE_HORIZONTAL_SECTION = 4
  }

  override val asyncDiffer = AsyncListDiffer(this, MyShowsItemDiffCallback())

  var onSortOrderChangeListener: ((MyShowsSection, SortOrder) -> Unit)? = null
  var sectionMissingImageListener: ((MyShowsItem, MyShowsItem.HorizontalSection, Boolean) -> Unit)? = null

  private val horizontalPositions = mutableMapOf<MyShowsSection, Pair<Int, Int>>()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    when (viewType) {
      VIEW_TYPE_HEADER -> BaseViewHolder(MyShowHeaderView(parent.context))
      VIEW_TYPE_SHOW_ITEM -> BaseViewHolder(MyShowAllView(parent.context))
      VIEW_TYPE_RECENTS_SECTION -> BaseViewHolder(MyShowsRecentsView(parent.context))
      VIEW_TYPE_HORIZONTAL_SECTION -> BaseViewHolder(MyShowsSectionView(parent.context).apply {
        scrollPositionListener = { section, position -> horizontalPositions[section] = position }
      })
      else -> throw IllegalStateException()
    }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val item = asyncDiffer.currentList[position]
    when (holder.itemViewType) {
      VIEW_TYPE_HEADER -> (holder.itemView as MyShowHeaderView).bind(
        item.header!!,
        onSortOrderChangeListener
      )
      VIEW_TYPE_SHOW_ITEM -> (holder.itemView as MyShowAllView).bind(
        item,
        missingImageListener,
        itemClickListener
      )
      VIEW_TYPE_RECENTS_SECTION -> (holder.itemView as MyShowsRecentsView).bind(
        item.recentsSection!!,
        itemClickListener
      )
      VIEW_TYPE_HORIZONTAL_SECTION -> (holder.itemView as MyShowsSectionView).bind(
        item.horizontalSection!!,
        horizontalPositions[item.horizontalSection.section] ?: Pair(0, 0),
        itemClickListener,
        sectionMissingImageListener
      )
    }
  }

  override fun getItemViewType(position: Int) =
    when (asyncDiffer.currentList[position].type) {
      Type.HEADER -> VIEW_TYPE_HEADER
      Type.ALL_SHOWS_ITEM -> VIEW_TYPE_SHOW_ITEM
      Type.RECENT_SHOWS -> VIEW_TYPE_RECENTS_SECTION
      Type.HORIZONTAL_SHOWS -> VIEW_TYPE_HORIZONTAL_SECTION
    }
}
