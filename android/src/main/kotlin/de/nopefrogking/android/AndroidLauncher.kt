package de.nopefrogking.android

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.multidex.MultiDex
import android.view.Window
import android.view.WindowManager
import android.widget.RelativeLayout
import android.widget.Toast
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.games.GamesActivityResultCodes
import de.nopefrogking.ADMOB_BANNER_ID
import de.nopefrogking.Main
import de.nopefrogking.R
import de.nopefrogking.utils.PlatformHandler
import de.nopefrogking.utils.RegisterPlatformHandler

class AndroidLauncher : AndroidApplication() {
    private lateinit var adView: AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        requestWindowFeature(Window.FEATURE_ACTION_BAR)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)

        val gameView = initializeForView(Main(), AndroidApplicationConfiguration())
        LoginHandler.setContext(context, gameView)

        val layout = RelativeLayout(this)
        adView = AdView(this)
        adView.adSize = AdSize.SMART_BANNER
        adView.adUnitId = ADMOB_BANNER_ID


        val builder = AdRequest.Builder()
        builder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
        builder.addTestDevice("FAA78AC1FF5D5ABC1D9F1CCF518329F2")

        adView.loadAd(builder.build())

        layout.addView(gameView)

        val layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT)

        layout.addView(adView, layoutParams)

        val videoScreen = VideoView(this)
        layout.addView(videoScreen, RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT)
        )

        val loadingScreen = layoutInflater.inflate(R.layout.loading, layout, false)
        val imgLayoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT).apply {
            addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
            addRule(RelativeLayout.ALIGN_PARENT_TOP)
            addRule(RelativeLayout.ALIGN_PARENT_LEFT)
        }
        layout.addView(loadingScreen, imgLayoutParams)

        setContentView(layout)

        RegisterPlatformHandler(AndroidPlatformHandler(adView, loadingScreen, videoScreen, context))

        PlatformHandler.Instance?.hideAd()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == LoginHandler.REQUEST_RESOLVE_ERROR) {
            LoginHandler.setmResolvingError(false)
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                LoginHandler.login()
            } else if (resultCode == RESULT_CANCELED) {
                // The account picker dialog closed without selecting an account.
                // Notify users that they must pick an account to proceed.
                Toast.makeText(this, R.string.login_failed, Toast.LENGTH_SHORT).show()
            }
        }

        if (resultCode == GamesActivityResultCodes.RESULT_RECONNECT_REQUIRED && requestCode == LoginHandler.REQUEST_LEADERBOARD) {
            // force a disconnect to sync up state, ensuring that mClient reports "not connected"
            LoginHandler.logout()
        }
    }

    override fun onBackPressed() {
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase)
        MultiDex.install(this)
    }
}