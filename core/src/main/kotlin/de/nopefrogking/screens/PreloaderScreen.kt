package de.nopefrogking.screens

import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import de.nopefrogking.Assets
import de.nopefrogking.Main
import de.nopefrogking.utils.DelegatingBlockingInputProcessor
import de.project.ice.screens.BaseScreenAdapter
import ktx.app.clearScreen

class PreloaderScreen(game: Main, val autoClose: Boolean = true, val callback: ((PreloaderScreen)->Unit)? = null) : BaseScreenAdapter(game) {
    val font = BitmapFont()
    val batch = SpriteBatch()
    var finished: Boolean = false

    override val inputProcessor = DelegatingBlockingInputProcessor(InputAdapter())

    init {

    }

    override val priority: Int
        get() = 15

    override fun resize(width: Int, height: Int) {
    }

    override fun update(delta: Float) {
        if (Assets.fontManager.update()) {
            if (Assets.manager.update((delta * 1000).toInt()) && !finished) {
                finished = true
                if (autoClose) {
                    game.removeScreen(this, true)
                }
                callback?.invoke(this)
            }
        }
    }

    override fun render() {
        clearScreen(1f, 0f, 0f)
        val y = font.lineHeight
        batch.begin()
        font.draw(batch, "Loading Assets", 0f, y)
        batch.end()
    }

    override fun dispose() {
        font.dispose()
    }

    companion object {
        val FADE_TIME = 0.3f
    }
}
