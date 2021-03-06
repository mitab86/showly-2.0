package com.michaldrabik.ui_show.comments

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import com.michaldrabik.ui_base.utilities.extensions.addDivider
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_model.Comment
import com.michaldrabik.ui_show.R
import kotlinx.android.synthetic.main.view_comments.view.*

class CommentsView : ConstraintLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val commentsAdapter by lazy { CommentsAdapter() }

  init {
    inflate(context, R.layout.view_comments, this)
    layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
    setupRecycler()
  }

  fun bind(comments: List<Comment>) {
    commentsProgress.gone()
    commentsEmpty.visibleIf(comments.isEmpty())
    commentsAdapter.setItems(comments)
  }

  fun clear() {
    commentsProgress.visible()
    commentsEmpty.gone()
    commentsAdapter.setItems(emptyList())
  }

  private fun setupRecycler() {
    commentsRecycler.apply {
      setHasFixedSize(true)
      adapter = commentsAdapter
      layoutManager = LinearLayoutManager(context, VERTICAL, false)
      itemAnimator = null
      addDivider(R.drawable.divider_comments_list)
    }
  }
}
