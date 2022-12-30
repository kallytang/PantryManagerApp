package dev.kallytang.chomp

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dev.kallytang.chomp.models.Item
import dev.kallytang.chomp.models.Unit
import dev.kallytang.chomp.models.User
import java.util.*

class MyApplication : Application() {
    private lateinit var auth: FirebaseAuth
    var unitList: List<Unit>? = null
    private lateinit var db: FirebaseFirestore
    var itemsList: ArrayList<Item>? = null
    var storageLocationList: ArrayList<String>? = null

    var unitsAsString: MutableList<String>? = null
    var currUser: User? = null
    var pantryRef: DocumentReference? = null


    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        db = Firebase.firestore
        auth = Firebase.auth


        queryUnits()
        queryStorageLocations()

    }
    fun queryUnits(){
        db.collection("units").get().addOnSuccessListener { snapshot ->
            val dataList = snapshot.toObjects(Unit::class.java)
            unitsAsString = mutableListOf<String>()
            unitList = dataList
            for (items in unitList!!) {
                items.abbreviation?.let { unitsAsString?.add(it) }
            }


        }
    }

    fun queryStorageLocations(){
        db.collection("users").document(auth.currentUser?.uid.toString()).get()
            .addOnSuccessListener { doc ->
                currUser = doc.toObject(User::class.java)
                // get reference to pantry
                val pantryReference: DocumentReference? = currUser?.myPantry
                pantryReference?.get()
                    ?.addOnSuccessListener { pantryDoc ->
                        val location: Map<String, String> =
                            pantryDoc.get("storage_locations") as Map<String, String>

                        // set data to lists in application context and on main activity
                        storageLocationList = ArrayList(location.values)
                        storageLocationList!!.sort()
                        pantryRef = pantryReference


                    }
            }
    }
    //TODO create an async call
    fun getQueryStorageLocations(){
        lateinit var listLocation: ArrayList<String>

        db.collection("users").document(auth.currentUser?.uid.toString()).get()
            .addOnSuccessListener { doc ->
                currUser = doc.toObject(User::class.java)
                // get reference to pantry
                val pantryReference: DocumentReference? = currUser?.myPantry
                pantryReference?.get()
                    ?.addOnSuccessListener { pantryDoc ->
                        val location: Map<String, String> =
                            pantryDoc.get("storage_locations") as Map<String, String>
                        listLocation = ArrayList(location.values)
                        listLocation.sort()
                        storageLocationList?.clear()
                        storageLocationList?.addAll(listLocation)

                    }
            }

    }

    fun updatePantryReference(ref: DocumentReference){
        pantryRef = ref
    }
}

