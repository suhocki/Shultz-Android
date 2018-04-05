package suhockii.dev.shultz.util

import android.animation.Animator
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Parcelable
import android.support.design.widget.AppBarLayout
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import android.widget.Toast
import java.io.Serializable


fun Context.toast(message: CharSequence): Toast = Toast
        .makeText(this, message, Toast.LENGTH_SHORT)
        .apply {
            show()
        }

inline fun <reified T : Activity> Context.startActivity(vararg params: Pair<String, Any?>) =
        this.startActivity(Intent(this, T::class.java).let { intent ->
            if (params.isNotEmpty()) params.forEach {
                val value = it.second
                when (value) {
                    null -> intent.putExtra(it.first, null as Serializable?)
                    is Int -> intent.putExtra(it.first, value)
                    is Long -> intent.putExtra(it.first, value)
                    is CharSequence -> intent.putExtra(it.first, value)
                    is String -> intent.putExtra(it.first, value)
                    is Float -> intent.putExtra(it.first, value)
                    is Double -> intent.putExtra(it.first, value)
                    is Char -> intent.putExtra(it.first, value)
                    is Short -> intent.putExtra(it.first, value)
                    is Boolean -> intent.putExtra(it.first, value)
                    is Serializable -> intent.putExtra(it.first, value)
                    is Bundle -> intent.putExtra(it.first, value)
                    is Parcelable -> intent.putExtra(it.first, value)
                    is Array<*> -> when {
                        value.isArrayOf<CharSequence>() -> intent.putExtra(it.first, value)
                        value.isArrayOf<String>() -> intent.putExtra(it.first, value)
                        value.isArrayOf<Parcelable>() -> intent.putExtra(it.first, value)
                        else -> throw Exception("Intent extra ${it.first} has wrong type ${value.javaClass.name}")
                    }
                    is IntArray -> intent.putExtra(it.first, value)
                    is LongArray -> intent.putExtra(it.first, value)
                    is FloatArray -> intent.putExtra(it.first, value)
                    is DoubleArray -> intent.putExtra(it.first, value)
                    is CharArray -> intent.putExtra(it.first, value)
                    is ShortArray -> intent.putExtra(it.first, value)
                    is BooleanArray -> intent.putExtra(it.first, value)
                    else -> throw Exception("Intent extra ${it.first} has wrong type ${value.javaClass.name}")
                }
                return@forEach
            }
            return@let intent
        })

fun Context.isInternetConnected(): Boolean {
    val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val netInfo = cm.activeNetworkInfo
    return netInfo != null && netInfo.isConnectedOrConnecting
}

fun Activity.closeKeyboard() {
    val view = this.currentFocus
    if (view != null) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}

fun Animation.withEndAction(action: () -> Unit) {
    setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationRepeat(p0: Animation?) {}
        override fun onAnimationStart(p0: Animation?) {}
        override fun onAnimationEnd(p0: Animation?) = action.invoke()

    })
}

fun ObjectAnimator.withEndAction(action: () -> Unit) {
    this.addListener(object : Animator.AnimatorListener {
        override fun onAnimationRepeat(p0: Animator?) {}
        override fun onAnimationCancel(p0: Animator?) {}
        override fun onAnimationStart(p0: Animator?) {}
        override fun onAnimationEnd(p0: Animator?) = action.invoke()

    })
}

fun ProgressBar.animateProgressTo(progressTo: Int): ObjectAnimator {
    val animation = ObjectAnimator.ofInt(this, "progress", this.progress, progressTo)
    animation.duration = 300
    animation.interpolator = DecelerateInterpolator()
    animation.start()
    return animation
}

fun View.setInTouchListener(inTouch: () -> Unit, released: () -> Unit) {
    setOnTouchListener { _, motionEvent ->
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> inTouch.invoke()
            MotionEvent.ACTION_UP -> {
                performClick()
                released.invoke()
            }
            MotionEvent.ACTION_CANCEL -> released.invoke()
        }
        return@setOnTouchListener true
    }
}

fun AppBarLayout.addCollapsingListener(updateState: (collapsed: Boolean) -> Unit) {
    val expandedState = 0
    val collapsedState = 1
    val idleState = 2
    var mCurrentState = idleState
    addOnOffsetChangedListener { appBarLayout, i ->
        if (i == 0) {
            if (mCurrentState != expandedState) {
                updateState(false)
            }
            mCurrentState = expandedState
        } else if (Math.abs(i) >= appBarLayout.totalScrollRange) {
            if (mCurrentState != collapsedState) {
                updateState(true)
            }
            mCurrentState = collapsedState
        } else {
            if (mCurrentState != idleState) {
                updateState(false)
            }
            mCurrentState = idleState
        }
    }
}