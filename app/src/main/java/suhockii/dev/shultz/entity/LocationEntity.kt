package suhockii.dev.shultz.entity

import android.os.Parcel
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng

class LocationEntity : Parcelable {

    private val latitude: Double
    private val longitude: Double
    private var latLng: LatLng? = null

    constructor(latitude: Double, longitude: Double) {
        this.latitude = latitude
        this.longitude = longitude
    }


    constructor(parcel: Parcel) : this(
            parcel.readDouble(),
            parcel.readDouble())

    fun toLatLng(): LatLng {
        return if (latLng != null) {
            latLng!!
        } else {
            latLng = LatLng(latitude, longitude)
            latLng!!
        }
    }

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
