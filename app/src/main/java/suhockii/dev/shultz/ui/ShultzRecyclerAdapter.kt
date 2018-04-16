package suhockii.dev.shultz.ui

import android.support.v7.recyclerview.extensions.AsyncDifferConfig
import android.support.v7.recyclerview.extensions.AsyncListDiffer
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_retry.view.*
import kotlinx.android.synthetic.main.item_shultz.view.*
import suhockii.dev.shultz.R
import suhockii.dev.shultz.entity.BaseEntity
import suhockii.dev.shultz.entity.LoadingEntity
import suhockii.dev.shultz.entity.RetryEntity
import suhockii.dev.shultz.entity.ShultzInfoEntity
import suhockii.dev.shultz.util.AdapterListUpdateCallback
import suhockii.dev.shultz.util.Util


class ShultzRecyclerAdapter(private val onRetryClickListener: View.OnClickListener) : RecyclerView.Adapter<ShultzRecyclerAdapter.ViewHolder>() {

    private val listUpdateCallback = AdapterListUpdateCallback(this, null)

    private val differ = AsyncListDiffer(
            listUpdateCallback,
            AsyncDifferConfig.Builder<BaseEntity>(object : DiffUtil.ItemCallback<BaseEntity>() {
                override fun areItemsTheSame(oldItem: BaseEntity, newItem: BaseEntity): Boolean {
                    return oldItem.id == newItem.id
                }

                override fun areContentsTheSame(oldItem: BaseEntity, newItem: BaseEntity): Boolean {
                    return oldItem == newItem
                }

            }).build())

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
        val entity = differ.currentList[position]
        if (entity is ShultzInfoEntity) {
            with(holder.itemView) {
                userName.text = entity.user
                timeInfo.text = entity.date
                shultzType.text = Util.getShultzType(entity.power)
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (differ.currentList[position]) {
            is ShultzInfoEntity -> SHULTZ_INFO_VIEW_TYPE
            is LoadingEntity -> LOADING_VIEW_TYPE
            is RetryEntity -> RETRY_VIEW_TYPE
            else -> throw IllegalStateException()
        }
    }

    fun submitList(list: List<BaseEntity>) {
        mutableListOf<BaseEntity>().apply { addAll(list); differ.submitList(this) }
    }

    fun submitList(list: List<BaseEntity>, endAction: () -> Unit) {
        listUpdateCallback.endAction = endAction
        mutableListOf<BaseEntity>().apply {
            addAll(list)
            differ.submitList(this)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    companion object {
        private const val SHULTZ_INFO_VIEW_TYPE = 1
        private const val LOADING_VIEW_TYPE = 2
        private const val RETRY_VIEW_TYPE = 3
    }
}