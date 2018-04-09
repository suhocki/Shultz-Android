package suhockii.dev.shultz

import android.content.Context
import android.content.SharedPreferences

class SharedPreferences (context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
            context.getString(R.string.shared_preferences_file_name), Context.MODE_PRIVATE)

    var pushToken: String?
        get() = sharedPreferences.getString(PREFERENCE_FIREBASE_ID, null)
        set(value) = sharedPreferences.edit().putString(PREFERENCE_FIREBASE_ID, value).apply()

    var userToken: String?
        get() = sharedPreferences.getString(PREFERENCE_TOKEN, null)
        set(value) = sharedPreferences.edit().putString(PREFERENCE_TOKEN, value).apply()

    var requestMeizuPushNotifications: Boolean
        get() = sharedPreferences.getBoolean(PREFERENCE_MEIZU_PUSH, true)
        set(value) = sharedPreferences.edit().putBoolean(PREFERENCE_MEIZU_PUSH, value).apply()

    companion object {
        const val PREFERENCE_FIREBASE_ID = "PREFERENCE_FIREBASE_ID"
        const val PREFERENCE_TOKEN = "PREFERENCE_TOKEN"
        const val PREFERENCE_MEIZU_PUSH= "PREFERENCE_MEIZU_PUSH"
    }
}