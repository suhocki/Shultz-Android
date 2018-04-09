package suhockii.dev.shultz.ui

import android.content.Context
import android.os.Bundle
import android.os.Vibrator
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.View
import android.widget.LinearLayout
import com.github.kittinunf.fuel.httpPost
import kotlinx.android.synthetic.main.activity_scrolling.*
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


class MainActivity : LocationActivity() {

    private lateinit var progressDeferred: Deferred<Unit>
    private lateinit var vibrator: Vibrator
    private var fabStartElevation: Int = 0
    private var fabYStart: Float = 0f
    private val shultzList = mutableListOf<ShultzInfoEntity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scrolling)
        setSupportActionBar(toolbar).also { title = "" }
        shultzTypes = resources.getStringArray(R.array.shultz_types)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        initShultzRecycler()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean = onLayoutVisible()

    private fun onLayoutVisible(): Boolean {
        fabStartElevation = resources.getDimensionPixelSize(R.dimen.fab_elevation)
        fabYStart = flShultz.y + resources.getDimensionPixelSize(R.dimen.fab_offset)
        setListeners()
        return true
    }

    private fun setListeners() {
        var currentProgressView = progressBarCircle
        var currentShultzIndex = 0

        appBar.addOnOffsetChangedListener { appBar, offset ->
            val collapsedPercent = Math.abs(offset.toFloat() / appBar.totalScrollRange)
            val delta = 1 - collapsedPercent
            val fabZFactor = if (delta > 1 / 2f) {
                1f
            } else {
                delta * delta * 2f
            }
            fabShultz.compatElevation = fabStartElevation * fabZFactor
            flShultz.y = fabYStart - (offset / 2)
        }

        appBar.addCollapsingListener { collapsed ->
            if (collapsed) {
                ivProgressBarHorizontalMask.visibility = View.VISIBLE
                currentProgressView = progressBarHorizontal
            } else {
                ivProgressBarHorizontalMask.visibility = View.INVISIBLE
                currentProgressView = progressBarCircle
            }
        }

        fabShultz.setInTouchListener({
            with(currentProgressView) {
                progress = 0
                currentShultzIndex = 0
                animate().alpha(1f)
                progressDeferred = async {
                    for (index in 1..shultzTypes.size) {
                        runOnUiThread {
                            animateProgressTo(index * 100 / shultzTypes.size)
                                    .withEndAction { if (progressDeferred.isCancelled) progress = 0 }
                        }
                        delay(progressTickDuration)
                        currentShultzIndex = if (index in 0 until shultzTypes.size) index else shultzTypes.size - 1
                        if (vibrator.hasVibrator()) {
                            @Suppress("DEPRECATION")
                            vibrator.vibrate(progress.toLong())
                        }
                    }
                }
            }
        }, {
            currentProgressView.animate().alpha(0f)
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
                                toast("Success")
                            }, {
                                val data = it.response.data
                                if (data.isNotEmpty()) {
                                    val serverMessage = InputStreamReader(ByteArrayInputStream(data)).readLines().first()
                                    toast(serverMessage)
                                } else {
                                    toast(getString(R.string.check_internet))
                                }
                            })
                        }
            }
        }
    }

    private fun initShultzRecycler() {
        val pageSize = Util.getPageSize(recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        val adapter = ShultzRecyclerAdapter(shultzList)
        recyclerView.adapter = adapter

        recyclerView.setPagination(1, { offset ->
            adapter.loading = true
            adapter.notifyDataSetChanged()
            getShultzList(offset, recyclerView.adapter.itemCount + pageSize) { shultzList ->
                if (shultzList.isEmpty()) recyclerView.clearOnScrollListeners()
                formatDate(shultzList)
                this@MainActivity.shultzList.addAll(shultzList)
                adapter.loading = false
                adapter.notifyDataSetChanged()
                recyclerView.tag = PaginationState.FREE
            }
        })
    }

    private fun formatDate(list: List<ShultzInfoEntity>) {
        val locale = Util.getCurrentLocale(this)
        val simpleDateFormat = SimpleDateFormat(getString(R.string.shultz_date_format), locale)
        val currentDateString = simpleDateFormat.format(Date())
        list.forEach {
            simpleDateFormat.timeZone = TimeZone.getTimeZone("GMT+0")
            simpleDateFormat.applyPattern(getString(R.string.date_time_format))
            val shultzTime = simpleDateFormat.parse(it.date)
            simpleDateFormat.applyPattern(getString(R.string.shultz_date_format))
            simpleDateFormat.timeZone = TimeZone.getDefault()
            val shultzDateString = simpleDateFormat.format(shultzTime)
            simpleDateFormat.applyPattern(getString(R.string.shultz_time_format))
            it.date = if (shultzDateString == currentDateString) simpleDateFormat.format(shultzTime)
            else shultzDateString.replace(".", "")
        }
    }

    private fun getShultzList(offset: Int,
                              limit: Int,
                              onShultzListReceived: (data: List<ShultzInfoEntity>) -> Unit) {
        getString(R.string.url_shultz_list).httpPost()
                .body(Common.gson.toJson(ShultzListRequest(FilterEntity(offset, limit))))
                .header(mutableMapOf("auth" to Common.sharedPreferences.userToken!!, "Content-Type" to "application/json"))
                .responseObject(ShultzListEntity.Deserializer()) { _, _, result ->
                    result.fold({ onShultzListReceived.invoke(it) }, {
                        recyclerView.tag = PaginationState.FREE
                        val data = it.response.data
                        if (data.isNotEmpty()) {
                            val serverMessage = InputStreamReader(ByteArrayInputStream(data)).readLines().first()
                            toast(serverMessage)
                            /*деловая колбаса*/
                        } else {
                            toast(getString(R.string.check_internet))
                        }
                    })
                }
    }

    companion object {
        const val progressTickDuration = 650L
        lateinit var shultzTypes: Array<String>
    }
}

