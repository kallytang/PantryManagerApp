package dev.kallytang.chomp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import dev.kallytang.chomp.R
import dev.kallytang.chomp.models.Unit

class UnitSpinnerAdapter(var contextSpinner: Context, var resources: Int, val unitList: ArrayList<Unit>): ArrayAdapter<Unit>(contextSpinner, resources, unitList){


    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return setUpView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return setUpView(position, convertView, parent)
    }

    private fun setUpView(position: Int, convertView: View?, parent: ViewGroup):View {
        val viewItem: View
        val tvUnitName: TextView
        val unitItem: Unit? = getItem(position)
        if (convertView == null){
            viewItem = LayoutInflater.from(context).inflate(
                R.layout.spinner_row, parent, false
            )
            tvUnitName = viewItem.findViewById(R.id.tv_spinner_item_name)
            if (unitItem != null) {
                tvUnitName.setText(unitItem.abbreviation)
            }
            return viewItem
        }else{
            tvUnitName = convertView.findViewById(R.id.tv_spinner_item_name)
            if (unitItem != null) {
                tvUnitName.setText(unitItem.abbreviation)
            }
            return  convertView
        }
    }
}