package suhockii.dev.shultz.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import kotlinx.android.synthetic.main.activity_scrolling.*
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import suhockii.dev.shultz.R
import suhockii.dev.shultz.util.animateProgressTo
import suhockii.dev.shultz.util.setInTouchListener


class ScrollingActivity : AppCompatActivity() {

    private lateinit var progressDeferred: Deferred<Unit>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scrolling)
        setSupportActionBar(toolbar).also { title = "" }
        fabShultz.setInTouchListener({
            progressDeferred = async {
                for (newProgress in 1..5) {
                    delay(progressTickDuration)
                    runOnUiThread {
                        progressBar.animateProgressTo(newProgress * 20, progressDeferred)
                    }
                }
            }
        }, {
            progressDeferred.cancel()
            progressBar.progress = 0
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean = onLayoutVisible()

    private fun onLayoutVisible(): Boolean {
        val fabStartElevation = resources.getDimensionPixelSize(R.dimen.fab_elevation)
        val fabYStart = flShultz.y + resources.getDimensionPixelSize(R.dimen.fab_offset)
        appBar.addOnOffsetChangedListener { appBar, offset ->
            val collapsedPercent = Math.abs(offset.toFloat() / appBar.totalScrollRange)
            val delta = 1 - collapsedPercent
            val fabZFactor = if (delta > 1 / 2f) {
                1f
            } else {
                delta * delta * 2f
            }
            fabShultz.compatElevation = fabStartElevation * fabZFactor
            flShultz.y = fabYStart - (offset / 2)
        }
        return true
    }

    companion object {
        const val progressTickDuration = 650L
    }
}