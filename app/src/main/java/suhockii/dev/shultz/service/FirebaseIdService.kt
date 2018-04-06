package suhockii.dev.shultz.service

import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService
import suhockii.dev.shultz.Common
import suhockii.dev.shultz.util.PushTokenListener


class FirebaseIdService : FirebaseInstanceIdService() {

    override fun onTokenRefresh() {
        val token = FirebaseInstanceId.getInstance().token!!
        Common.sharedPreferences.pushToken = token
        val activityHandler = Common.activityHandler
        val currentActivity = activityHandler.currentActivity
        if (currentActivity != null && currentActivity is PushTokenListener) {
            (currentActivity as PushTokenListener).onPushTokenRefreshed()
        }
    }
}