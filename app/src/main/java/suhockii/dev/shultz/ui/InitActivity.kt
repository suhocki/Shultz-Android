package suhockii.dev.shultz.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.transition.ChangeBounds
import android.transition.TransitionManager
import kotlinx.android.synthetic.main.activity_init.*
import suhockii.dev.shultz.R
import suhockii.dev.shultz.util.KeyboardHeightObserver
import suhockii.dev.shultz.util.KeyboardHeightProvider


class InitActivity : AppCompatActivity(), KeyboardHeightObserver {

    private lateinit var keyboardHeightProvider: KeyboardHeightProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_init)

        keyboardHeightProvider = KeyboardHeightProvider(this)

        flInit.post { keyboardHeightProvider.start() }
    }

    override fun onResume() {
        super.onResume()
        keyboardHeightProvider.setKeyboardHeightObserver(this)
    }

    override fun onPause() {
        super.onPause()
        keyboardHeightProvider.setKeyboardHeightObserver(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        keyboardHeightProvider.close()
    }

    override fun onKeyboardHeightChanged(height: Int, orientation: Int) {
        svInit.setPadding(0, 0, 0, height)
        TransitionManager.beginDelayedTransition(flInit, ChangeBounds().setDuration(200L))
    }
}