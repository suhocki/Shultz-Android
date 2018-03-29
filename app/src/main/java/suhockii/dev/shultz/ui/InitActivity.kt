package suhockii.dev.shultz.ui

import android.os.Bundle
import android.support.v4.view.ViewCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.httpPost
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_init.*
import suhockii.dev.shultz.Common
import suhockii.dev.shultz.R
import suhockii.dev.shultz.util.KeyboardHeightObserver
import suhockii.dev.shultz.util.KeyboardHeightProvider
import suhockii.dev.shultz.util.startActivity
import suhockii.dev.shultz.util.toast
import java.io.ByteArrayInputStream
import java.io.InputStreamReader


class InitActivity : AppCompatActivity(), KeyboardHeightObserver, FirebaseTokenActions {

    private lateinit var keyboardHeightProvider: KeyboardHeightProvider
    private var networkRequest: Request? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_init)

        keyboardHeightProvider = KeyboardHeightProvider(this)

        flInit.post { keyboardHeightProvider.start() }

        fabShultz.setOnClickListener {
            sendInitRequest(listOf("name" to etLogin.text.toString(),
                    "pushToken" to FirebaseInstanceId.getInstance().token!!))
        }
    }

    private fun sendInitRequest(parameters: List<Pair<String, String>>) {
        networkRequest?.cancel()

        progressBar.visibility = View.VISIBLE

        "init/".httpPost(parameters).responseObject(InitResponse.Deserializer()) { _, _, result ->
            progressBar.visibility = View.INVISIBLE
            result.fold({ onInitSuccess(it) }, { onInitFailure(it) })
        }.let { networkRequest = it }
    }

    private fun onInitSuccess(initResponse: InitResponse) {
        Common.sharedPreferences.token = initResponse._id
        startActivity<ScrollingActivity>(getString(R.string.extra_firebase_id) to initResponse._id)
                .also { finish() }
    }

    private fun onInitFailure(error: FuelError?) {
        val data = error?.response?.data
        if (data?.isNotEmpty() == true) {
            toast(InputStreamReader(ByteArrayInputStream(data)).readLines().first())
        } else {
            toast(getString(R.string.check_internet))
        }
    }

    override fun onResume() {
        super.onResume()
        keyboardHeightProvider.setKeyboardHeightObserver(this)
    }

    override fun onPause() {
        super.onPause()
        keyboardHeightProvider.setKeyboardHeightObserver(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        keyboardHeightProvider.close()
    }

    override fun onTokenRefreshed(): Unit = with(flInit) {
        post { toast(getString(R.string.firebase_token_received)) }
    }

    override fun onKeyboardHeightChanged(height: Int, orientation: Int) {
        ViewCompat.animate(llInit).translationY((-height / 2).toFloat())
    }
}

data class InitResponse(
        val _id: String,
        val name: String
) {
    class Deserializer : ResponseDeserializable<InitResponse> {
        override fun deserialize(content: String) = Gson().fromJson(content, InitResponse::class.java)!!
    }
}

interface FirebaseTokenActions {
    fun onTokenRefreshed()
}