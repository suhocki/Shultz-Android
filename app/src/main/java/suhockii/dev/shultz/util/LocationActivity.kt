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
import android.os.SystemClock
import com.google.android.gms.common.api.GoogleApiClient
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import suhockii.dev.shultz.R


@SuppressLint("Registered")
open class LocationActivity : PermissionActivity(), LocationListener {

    internal lateinit var locationManager: LocationManager
    internal lateinit var googleApiClient: GoogleApiClient
    private lateinit var locationTimeoutDeferred: Deferred<Unit>
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
    protected fun getLocation(onLocationReceived: (Location?) -> Unit) {
        this.onLocationReceived = onLocationReceived
        requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, {
            requestGpsModule({
                val lastGpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                val lastNetworkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                val gpsElapsedTime = lastGpsLocation?.elapsedRealtimeNanos ?: 0L
                val networkElapsedTime = lastNetworkLocation?.elapsedRealtimeNanos ?: 0L
                val maxElapsedTime = Math.max(gpsElapsedTime, networkElapsedTime)
                val timeDelta = SystemClock.elapsedRealtimeNanos() - maxElapsedTime
                var lastBestLocation = if (maxElapsedTime == gpsElapsedTime) lastGpsLocation else lastNetworkLocation
                if (maxElapsedTime != 0L && timeDelta < MAX_NANOS_OF_LAST_LOCATION) {
                    onLocationReceived.invoke(lastBestLocation)
                    return@requestGpsModule
                }
                locationTimeoutDeferred = async {
                    delay(LOCATION_TIMEOUT)
                    if (timeDelta > CRITICAL_NANOS_OF_LAST_LOCATION) lastBestLocation = null
                    locationManager.removeUpdates(this@LocationActivity)
                    toast(getString(R.string.gps_timeout))
                    runOnUiThread { onLocationReceived.invoke(lastBestLocation) }
                }
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                    locationManager.requestSingleUpdate(criteria, this, null)
                if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
                    locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this, null)
            }, {
                toast(getString(R.string.gps_module_is_off))
            })
        }, {
            toast(getString(R.string.location_permission_disabled))
        })
    }

    override fun onLocationChanged(location: Location?) {
        locationTimeoutDeferred.cancel()
        location?.let { onLocationReceived?.invoke(it) }
        locationManager.removeUpdates(this)
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

    companion object {
        private const val MAX_NANOS_OF_LAST_LOCATION = 60000000000
        private const val CRITICAL_NANOS_OF_LAST_LOCATION = 60000000000 * 30
        private const val LOCATION_TIMEOUT = 10000
    }
}