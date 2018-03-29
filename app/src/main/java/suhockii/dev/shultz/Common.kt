package suhockii.dev.shultz

object Common {
    val sharedPreferences: SharedPreferences by lazy {
        App.sharedPreferences!!
    }

    val activityHandler: ActivityHandler by lazy {
        App.activityHandler!!
    }
}