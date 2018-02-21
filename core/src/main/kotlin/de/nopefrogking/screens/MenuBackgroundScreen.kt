package de.nopefrogking.screens

import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.utils.viewport.ExtendViewport
import de.nopefrogking.Assets
import de.nopefrogking.Main
import de.nopefrogking.Sounds
import de.nopefrogking.utils.DefaultSkin
import de.nopefrogking.utils.DelegatingBlockingInputProcessor
import de.nopefrogking.utils.UIScale
import de.project.ice.screens.BaseScreenAdapter
import ktx.actors.then
import ktx.collections.gdxArrayOf
import com.badlogic.gdx.utils.Array as GdxArray

open class MenuBackgroundScreen(game: Main) : BaseScreenAdapter(game) {
    private val stage = Stage()
    private val scale = DefaultSkin.UIScale

    private val introBgView: Image by lazy { Image(DefaultSkin.intro_bg()) }
    private val introClouds: GdxArray<Cloud> by lazy { gdxArrayOf(
                Cloud(Image(DefaultSkin.intro_cloud()), 300f * scale, 10f, 0.9f, 0f),
                Cloud(Image(DefaultSkin.intro_cloud()), 200f * scale, 15f, 0.25f, 8f),
                Cloud(Image(DefaultSkin.intro_cloud()), 155f * scale, 20f, 0.15f, 3f)
    )}
    private val introPrincessView: Image by lazy { Image(DefaultSkin.intro_princess()) }
    private val introCastleView: Image by lazy { Image(DefaultSkin.intro_castle()) }
    private val introWellView: Image by lazy { Image(DefaultSkin.intro_well()) }

    override val inputProcessor: InputProcessor = DelegatingBlockingInputProcessor(stage)

    private val music by Assets.getMusic("music/menu_bg.mp3") {
        it?.isLooping = true
        it?.play()
        it?.volume = 0.5f
    }

    init {
//        if (Config.RENDER_DEBUG) {
//            stage.setDebugAll(true)
//        }
        stage.viewport = ExtendViewport(450f * scale, 800f * scale, OrthographicCamera())

        stage.addActor(introBgView)
        introClouds.forEach { stage.addActor(it.image) }
        stage.addActor(introPrincessView)
        stage.addActor(introCastleView)
        stage.addActor(introWellView)

        queueFrogSound()
    }

    private fun queueFrogSound() {
        stage.addAction(Actions.delay(20 + Math.random().toFloat() * 20) then Actions.run {
            Sounds.frog_ribit().play()
            queueFrogSound()
        })
    }

    override val priority: Int
        get() = 150


    fun startMusic() {
        music?.play()
    }

    fun stopMusic() {
        music?.stop()
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)

        val worldWidth = stage.viewport.worldWidth
        val worldHeight = stage.viewport.worldHeight

        val scaleFactor = worldHeight/introBgView.drawable.minHeight

        introBgView.height = worldHeight
        introBgView.width = introBgView.drawable.minWidth * scaleFactor

        introBgView.y = 0f
        introBgView.x = -(introBgView.width - worldWidth) / 2

        val centerX = worldWidth / 2
        val centerY = worldHeight / 2

        introClouds.forEach {
            it.image.width = it.image.drawable.minWidth * scaleFactor * it.scale
            it.image.height = it.image.drawable.minHeight * scaleFactor * it.scale

            it.image.y = centerY - it.image.height/2 + it.y

            it.image.actions.clear()
            it.image.addAction(Actions.forever(
                    Actions.moveTo(-it.image.width, it.image.y, 0f)
                            then Actions.delay(it.delay)
                            then Actions.moveTo(worldWidth, it.image.y, it.animDuration)
            ))
        }


        introCastleView.width = introCastleView.drawable.minWidth * scaleFactor
        introCastleView.height = introCastleView.drawable.minHeight * scaleFactor

        introCastleView.x = centerX - introCastleView.width/2 - 128f * scale
        introCastleView.y = centerY - introCastleView.height/2 + 160 * scale


        introPrincessView.width = introPrincessView.drawable.minWidth * scaleFactor
        introPrincessView.height = introPrincessView.drawable.minHeight * scaleFactor

        introPrincessView.x = centerX - introPrincessView.width/2 + 35f * scale
        introPrincessView.y = centerY - introPrincessView.height/2 + 120f * scale


        introWellView.width = introWellView.drawable.minWidth * scaleFactor
        introWellView.height = introWellView.drawable.minHeight * scaleFactor

        introWellView.x = centerX - introWellView.width/2  + 100f * scale
        introWellView.y = centerY - introWellView.height/2 + 50f * scale
    }

    override fun show() {
    }

    override fun update(delta: Float) {
        stage.act(delta)

        music
    }

    override fun render() {
        stage.viewport.apply()
        stage.draw()
    }


    override fun dispose() {
        stage.dispose()
        music?.stop()
    }

    private data class Cloud(val image: Image, val y: Float, val animDuration: Float, val scale: Float, val delay: Float = 0f)
}
