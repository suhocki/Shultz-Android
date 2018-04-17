package suhockii.dev.shultz.ui

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.coroutines.experimental.async
import suhockii.dev.shultz.Common
import suhockii.dev.shultz.R


class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        logout.setOnClickListener {
            Common.sharedPreferences.onLogout()
            async { FirebaseInstanceId.getInstance().deleteInstanceId() }
            val intent = Intent(this, AuthenticationActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        back.setOnClickListener { onBackPressed() }
    }
}