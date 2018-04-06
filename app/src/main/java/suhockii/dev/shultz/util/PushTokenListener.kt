package suhockii.dev.shultz.util

interface PushTokenListener {
    fun onPushTokenRefreshed()
    fun onPushTokenRefreshFailed()
}