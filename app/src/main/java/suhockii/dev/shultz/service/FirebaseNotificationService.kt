package suhockii.dev.shultz.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONObject
import suhockii.dev.shultz.Common
import suhockii.dev.shultz.R
import suhockii.dev.shultz.entity.LocationEntity
import suhockii.dev.shultz.entity.ShultzInfoEntity
import suhockii.dev.shultz.ui.MainActivity
import suhockii.dev.shultz.util.PushNotificationListener
import suhockii.dev.shultz.util.Util


class FirebaseNotificationService : FirebaseMessagingService() {

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "oncreate")
    }
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "FirebaseNotificationService" + remoteMessage.from!!)

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            val currentActivity = Common.activityHandler.currentActivity
            val location = Common.gson.fromJson(remoteMessage.data[KEY_LOCATION], LocationEntity::class.java)
            remoteMessage.data.remove(KEY_LOCATION)
            val data = JSONObject(remoteMessage.data).toString()
            val shultzInfoEntity = Common.gson.fromJson(data, ShultzInfoEntity::class.java)
            shultzInfoEntity.location = location
            if (currentActivity != null && currentActivity is PushNotificationListener) {
                val list = listOf(shultzInfoEntity)
                Util.formatDate(this, list)
                currentActivity.onPushNotificationReceived(list.first())
            } else if (currentActivity == null) {
                sendNotification(shultzInfoEntity)
            }
        }

        // FOREGROUND
        if (remoteMessage.notification != null) {

        }

    }

    private fun sendNotification(shultzInfoEntity: ShultzInfoEntity) {
        Log.d(TAG, "sendNotification")
        val shultzIndex = shultzInfoEntity.power - 1
        val shultzTypes = Common.shultzTypes

        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT)

        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_stat_ic_notification)
                .setContentTitle(shultzInfoEntity.user)
                .setContentText(if (shultzIndex in 0..shultzTypes.size) shultzTypes[shultzIndex] else "n/a")
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }

    companion object {

        private val TAG = "FirebaseNotification"
        private const val KEY_LOCATION = "location"
    }
}