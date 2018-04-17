package suhockii.dev.shultz.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.os.Vibrator
import android.support.v4.app.NotificationCompat
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

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
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
    }

    private fun sendNotification(shultzInfoEntity: ShultzInfoEntity) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT)

        val channelId = getString(R.string.default_notification_channel_id)
        val soundUri = Uri.parse("android.resource://$packageName/raw/rec_1s")
        val vibrationPattern = LongArray(1, { (shultzInfoEntity.power * 200).toLong() })
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_stat_ic_notification)
                .setContentTitle(shultzInfoEntity.user)
                .setContentText(Util.getShultzType(shultzInfoEntity.power))
                .setAutoCancel(true)
                .setLights(Color.GREEN, 500, 500)
                .setVibrate(vibrationPattern)
                .setSound(soundUri)
                .setContentIntent(pendingIntent)
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate((shultzInfoEntity.power * 200).toLong())

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,
                    getString(R.string.default_notification_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT)
            val att = AudioAttributes.Builder().build()
            channel.setSound(soundUri, att)
            channel.enableVibration(true)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(shultzInfoEntity.id.hashCode(), notificationBuilder.build())
    }

    companion object {
        private const val KEY_LOCATION = "location"
    }
}