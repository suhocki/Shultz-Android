package suhockii.dev.shultz.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Parcelable
import android.widget.Toast
import java.io.Serializable




fun Context.toast(message: CharSequence): Toast = Toast
        .makeText(this, message, Toast.LENGTH_SHORT)
        .apply {
            show()
        }

inline fun <reified T : Activity> Context.startActivity(vararg params: Pair<String, Any?>) =
        this.startActivity(Intent(this, T::class.java).let { intent ->
            if (params.isNotEmpty()) params.forEach {
                val value = it.second
                when (value) {
                    null -> intent.putExtra(it.first, null as Serializable?)
                    is Int -> intent.putExtra(it.first, value)
                    is Long -> intent.putExtra(it.first, value)
                    is CharSequence -> intent.putExtra(it.first, value)
                    is String -> intent.putExtra(it.first, value)
                    is Float -> intent.putExtra(it.first, value)
                    is Double -> intent.putExtra(it.first, value)
                    is Char -> intent.putExtra(it.first, value)
                    is Short -> intent.putExtra(it.first, value)
                    is Boolean -> intent.putExtra(it.first, value)
                    is Serializable -> intent.putExtra(it.first, value)
                    is Bundle -> intent.putExtra(it.first, value)
                    is Parcelable -> intent.putExtra(it.first, value)
                    is Array<*> -> when {
                        value.isArrayOf<CharSequence>() -> intent.putExtra(it.first, value)
                        value.isArrayOf<String>() -> intent.putExtra(it.first, value)
                        value.isArrayOf<Parcelable>() -> intent.putExtra(it.first, value)
                        else -> throw Exception("Intent extra ${it.first} has wrong type ${value.javaClass.name}")
                    }
                    is IntArray -> intent.putExtra(it.first, value)
                    is LongArray -> intent.putExtra(it.first, value)
                    is FloatArray -> intent.putExtra(it.first, value)
                    is DoubleArray -> intent.putExtra(it.first, value)
                    is CharArray -> intent.putExtra(it.first, value)
                    is ShortArray -> intent.putExtra(it.first, value)
                    is BooleanArray -> intent.putExtra(it.first, value)
                    else -> throw Exception("Intent extra ${it.first} has wrong type ${value.javaClass.name}")
                }
                return@forEach
            }
            return@let intent
        })

inline fun Context.isInternetConnected(): Boolean {
    val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val netInfo = cm.activeNetworkInfo
    return netInfo != null && netInfo.isConnectedOrConnecting
}