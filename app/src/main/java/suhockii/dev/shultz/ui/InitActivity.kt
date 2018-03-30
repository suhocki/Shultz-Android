package suhockii.dev.shultz.ui

import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AppCompatActivity
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.httpPost
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_init.*
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import suhockii.dev.shultz.Common
import suhockii.dev.shultz.R
import suhockii.dev.shultz.util.*
import java.io.ByteArrayInputStream
import java.io.InputStreamReader


class InitActivity : AppCompatActivity(), KeyboardHeightObserver, FirebaseTokenActions {

    private lateinit var keyboardHeightProvider: KeyboardHeightProvider
    private lateinit var firebaseInstanceId: FirebaseInstanceId
    private lateinit var shakeAnimation: Animation
    private var checkPushTokenDelayed = async{}
    private var networkRequest: Request? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_init)

        keyboardHeightProvider = KeyboardHeightProvider(this)
        firebaseInstanceId = FirebaseInstanceId.getInstance()
        shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake)
        shakeAnimation.withEndAction {
            etLogin.background.clearColorFilter()
            etPassword.background.clearColorFilter()
        }

        flInit.post { keyboardHeightProvider.start() }

        fabShultz.setOnClickListener {
            val login = etLogin.text.toString()
            val password = etPassword.text.toString()
            if (isPushTokenRetrieved() && isUserInputValid(login, password)) {
                sendInitRequest(listOf(
                        "name" to login,
                        "password" to password,
                        "pushToken" to Common.sharedPreferences.pushToken!!))
            }
        }

        ivRefreshToken.setOnClickListener {
            if (checkPushTokenDelayed.isActive) {
                return@setOnClickListener
            }
            tvFirebaseToken.text = getString(R.string.retrieving_firebase_token)
            ivRefreshToken.visibility = View.GONE
            TransitionManager.beginDelayedTransition(llFireBaseToken, ChangeBounds())
            checkPushTokenDelayed = async {
                delay(3000)
                runOnUiThread { isPushTokenRetrieved() }
            }
        }
    }


    private fun isPushTokenRetrieved(): Boolean =
            if (checkPushTokenDelayed.isActive ||
                    Common.sharedPreferences.pushToken.isNullOrBlank()) {
                onTokenRefreshFailed()
                closeKeyboard()
                false
            } else {
                true
            }

    private fun isUserInputValid(login: String, password: String): Boolean {
        val redColor = ResourcesCompat.getColor(resources, R.color.colorRed, theme)
        var isAllValid = true
        if (!login.matches(Regex(getString(R.string.regex_login)))) {
            etLogin.startAnimation(shakeAnimation)
            etLogin.background.mutate().setColorFilter(redColor, PorterDuff.Mode.SRC_ATOP)
            isAllValid = false
        }
        if (!password.matches(Regex(getString(R.string.regex_password)))) {
            etPassword.startAnimation(shakeAnimation)
            etPassword.background.mutate().setColorFilter(redColor, PorterDuff.Mode.SRC_ATOP)
            isAllValid = false
        }
        return isAllValid
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
        Common.sharedPreferences.userToken = initResponse._id
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

    override fun onStart() {
        super.onStart()
        if (Common.sharedPreferences.pushToken.isNullOrBlank()) {
            tvFirebaseToken.text = getString(R.string.retrieving_firebase_token)
            tvFirebaseToken.visibility = View.VISIBLE
            checkPushTokenDelayed = async {
                delay(3000)
                runOnUiThread { isPushTokenRetrieved() }
            }
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

    override fun onTokenRefreshed() {
        runOnUiThread {
            checkPushTokenDelayed.cancel()
            tvFirebaseToken.text = getString(R.string.firebase_token_retrieved)
            tvFirebaseToken.animate().alpha(0f)
                    .withEndAction { tvFirebaseToken.visibility = View.GONE }
                    .startDelay = 2000
            ivRefreshToken.visibility = View.GONE
            TransitionManager.beginDelayedTransition(llFireBaseToken, ChangeBounds())
        }
    }

    override fun onTokenRefreshFailed() {
        val retrieveFailedRes = getString(R.string.retrieving_firebase_token_failed)
        if (tvFirebaseToken.text.toString() == retrieveFailedRes) {
            return
        }
        tvFirebaseToken.text = retrieveFailedRes
        ivRefreshToken.alpha = 0f
        ivRefreshToken.visibility = View.VISIBLE
        ivRefreshToken.animate().alpha(1f)
        TransitionManager.beginDelayedTransition(llFireBaseToken, ChangeBounds())
    }

    override fun onKeyboardHeightChanged(height: Int, orientation: Int) {
        llInit.animate().translationY((-height / 2).toFloat())
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
    fun onTokenRefreshFailed()
}