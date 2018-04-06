package suhockii.dev.shultz.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import com.google.android.gms.common.api.GoogleApiClient
import suhockii.dev.shultz.R


@SuppressLint("Registered")
open class LocationActivity : PermissionActivity(), LocationListener {

    internal lateinit var locationManager: LocationManager
    internal lateinit var googleApiClient: GoogleApiClient
    private var onLocationReceived: ((Location) -> Unit)? = null
    private var onActivityResult: ((Int, Int) -> Unit)? = null
    private var criteria: Criteria = Criteria()

    init {
        criteria.accuracy = Criteria.ACCURACY_FINE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    @SuppressLint("MissingPermission")
    protected fun getLocation(onLocationReceived: (Location) -> Unit) {
        this.onLocationReceived = onLocationReceived
        requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, {
            requestGpsModule({
                locationManager.requestSingleUpdate(criteria, this, null)
            }, {
                toast(getString(R.string.gps_module_is_off))
            })
        }, {
            toast(getString(R.string.location_permission_disabled))
        })
    }

    override fun onLocationChanged(location: Location?) {
        location?.let { onLocationReceived?.invoke(it) }
    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {}

    override fun onProviderEnabled(p0: String?) {}

    override fun onProviderDisabled(p0: String?) {}

    fun setActivityResultListener(onActivityResult: (Int, Int) -> Unit) {
        this.onActivityResult = onActivityResult
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        onActivityResult?.invoke(requestCode, resultCode)
    }
}