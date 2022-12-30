package dev.kallytang.chomp

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dev.kallytang.chomp.adapters.StorageSpinnerAdapter
import dev.kallytang.chomp.adapters.UnitSpinnerAdapter
import dev.kallytang.chomp.databinding.ActivityAddFoodItemBinding
import dev.kallytang.chomp.models.Item
import dev.kallytang.chomp.models.Unit
import dev.kallytang.chomp.models.User
import java.text.SimpleDateFormat
import java.util.*


class AddFoodItemActivity : AppCompatActivity() {
    private final val TAG = "AddFoodItemActivity"
    private val db = Firebase.firestore
    private lateinit var auth: FirebaseAuth
    private lateinit var datePicker: MaterialDatePicker.Builder<Long>
    private lateinit var materialDatePicker: MaterialDatePicker<Long>
    private val stringPatternEditText = "MMM d, yyyy"
    private val timestampPatternFirebase = "yyyy-MM-dd'T'HH:mm:ssXXX"
    private lateinit var unitList: ArrayList<Unit>
    private lateinit var unitSpinnerAdapter: UnitSpinnerAdapter
    private lateinit var unitChosen: Unit
    private lateinit var storageList: ArrayList<String>
    private lateinit var storageSpinnerAdapter: StorageSpinnerAdapter
    private var photoFile: Uri? = null
    private lateinit var binding: ActivityAddFoodItemBinding
    private lateinit var storageRef: StorageReference

    companion object {
        private final var REQUEST_CODE = 86

        //        private final var RESULT_OK = 90
        private final var PHOTO_CODE = 311
        private val PERMISSION_CODE_GALLERY = 46;
        private val IDENTIFIER_CODE = 2000
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddFoodItemBinding.inflate(layoutInflater)
        var view = binding.root
        setContentView(view)
        storageRef = FirebaseStorage.getInstance().getReference();
        auth = Firebase.auth


        //set up storage location
        storageList = ArrayList()
        if((applicationContext as MyApplication).storageLocationList.isNullOrEmpty()){
            (applicationContext as MyApplication).queryStorageLocations()
            getStorageLocations()

        }else{
            (applicationContext as MyApplication).storageLocationList?.let { storageList.addAll(it) }
            storageSpinnerAdapter = StorageSpinnerAdapter(this, R.layout.spinner_row, storageList)
            binding.addLocationSpinner.adapter = storageSpinnerAdapter
            storageSpinnerAdapter.notifyDataSetChanged()
        }
//        storageSpinnerAdapter = StorageSpinnerAdapter(this, R.layout.spinner_row, storageList)
//        binding.addLocationSpinner.adapter = storageSpinnerAdapter


        // set up calendar dialog
        datePicker = MaterialDatePicker.Builder.datePicker()
        datePicker.setTitleText("Select an Expiration Date")
        materialDatePicker = datePicker.build()

        // for opening calendar dialog
        binding.etDateExpiry.setOnClickListener {
            binding.etDateExpiry.isEnabled = false
            materialDatePicker.show(supportFragmentManager, "DATE_PICKER")
        }

        // for when user confirms date, convert date into a timestamp
        materialDatePicker.addOnPositiveButtonClickListener { date ->
            binding.etDateExpiry.setText(materialDatePicker.headerText)
            binding.etDateExpiry.isEnabled = true
        }
        materialDatePicker.addOnDismissListener {
            binding.etDateExpiry.isEnabled = true
        }
        materialDatePicker.addOnCancelListener {
            binding.etDateExpiry.isEnabled = true
        }


        binding.removeImage.setOnClickListener(){
            binding.removeImage.visibility = View.GONE
            binding.ivNewFoodImage.visibility = View.GONE
            // set photoFile to null since image was closed
            photoFile = null
        }



        // set up units spinner
        unitList = ArrayList()

        (applicationContext as MyApplication).unitList?.let { unitList.addAll(it) }

        var indexUnit = 0
        for (idx in unitList.indices) {
            if (unitList[idx].unitName == "none") {
                indexUnit = idx
            }
        }


        unitSpinnerAdapter = UnitSpinnerAdapter(this, R.layout.spinner_row, unitList)
        binding.addUnitSpinner.adapter = unitSpinnerAdapter
        binding.addUnitSpinner.setSelection(indexUnit)


        var unitChoice: Unit
        binding.addUnitSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long,
                ) {
                    unitChosen = parent?.getItemAtPosition(position) as Unit
//                var clickedUnitName:String = unitChosen.abbreviation
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            }

        // for keeping track of what user chose as storage location
        var locationChosen = "Other"
        binding.addLocationSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long,
                ) {
                    locationChosen = parent?.getItemAtPosition(position) as String
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            }

        //for closing button
        binding.ivExitAddTask.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }



        binding.fabCamera.setOnClickListener {
            var intent = Intent(this, CameraActivity::class.java)
            intent.putExtra("identifier", IDENTIFIER_CODE)
            startActivityForResult(intent, REQUEST_CODE)
        }
