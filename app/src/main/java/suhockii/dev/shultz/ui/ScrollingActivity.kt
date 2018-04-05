package suhockii.dev.shultz.ui

import android.content.Context
import android.os.Bundle
import android.os.Vibrator
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_scrolling.*
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import suhockii.dev.shultz.R
import suhockii.dev.shultz.util.addCollapsingListener
import suhockii.dev.shultz.util.animateProgressTo
import suhockii.dev.shultz.util.setInTouchListener
import suhockii.dev.shultz.util.withEndAction


class ScrollingActivity : AppCompatActivity() {

    private lateinit var progressDeferred: Deferred<Unit>
    private lateinit var shultzTypes: Array<String>
    private lateinit var vibrator: Vibrator
    private var fabStartElevation: Int = 0
    private var fabYStart: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scrolling)
        setSupportActionBar(toolbar).also { title = "" }
        shultzTypes = resources.getStringArray(R.array.shultz_types)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean = onLayoutVisible()

    private fun onLayoutVisible(): Boolean {
        fabStartElevation = resources.getDimensionPixelSize(R.dimen.fab_elevation)
        fabYStart = flShultz.y + resources.getDimensionPixelSize(R.dimen.fab_offset)
        setListeners()
        return true
    }

    private fun setListeners() {
        var currentProgressView = progressBarCircle
        var currentShultzIndex = 0

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

        appBar.addCollapsingListener { collapsed ->
            if (collapsed) {
                ivProgressBarHorizontalMask.visibility = View.VISIBLE
                currentProgressView = progressBarHorizontal
            } else {
                ivProgressBarHorizontalMask.visibility = View.INVISIBLE
                currentProgressView = progressBarCircle
            }
        }

        fabShultz.setInTouchListener({
            with(currentProgressView) {
                progress = 0
                currentShultzIndex = 0
                animate().alpha(1f)
                progressDeferred = async {
                    for (index in 1..shultzTypes.size) {
                        runOnUiThread {
                            animateProgressTo(index * 100 / shultzTypes.size)
                                    .withEndAction { if (progressDeferred.isCancelled) progress = 0 }
                        }
                        delay(progressTickDuration)
                        currentShultzIndex = index
                        if (vibrator.hasVibrator()) {
                            vibrator.vibrate(progress.toLong())
                        }
                    }
                }
            }
        }, {
            currentProgressView.animate().alpha(0f)
            progressDeferred.cancel()
        })

        fabShultz.setOnClickListener {
            Toast.makeText(this, shultzTypes[currentShultzIndex], Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val progressTickDuration = 650L
    }
}

