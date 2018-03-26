package suhockii.dev.shultz

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.View
import kotlinx.android.synthetic.main.activity_scrolling.*

class ScrollingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scrolling)
        setSupportActionBar(toolbar)
        title = ""

        fab_shultz.setOnClickListener {}
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean = onLayoutVisible()

    private fun onLayoutVisible(): Boolean {
        setCollapsingToolbarListener()
        return true
    }

    private fun setCollapsingToolbarListener() {
        val fabYStart = fab_shultz.y
        val fabStartElevation = fab_shultz.elevation

        app_bar.addOnOffsetChangedListener { appBar, verticalOffset ->
            fab_shultz.y = fabYStart - (verticalOffset / 2)
            val collapsedPercent = Math.abs(verticalOffset.toFloat() / appBar.totalScrollRange)

            with(fab_shultz) {
                elevation = fabStartElevation * getFabDelta(collapsedPercent)
            }
        }

        app_bar.addOnOffsetChangedListener(AppBarStateChangeListener { state ->
            iv_shultz.visibility = if (state == AppBarStateChangeListener.COLLAPSED) {
                View.VISIBLE
            } else {
                View.GONE
            }
        })
    }

    private fun getFabDelta(collapsedPercent: Float): Float {
        val delta = 1 - collapsedPercent
        return if (delta > 1 / 2f) {
            1f
        } else {
            delta*delta * 2f
        }
    }
}
