package suhockii.dev.shultz.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import suhockii.dev.shultz.Common
import suhockii.dev.shultz.util.startActivity


class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val token = Common.sharedPreferences.antonToken
        if (token == null) {
            startActivity<InitActivity>()
        } else {
            startActivity<ScrollingActivity>("antonToken" to token)
        }
        finish()
    }
}