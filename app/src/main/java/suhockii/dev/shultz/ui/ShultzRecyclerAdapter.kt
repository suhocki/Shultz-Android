package suhockii.dev.shultz.ui

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_shultz.view.*
import suhockii.dev.shultz.Common
import suhockii.dev.shultz.R
import suhockii.dev.shultz.entity.ShultzInfoEntity

class ShultzRecyclerAdapter(private val shultzList: List<ShultzInfoEntity>) : RecyclerView.Adapter<ShultzRecyclerAdapter.ViewHolder>() {

    var loading: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutId = if (viewType == DEFAULT_VIEW_TYPE) R.layout.item_shultz else R.layout.item_loading
        val itemView = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (loading && position == itemCount - 1) return
        with(holder.itemView) {
            userName.text = shultzList[position].user
            timeInfo.text = shultzList[position].date
            val shultzIndex = shultzList[position].power - 1
            val shultzTypes = Common.shultzTypes
            shultzType.text = if (shultzIndex in 0..shultzTypes.size) shultzTypes[shultzIndex] else "n/a"
        }
    }

    override fun getItemCount(): Int {
        return if (loading) shultzList.size + LOADING_VIEW_SIZE
        else shultzList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (loading && position == itemCount - 1) LOADING_VIEW_TYPE else DEFAULT_VIEW_TYPE
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    companion object {
        private const val DEFAULT_VIEW_TYPE = 1
        private const val LOADING_VIEW_TYPE = 2
        private const val LOADING_VIEW_SIZE = 1
    }
}