package com.michaldrabik.ui_base.utilities.extensions

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
import android.widget.TextView
import androidx.core.animation.doOnEnd
import androidx.core.view.updateMargins
import androidx.fragment.app.Fragment

fun View.visible() {
  if (visibility != View.VISIBLE) visibility = View.VISIBLE
}

fun View.gone() {
  if (visibility != View.GONE) visibility = View.GONE
}

fun View.invisible() {
  if (visibility != View.INVISIBLE) visibility = View.INVISIBLE
}

fun View.visibleIf(condition: Boolean, gone: Boolean = true) =
  if (condition) {
    visible()
  } else {
    if (gone) gone() else invisible()
  }

fun View.fadeIf(condition: Boolean, duration: Long = 250, startDelay: Long = 0) =
  if (condition) {
    fadeIn(duration, startDelay)
  } else {
    fadeOut(duration, startDelay)
  }

fun View.fadeIn(duration: Long = 250, startDelay: Long = 0, endAction: () -> Unit = {}) {
  if (visibility == View.VISIBLE) {
    endAction()
    return
  }
  visibility = View.VISIBLE
  alpha = 0F
  animate().alpha(1F).setDuration(duration).setStartDelay(startDelay).withEndAction(endAction).start()
}

fun View.fadeOut(duration: Long = 250, startDelay: Long = 0, endAction: () -> Unit = {}) {
  if (visibility == View.GONE) {
    endAction()
    return
  }
  animate().alpha(0F).setDuration(duration).setStartDelay(startDelay).withEndAction {
    gone()
    endAction()
  }.start()
}

fun View.shake() = ObjectAnimator.ofFloat(this, "translationX", 0F, -15F, 15F, -10F, 10F, -5F, 5F, 0F)
  .setDuration(500)
  .start()

fun View.bump(action: () -> Unit = {}) {
  val x = ObjectAnimator.ofFloat(this, "scaleX", 1F, 1.1F, 1F)
  val y = ObjectAnimator.ofFloat(this, "scaleY", 1F, 1.1F, 1F)

  AnimatorSet().apply {
    playTogether(x, y)
    duration = 250
    doOnEnd { action() }
    start()
  }
}

fun View.updateTopMargin(margin: Int) {
  (layoutParams as ViewGroup.MarginLayoutParams).updateMargins(top = margin)
}

fun TextView.setTextIfEmpty(text: String) {
  if (this.text.isBlank()) this.text = text
}

fun TextView.setTextFade(text: String, duration: Long = 125) {
  fadeOut(
    duration = duration,
    endAction = {
      setText(text)
      fadeIn(duration = duration)
    }
  )
}

fun Activity.disableUi() = window.setFlags(FLAG_NOT_TOUCHABLE, FLAG_NOT_TOUCHABLE)

fun Activity.enableUi() = window.clearFlags(FLAG_NOT_TOUCHABLE)

fun Fragment.disableUi() = activity?.disableUi()

fun Fragment.enableUi() = activity?.enableUi()
