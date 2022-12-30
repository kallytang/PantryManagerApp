package dev.kallytang.chomp

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import dev.kallytang.chomp.adapters.StorageSpinnerAdapter
import dev.kallytang.chomp.adapters.UnitSpinnerAdapter
import dev.kallytang.chomp.databinding.ActivityEditItemBinding
import dev.kallytang.chomp.models.Item
import dev.kallytang.chomp.models.Unit
import dev.kallytang.chomp.models.User
import kotlinx.android.synthetic.main.activity_edit_item.*
import java.text.SimpleDateFormat
import java.util.*

class EditItemActivity : AppCompatActivity() {
    private val db = Firebase.firestore
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityEditItemBinding
    private final val TAG = "EditItemActivity"
    private lateinit var unitList: ArrayList<Unit>
    private lateinit var storageList: ArrayList<String>
    private lateinit var spinnerStorageAdapter: StorageSpinnerAdapter
    private lateinit var spinnerUnitAdapter: UnitSpinnerAdapter
    private lateinit var datePicker: MaterialDatePicker.Builder<Long>
    private lateinit var materialDatePicker: MaterialDatePicker<Long>
    private lateinit var timeStampOld: Timestamp
    private lateinit var locationChosen: String

    private lateinit var unitChosen: Unit
    private var photoFile: Uri? = null
    private var imageDeleted = false
    private val stringPatternEditText = "MMM d, yyyy"
    private lateinit var storageRef: StorageReference
    private val timestampPatternFirebase = "yyyy-MM-dd'T'HH:mm:ssXXX"

    companion object {
        private final var REQUEST_CODE = 86
        private var changesMade = false

        //        private final var RESULT_OK = 90
        private final var PHOTO_CODE = 311
        private val PERMISSION_CODE_GALLERY = 46;
        private val IDENTIFIER_EDIT_ACTIVITY = 8000
    }


    //todo use concurrency alternative to threading https://kotlinlang.org/docs/native-concurrency.html
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storageRef = FirebaseStorage.getInstance().getReference();
        auth = Firebase.auth
        binding = ActivityEditItemBinding.inflate(layoutInflater)
        var view = binding.root
        setContentView(view)
        val intent: Intent = intent
        val item: Item? = intent.getParcelableExtra("item")
        //set up unitlist adapter
        unitList = ArrayList()
        (applicationContext as MyApplication).unitList?.let { unitList.addAll(it) }

        // set up the date on the form and a dialog to open calendar
        datePicker = MaterialDatePicker.Builder.datePicker()
        datePicker.setTitleText("Select an Expiration Date")
        materialDatePicker = datePicker.build()

        var indexUnit = 0
        for (idx in unitList.indices) {

            if (item != null) {
                if (item.units?.unitName == unitList[idx].unitName) {
                    unitChosen = item.units!!
                    indexUnit = idx

                }
            }
        }

        // set up drop down unit options
        spinnerUnitAdapter = UnitSpinnerAdapter(this, R.layout.spinner_row,
            unitList)

        binding.addUnitSpinner.adapter = spinnerUnitAdapter
        binding.addUnitSpinner.setSelection(indexUnit)

