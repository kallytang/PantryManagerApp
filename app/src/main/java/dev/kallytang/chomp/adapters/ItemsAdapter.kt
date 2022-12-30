package dev.kallytang.chomp.adapters

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import dev.kallytang.chomp.EditItemActivity
import dev.kallytang.chomp.R
import dev.kallytang.chomp.models.Item
import dev.kallytang.chomp.models.Unit
import dev.kallytang.chomp.models.User
import kotlinx.android.synthetic.main.item_food_list.view.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class ItemsAdapter(val context: Context, val items: ArrayList<Item>) :
    RecyclerView.Adapter<ItemsAdapter.ViewHolder>() {
    private val db = Firebase.firestore
    private lateinit var auth: FirebaseAuth
    private val stringPatternEditText = "MMM d, yyyy"

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindView(item: Item) {
            auth = Firebase.auth
            itemView.tv_title_item.text = item.name
            itemView.iv_red_dot.visibility = View.INVISIBLE
            // check expiry date
            //convert the expiry date to a local date time


            val simpleDateFormatter = SimpleDateFormat(stringPatternEditText, Locale.getDefault())
            val dateFormatted = simpleDateFormatter.format(item.expiryDate?.toDate())
            val date = simpleDateFormatter.parse(dateFormatted)
            val currDateFormatted = simpleDateFormatter.format(Date())
            val currDate = simpleDateFormatter.parse(currDateFormatted)
            itemView.item_delete_btn.visibility = View.GONE

            var timeDiff = date?.time?.minus(currDate.time)
            //calculate the time difference
            var days: Double? = null
            if (timeDiff != null) {
                days = TimeUnit.DAYS.convert(timeDiff, TimeUnit.MILLISECONDS).toDouble()
            }


            //check if item is expired or gone bad
            if (days != null) {
                if (days <= 0) {
                    itemView.tv_status_of_food.visibility = View.VISIBLE
                    itemView.iv_red_dot.visibility = View.VISIBLE
                    itemView.iv_red_dot.setImageResource(R.drawable.ic_red_dot)
                    itemView.tv_status_of_food.setTextColor(Color.RED)
                    itemView.tv_status_of_food.text = "Food Expired ${dateFormatted}"
                } else if (days > 0 && days < 4) {
                    itemView.tv_status_of_food.visibility = View.VISIBLE
                    itemView.iv_red_dot.visibility = View.VISIBLE
                    itemView.iv_red_dot.setImageResource(R.drawable.ic_yellow_dot)
                    itemView.tv_status_of_food.setTextColor(Color.RED)
                    itemView.tv_status_of_food.text = "Going Bad Soon(${days.toInt()} day(s))"

                } else {
                    itemView.tv_status_of_food.visibility = View.GONE
                    itemView.iv_red_dot.visibility = View.INVISIBLE
                }
            }
            var unitName = item.units?.unitName.toString()
            if (unitName == "none") {
                itemView.tv_quantity.text = "Qty: ${item.quantity}"
            } else {
                itemView.tv_quantity.text = "Qty: ${item.quantity} ${item.units?.abbreviation}"
            }


            //check if item is expired
            itemView.setOnClickListener {
                val intent = Intent(context, EditItemActivity::class.java)
                intent.putExtra("item", item)
                var unit: Unit? = item.units
                context.startActivity(intent)

            }
            itemView.iv_more_info.setOnClickListener {
                if (itemView.item_delete_btn.visibility != View.VISIBLE) {
                    itemView.item_delete_btn.visibility = View.VISIBLE
                } else {
                    itemView.item_delete_btn.visibility = View.GONE
                }
            }
            itemView.item_delete_btn.setOnClickListener {
                deleteFromDatabase(item)
                removeAt(layoutPosition)
                itemView.item_delete_btn.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_food_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindView(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun clear() {
        items.clear()

    }

    fun addAll(list: ArrayList<Item>) {
        items.addAll(list)
        notifyDataSetChanged()
    }

    fun removeAt(position: Int) {
        items.removeAt(position);
        notifyItemRemoved(position)
        notifyDataSetChanged()

    }

    fun deleteFromDatabase(item: Item) {
        lateinit var photoRef: StorageReference
        var storage = Firebase.storage
        // delete image if it exists
        if (!item.imageUrl.isNullOrEmpty()) {
            photoRef = storage.getReferenceFromUrl(item.imageUrl.toString())
            photoRef.delete().addOnSuccessListener {
            }
        }

        db.collection("users").document(auth.currentUser?.uid.toString()).get()
            .addOnSuccessListener { snapshot ->
                var user = snapshot.toObject(User::class.java)
                var pantryRef = user?.myPantry
                pantryRef?.collection("my_pantry")?.document(item.documentId.toString())
                    ?.delete()?.addOnSuccessListener {
                    }
                    ?.addOnFailureListener { e ->
                    }

            }


    }


}
