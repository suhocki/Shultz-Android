package suhockii.dev.shultz.util

import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.view.View
import android.view.ViewGroup
import suhockii.dev.shultz.R
import suhockii.dev.shultz.entity.ShultzInfoEntity
import java.text.SimpleDateFormat
import java.util.*

object Util {
    fun getCurrentLocale(context: Context): Locale {
        val configuration = context.resources.configuration
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.locales.get(0)
        } else {

            configuration.locale
        }
    }

    fun getPageSize(resources: Resources): Int {
        val displayMetrics = resources.displayMetrics
        val itemHeight = resources.getDimensionPixelSize(R.dimen.item_shultz_height)
        return displayMetrics.heightPixels / itemHeight * 3 / 2
    }

    fun getFabY(resources: Resources, visibleCount: Float): Float {
        val displayMetrics = resources.displayMetrics
        val itemHeight = resources.getDimensionPixelSize(R.dimen.item_shultz_height)
        val fabHeight = resources.getDimensionPixelSize(R.dimen.fab_width)
        val containerHeight = displayMetrics.heightPixels - itemHeight * visibleCount
        return ((containerHeight - fabHeight) / 2 - resources.getDimensionPixelSize(R.dimen.fab_offset))
    }

    fun setMargins(v: View, l: Int, t: Int, r: Int, b: Int) {
        if (v.layoutParams is ViewGroup.MarginLayoutParams) {
            val p = v.layoutParams as ViewGroup.MarginLayoutParams
            p.setMargins(l, t, r, b)
            v.requestLayout()
        }
    }

    fun formatDate(context: Context, list: List<ShultzInfoEntity>) {
        val locale = getCurrentLocale(context)
        val simpleDateFormat = SimpleDateFormat(context.getString(R.string.shultz_date_format), locale)
        val currentDateString = simpleDateFormat.format(Date())
        list.forEach {
            if (it.date.isBlank()) {
                it.date = "n/a"
            } else {
                simpleDateFormat.timeZone = TimeZone.getTimeZone("GMT+0")
                simpleDateFormat.applyPattern(context.getString(R.string.date_time_format))
                val shultzTime = simpleDateFormat.parse(it.date)
                simpleDateFormat.applyPattern(context.getString(R.string.shultz_date_format))
                simpleDateFormat.timeZone = TimeZone.getDefault()
                val shultzDateString = simpleDateFormat.format(shultzTime)
                simpleDateFormat.applyPattern(context.getString(R.string.shultz_time_format))
                it.date = if (shultzDateString == currentDateString) simpleDateFormat.format(shultzTime)
                else shultzDateString.replace(".", "")
            }
        }
    }

}
