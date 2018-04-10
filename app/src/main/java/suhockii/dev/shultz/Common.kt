package suhockii.dev.shultz

import com.google.gson.Gson

object Common {
    val sharedPreferences: SharedPreferences by lazy {
        App.sharedPreferences
    }

    val activityHandler: ActivityHandler by lazy {
        App.activityHandler
    }

    val shultzTypes: Array<String> by lazy {
        App.shultzTypes
    }

    val gson = Gson()
}