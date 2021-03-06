package suhockii.dev.shultz

import android.content.Context
import android.content.SharedPreferences

class SharedPreferences(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
            context.getString(R.string.shared_preferences_file_name), Context.MODE_PRIVATE)

    var pushToken: String?
        get() = sharedPreferences.getString(PREFERENCE_FIREBASE_ID, null)
        set(value) = sharedPreferences.edit().putString(PREFERENCE_FIREBASE_ID, value).apply()

    var userToken: String?
        get() = sharedPreferences.getString(PREFERENCE_TOKEN, null)
        set(value) = sharedPreferences.edit().putString(PREFERENCE_TOKEN, value).apply()

    var userName: String
        get() = sharedPreferences.getString(PREFERENCE_USER_NAME, "")
        set(value) = sharedPreferences.edit().putString(PREFERENCE_USER_NAME, value).apply()

    fun onLogout() = sharedPreferences.edit().clear().apply()

    companion object {
        const val PREFERENCE_FIREBASE_ID = "PREFERENCE_FIREBASE_ID"
        const val PREFERENCE_TOKEN = "PREFERENCE_TOKEN"
        const val PREFERENCE_USER_NAME = "PREFERENCE_USER_NAME"
    }
}