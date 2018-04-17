package suhockii.dev.shultz.util

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.httpPost
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterManager
import suhockii.dev.shultz.Common
import suhockii.dev.shultz.R
import suhockii.dev.shultz.entity.*
import java.io.InterruptedIOException
import java.net.SocketTimeoutException
import java.util.*


@SuppressLint("Registered")
abstract class MapActivity : LocationActivity(), OnMapReadyCallback, PushNotificationListener {

    abstract var progressView: ProgressBar
    abstract var retryButton: ImageView
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var clusterManager: ClusterManager<ShultzInfoEntity>
    private lateinit var shultzListUnit: () -> Unit
    private var shultzListRequest: Request? = null
    private val alreadyShowedAreas = mutableListOf<Pair<LatLng, LatLng>>()
    private var shultzHashMap = WeakHashMap<String, ShultzInfoEntity>()
    private var initialLatLngZoom: CameraUpdate? = null

    protected var mapOpened: Boolean = false
        get() = googleMap.isMyLocationEnabled

    protected open fun setListeners() {
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
        googleMap.apply {
            this@MapActivity.googleMap = this
            clusterManager = ClusterManager(this@MapActivity, this)
            isIndoorEnabled = true
            uiSettings.isZoomControlsEnabled = true
            setOnMarkerClickListener(clusterManager)
            setOnCameraIdleListener(clusterManager)
            setOnCameraMoveStartedListener { shultzListRequest?.cancel() }
            setOnCameraIdleListener {
                if (mapOpened && initialLatLngZoom != null) {
                    val center = googleMap.cameraPosition.target
                    val visibleRegion = googleMap.projection.visibleRegion
                    val (newTopLeft, newBottomRight) = Pair(
                            LatLng(visibleRegion.nearLeft.latitude, visibleRegion.nearLeft.longitude),
                            LatLng(visibleRegion.farRight.latitude, visibleRegion.farRight.longitude))
                    var wasLoaded = false
                    alreadyShowedAreas.forEach { (topLeft, bottomRight) ->
                        if (newTopLeft.latitude >= topLeft.latitude && newTopLeft.longitude >= topLeft.longitude &&
                                newBottomRight.latitude <= bottomRight.latitude && newBottomRight.longitude <= bottomRight.longitude)
                            wasLoaded = true
                    }
                    if (!wasLoaded) {
                        alreadyShowedAreas.add(Pair(newTopLeft, newBottomRight))
                        requestShultzListByCenter(center)
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    protected fun onMapShow() {
        if (initialLatLngZoom == null) {
            getLocation {
                googleMap.isMyLocationEnabled = true
                initialLatLngZoom = CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 15f)
                googleMap.moveCamera(initialLatLngZoom)
            }
        } else {
            requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, { googleMap.isMyLocationEnabled = true })
        }
    }

    private fun requestShultzListByCenter(center: LatLng) {
        val locationEntity = LocationEntity(center.latitude, center.longitude)
        val arrayDiameter = FloatArray(1)
        val visibleRegion = googleMap.projection.visibleRegion
        val (topLeft, bottomRight) = Pair(
                LatLng(visibleRegion.nearLeft.latitude, visibleRegion.nearLeft.longitude),
                LatLng(visibleRegion.farRight.latitude, visibleRegion.farRight.longitude))
        Location.distanceBetween(topLeft.latitude, topLeft.longitude, bottomRight.latitude, bottomRight.longitude, arrayDiameter)
        val radius = arrayDiameter.first() / 2 / METERS_IN_KM
        val filterEntity = FilterEntity(FilterData(locationEntity, radius))
        shultzListUnit = {
            getShultzListByCenter(filterEntity, {
                log("getShultzListByCenter")
                Util.formatDate(this, it)
                it.forEach { shultz ->
                    if (!shultzHashMap.containsKey(shultz.id)) {
                        shultzHashMap[shultz.id] = shultz
                        clusterManager.addItem(shultz)
                    }
                }
                clusterManager.cluster()
            }, {
                if (mapOpened) {
                    if ((it.exception is SocketTimeoutException || it.exception !is InterruptedIOException)) {
                        toast(getString(R.string.check_internet))
                        retryButton.visibility = View.VISIBLE
                        retryButton.animate().alpha(1f)
                    }
                }
            })
        }.apply { invoke() }
    }

    @SuppressLint("MissingPermission")
    protected fun onMapHide() {
        shultzListRequest?.cancel()
        googleMap.isMyLocationEnabled = false
        retryButton.animate().alpha(0f).withEndAction { retryButton.visibility = View.INVISIBLE }
        progressView.animate().alpha(0f).withEndAction { progressView.visibility = View.INVISIBLE }
    }

    private fun getShultzListByCenter(filterEntity: FilterEntity,
                                      onShultzListReceived: (data: List<ShultzInfoEntity>) -> Unit,
                                      onError: (fuelError: FuelError) -> Unit) {
        retryButton.visibility = View.INVISIBLE
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

    override fun onPushNotificationReceived(shultzInfoEntity: ShultzInfoEntity) {
        clusterManager.addItem(shultzInfoEntity)
        runOnUiThread { clusterManager.cluster() }
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
        const val METERS_IN_KM = 1000
    }
}