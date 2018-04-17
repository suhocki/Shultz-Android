package suhockii.dev.shultz

import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.crashlytics.android.Crashlytics
import com.github.kittinunf.fuel.core.FuelManager
import io.fabric.sdk.android.Fabric


class AppRelease : App() {

    override fun onCreate() {
        initFabric()
        super.onCreate()
    }

    private fun initFabric() {
        Fabric.with(this, Crashlytics())
    }
}