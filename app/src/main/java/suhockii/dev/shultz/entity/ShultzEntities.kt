package suhockii.dev.shultz.entity

import com.google.gson.annotations.SerializedName

interface BaseEntity {
    val id: String
}

data class RetryEntity(override val id: String = "RetryEntity") : BaseEntity

data class LoadingEntity(override val id: String = "LoadingEntity") : BaseEntity

data class ShultzInfoEntity(@SerializedName("_id") override val id: String,
                            val user: String,
                            val power: Int,
                            var date: String,
                            var location: LocationEntity) : BaseEntity