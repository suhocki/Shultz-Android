package suhockii.dev.shultz.ui

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import suhockii.dev.shultz.Common


class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val token = Common.sharedPreferences.userToken
        val clazz = if (token == null) AuthenticationActivity::class.java else MainActivity::class.java
        startActivity(Intent(this, clazz))
        finish()
    }
}