        // keep track of what user has chosen as unit
        binding.addUnitSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long,
                ) {
                    unitChosen = parent?.getItemAtPosition(position) as Unit
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

            }


        // Storage list drop down
        storageList = ArrayList()
        (applicationContext as MyApplication).storageLocationList?.let { storageList.addAll(it) }

        var indexStorage = 0
        for (idx in storageList.indices) {

            if (item?.location == storageList[idx]) {
                indexStorage = idx
                locationChosen = item.location!!

            }
        }
        spinnerStorageAdapter = StorageSpinnerAdapter(this, R.layout.spinner_row, storageList)

        binding.addLocationSpinner.adapter = spinnerStorageAdapter
        binding.addLocationSpinner.setSelection(indexStorage)

        // keep track of what user has chosen as storage location
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

        // add timestamp to document and an x to image if there's an image
        if (item != null) {
            timeStampOld = item.expiryDate!!
            val simpleDateFormatter = SimpleDateFormat(stringPatternEditText, Locale.getDefault())
            val dateForForm = simpleDateFormatter.format(timeStampOld.toDate())
            binding.etDateExpiry.text = dateForForm
            binding.etQuantity.setText(item.quantity.toString(), TextView.BufferType.EDITABLE)
            binding.etBrand.setText(item.brand,  TextView.BufferType.EDITABLE)

        }
        binding.ivRemoveImage.setOnClickListener {
            binding.ivRemoveImage.visibility = View.GONE
            binding.ivFoodPhoto.visibility = View.GONE
            // set photoFile to null since image was closed
            photoFile = null
            imageDeleted = true
        }


        //expiration date picking
        binding.etDateExpiry.setOnClickListener {
            binding.etDateExpiry.isEnabled = false
            materialDatePicker.show(supportFragmentManager, "DATE_PICKER")
        }
        // create date picker
        // TODO add date to calendar picker
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

        //edit item name
        binding.editNamePencil.setOnClickListener {
            binding.editNamePencil.isEnabled = false
            binding.tvItemTitle.visibility = View.VISIBLE
            binding.etItemName.setText(binding.tvInfoItemName.text.toString())
            binding.etItemName.visibility = View.VISIBLE
            binding.tvInfoItemName.visibility = View.INVISIBLE
            binding.editNamePencil.visibility = View.INVISIBLE
            binding.editNamePencil.isEnabled = true
        }
        // for editing name of item
        binding.etItemName.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                binding.tvItemTitle.visibility = View.INVISIBLE
                binding.etItemName.visibility = View.INVISIBLE
                binding.tvInfoItemName.setText(binding.etItemName.text.toString())
                binding.tvInfoItemName.visibility = View.VISIBLE
                binding.editNamePencil.visibility = View.VISIBLE

            }

        }
        binding.etItemName.setOnKeyListener { v, keyCode, event ->

            when {
                ((keyCode == KeyEvent.KEYCODE_ENTER) && (event.action == KeyEvent.ACTION_DOWN)) -> {

                    binding.tvItemTitle.visibility = View.INVISIBLE
                    binding.etItemName.visibility = View.INVISIBLE
                    binding.tvInfoItemName.setText(binding.etItemName.text.toString())
                    binding.tvInfoItemName.visibility = View.VISIBLE
                    binding.editNamePencil.visibility = View.VISIBLE

                    return@setOnKeyListener true
                }
                else -> false
            }
        }
        binding.fabCamera.setOnClickListener {
            var intent = Intent(this, CameraActivity::class.java)
            intent.putExtra("identifier", 30)
            startActivityForResult(intent, EditItemActivity.REQUEST_CODE)
        }


        binding.fabGetFromGallery.setOnClickListener {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                    val permission = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    requestPermissions(permission, EditItemActivity.PERMISSION_CODE_GALLERY)
                } else {
                    openPhotos()
                }
            } else {
                openPhotos()
            }
        }

        // for handling updates to task item
        binding.btnUpdate.setOnClickListener {
            binding.btnUpdate.isEnabled = false
            var itemNameInput = binding.tvInfoItemName.text.toString()
            if(binding.etItemName.visibility == View.VISIBLE){
                itemNameInput = binding.etItemName.text.toString()
            }
            var itemBrandUnput = binding.etBrand.text.toString()
            if (itemBrandUnput.isEmpty()) {
                itemBrandUnput = ""
            }
            var itemNotesInput = binding.etFoodNotes.text.toString()
            if (itemNotesInput.isEmpty()) {
                itemNotesInput = ""
            }
            val expirationDateString = binding.etDateExpiry.text.toString()
            val formatter = SimpleDateFormat(stringPatternEditText, Locale.getDefault())
            val quantityInput = binding.etQuantity.text.toString().toInt()
            var error: Boolean = false

            if (itemNameInput.isEmpty()) {
                binding.etItemName.setHint("Enter Item Name")
                binding.etItemName.setHintTextColor(Color.RED)
                binding.itemNameDot.visibility = View.VISIBLE
                binding.etItemName.setBackgroundResource(R.drawable.text_input_layout_red)
                error = true
            }
            if (error) {
                binding.btnUpdate.isEnabled = true
                return@setOnClickListener
            } else {
                binding.progressBar.visibility = View.VISIBLE
                val date: Date = formatter.parse(expirationDateString)
                if (item != null) {
                    if (item.expiryDate.toString() != Timestamp(date).toString()) {
                        item.expiryDate = Timestamp(date)
                        changesMade = true
                    }
                    if (item.name != itemNameInput) {
                        item.name = itemNameInput
                        changesMade = true
                    }
                    if (item.quantity != quantityInput) {
                        item.quantity = quantityInput
                        changesMade = true
                    }
                    if (item.notes != itemNotesInput) {
                        item.notes = itemNotesInput
                        changesMade = true
                    }
                    if (item.brand != itemBrandUnput) {
                        item.brand = itemBrandUnput
                        changesMade = true
                    }
                    if (item.location != locationChosen) {
                        item.location = locationChosen
                        changesMade = true
                    }
                    if (item.units?.unitName != unitChosen.unitName) {
                        item.units = unitChosen
                        changesMade = true
                    }
                    if (imageDeleted) {
                        lateinit var photoRef: StorageReference
                        var storage = Firebase.storage
                        // delete image if it exists
                        if (!item?.imageUrl.isNullOrEmpty()) {
                            photoRef = storage.getReferenceFromUrl(item?.imageUrl.toString())
                            photoRef.delete().addOnSuccessListener {
                            }
                        }
                        item.imageUrl = ""
                        changesMade = true

                    }

                    if (changesMade == true) {
                        binding.progressBar.visibility = View.VISIBLE
                        var userID = auth.currentUser?.uid.toString()
                        if (photoFile != null) {
                            updateItemWithPhoto(userID, item)
                        } else {
                            // if there's no photofile uploaded
                            updateItem(userID, item)
                        }
                    }else{
                        binding.progressBar.visibility = View.GONE
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()

                    }
                }

            }

        }
        binding.ivExitEditTask.setOnClickListener {
            val intentMain = Intent(this, MainActivity::class.java)
            startActivity(intentMain)
            finish()
        }

        // embedding image to the activity
        if (item != null) {
            if (item.imageUrl?.isNotEmpty() == true) {
                Glide.with(this).load(item.imageUrl).into(binding.ivFoodPhoto)
                binding.ivFoodPhoto.visibility = View.VISIBLE
                binding.ivRemoveImage.visibility = View.VISIBLE
            }
            binding.tvInfoItemName.setText(item.name.toString())
            binding.etFoodNotes.setText(item.notes.toString())

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EditItemActivity.REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                photoFile = data.extras?.get("photo") as Uri
                if (photoFile != null) {
                    EditItemActivity.changesMade = true
                    binding.ivFoodPhoto.visibility = View.VISIBLE
                    Glide.with(this).load(photoFile).into(binding.ivFoodPhoto)
                    binding.ivRemoveImage.visibility = View.VISIBLE
                }
            }
        }
        if (requestCode == EditItemActivity.PHOTO_CODE && resultCode == RESULT_OK) {
            photoFile = data?.data!!

            if (photoFile != null) {
                EditItemActivity.changesMade = true
                binding.ivFoodPhoto.visibility = View.VISIBLE
                Glide.with(this).load(photoFile).into(binding.ivFoodPhoto)
                binding.ivRemoveImage.visibility = View.VISIBLE

            }
        }
    }

    private fun openPhotos() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"),
            PHOTO_CODE)

    }

    private fun updateItemWithPhoto(userID: String, item: Item) {
        val photoRef =
            storageRef.child("images/$userID/${System.currentTimeMillis()}_photo.jpg")
        photoRef.putFile(photoFile!!)
            .continueWithTask { photoUploadTask ->
                photoRef.downloadUrl

            }.continueWith { downloadUrl ->
                item.imageUrl = downloadUrl.result.toString()
                updateItem(userID, item)

            }
    }

    private fun updateItem(userID: String, item: Item) {
        binding.btnUpdate.isEnabled = true
        // if reference is stored in application context
        if ((applicationContext as MyApplication).pantryRef == null) {
            db.collection("users").document(userID)
                .get().addOnSuccessListener { snapshot ->
                    var user = snapshot.toObject(User::class.java)
                    var pantryRef = user?.myPantry
                    pantryRef?.collection("my_pantry")
                        ?.document(item.documentId.toString())
                        ?.update(item.toMap())?.addOnFailureListener { e ->
                        }?.addOnCompleteListener {

                        }
                }
            binding.progressBar.visibility = View.GONE
            startActivity(Intent(this, MainActivity::class.java))
            finish()

        } else {
            var pantryRef = (applicationContext as MyApplication).pantryRef
            pantryRef?.collection("my_pantry")
                ?.document(item.documentId.toString())
                ?.update(item.toMap())?.addOnFailureListener { e ->
                }?.addOnCompleteListener {

                }
            binding.progressBar.visibility = View.GONE
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

    }

}