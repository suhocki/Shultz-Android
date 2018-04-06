package suhockii.dev.shultz.util

import android.annotation.SuppressLint
import android.support.v7.app.AppCompatActivity

@SuppressLint("Registered")
open class PermissionActivity : AppCompatActivity() {

    private var onPermissionResult: ((Int, IntArray) -> Unit)? = null

    fun setPermissionsResultListener(onPermissionResult: (Int, IntArray) -> Unit) {
        this.onPermissionResult = onPermissionResult
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onPermissionResult?.invoke(requestCode, grantResults)
    }
}