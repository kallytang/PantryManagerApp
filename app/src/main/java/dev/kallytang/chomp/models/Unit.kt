package dev.kallytang.chomp.models

import android.os.Parcelable
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import kotlinx.android.parcel.Parcelize

@IgnoreExtraProperties
@Parcelize
data class Unit(
    @get: PropertyName("unit_name") @set: PropertyName("unit_name") var unitName: String? = "",
    var abbreviation: String? = ""
) : Parcelable{
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
           "unit_name" to unitName,
            "abbreviation" to abbreviation
        )
    }
}
