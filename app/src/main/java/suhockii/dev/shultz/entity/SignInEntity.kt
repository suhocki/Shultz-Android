package suhockii.dev.shultz.entity

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson

data class SignInEntity(val token: String) {
    class Deserializer : ResponseDeserializable<SignInEntity> {
        override fun deserialize(content: String) =
                Gson().fromJson(content, SignInEntity::class.java)!!
    }
}
