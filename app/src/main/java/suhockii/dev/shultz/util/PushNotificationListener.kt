package suhockii.dev.shultz.util

import suhockii.dev.shultz.entity.ShultzInfoEntity

interface PushNotificationListener {
    fun onPushNotificationReceived(shultzInfoEntity: ShultzInfoEntity)
}