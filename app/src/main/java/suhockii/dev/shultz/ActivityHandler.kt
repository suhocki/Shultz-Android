package suhockii.dev.shultz

import android.app.Activity
import android.app.Application
import android.os.Bundle

class ActivityHandler : Application.ActivityLifecycleCallbacks {
    var currentActivity: Activity? = null

    override fun onActivityResumed(activity: Activity?) {
        this.currentActivity = activity
    }

    override fun onActivityPaused(activity: Activity?) {
        this.currentActivity = null
    }

    override fun onActivityCreated(activity: Activity?, bundle: Bundle?) {}

    override fun onActivityStarted(activity: Activity?) {}

    override fun onActivityDestroyed(activity: Activity?) {}

    override fun onActivitySaveInstanceState(activity: Activity?, bundle: Bundle?) {}

    override fun onActivityStopped(activity: Activity?) {}
}