//        TODO animate the mini fab
//        binding.fabCamera.setOnLongClickListener {
//            binding.fabCamera.isExpanded
//
//            // move the image fab button up
//
//            return@setOnLongClickListener true
//        }

        binding.fabGetFromGallery.setOnClickListener {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                    val permission = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    requestPermissions(permission, PERMISSION_CODE_GALLERY)
                } else {
                    openPhotos()
                }
            } else {
                openPhotos()
            }
        }

        // submit button 
        binding.btnAddNewItem.setOnClickListener {
            val itemNameInput = binding.etItemName.text.toString()
            var itemBrandUnput = binding.etBrand.text.toString()
            if (itemBrandUnput.isEmpty()) {
                itemBrandUnput = ""
            }
            var itemNotesInput = binding.etFoodNotes.text.toString()
            if (itemNotesInput.isEmpty()) {
                itemNotesInput = ""
            }
            val expirationDateString = binding.etDateExpiry.text.toString()
            val quantityInput = binding.etQuantity.text.toString().toInt()
            var error: Boolean = false

            if (itemNameInput.isEmpty()) {
                binding.etItemName.setHint("Enter Item Name")
                binding.etItemName.setHintTextColor(Color.RED)
                binding.itemNameDot.visibility = View.VISIBLE
                binding.etItemName.setBackgroundResource(R.drawable.text_input_layout_red)
                error = true
            }
            // check if expiration date is not set
            if (expirationDateString.isEmpty()) {
                binding.etDateExpiry.setHint(R.string.errorExpirySelection)
                binding.etDateExpiry.setHintTextColor(Color.RED)
                binding.expirationDateDot.visibility = View.VISIBLE
                binding.etDateExpiry.setBackgroundResource(R.drawable.text_input_layout_red)
//                errorNoExpirationDate.visibility = View.VISIBLE
                error = true
            }


            if (error == true) {
                return@setOnClickListener
            } else {
                binding.progressBar.visibility = View.VISIBLE
                binding.btnAddNewItem.isEnabled = false
                // get value from the spinner
                val formatter = SimpleDateFormat(stringPatternEditText, Locale.getDefault())
                val simpleDateFormat =
                    SimpleDateFormat(timestampPatternFirebase, Locale.getDefault())

                var item: Item

                if (photoFile != null) {
                    val userID = auth.currentUser?.uid.toString()
                    val photoRef =
                        storageRef.child("images/$userID/${System.currentTimeMillis()}_photo.jpg")
                    photoRef.putFile(photoFile!!)
                        .continueWithTask { photoUploadTask ->
                            photoRef.downloadUrl

                        }.continueWith { downloadUrl ->

                            val date: Date = formatter.parse(expirationDateString)
                            val timestampExpiration = Timestamp(date)
                            var currUser: User
                            item = Item(
                                itemNameInput,
                                itemBrandUnput,
                                quantityInput,
                                unitChosen,
                                timestampExpiration,
                                locationChosen,
                                itemNotesInput,
                                downloadUrl.result.toString(),
                                ""
                            )
                            val pantryReference = (applicationContext as MyApplication).pantryRef
                            pantryReference?.collection("my_pantry")?.add(item)
                        }.addOnCompleteListener {

                            binding.btnAddNewItem.isEnabled = false
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }.addOnFailureListener { e ->
                        }.addOnSuccessListener {
                        }

                } else {
                    val imageUrl = ""
                    val date: Date = formatter.parse(expirationDateString)
                    val timestampExpiration = Timestamp(date)
                    var currUser: User
                    item = Item(
                        itemNameInput,
                        itemBrandUnput,
                        quantityInput,
                        unitChosen,
                        timestampExpiration,
                        locationChosen,
                        itemNotesInput,
                        imageUrl,
                        ""

                    )
                    val pantryReference = (applicationContext as MyApplication).pantryRef
                    pantryReference?.collection("my_pantry")?.add(item)
                    binding.btnAddNewItem.isEnabled = false
                    binding.progressBar.visibility = View.GONE
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }

            }

        }
    }

    private fun openPhotos() {
//        val imageSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
////        imageSelectionIntent.type = "image/*"
////
//
//        if (imageSelectionIntent.resolveActivity(packageManager) != null) {
//            startActivityForResult(imageSelectionIntent, PHOTO_CODE)
//        }
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PHOTO_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                photoFile = data.extras?.get("photo") as Uri
                if (photoFile != null) {
                    binding.ivNewFoodImage.visibility = View.VISIBLE
                    Glide.with(this).load(photoFile).into(binding.ivNewFoodImage)
                    binding.removeImage.visibility = View.VISIBLE
                }
            }
        }
        if (requestCode == PHOTO_CODE && resultCode == RESULT_OK) {
            photoFile = data?.data!!
            if (photoFile != null) {
                binding.ivNewFoodImage.visibility = View.VISIBLE
                Glide.with(this).load(photoFile).into(binding.ivNewFoodImage)
                binding.removeImage.visibility = View.VISIBLE
            }

        }
    }

    private fun getStorageLocations() {

        db.collection("users").document(auth.currentUser?.uid.toString()).get()
            .addOnSuccessListener { snapshot ->
                var user = snapshot.toObject(User::class.java)
                if (user != null) {
                    user.myPantry?.get()?.addOnSuccessListener { pantryDoc ->
                        val location: Map<String, String> =
                            pantryDoc.get("storage_locations") as Map<String, String>
                        val listLocation = ArrayList(location.values)
                        listLocation.sort()
                        storageList.clear()
                        storageList.addAll(listLocation)
                        storageSpinnerAdapter = StorageSpinnerAdapter(this,
                            R.layout.spinner_row,
                            storageList)
                        binding.addLocationSpinner.adapter = storageSpinnerAdapter
                        storageSpinnerAdapter.notifyDataSetChanged()
                    }
                }

            }
    }
}

