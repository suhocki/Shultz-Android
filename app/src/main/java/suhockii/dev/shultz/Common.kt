package suhockii.dev.shultz

import com.google.gson.Gson
import suhockii.dev.shultz.ui.MainActivity

object Common {
    val sharedPreferences: SharedPreferences by lazy {
        App.sharedPreferences
    }

    val activityHandler: ActivityHandler by lazy {
        App.activityHandler
    }

    val shultzTypes: Array<String> by lazy {
        MainActivity.shultzTypes
    }

    val gson = Gson()
}