package suhockii.dev.shultz.util

import android.Manifest
import android.animation.Animator
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import android.widget.Toast
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import suhockii.dev.shultz.R


fun Context.toast(message: Any): Toast = Toast
    .makeText(this, message.toString(), Toast.LENGTH_SHORT)
    .apply {
        show()
    }

fun Activity.closeKeyboard() {
    val view = this.currentFocus
    if (view != null) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}

fun Animation.withEndAction(action: () -> Unit) {
    setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationRepeat(p0: Animation?) {}
        override fun onAnimationStart(p0: Animation?) {}
        override fun onAnimationEnd(p0: Animation?) = action.invoke()

    })
}

fun ObjectAnimator.withEndAction(action: () -> Unit) {
    this.addListener(object : Animator.AnimatorListener {
        override fun onAnimationRepeat(p0: Animator?) {}
        override fun onAnimationCancel(p0: Animator?) {}
        override fun onAnimationStart(p0: Animator?) {}
        override fun onAnimationEnd(p0: Animator?) = action.invoke()

    })
}

fun ProgressBar.animateProgressTo(progressTo: Int): ObjectAnimator {
    val animation = ObjectAnimator.ofInt(this, "progress", this.progress, progressTo)
    animation.duration = 300
    animation.interpolator = DecelerateInterpolator()
    animation.start()
    return animation
}

fun View.setInTouchListener(inTouch: () -> Unit, released: () -> Unit) {
    tag = TouchState.TOUCHABLE
    setOnTouchListener { _, motionEvent ->
        if (tag == TouchState.UNTOUCHABLE) return@setOnTouchListener true
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> inTouch.invoke()
            MotionEvent.ACTION_UP -> {
                performClick()
                released.invoke()
            }
            MotionEvent.ACTION_CANCEL -> released.invoke()
        }
        return@setOnTouchListener true
    }
}

enum class TouchState { TOUCHABLE, UNTOUCHABLE }

fun AppBarLayout.addCollapsingListener(updateState: (collapsed: Boolean) -> Unit) {
    val expandedState = 0
    val collapsedState = 1
    val idleState = 2
    var mCurrentState = idleState
    addOnOffsetChangedListener { appBarLayout, i ->
        if (i == 0) {
            if (mCurrentState != expandedState) {
                updateState(false)
            }
            mCurrentState = expandedState
        } else if (Math.abs(i) >= appBarLayout.totalScrollRange) {
            if (mCurrentState != collapsedState) {
                updateState(true)
            }
            mCurrentState = collapsedState
        } else {
            if (mCurrentState != idleState) {
                updateState(false)
            }
            mCurrentState = idleState
        }
    }
}

fun PermissionActivity.requestPermission(permissionName: String,
                                         onPermissionGranted: () -> Unit) {
    val permissionRequestCode = 101
    setPermissionsResultListener { requestCode, grantResults ->
        when (requestCode) {
            permissionRequestCode -> {
                if (grantResults.isNotEmpty() && grantResults.first() == PackageManager.PERMISSION_GRANTED)
                    onPermissionGranted.invoke()
            }
        }
    }
    val permission = ContextCompat.checkSelfPermission(this, permissionName)
    if (permission != PackageManager.PERMISSION_GRANTED) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissionName)) {
            val builder = AlertDialog.Builder(this)
            val rationaleMessage = when (permissionName) {
                Manifest.permission.ACCESS_FINE_LOCATION -> getString(R.string.rationale_location)
                else -> getString(R.string.rationale_undefined)
            }
            builder.setMessage(rationaleMessage)
                    .setTitle(getString(R.string.permission_required))
                    .setPositiveButton(getString(R.string.ok)) { _, _ ->
                        ActivityCompat.requestPermissions(this, arrayOf(permissionName), permissionRequestCode)
                    }
                    .create()
                    .show()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(permissionName), permissionRequestCode)
        }
    } else {
        onPermissionGranted.invoke()
    }
}

fun LocationActivity.requestGpsModule(onGpsEnabled: () -> Unit, onGpsDisabled: () -> Unit) {
    val requestLocation = 199
    setActivityResultListener { requestCode, resultCode ->
        if (requestCode == requestLocation && resultCode == Activity.RESULT_OK) {
            onGpsEnabled.invoke()
        } else {
            onGpsDisabled.invoke()
        }
    }
    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
        val googleApiClient = GoogleApiClient.Builder(this)
            .addApi(LocationServices.API)
            .addConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks {
                override fun onConnected(bundle: Bundle?) {}
                override fun onConnectionSuspended(i: Int) {
                    googleApiClient.connect()
                }
            })
            .addOnConnectionFailedListener { connectionResult -> toast(connectionResult.errorCode.toString()) }
            .build()
            .apply { connect() }
        val locationRequest = LocationRequest.create()
        with(locationRequest) {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 30 * 1000L
            fastestInterval = 5 * 1000L
        }
        @Suppress("DEPRECATION")
        LocationServices.SettingsApi
            .checkLocationSettings(
                googleApiClient,
                LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest)
                    .setAlwaysShow(true).build()
            )
            .setResultCallback { result1 ->
                val status = result1.status
                if (status.statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                    status.startResolutionForResult(this, requestLocation)
                } else {
                    onGpsEnabled.invoke()
                }
            }
    } else {
        onGpsEnabled.invoke()
    }
}

fun RecyclerView.setPagination(visibleThreshold: Int, onLoadMore: (offset: Int) -> Unit) {
    tag = PaginationState.FREE
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val lastVisibleItemPosition =
                (layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
            if (tag == PaginationState.FREE && lastVisibleItemPosition + visibleThreshold >= adapter.itemCount) {
                recyclerView.tag = PaginationState.BUSY
                onLoadMore.invoke(adapter.itemCount)
            }
        }
    })
    if (adapter.itemCount == 0) {
        tag = PaginationState.BUSY
        onLoadMore.invoke(adapter.itemCount)
    }
}

enum class PaginationState { BUSY, FREE, ALL_LOADED }

fun View.onViewShown(onViewShown: () -> Unit) {
    var listener: ViewTreeObserver.OnGlobalLayoutListener? = null
    listener = ViewTreeObserver.OnGlobalLayoutListener {
        this.viewTreeObserver.removeOnGlobalLayoutListener(listener)
        onViewShown.invoke()
    }
    this.viewTreeObserver.addOnGlobalLayoutListener(listener)
}

fun Any.log(message: String) {
    Log.d("ShultzLog", message)
}