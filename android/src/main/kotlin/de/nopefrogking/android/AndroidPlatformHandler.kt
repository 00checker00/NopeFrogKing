package de.nopefrogking.android

import android.annotation.SuppressLint
import android.content.Context
import android.os.*
import android.util.Log
import android.view.View
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.math.Rectangle
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.games.Games
import com.google.android.gms.games.leaderboard.LeaderboardVariant
import de.nopefrogking.ADMOB_INTERSTITIAL_ID
import de.nopefrogking.PLAY_SERVICE_LEADERBOARD
import de.nopefrogking.utils.PlatformHandler
import java.io.Serializable

class AndroidPlatformHandler : PlatformHandler {

    private sealed class Msg<out ARG1: Serializable, out ARG2: Serializable>(val arg1: ARG1? = null, val arg2: ARG2? = null) {
        class HideAd : Msg<Nothing, Nothing>()
        class ShowAd : Msg<Nothing, Nothing>()
        class HideLoadingScreen : Msg<Nothing, Nothing>()
        class ShowLoadingScreen : Msg<Nothing, Nothing>()
        class ShowVideo(video: String, bounds: Rectangle) : Msg<String, Rectangle>(video, bounds)

        val ordinal: Int get() = Msg::class::nestedClasses.get().indexOf(this::class)
        operator fun invoke() = ordinal

        companion object {
            val values = Msg::class::nestedClasses.get().associate { Pair(Msg::class::nestedClasses.get().indexOf(it), it) }

            val MSG_ARG1_KEY = "__MSG_ARG1__"
            val MSG_ARG2_KEY = "__MSG_ARG2__"
        }
    }

    private var context: Context
    private val adView: AdView
    private val loadingView: View?
    private var interstitialAd: InterstitialAd
    private var videoFinishedCallback: (()->Unit)? = null

    private fun sendMessage(msg: Msg<Serializable, Serializable>) {
        handler?.sendMessage(Message.obtain().apply {
            what = msg.ordinal
            data = Bundle().apply {
                putSerializable(Msg.MSG_ARG1_KEY, msg.arg1)
                putSerializable(Msg.MSG_ARG2_KEY, msg.arg2)
            }
        })
    }

    override var loadingScreenVisible: Boolean = true
        set(value) {
            field = value
            sendMessage(if (value) Msg.ShowLoadingScreen() else Msg.HideLoadingScreen())
        }

    private var handler: Handler? = null

    constructor(adView: AdView, loadingView: View, videoView: VideoView, context: Context) {
        this.adView = adView
        this.loadingView = loadingView
        this.context = context

        interstitialAd = InterstitialAd(context)
        interstitialAd.adUnitId = ADMOB_INTERSTITIAL_ID
        interstitialAd.adListener = object : AdListener() {
            override fun onAdClosed() {
                requestNewInterstitial()
                super.onAdClosed()
            }
        }

        requestNewInterstitial()

        handler = @SuppressLint("HandlerLeak")
        object : Handler() {
            override fun handleMessage(msg: Message) {
                when (Msg.values[msg.what]) {
                    Msg.HideAd::class -> adView.visibility = View.GONE
                    Msg.ShowAd::class -> adView.visibility = View.VISIBLE
                    Msg.HideLoadingScreen::class -> loadingView.visibility = View.GONE
                    Msg.ShowLoadingScreen::class -> loadingView.visibility = View.VISIBLE
                    Msg.ShowVideo::class -> videoView.showVideo(
                        msg.data.getSerializable(Msg.MSG_ARG1_KEY) as String,
                        msg.data.getSerializable(Msg.MSG_ARG2_KEY) as Rectangle
                    )
                }
            }
        }

        videoView.onVideoFinishedCallback = {
            videoFinishedCallback?.let { Gdx.app.postRunnable(it) }
            videoFinishedCallback = null
        }
    }

    private fun requestNewInterstitial() {
        val adRequestBuilder = AdRequest.Builder()
        adRequestBuilder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
        adRequestBuilder.addTestDevice("FAA78AC1FF5D5ABC1D9F1CCF518329F2")

        interstitialAd.loadAd(adRequestBuilder.build())
    }

    override fun showVideo(video: FileHandle, bounds: Rectangle, onFinish: ()->Unit) {
        videoFinishedCallback?.invoke()
        videoFinishedCallback = onFinish
        sendMessage(Msg.ShowVideo(video.path(), bounds))
    }

    override fun login() {
        if (!LoginHandler.isConnected) {
            LoginHandler.login()
        }
    }

    override fun displayLeaderBoard() {
        login()
        LoginHandler.callLeaderBoard(context as AndroidLauncher)
    }

    override fun showAd() {
        sendMessage(Msg.ShowAd())
    }

    override fun hideAd() {
        sendMessage(Msg.HideAd())
    }

    override fun submitScore() {
        LoginHandler.submitScore()
    }

    override fun showInterstitialAd() {
        (context as AndroidLauncher).runOnUiThread {
            if (interstitialAd.isLoaded) {
                interstitialAd.show()
            }
        }
    }

    override fun loadHighScore() {
        if (LoginHandler.isConnected) {
            Games.Leaderboards.loadCurrentPlayerLeaderboardScore(LoginHandler.getmGoogleApiClient(), PLAY_SERVICE_LEADERBOARD,
                    LeaderboardVariant.TIME_SPAN_ALL_TIME, LeaderboardVariant.COLLECTION_PUBLIC).setResultCallback { loadPlayerScoreResult ->
                var score: Long = 0
                if (loadPlayerScoreResult.score != null) {
                    score = loadPlayerScoreResult.score.rawScore
                }

                Log.d("AndroidLauncher", "onResult score: " + score)

//                if (score > 0 && score != GameManager.score) {
//                    GameManager.onlineScore = score
//                }
            }
        }
    }
}