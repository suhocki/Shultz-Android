package suhockii.dev.shultz.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import com.google.android.gms.common.api.GoogleApiClient


@SuppressLint("Registered")
open class LocationActivity : PermissionActivity(), LocationListener {

    internal lateinit var locationManager: LocationManager
    internal lateinit var googleApiClient: GoogleApiClient
    private var onActivityResult: ((Int, Int) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    protected fun getLocation() {
        requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, {
            requestGpsModule({
                toast("Gps Module Turned ON")
            },{
                toast("Gps Module Turned OFF")
            })
        }, {
            toast("Location permission disabled")
        })
    }

    override fun onLocationChanged(p0: Location?) {

    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {

    }

    override fun onProviderEnabled(p0: String?) {

    }

    override fun onProviderDisabled(p0: String?) {

    }

    fun setActivityResultListener(onActivityResult: (Int, Int) -> Unit) {
        this.onActivityResult = onActivityResult
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        onActivityResult?.invoke(requestCode, resultCode)
    }
}