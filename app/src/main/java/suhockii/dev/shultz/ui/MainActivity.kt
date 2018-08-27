package suhockii.dev.shultz.ui

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.httpPost
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import suhockii.dev.shultz.Common
import suhockii.dev.shultz.R
import suhockii.dev.shultz.entity.*
import suhockii.dev.shultz.util.*
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : MapActivity() {
    override lateinit var progressView: ProgressBar
    override lateinit var retryButton: ImageView
    private lateinit var progressDeferred: Deferred<Unit>
    private lateinit var vibrator: Vibrator
    private lateinit var shultzListUnit: () -> Unit
    private var fabStartElevation: Float = 0f
    private var fabYStart: Float = 0f
    private var listAll = ArrayList<BaseEntity>()

    private var onNewShultz: (ShultzInfoEntity) -> Unit = {
        listAll.add(0, it)
        (recyclerView.adapter as ShultzRecyclerAdapter).submitList(listAll) {
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            if (layoutManager.findFirstCompletelyVisibleItemPosition() == 0) {
                layoutManager.scrollToPosition(0)
            }
        }
        clusterManager.addItem(it)
        runOnUiThread { clusterManager.cluster() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val contentView = layoutInflater.inflate(R.layout.activity_main, null)
        initMapView(savedInstanceState, contentView.mapView)
        setContentView(contentView)
        this.progressView = progressBar
        this.retryButton = ivRestart
        fabStartElevation = resources.getDimensionPixelSize(R.dimen.fab_elevation).toFloat()
        fabYStart = Util.getFabY(resources, VISIBLE_ITEMS_ON_START)
        val appBarMarginBottom =
            resources.getDimensionPixelSize(R.dimen.item_shultz_height) * VISIBLE_ITEMS_ON_START
        Util.setMargins(appBar, 0, 0, 0, appBarMarginBottom.toInt())
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        listAll = savedInstanceState?.getParcelableArrayList(INSTANCE_STATE_LIST_ALL) ?: listAll
        initAdapter(savedInstanceState)
        setListeners()
    }

    override fun onStart() {
        super.onStart()
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val hasActiveNotifications =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                notificationManager.activeNotifications.isNotEmpty()
            else false

        if (intent.hasExtra(EXTRA_FROM_NOTIFICATION) || hasActiveNotifications) {
            recreate()
        }
    }

    private fun initAdapter(savedInstanceState: Bundle?) {
        val adapter = ShultzRecyclerAdapter(View.OnClickListener { shultzListUnit.invoke() })
        adapter.submitList(listAll)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)

        val notAllLoaded = savedInstanceState == null ||
                !savedInstanceState.getBoolean(INSTANCE_STATE_ALL_LOADED)

        if (notAllLoaded) initPagination(adapter) else {
            if (listAll.lastOrNull() is LoadingEntity) {
                listAll.removeAt(listAll.lastIndex)
                adapter.submitList(listAll)
            }
            recyclerView.tag = PaginationState.ALL_LOADED
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val hasActiveNotifications =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                notificationManager.activeNotifications.isNotEmpty()
            else false
        if (intent.hasExtra(EXTRA_FROM_NOTIFICATION) || hasActiveNotifications) {
            notificationManager.cancelAll()
            intent.removeExtra(EXTRA_FROM_NOTIFICATION)
        } else {
            outState.putParcelableArrayList(INSTANCE_STATE_LIST_ALL, listAll)
            outState.putBoolean(
                INSTANCE_STATE_ALL_LOADED,
                recyclerView.tag == PaginationState.ALL_LOADED
            )
        }
    }

    private fun initPagination(adapter: ShultzRecyclerAdapter) {
        val pageSize = Util.getPageSize(resources)
        recyclerView.setPagination(PAGINATION_VISIBLE_THRESHOLD) { offset ->
            shultzListUnit = {
                if (listAll.lastOrNull() is RetryEntity) listAll.removeAt(listAll.lastIndex)
                listAll.add(LoadingEntity())
                adapter.submitList(listAll)

                val limit = offset + pageSize * 2
                getShultzList(offset, limit, { shultzList ->
                    Util.formatDate(this, shultzList)
                    if (shultzList.isEmpty()) {
                        recyclerView.clearOnScrollListeners()
                        recyclerView.tag = PaginationState.ALL_LOADED
                    }
                    if (listAll.lastOrNull() is LoadingEntity) listAll.removeAt(listAll.lastIndex)
                    listAll.addAll(shultzList)
                    adapter.submitList(listAll) {
                        if (recyclerView.tag != PaginationState.ALL_LOADED) recyclerView.tag =
                                PaginationState.FREE
                    }
                }, { error ->
                    onHttpError(error.response.data)
                    if (listAll.lastOrNull() is LoadingEntity) listAll.removeAt(listAll.lastIndex)
                    listAll.add(RetryEntity())
                    adapter.submitList(listAll)
                })
            }.apply { invoke() }
        }
    }

    override fun setListeners() {
        super.setListeners()

        var currentShultzIndex = 0

        appBar.addOnOffsetChangedListener { appBar, offset ->
            val collapsedPercent = Math.abs(offset.toFloat() / appBar.totalScrollRange)
            val delta = 1 - collapsedPercent
            var fabZFactor = if (delta > 1 / 2f) 1f else delta * delta * 2f
            if (fabZFactor < 0.2f) fabZFactor = 0f
            fabShultz.tag = if (fabZFactor == 0f) TouchState.UNTOUCHABLE else TouchState.TOUCHABLE
            fabShultz.compatElevation = fabStartElevation * fabZFactor
            flShultz.y = (fabYStart - (offset / 2))
            mapView.alpha = collapsedPercent * collapsedPercent
        }

        fabShultz.setInTouchListener({
            currentShultzIndex = 0
            progressBarCircle.progress = 0
            progressBarCircle.visibility = View.VISIBLE
            progressBarCircle.animate().alpha(1f)
            progressDeferred = async {
                for (index in 1..Common.shultzTypes.size) {
                    runOnUiThread {
                        progressBarCircle.animateProgressTo(index * 100 / Common.shultzTypes.size)
                            .withEndAction {
                                if (progressDeferred.isCancelled) progressBarCircle.progress = 0
                            }
                    }
                    delay(PROGRESS_TICK_DURATION / 2)
                    if (vibrator.hasVibrator()) {
                        val vibrationTime = progressBarCircle.progress.toLong()
                        if (vibrationTime > 0) vibrator.vibrate(vibrationTime)
                    }
                    delay(PROGRESS_TICK_DURATION / 2)
                    currentShultzIndex =
                            if (index in 0 until Common.shultzTypes.size) index else Common.shultzTypes.size - 1
                }
            }
        }, {
            progressBarCircle.animate().alpha(0f)
                .withEndAction { progressBarCircle.visibility = View.INVISIBLE }
            progressDeferred.cancel()
        })

        fabShultz.setOnClickListener {
            getLocation {
                val locationEntity = LocationEntity(it.latitude, it.longitude)
                val shultzEntity = ShultzEntity(currentShultzIndex + 1, locationEntity)
                getString(R.string.url_shultz).httpPost()
                    .body(Common.gson.toJson(shultzEntity))
                    .header(
                        mutableMapOf(
                            "auth" to Common.sharedPreferences.userToken!!,
                            "Content-Type" to "application/json"
                        )
                    )
                    .response { _, _, result ->
                        result.fold({
                            val currentDate = Date()
                            val date = SimpleDateFormat(
                                getString(R.string.shultz_time_format),
                                Util.getCurrentLocale(this)
                            ).format(currentDate)
                            val shultzInfoEntity = ShultzInfoEntity(
                                currentDate.time.toString(), Common.sharedPreferences.userName,
                                shultzEntity.power, date, shultzEntity.location
                            )
                            onNewShultz.invoke(shultzInfoEntity)
                        }, {
                            onHttpError(it.response.data)
                        })
                    }
            }
        }

        var appBarCollapsed = false
        var appBarWasCollapsed = false

        appBar.addCollapsingListener { appBarCollapsed = it }

        ivSettings.setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)) }

        tvMap.setOnClickListener {
            recyclerView.stopScroll()
            if (tvMap.text.toString() == getString(R.string.map)) {
                appBarWasCollapsed = appBarCollapsed
                tvMap.text = getString(R.string.list)
                appBar.setExpanded(false, true)
                ivSettings.animate().alpha(0f)
                    .withEndAction { ivSettings.visibility = View.INVISIBLE }
                recyclerView.animate()
                    .translationY(recyclerView.y)
                    .alpha(0f)
                    .withEndAction { recyclerView.visibility = View.INVISIBLE; onMapShow() }
                    .duration = 200
            } else {
                onMapHide()
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                if (layoutManager.findFirstCompletelyVisibleItemPosition() == 0 && !appBarWasCollapsed) {
                    appBar.setExpanded(true, true)
                }
                tvMap.text = getString(R.string.map)
                ivSettings.visibility = View.VISIBLE
                ivSettings.animate().alpha(1f)
                recyclerView.visibility = View.VISIBLE
                recyclerView.animate()
                    .translationY(0f)
                    .alpha(1f)
            }
        }
    }

    private fun onHttpError(data: ByteArray) {
        if (data.isNotEmpty()) {
            val serverMessage = InputStreamReader(ByteArrayInputStream(data)).readLines().first()
            toast(serverMessage)
        } else {
            toast(getString(R.string.check_internet))
        }
    }

    private fun getShultzList(
        offset: Int,
        limit: Int,
        onShultzListReceived: (data: List<ShultzInfoEntity>) -> Unit,
        onError: (fuelError: FuelError) -> Unit
    ) {
        getString(R.string.url_shultz_list).httpPost()
            .body(Common.gson.toJson(ShultzListRequest(PaginationEntity(offset, limit))))
            .header(
                mutableMapOf(
                    "auth" to Common.sharedPreferences.userToken!!,
                    "Content-Type" to "application/json"
                )
            )
            .responseObject(ShultzListEntity.Deserializer()) { _, _, result ->
                result.fold({ onShultzListReceived.invoke(it) }, { onError(it) })
            }
    }

    override fun onPushNotificationReceived(shultzInfoEntity: ShultzInfoEntity) {
        super.onPushNotificationReceived(shultzInfoEntity)
        onNewShultz.invoke(shultzInfoEntity)
        if (vibrator.hasVibrator()) vibrator.vibrate(VIBRATION_TICK_DURATION * shultzInfoEntity.power)
    }

    override fun onBackPressed() {
        if (mapOpened) tvMap.performClick()
        else super.onBackPressed()
    }

    companion object {
        const val PROGRESS_TICK_DURATION = 650L
        const val VIBRATION_TICK_DURATION = 35L
        const val PAGINATION_VISIBLE_THRESHOLD = 6
        const val VISIBLE_ITEMS_ON_START = 2f
        const val INSTANCE_STATE_LIST_ALL = "INSTANCE_STATE_LIST_ALL"
        const val INSTANCE_STATE_ALL_LOADED = "INSTANCE_STATE_ALL_LOADED"
        const val EXTRA_FROM_NOTIFICATION = "EXTRA_FROM_NOTIFICATION"
    }
}

