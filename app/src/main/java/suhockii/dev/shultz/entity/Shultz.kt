package suhockii.dev.shultz.entity

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

interface BaseEntity : Parcelable {
    val id: String
}

data class RetryEntity(override val id: String = "RetryEntity") : BaseEntity {
    constructor(parcel: Parcel) : this(parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RetryEntity> {
        override fun createFromParcel(parcel: Parcel): RetryEntity {
            return RetryEntity(parcel)
        }

        override fun newArray(size: Int): Array<RetryEntity?> {
            return arrayOfNulls(size)
        }
    }
}

data class LoadingEntity(override val id: String = "LoadingEntity") : BaseEntity {
    constructor(parcel: Parcel) : this(parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LoadingEntity> {
        override fun createFromParcel(parcel: Parcel): LoadingEntity {
            return LoadingEntity(parcel)
        }

        override fun newArray(size: Int): Array<LoadingEntity?> {
            return arrayOfNulls(size)
        }
    }
}

data class ShultzInfoEntity(@SerializedName("_id") override val id: String,
                            val user: String,
                            val power: Int,
                            var date: String,
                            var location: LocationEntity) : BaseEntity {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readInt(),
            parcel.readString(),
            parcel.readParcelable(LocationEntity::class.java.classLoader))

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(user)
        parcel.writeInt(power)
        parcel.writeString(date)
        parcel.writeParcelable(location, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ShultzInfoEntity> {
        override fun createFromParcel(parcel: Parcel): ShultzInfoEntity {
            return ShultzInfoEntity(parcel)
        }

        override fun newArray(size: Int): Array<ShultzInfoEntity?> {
            return arrayOfNulls(size)
        }
    }
}