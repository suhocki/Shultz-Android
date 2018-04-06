package suhockii.dev.shultz.ui

import android.annotation.SuppressLint
import android.support.v7.app.AppCompatActivity

@SuppressLint("Registered")
open class PermissionActivity : AppCompatActivity() {

    private var onPermissionResultReceived: ((Int, IntArray) -> Unit)? = null

    fun setPermissionsResultListener(onPermissionResultReceived: (Int, IntArray) -> Unit) {
        this.onPermissionResultReceived = onPermissionResultReceived
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onPermissionResultReceived?.invoke(requestCode, grantResults)
    }
}