package de.nopefrogking.screens

import de.nopefrogking.Assets
import de.nopefrogking.Main
import de.nopefrogking.utils.PlatformHandler
import de.project.ice.screens.BaseScreenAdapter

class LoadingScreen(game: Main, val autoClose: Boolean = true, val callback: ((LoadingScreen)->Unit)? = null) : BaseScreenAdapter(game) {
//    private var stage: Stage = Stage()
//
    private var finished = false
//
//    private lateinit var logo: Image
//    private lateinit var loadingFrame: Image
//    private lateinit var loadingBarHidden: Image
//    private lateinit var screenBg: Image
//    private lateinit var loadingBg: Image
//
//    private lateinit var loadingBar: Actor
//
//    private var startX: Float = 0.toFloat()
//    private var endX: Float = 0.toFloat()
//
//    private var percent: Float = 0.toFloat()

    override fun show() {
        PlatformHandler.Instance?.loadingScreenVisible = true
//        // Tell the manager to load assets for the loading screen
//        Assets.manager.load("ui/loadingscreen/loading.atlas", TextureAtlas::class.java)
//        // Wait until they are finished loading
//        Assets.manager.finishLoading()
//
//        // Get our textureatlas from the manager
//        val atlas = Assets.manager.get("ui/loadingscreen/loading.atlas", TextureAtlas::class.java)
//
//        // Grab the regions from the atlas and create some images
//        logo = Image(atlas.findRegion("libgdx-logo"))
//        loadingFrame = Image(atlas.findRegion("loading-frame"))
//        loadingBarHidden = Image(atlas.findRegion("loading-bar-hidden"))
//        screenBg = Image(atlas.findRegion("screen-bg"))
//        loadingBg = Image(atlas.findRegion("loading-frame-bg"))
//
//        // Add the loading bar animation
//        val anim = Animation(0.05f, atlas.findRegions("loading-bar-anim"))
//        anim.playMode = Animation.PlayMode.LOOP_REVERSED
//        //loadingBar = LoadingBar(anim)
//
//        // Or if you only need a static bar, you can do
//        loadingBar = Image(atlas.findRegion("loading-bar1"))
//
//        // Add all the actors to the stage
//        stage.addActor(screenBg)
//        stage.addActor(loadingBar)
//        stage.addActor(loadingBg)
//        stage.addActor(loadingBarHidden)
//        stage.addActor(loadingFrame)
//        stage.addActor(logo)
//
//
//        // Add everything to be loaded, for instance:
//        // game.manager.load("data/assets1.pack", TextureAtlas.class);
//        // game.manager.load("data/assets2.pack", TextureAtlas.class);
//        // game.manager.load("data/assets3.pack", TextureAtlas.class);
//
//        val width = 800f
//        val height = 480f
//
//        stage.viewport = StretchViewport(width, height)
//
//        // Place the logo in the middle of the screen and 100 px up
//        logo.x = (width - logo.width) / 2
//        logo.y = (height - logo.height) / 2 + 100
//
//        // Place the loading frame in the middle of the screen
//        loadingFrame.x = (stage.width - loadingFrame.width) / 2
//        loadingFrame.y = (stage.height - loadingFrame.height) / 2
//
//        // Place the loading bar at the same spot as the frame, adjusted a few px
//        loadingBar.x = loadingFrame.x + 15
//        loadingBar.y = loadingFrame.y + 5
//
//        // Place the image that will hide the bar on top of the bar, adjusted a few px
//        loadingBarHidden.x = loadingBar.x + 35
//        loadingBarHidden.y = loadingBar.y - 3
//        // The start position and how far to move the hidden loading bar
//        startX = loadingBarHidden.x
//        endX = 440f
//
//        // The rest of the hidden bar
//        loadingBg.setSize(450f, 50f)
//        loadingBg.x = loadingBarHidden.x + 30
//        loadingBg.y = loadingBarHidden.y + 3
    }

    override fun hide() {
        PlatformHandler.Instance?.loadingScreenVisible = false
    }

    override fun resize(width: Int, height: Int) {
//        stage.viewport.update(width, height, true)
//
//        // Make the background fill the screen
//        screenBg.setSize(stage.viewport.worldWidth, stage.viewport.worldHeight)
    }

    override fun update(delta: Float) {
        if (Assets.fontManager.update()) {
            if (Assets.manager.update((delta * 1000).toInt()) && !finished) {
                if (autoClose) {
                    game.removeScreen(this, true)
                    callback?.invoke(this)
                } else {
                    finished = true
                    callback?.invoke(this)
                }
            }
        }

//        // Interpolate the percentage to make it more smooth
//        percent = Interpolation.linear.apply(percent, Assets.fontManager.progress, 0.1f)
//
//        // Update positions (and size) to match the percentage
//        loadingBarHidden.x = startX + endX * percent
//        loadingBg.x = loadingBarHidden.x + 30
//        loadingBg.width = 450 - 450 * percent
//        loadingBg.invalidate()
//
//        // Show the loading screen
//        stage.act()
    }

    override val priority: Int
        get() = 15

    override fun render() {
//        clearScreen(0.3f, 0.3f, 0.3f)
//        stage.viewport.apply()
//        stage.draw()
    }

    override fun dispose() {
//        Assets.manager.unload("ui/loadingscreen/loading.atlas")
    }
}
