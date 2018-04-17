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
    protected fun getLocation(onLocationReceived: (Location) -> Unit) {
        this.onLocationReceived = onLocationReceived
        requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, {
            requestGpsModule({
                var (maxElapsedTime: Long, location: Location?) = Pair<Long, Location?>(0L, null)
                locationTimeoutDeferred = async {
                    delay(LOCATION_TIMEOUT)
                    locationManager.removeUpdates(this@LocationActivity)
                    if (location == null) toast(getString(R.string.gps_timeout))
                    else runOnUiThread { onLocationChanged(location!!) }
                    return@async
                }
                locationManager.allProviders.forEach {
                    if (locationManager.isProviderEnabled(it))
                        locationManager.requestLocationUpdates(it, MAX_NANOS_OF_LAST_LOCATION, 100f, this, null)
                    val lastKnownLocation = locationManager.getLastKnownLocation(it)
                    if (lastKnownLocation != null && lastKnownLocation.elapsedRealtimeNanos > maxElapsedTime) {
                        maxElapsedTime = lastKnownLocation.elapsedRealtimeNanos
                        location = lastKnownLocation
                    }
                }
                val timeDelta = SystemClock.elapsedRealtimeNanos() - maxElapsedTime
                if (maxElapsedTime != 0L && timeDelta < MAX_NANOS_OF_LAST_LOCATION) {
                    onLocationChanged(location!!)
                    return@requestGpsModule
                }
            }, {
                toast(getString(R.string.gps_module_is_off))
            })
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
        private const val MAX_NANOS_OF_LAST_LOCATION = 60000000000 * 5L
        private const val CRITICAL_NANOS_OF_LAST_LOCATION = 60000000000 * 30
        private const val LOCATION_TIMEOUT = 3500
    }
}