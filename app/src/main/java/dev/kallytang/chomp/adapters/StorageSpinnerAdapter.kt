package dev.kallytang.chomp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import dev.kallytang.chomp.R

class StorageSpinnerAdapter(var contextSpinner: Context, var resources: Int, val storageList: ArrayList<String>):ArrayAdapter<String>(contextSpinner, resources, storageList) {
    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }
    private fun createView(position: Int, convertView: View?, parent: ViewGroup): View {
        val viewItem: View
        val tvStorageName: TextView
        val storageName: String? = getItem(position)
        if(convertView == null){
            viewItem = LayoutInflater.from(context).inflate(
                R.layout.spinner_row, parent,false
            )
            tvStorageName = viewItem.findViewById(R.id.tv_spinner_item_name)
            if(storageName!=null){
                tvStorageName.setText(storageName)
            }
            return viewItem
        }else{
            tvStorageName = convertView.findViewById(R.id.tv_spinner_item_name)
            if(storageName != null){
                tvStorageName.setText(storageName)


            }
            return convertView
        }

    }
}