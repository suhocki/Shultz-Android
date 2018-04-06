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
import com.github.kittinunf.fuel.httpPost
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_init.*
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import suhockii.dev.shultz.Common
import suhockii.dev.shultz.R
import suhockii.dev.shultz.entity.SignInEntity
import suhockii.dev.shultz.util.*
import java.io.ByteArrayInputStream
import java.io.InputStreamReader


class InitActivity : AppCompatActivity(), KeyboardHeightObserver, PushTokenListener {

    private lateinit var keyboardHeightProvider: KeyboardHeightProvider
    private lateinit var firebaseInstanceId: FirebaseInstanceId
    private lateinit var shakeAnimation: Animation
    private lateinit var loginParameters: List<Pair<String, String>>
    private var checkPushTokenDelayed = async {}
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
                loginParameters = listOf("name" to login,
                        "password" to password,
                        "pushToken" to Common.sharedPreferences.pushToken!!)
                etFocusable.requestFocus()
                etLogin.isEnabled = false
                etPassword.isEnabled = false
                sendInitRequest()
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
                onPushTokenRefreshFailed()
                closeKeyboard()
                false
            } else {
                true
            }

    private fun sendInitRequest() {
        progressBarCircle.visibility = View.VISIBLE
        networkRequest?.cancel()
        networkRequest = getString(R.string.url_init).httpPost(loginParameters)
                .response { _, _, result ->
                    result.fold({ onInitSuccess() }, { onInitFailure(it) })
                }
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

    private fun onInitSuccess() {
        sendSignInRequest()
    }

    private fun onInitFailure(fuelError: FuelError) {
        val data = fuelError.response.data
        if (data.isNotEmpty()) {
            val serverMessage = InputStreamReader(ByteArrayInputStream(data)).readLines().first()
            if (serverMessage == getString(R.string.server_response_name_exists)) {
                sendSignInRequest()
                return
            } else {
                toast(serverMessage)
            }
        } else {
            toast(getString(R.string.check_internet))
        }
        progressBarCircle.visibility = View.INVISIBLE
        etLogin.isEnabled = true
        etPassword.isEnabled = true
        etLogin.requestFocus()
    }

    private fun sendSignInRequest() {
        getString(R.string.url_signin).httpPost(loginParameters)
                .responseObject(SignInEntity.Deserializer()) { _, _, result ->
                    progressBarCircle.visibility = View.INVISIBLE
                    result.fold({ onSignInSuccess(it) }, { onSignInFailure(it) })
                }
    }

    private fun onSignInSuccess(signInResponse: SignInEntity) {
        Common.sharedPreferences.userToken = signInResponse.token
        startActivity<ScrollingActivity>(getString(R.string.extra_firebase_id) to signInResponse.token)
                .also { finish() }
    }

    private fun onSignInFailure(fuelError: FuelError) {
        etLogin.isEnabled = true
        etPassword.isEnabled = true
        etPassword.requestFocus()
        val data = fuelError.response.data
        if (data.isNotEmpty()) {
            val serverMessage = InputStreamReader(ByteArrayInputStream(data)).readLines().first()
            if (serverMessage == getString(R.string.server_response_unauthorized)) {
                etPassword.startAnimation(shakeAnimation)
                val redColor = ResourcesCompat.getColor(resources, R.color.colorRed, theme)
                etPassword.background.mutate().setColorFilter(redColor, PorterDuff.Mode.SRC_ATOP)
            } else {
                toast(serverMessage)
            }
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
                delay(9000)
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

    override fun onPushTokenRefreshed() {
        runOnUiThread {
            checkPushTokenDelayed.cancel()
            tvFirebaseToken.text = getString(R.string.firebase_token_retrieved)
            tvFirebaseToken.animate().alpha(0f)
                    .withEndAction { tvFirebaseToken.visibility = View.GONE }
                    .startDelay = 3000
            ivRefreshToken.visibility = View.GONE
            TransitionManager.beginDelayedTransition(llFireBaseToken, ChangeBounds())
        }
    }

    override fun onPushTokenRefreshFailed() {
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