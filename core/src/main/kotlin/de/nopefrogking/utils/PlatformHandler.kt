package de.nopefrogking.utils

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.math.Rectangle

interface PlatformHandler {
    fun login()
    fun displayLeaderBoard()
    fun showAd()
    fun hideAd()
    fun submitScore()
    fun showInterstitialAd()
    fun loadHighScore()

    var loadingScreenVisible: Boolean

    fun showVideo(video: FileHandle, bounds: Rectangle, onFinish: ()->Unit)

    companion object {
        var Instance: PlatformHandler? = null
            internal set
    }
}



fun RegisterPlatformHandler(handler: PlatformHandler) {
    PlatformHandler.Instance = handler
}