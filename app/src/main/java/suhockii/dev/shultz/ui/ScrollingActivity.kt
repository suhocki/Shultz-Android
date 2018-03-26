package suhockii.dev.shultz.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import kotlinx.android.synthetic.main.activity_scrolling.*
import suhockii.dev.shultz.R

class ScrollingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scrolling)
        setSupportActionBar(toolbar)
        title = ""

        fabShultz.setOnClickListener {}
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean = onLayoutVisible()

    private fun onLayoutVisible(): Boolean {
        setCollapsingToolbarListener(resources.getDimensionPixelSize(R.dimen.fab_elevation))
        return true
    }

    private fun setCollapsingToolbarListener(fabStartElevation: Int) {
        val fabYStart = fabShultz.y

        appBar.addOnOffsetChangedListener { appBar, offset ->
            val collapsedPercent = Math.abs(offset.toFloat() / appBar.totalScrollRange)

            with(fabShultz) {
                compatElevation = fabStartElevation * getFabZFactor(collapsedPercent, 2f)
                y = fabYStart - (offset / 2)
            }
        }
    }

    private fun getFabZFactor(collapsedPercent: Float, startFrom: Float): Float {
        val delta = 1 - collapsedPercent

        return if (delta > 1 / startFrom) {
            1f
        } else {
            delta * delta * startFrom
        }
    }
}
