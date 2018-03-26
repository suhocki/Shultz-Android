package suhockii.dev.shultz.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity


class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        val clazz = if (sharedPref.getString(PREF_ID, null) == null) {
            InitActivity::class
        } else {
            ScrollingActivity::class
        }
        val intent = Intent(this, clazz.java)
        startActivity(intent)
        finish()
    }

    companion object {
        const val PREF_ID: String = "id"
    }
}