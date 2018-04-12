package suhockii.dev.shultz.util

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.httpPost
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import suhockii.dev.shultz.Common
import suhockii.dev.shultz.R
import suhockii.dev.shultz.entity.*
import java.util.*


@SuppressLint("Registered")
abstract class MapActivity : LocationActivity(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var shultzListRequest: Request
    abstract var progressView: ProgressBar
    abstract var gpsButton: FloatingActionButton
    abstract var retryButton: ImageView
    private lateinit var shultzListUnit: () -> Unit
    private var shultzHashMap = WeakHashMap<String, Marker>()

    protected open fun setListeners() {
        gpsButton.setOnClickListener {
            getLocation {
                if (it == null) return@getLocation
                val update = CameraUpdateFactory.newLatLng(LatLng(it.latitude, it.longitude))
                googleMap.animateCamera(update)
            }
        }

        retryButton.setOnClickListener {
            retryButton.visibility = View.INVISIBLE
            shultzListUnit.invoke()
        }
    }

    protected fun initMapView(savedInstanceState: Bundle?, mapView: MapView) {
        this.mapView = mapView
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(INSTANCE_STATE_MAP_VIEW)
        }

        this.mapView = mapView
        mapView.onCreate(mapViewBundle)
        mapView.getMapAsync(this)
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        var mapViewBundle = outState.getBundle(INSTANCE_STATE_MAP_VIEW)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(INSTANCE_STATE_MAP_VIEW, mapViewBundle)
        }

        mapView.onSaveInstanceState(mapViewBundle)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        googleMap.isIndoorEnabled = true
        this.googleMap = googleMap

    }

    @SuppressLint("MissingPermission")
    protected fun onMapShow() {
        gpsButton.show()
        getLocation {
            it?.let { googleMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(it.latitude, it.longitude))) }
            googleMap.isMyLocationEnabled = true
            googleMap.uiSettings.isMyLocationButtonEnabled = false
        }
        val latLng = googleMap.cameraPosition.target
        val locationEntity = LocationEntity(latLng.latitude, latLng.longitude)
        val visibleRegion = googleMap.projection.visibleRegion
        val arrayRadius = FloatArray(1)
        Location.distanceBetween(
                visibleRegion.nearLeft.latitude,
                visibleRegion.nearLeft.longitude,
                visibleRegion.nearRight.latitude,
                visibleRegion.nearRight.longitude,
                arrayRadius)
        val filterEntity = FilterEntity(FilterData(locationEntity, arrayRadius.first()))
        shultzListUnit = {
            getShultzListByCenter(filterEntity, {
                it.forEach { shultz ->
                    if (!shultzHashMap.containsKey(shultz.id))
                        shultzHashMap[shultz.id] = googleMap.addMarker(MarkerOptions()
                                .apply { position(shultz.location.toLatLng()) })
                }

                toast(it.size)
            }, {
                toast(getString(R.string.check_internet))
                retryButton.visibility = View.VISIBLE
                retryButton.animate().alpha(1f)
            })
        }.apply { invoke() }
    }

    @SuppressLint("MissingPermission")
    protected fun onMapHide() {
        shultzListRequest.cancel()
        googleMap.isMyLocationEnabled = false
        retryButton.animate().alpha(0f).withEndAction { retryButton.visibility = View.INVISIBLE }
        progressView.animate().alpha(0f).withEndAction { progressView.visibility = View.INVISIBLE }
        gpsButton.hide()
    }

    private fun getShultzListByCenter(filterEntity: FilterEntity,
                                      onShultzListReceived: (data: List<ShultzInfoEntity>) -> Unit,
                                      onError: (fuelError: FuelError) -> Unit) {
        progressView.visibility = View.VISIBLE
        progressView.animate().alpha(1f)
        shultzListRequest = getString(R.string.url_shultz_list_bycenter).httpPost()
                .body(Common.gson.toJson(filterEntity))
                .header(mutableMapOf("auth" to Common.sharedPreferences.userToken!!, "Content-Type" to "application/json"))
                .responseObject(ShultzListEntity.Deserializer()) { _, _, result ->
                    progressView.animate().alpha(0f).withEndAction { progressView.visibility = View.INVISIBLE }
                    result.fold({ onShultzListReceived.invoke(it) }, { onError(it) })
                }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    companion object {
        const val INSTANCE_STATE_MAP_VIEW = "INSTANCE_STATE_MAP_VIEW"
        const val MARKER_USER_LOCATION = "MARKER_USER_LOCATION"
    }
}