package com.michaldrabik.showly2.ui.followedshows

import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.GridLayout
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.appComponent
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.ui.common.OnTabReselectedListener
import com.michaldrabik.showly2.ui.common.base.BaseFragment
import com.michaldrabik.showly2.ui.followedshows.myshows.MyShowsFragment
import com.michaldrabik.showly2.ui.followedshows.myshows.helpers.MyShowsSearchResult
import com.michaldrabik.showly2.ui.followedshows.myshows.helpers.ResultType.EMPTY
import com.michaldrabik.showly2.ui.followedshows.myshows.helpers.ResultType.NO_RESULTS
import com.michaldrabik.showly2.ui.followedshows.myshows.helpers.ResultType.RESULTS
import com.michaldrabik.showly2.ui.followedshows.myshows.recycler.MyShowsListItem
import com.michaldrabik.showly2.ui.followedshows.myshows.views.MyShowFanartView
import com.michaldrabik.showly2.ui.followedshows.watchlater.LaterShowsFragment
import com.michaldrabik.showly2.ui.show.ShowDetailsFragment.Companion.ARG_SHOW_ID
import com.michaldrabik.showly2.utilities.extensions.dimenToPx
import com.michaldrabik.showly2.utilities.extensions.fadeOut
import com.michaldrabik.showly2.utilities.extensions.gone
import com.michaldrabik.showly2.utilities.extensions.hideKeyboard
import com.michaldrabik.showly2.utilities.extensions.onClick
import com.michaldrabik.showly2.utilities.extensions.showKeyboard
import com.michaldrabik.showly2.utilities.extensions.visible
import kotlinx.android.synthetic.main.fragment_followed_shows.*
import kotlinx.android.synthetic.main.view_search.*

class FollowedShowsFragment : BaseFragment<FollowedShowsViewModel>(), OnTabReselectedListener {

  override val layoutResId = R.layout.fragment_followed_shows

  private lateinit var pagesAdapter: FollowedPagesAdapter

  override fun onCreate(savedInstanceState: Bundle?) {
    appComponent().inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun createViewModel() =
    ViewModelProvider(this, viewModelFactory).get(FollowedShowsViewModel::class.java)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    setupPager()
    viewModel.run {
      uiStream.observe(viewLifecycleOwner, Observer { render(it!!) })
      clearCache()
    }
  }

  private fun setupView() {
    followedShowsSearchView.hint = getString(R.string.textSearchForMyShows)
    followedShowsSearchView.onClick { enterSearch() }
    searchViewInput.run {
      imeOptions = EditorInfo.IME_ACTION_DONE
      setOnEditorActionListener { _, _, _ ->
        clearFocus()
        hideKeyboard()
        true
      }
    }
  }

  private fun setupPager() {
    //TODO Look for possible optimizations
    pagesAdapter = FollowedPagesAdapter(this)
    pagesAdapter.addPages(
      MyShowsFragment(),
      LaterShowsFragment()
    )
    followedShowsPager.run {
      isUserInputEnabled = false
      adapter = pagesAdapter
    }
  }

  private fun enterSearch() {
    searchViewText.gone()
    searchViewInput.run {
      setText("")
      doAfterTextChanged { viewModel.searchMyShows(it?.toString() ?: "") }
      visible()
      showKeyboard()
      requestFocus()
    }
    getMainActivity().hideNavigation()
    (searchViewIcon.drawable as Animatable).start()
    searchViewIcon.onClick { exitSearch() }
  }

  private fun exitSearch() {
    searchViewText.visible()
    searchViewInput.run {
      setText("")
      gone()
      hideKeyboard()
      clearFocus()
    }
    getMainActivity().showNavigation()
    searchViewIcon.setImageResource(R.drawable.ic_anim_search_to_close)
  }

  private fun render(uiModel: FollowedShowsUiModel) {
    uiModel.run {
      searchResult?.let { renderSearchResults(it) }
    }
  }

  private fun renderSearchResults(result: MyShowsSearchResult) {
    when (result.type) {
      RESULTS -> {
        followedShowsSearchContainer.visible()
        followedShowsPager.gone()
        followedShowsSearchEmptyView.gone()
        renderSearchContainer(result.items)
      }
      NO_RESULTS -> {
        followedShowsSearchContainer.gone()
        followedShowsPager.gone()
        followedShowsSearchEmptyView.visible()
      }
      EMPTY -> {
        followedShowsSearchContainer.gone()
        followedShowsPager.visible()
        followedShowsSearchEmptyView.gone()
      }
    }
    onTabReselected()
  }

  private fun renderSearchContainer(items: List<MyShowsListItem>) {
    followedShowsSearchContainer.removeAllViews()

    val context = requireContext()
    val itemHeight = context.dimenToPx(R.dimen.myShowsFanartHeight)
    val itemMargin = context.dimenToPx(R.dimen.spaceTiny)

    val clickListener: (Show) -> Unit = {
      followedShowsRoot.hideKeyboard()
      openShowDetails(it)
    }

    items.forEachIndexed { index, item ->
      val view = MyShowFanartView(context).apply {
        layoutParams = FrameLayout.LayoutParams(0, MATCH_PARENT)
        bind(item.show, item.image, clickListener)
      }
      val layoutParams = GridLayout.LayoutParams().apply {
        width = 0
        height = itemHeight
        columnSpec = GridLayout.spec(index % 2, 1F)
        setMargins(itemMargin, itemMargin, itemMargin, itemMargin)
      }
      followedShowsSearchContainer.addView(view, layoutParams)
    }
  }

  fun openShowDetails(show: Show) {
    getMainActivity().hideNavigation()
    followedShowsRoot.fadeOut {
      val bundle = Bundle().apply { putLong(ARG_SHOW_ID, show.id) }
      findNavController().navigate(R.id.actionFollowedShowsFragmentToShowDetailsFragment, bundle)
    }
  }

  fun enableSearch(enable: Boolean) {
    followedShowsSearchView.isClickable = enable
    followedShowsSearchView.isEnabled = enable
  }

  override fun onTabReselected() {
    followedShowsSearchView.translationY = 0F
    childFragmentManager.fragments.forEach {
      (it as? OnTabReselectedListener)?.onTabReselected()
    }
  }
}