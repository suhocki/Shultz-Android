package suhockii.dev.shultz.entity

import android.os.Parcel
import android.os.Parcelable

data class LocationEntity(private val latitude: Double,
                          private val longitude: Double): Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readDouble(),
            parcel.readDouble())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LocationEntity> {
        override fun createFromParcel(parcel: Parcel): LocationEntity {
            return LocationEntity(parcel)
        }

        override fun newArray(size: Int): Array<LocationEntity?> {
            return arrayOfNulls(size)
        }
    }
}
