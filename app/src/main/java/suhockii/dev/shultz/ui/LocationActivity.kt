package suhockii.dev.shultz.ui

import android.annotation.SuppressLint
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle


@SuppressLint("Registered")
open class LocationActivity : PermissionActivity(), LocationListener {

    private lateinit var locationManager: LocationManager

    override fun onLocationChanged(p0: Location?) {

    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {

    }

    override fun onProviderEnabled(p0: String?) {

    }

    override fun onProviderDisabled(p0: String?) {

    }

}