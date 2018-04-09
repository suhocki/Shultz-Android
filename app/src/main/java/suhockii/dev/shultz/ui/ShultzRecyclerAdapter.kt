package suhockii.dev.shultz.ui

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_retry.view.*
import kotlinx.android.synthetic.main.item_shultz.view.*
import suhockii.dev.shultz.Common
import suhockii.dev.shultz.R
import suhockii.dev.shultz.entity.ShultzInfoEntity

class ShultzRecyclerAdapter(private val shultzList: List<ShultzInfoEntity>,
                            private val onRetryClickListener: View.OnClickListener) : RecyclerView.Adapter<ShultzRecyclerAdapter.ViewHolder>() {

    var loading: Boolean = false
    var showRetry: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutId = when (viewType) {
            RETRY_VIEW_TYPE -> R.layout.item_retry
            LOADING_VIEW_TYPE -> R.layout.item_loading
            else -> R.layout.item_shultz
        }
        val itemView = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        if (layoutId == R.layout.item_retry) itemView.fabRetry.setOnClickListener(onRetryClickListener)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val last = itemCount - 1
        if (position == last && (loading || showRetry)) return
        with(holder.itemView) {
            userName.text = shultzList[position].user
            timeInfo.text = shultzList[position].date
            val shultzIndex = shultzList[position].power - 1
            val shultzTypes = Common.shultzTypes
            shultzType.text = if (shultzIndex in 0..shultzTypes.size) shultzTypes[shultzIndex] else "n/a"
        }
    }

    override fun getItemCount(): Int {
        return when {
            loading -> shultzList.size + LOADING_VIEW_COUNT
            showRetry -> shultzList.size + RETRY_VIEW_COUNT
            else -> shultzList.size
        }
    }

    override fun getItemViewType(position: Int): Int {
        val last = itemCount - 1
        return if (loading && position == last) LOADING_VIEW_TYPE
        else if (showRetry && position == last) RETRY_VIEW_TYPE
        else DEFAULT_VIEW_TYPE
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    companion object {
        private const val DEFAULT_VIEW_TYPE = 1
        private const val LOADING_VIEW_TYPE = 2
        private const val RETRY_VIEW_TYPE = 3

        private const val LOADING_VIEW_COUNT = 1
        private const val RETRY_VIEW_COUNT = 1
    }
}