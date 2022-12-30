package dev.kallytang.chomp.models

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.PropertyName

data class User(
    var username: String = "",
    @get:PropertyName("display_name") @set:PropertyName("display_name") var displayName: String = "",
//    @get:PropertyName("image_url") @set:PropertyName("image_url") var imageUrl: String ="",
    @get:PropertyName("limited_plan") @set: PropertyName("limited_plan")var limitedPlan: Boolean = true,
    @get:PropertyName("my_pantry") @set: PropertyName("my_pantry")var myPantry: DocumentReference? = null
)