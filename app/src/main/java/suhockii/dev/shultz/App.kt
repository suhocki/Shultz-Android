package suhockii.dev.shultz

import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import com.github.kittinunf.fuel.core.FuelManager


open class App : Application() {

    override fun onCreate() {
        super.onCreate()
        initActivityHandler()
        initSharedPreferences()
        initFuel()
        setNotificationChannel()
        shultzTypes = resources.getStringArray(R.array.shultz_types)
    }

    private fun initActivityHandler() {
        activityHandler = ActivityHandler()
        registerActivityLifecycleCallbacks(activityHandler)
    }

    private fun initSharedPreferences() {
        sharedPreferences = SharedPreferences(this)
    }

    private fun initFuel() {
        FuelManager.instance.basePath = getString(R.string.server_path)
    }

    private fun setNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = getString(R.string.default_notification_channel_id)
            val channelName = getString(R.string.default_notification_channel_name)
            val notificationManager = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_DEFAULT)
            val att = AudioAttributes.Builder().build()
            val soundUri = Uri.parse("android.resource://$packageName/raw/rec_1s")
            channel.enableVibration(true)
            channel.setSound(soundUri, att)
            notificationManager!!.createNotificationChannel(channel)
        }
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var activityHandler: ActivityHandler
        lateinit var sharedPreferences: SharedPreferences
        lateinit var shultzTypes: Array<String>
    }
}