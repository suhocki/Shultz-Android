package suhockii.dev.shultz.ui

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_retry.view.*
import kotlinx.android.synthetic.main.item_shultz.view.*
import suhockii.dev.shultz.Common
import suhockii.dev.shultz.R
import suhockii.dev.shultz.entity.BaseEntity
import suhockii.dev.shultz.entity.LoadingEntity
import suhockii.dev.shultz.entity.RetryEntity
import suhockii.dev.shultz.entity.ShultzInfoEntity

class ShultzRecyclerAdapter(var shultzList: MutableList<BaseEntity>,
                            private val onRetryClickListener: View.OnClickListener) : RecyclerView.Adapter<ShultzRecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutId = when (viewType) {
            SHULTZ_INFO_VIEW_TYPE -> R.layout.item_shultz
            RETRY_VIEW_TYPE -> R.layout.item_retry
            LOADING_VIEW_TYPE -> R.layout.item_loading
            else -> throw IllegalStateException()
        }
        val itemView = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        if (layoutId == R.layout.item_retry) itemView.fabRetry.setOnClickListener(onRetryClickListener)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entity = shultzList[position]
        if (entity is ShultzInfoEntity) {
            with(holder.itemView) {
                userName.text = entity.user
                timeInfo.text = entity.date
                val shultzIndex = entity.power - 1
                val shultzTypes = Common.shultzTypes
                shultzType.text = if (shultzIndex in 0..shultzTypes.size) shultzTypes[shultzIndex] else "n/a"
            }
        }
    }

    override fun getItemCount(): Int {
        return shultzList.size
    }

    override fun getItemViewType(position: Int): Int {
        val entity = shultzList[position]
        return when (entity) {
            is ShultzInfoEntity -> SHULTZ_INFO_VIEW_TYPE
            is LoadingEntity -> LOADING_VIEW_TYPE
            is RetryEntity -> RETRY_VIEW_TYPE
            else -> throw IllegalStateException()
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    companion object {
        private const val SHULTZ_INFO_VIEW_TYPE = 1
        private const val LOADING_VIEW_TYPE = 2
        private const val RETRY_VIEW_TYPE = 3
    }
}