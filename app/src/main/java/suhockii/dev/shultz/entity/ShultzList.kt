package suhockii.dev.shultz.entity

data class ShultzListRequest(val filter: PaginationEntity)

data class PaginationEntity(val offset: Int,
                            val limit: Int)

