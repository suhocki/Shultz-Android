package suhockii.dev.shultz.entity

data class FilterEntity(private val filter: FilterData)

data class FilterData(private val center: LocationEntity,
                      private val radius: Float)