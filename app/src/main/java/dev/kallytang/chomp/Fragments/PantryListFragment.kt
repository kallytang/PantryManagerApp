package dev.kallytang.chomp.Fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dev.kallytang.chomp.AddFoodItemActivity
import dev.kallytang.chomp.MyApplication
import dev.kallytang.chomp.R
import dev.kallytang.chomp.models.Unit
import dev.kallytang.chomp.models.User
import kotlinx.android.synthetic.main.fragment_pantry_list.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

/**
 * A simple [Fragment] subclass.
 * Use the [PantryListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PantryListFragment : Fragment() {
    private val db = Firebase.firestore
    private var unitsList = mutableListOf<Unit>()
    private var unitsStrings = mutableListOf<String>()
    private var storageLocationList = ArrayList<String>()
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        auth = Firebase.auth
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_pantry_list, container, false)
        val fab: View = view.findViewById(R.id.fab_add_pantry_item)
        fab.setOnClickListener {
            val intent = Intent(context, AddFoodItemActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    private fun getUnitData(){
        val applicationContext = context
        unitsList = ((applicationContext as MyApplication).unitList as MutableList<Unit>?)!!
        unitsStrings = (applicationContext as MyApplication).unitsAsString!!

        // check if pantry list already initialized, if not, set the data
        if ((applicationContext as MyApplication).storageLocationList == null){
            db.collection("users").document(auth.currentUser?.uid.toString()).get()
                .addOnSuccessListener { doc ->
                    (applicationContext as MyApplication).currUser = doc.toObject(User::class.java)

                    val pantryRef: DocumentReference? = (applicationContext as MyApplication).currUser?.myPantry
                        pantryRef?.get()
                        ?.addOnSuccessListener { pantryDoc ->
                            val location:Map<String, String> = pantryDoc.get("storage_locations") as Map<String, String>
                            val listLocation = ArrayList(location.keys)
                            listLocation.sort()
                            (applicationContext as MyApplication).storageLocationList?.clear()
                            (applicationContext as MyApplication).storageLocationList?.addAll(listLocation)
                            storageLocationList = listLocation
                            (applicationContext as MyApplication).pantryRef = pantryRef

                        }
                }
            // if the list exists in the application context
        }else{
            storageLocationList = (applicationContext as MyApplication).storageLocationList!!

        }

    }
}