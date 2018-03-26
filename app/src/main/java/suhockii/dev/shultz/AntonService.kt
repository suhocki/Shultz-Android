package suhockii.dev.shultz

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST


interface AntonService {

    @POST("init/")
    @FormUrlEncoded
    fun init(@Field("name") name: String): Call<InitResponse>
}