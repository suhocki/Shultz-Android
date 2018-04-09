package suhockii.dev.shultz.entity

data class ShultzInfoEntity(val _id: String,
                            val user: String,
                            val power: Int,
                            var date: String,
                            val location: LocationEntity)
