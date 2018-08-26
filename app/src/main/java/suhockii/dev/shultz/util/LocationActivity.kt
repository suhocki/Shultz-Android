package suhockii.dev.shultz.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.experimental.Deferred
import suhockii.dev.shultz.R


@SuppressLint("Registered")
open class LocationActivity : PermissionActivity() {

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
        requestPermission(Manifest.permission.ACCESS_FINE_LOCATION) {
            requestPermission(Manifest.permission.ACCESS_COARSE_LOCATION) {
                requestGpsModule({
                    val client = FusedLocationProviderClient(this)
                    val location = client.lastLocation
                    location.addOnCompleteListener {
                        if (it.result != null) onLocationReceived.invoke(it.result)
                    }
                    location.addOnFailureListener {
                        toast(getString(R.string.gps_module_is_off))
                    }
                }, {
                    toast(getString(R.string.gps_module_is_off))
                })
            }
        }
    }

    fun setActivityResultListener(onActivityResult: (Int, Int) -> Unit) {
        this.onActivityResult = onActivityResult
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        onActivityResult?.invoke(requestCode, resultCode)
    }
}