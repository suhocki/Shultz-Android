package suhockii.dev.shultz.util

import com.google.firebase.messaging.RemoteMessage

interface PushNotificationListener {
    fun onPushNotificationReceived(remoteMessage: RemoteMessage)
}