package suhockii.dev.shultz

import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.github.kittinunf.fuel.core.FuelManager

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        initActivityHandler()
        initSharedPreferences()
        initFuel()
        setNotificationChannel()
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
            notificationManager!!.createNotificationChannel(NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_DEFAULT))
        }
    }

    companion object {
        var sharedPreferences: SharedPreferences? = null

        @SuppressLint("StaticFieldLeak")
        var activityHandler: ActivityHandler? = null
    }
}