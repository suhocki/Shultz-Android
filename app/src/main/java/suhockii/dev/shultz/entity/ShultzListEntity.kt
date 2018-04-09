package suhockii.dev.shultz.entity

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import org.json.JSONArray


data class ShultzListEntity(val shultzList: List<ShultzInfoEntity>) {
    class Deserializer : ResponseDeserializable<List<ShultzInfoEntity>> {
        override fun deserialize(content: String): ArrayList<ShultzInfoEntity>? {
            val jsonarray = JSONArray(content)
            val result = arrayListOf<ShultzInfoEntity>()
            for (i in 0 until jsonarray.length()) {
                val jsonobject = jsonarray.getJSONObject(i)
                result.add(
                        Gson().fromJson(jsonobject.toString(), ShultzInfoEntity::class.java)!!
                )
            }
            return result
        }
    }
}