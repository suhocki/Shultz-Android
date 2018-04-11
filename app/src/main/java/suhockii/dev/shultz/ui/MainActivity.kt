package suhockii.dev.shultz.ui

import android.content.Context
import android.os.Bundle
import android.os.Vibrator
import android.support.v7.util.DiffUtil
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.LinearLayout
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.httpPost
import kotlinx.android.synthetic.main.activity_main.*
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


class MainActivity : LocationActivity(), PushNotificationListener {

    private lateinit var progressDeferred: Deferred<Unit>
    private lateinit var vibrator: Vibrator
    private lateinit var shultzListUnit: () -> Unit
    private lateinit var onNewShultz: (ShultzInfoEntity) -> Unit
    private var fabStartElevation: Float = 0f
    private var fabYStart: Float = 0f
    private var tvMapXStart: Float = 0f
    private val listAll = mutableListOf<BaseEntity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val contentView = layoutInflater.inflate(R.layout.activity_main, null)
        contentView.onViewShown {
            tvMapXStart = tvMap.x
            setListeners()
        }
        fabStartElevation = resources.getDimensionPixelSize(R.dimen.fab_elevation).toFloat()
        fabYStart = Util.getFabY(resources, VISIBLE_ITEMS_ON_START)
        setContentView(contentView)
        val appBarMarginBottom = resources.getDimensionPixelSize(R.dimen.item_shultz_height) * VISIBLE_ITEMS_ON_START
        Util.setMargins(appBar, 0, 0, 0, appBarMarginBottom)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        initShultzRecycler()
    }

    private fun setListeners() {
        var currentShultzIndex = 0
        val tvMapFlyDistance = resources.displayMetrics.widthPixels / 2 - tvMap.layout.width

        appBar.addOnOffsetChangedListener { appBar, offset ->
            val collapsedPercent = Math.abs(offset.toFloat() / appBar.totalScrollRange)
            val delta = 1 - collapsedPercent
            var fabZFactor = if (delta > 1 / 2f) 1f else delta * delta * 2f
            if (fabZFactor < 0.2f) fabZFactor = 0f
            fabShultz.tag = if (fabZFactor == 0f) TouchState.UNTOUCHABLE else TouchState.TOUCHABLE
            fabShultz.compatElevation = fabStartElevation * fabZFactor
            flShultz.y = (fabYStart - (offset / 2))
            tvMap.x = tvMapXStart + tvMapFlyDistance * (1 - delta * delta)
        }

        fabShultz.setInTouchListener({
            currentShultzIndex = 0
            progressBarCircle.progress = 0
            progressBarCircle.animate().alpha(1f)
            progressDeferred = async {
                for (index in 1..Common.shultzTypes.size) {
                    runOnUiThread {
                        progressBarCircle.animateProgressTo(index * 100 / Common.shultzTypes.size)
                                .withEndAction { if (progressDeferred.isCancelled) progressBarCircle.progress = 0 }
                    }
                    delay(PROGRESS_TICK_DURATION)
                    currentShultzIndex = if (index in 0 until Common.shultzTypes.size) index else Common.shultzTypes.size - 1
                    if (vibrator.hasVibrator()) vibrator.vibrate(progressBarCircle.progress.toLong())
                }
            }
        }, {
            progressBarCircle.animate().alpha(0f)
            progressDeferred.cancel()
        })

        fabShultz.setOnClickListener {
            getLocation {
                val locationEntity = LocationEntity(it.latitude, it.longitude)
                val shultzEntity = ShultzEntity(currentShultzIndex + 1, locationEntity)
                getString(R.string.url_shultz).httpPost()
                        .body(Common.gson.toJson(shultzEntity))
                        .header(mutableMapOf("auth" to Common.sharedPreferences.userToken!!, "Content-Type" to "application/json"))
                        .response { _, _, result ->
                            result.fold({
                                val currentDate = Date()
                                val date = SimpleDateFormat(getString(R.string.shultz_time_format), Util.getCurrentLocale(this)).format(currentDate)
                                val shultzInfoEntity = ShultzInfoEntity(currentDate.time.toString(), Common.sharedPreferences.userName,
                                        shultzEntity.power, date, shultzEntity.location)
                                onNewShultz.invoke(shultzInfoEntity)
                            }, {
                                onHttpError(it.response.data)
                            })
                        }
            }
        }
    }

    private fun initShultzRecycler() {
        val pageSize = Util.getPageSize(resources)
        val adapter = ShultzRecyclerAdapter(
                listAll,
                View.OnClickListener { shultzListUnit.invoke() })

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)

        recyclerView.setPagination(PAGINATION_VISIBLE_THRESHOLD, { offset ->
            shultzListUnit = {
                val listWithLoading = mutableListOf<BaseEntity>()
                listWithLoading.addAll(listAll)
                if (listAll.lastOrNull() is RetryEntity) listAll.removeAt(listAll.lastIndex)
                listAll.add(LoadingEntity())
                animateRecyclerContentDiff(recyclerView, listWithLoading, listAll, {})

                getShultzList(offset, recyclerView.adapter.itemCount + pageSize * 2, { shultzList ->
                    Util.formatDate(this, shultzList)
                    if (shultzList.isEmpty()) recyclerView.clearOnScrollListeners()
                    val listWithShultz = mutableListOf<BaseEntity>()
                    listWithShultz.addAll(listAll)
                    if (listAll.lastOrNull() is LoadingEntity) listAll.removeAt(listAll.lastIndex)
                    listAll.addAll(shultzList)
                    animateRecyclerContentDiff(recyclerView, listWithShultz, listAll, { recyclerView.tag = PaginationState.FREE })
                }, { error ->
                    onHttpError(error.response.data)
                    val listWithError = mutableListOf<BaseEntity>()
                    listWithError.addAll(listAll)
                    if (listAll.lastOrNull() is LoadingEntity) listAll.removeAt(listAll.lastIndex)
                    listAll.add(RetryEntity())
                    animateRecyclerContentDiff(recyclerView, listWithError, listAll, {})
                })
            }
            shultzListUnit.invoke()
        })

        onNewShultz = {
            val oldList = mutableListOf<BaseEntity>().apply { addAll(listAll) }
            listAll.add(0, it)
            animateRecyclerContentDiff(recyclerView, oldList, listAll, {
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                if (layoutManager.findFirstCompletelyVisibleItemPosition() == 0) {
                    layoutManager.scrollToPosition(0)
                }
                recyclerView.tag = PaginationState.FREE
            })
        }
    }

    private fun animateRecyclerContentDiff(recyclerView: RecyclerView,
                                           oldList: List<BaseEntity>,
                                           newList: MutableList<BaseEntity>,
                                           endAction: () -> Unit) {
        async {
            val diffUtilCallback = ShultzDiffUtilCallback(oldList, newList)
            val diffResult = DiffUtil.calculateDiff(diffUtilCallback)
            runOnUiThread {
                diffResult.dispatchUpdatesTo(recyclerView.adapter)
                endAction.invoke()
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


    private fun getShultzList(offset: Int,
                              limit: Int,
                              onShultzListReceived: (data: List<ShultzInfoEntity>) -> Unit,
                              onError: (fuelError: FuelError) -> Unit) {
        getString(R.string.url_shultz_list).httpPost()
                .body(Common.gson.toJson(ShultzListRequest(FilterEntity(offset, limit))))
                .header(mutableMapOf("auth" to Common.sharedPreferences.userToken!!, "Content-Type" to "application/json"))
                .responseObject(ShultzListEntity.Deserializer()) { _, _, result ->
                    result.fold({ onShultzListReceived.invoke(it) }, { onError(it) })
                }
    }

    override fun onPushNotificationReceived(shultzInfoEntity: ShultzInfoEntity) {
        onNewShultz.invoke(shultzInfoEntity)
        if (vibrator.hasVibrator()) vibrator.vibrate(VIBRATION_TICK_DURATION * shultzInfoEntity.power)
    }

    companion object {
        const val PROGRESS_TICK_DURATION = 650L
        const val VIBRATION_TICK_DURATION = 35L
        const val PAGINATION_VISIBLE_THRESHOLD = 6
        const val VISIBLE_ITEMS_ON_START = 2
    }
}

