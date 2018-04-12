package suhockii.dev.shultz.util

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.httpPost
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import suhockii.dev.shultz.Common
import suhockii.dev.shultz.R
import suhockii.dev.shultz.entity.*


@SuppressLint("Registered")
abstract class MapActivity : LocationActivity(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    abstract var progressView: ProgressBar
    abstract var gpsButton: FloatingActionButton
    abstract var retryButton: ImageView
    private lateinit var shultzListUnit: () -> Unit
    private var shultzList = mutableListOf<ShultzInfoEntity>()

    protected open fun setListeners() {
        gpsButton.setOnClickListener {
            getLocation {
                if (it == null) { toast(getString(R.string.gps_timeout)); return@getLocation}
                val update = CameraUpdateFactory.newLatLng(LatLng(it.latitude, it.longitude))
                this.googleMap.animateCamera(update)
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
        this.googleMap = googleMap
        this.googleMap.setMinZoomPreference(15f)
        this.googleMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(40.7143528, -74.0059731)))
    }

    protected fun onMapShown() {
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
                toast(it.size)
            }, {
                retryButton.visibility = View.VISIBLE
            })
        }.apply { invoke() }
    }

    private fun getShultzListByCenter(filterEntity: FilterEntity,
                                      onShultzListReceived: (data: List<ShultzInfoEntity>) -> Unit,
                                      onError: (fuelError: FuelError) -> Unit) {
        progressView.visibility = View.VISIBLE
        getString(R.string.url_shultz_list_bycenter).httpPost()
                .body(Common.gson.toJson(filterEntity))
                .header(mutableMapOf("auth" to Common.sharedPreferences.userToken!!, "Content-Type" to "application/json"))
                .responseObject(ShultzListEntity.Deserializer()) { _, _, result ->
                    progressView.visibility = View.INVISIBLE
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
    }
}