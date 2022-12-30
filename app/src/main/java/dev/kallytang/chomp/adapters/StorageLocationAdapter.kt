package dev.kallytang.chomp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.kallytang.chomp.FilterItems
import dev.kallytang.chomp.R
import kotlinx.android.synthetic.main.item_storage_name.view.*


class StorageLocationAdapter(val context: Context, var locationList:ArrayList<String>, private val filter: FilterItems) :
    RecyclerView.Adapter<StorageLocationAdapter.ViewHolder>() {

    private var curr_position = 0
    inner class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        fun bindView(locationItem: String) {
            itemView.tv_location_item.text = locationItem
            itemView.setOnClickListener {
                filter.filterItems(locationItem)
                curr_position = layoutPosition
                notifyDataSetChanged()
            }
            if(curr_position == layoutPosition){
                // set a dark background
                itemView.setBackgroundResource(R.drawable.storage_tab_not_selected)
            }else{
                // set a light background
                itemView.setBackgroundResource(R.drawable.storage_tab_selected_layout)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_storage_name, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindView(locationList[position])
    }

    override fun getItemCount(): Int {
        return locationList.size
    }

    fun add(newStorageName: String) {
        locationList.add(newStorageName)

    }


}
