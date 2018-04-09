package suhockii.dev.shultz.entity

data class ShultzListRequest(val filter: FilterEntity)

data class FilterEntity(val offset: Int,
                        val limit: Int)

