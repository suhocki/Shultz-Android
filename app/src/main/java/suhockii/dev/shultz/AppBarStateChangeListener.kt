package suhockii.dev.shultz

import android.support.design.widget.AppBarLayout


class AppBarStateChangeListener(private val isAppBarExpanded: (Int) -> Unit) :
    AppBarLayout.OnOffsetChangedListener {

    private var mCurrentState = IDLE

    override fun onOffsetChanged(appBarLayout: AppBarLayout, i: Int) {
        @Suppress("CascadeIf")
        if (i <= 0.05) {
            if (mCurrentState != EXPANDED) {
                isAppBarExpanded(EXPANDED)
            }
            mCurrentState = EXPANDED
        } else if (Math.abs(i) >= appBarLayout.totalScrollRange) {
            if (mCurrentState != COLLAPSED) {
                isAppBarExpanded(COLLAPSED)
            }
            mCurrentState = COLLAPSED
        } else {
            if (mCurrentState != IDLE) {
                isAppBarExpanded(IDLE)
            }
            mCurrentState = IDLE
        }
    }

    companion object {
        const val EXPANDED = 0
        const val COLLAPSED = 1
        const val IDLE = 2
    }

}