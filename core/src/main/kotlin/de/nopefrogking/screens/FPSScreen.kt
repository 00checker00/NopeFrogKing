package de.nopefrogking.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import de.nopefrogking.Main
import de.project.ice.screens.BaseScreenAdapter

class FPSScreen(game: Main) : BaseScreenAdapter(game) {
    val font = BitmapFont()
    val batch = SpriteBatch()
    var updates: Int = 0
    var lastUpdates: Int = 0
    var updatesTime: Float = 0f
    var runtime = 0.0

    init {

    }

    override val priority: Int
        get() = 10

    override fun resize(width: Int, height: Int) {
    }

    override fun update(delta: Float) {
        updates++
        updatesTime += delta
        runtime += delta.toDouble()
    }

    override fun render() {

        if (updatesTime >= 1.0f) {
            updatesTime = 0.0f
            lastUpdates = updates
            updates = 0
        }

        val fps = Gdx.graphics.framesPerSecond
        var y = Gdx.graphics.height.toFloat()
        batch.begin()
        font.draw(batch, "FPS: $fps", 0f, y)
        y -= font.lineHeight
        font.draw(batch, "FrameTime: ${"%.2f".format(1000.0/fps)}ms/s", 0f, y)
        y -= font.lineHeight
        font.draw(batch, "Updates: $lastUpdates", 0f, y)
        y -= font.lineHeight
        font.draw(batch, "Runtime: ${"%.2f".format(runtime)}s", 0f, y)
        batch.end()
    }

    override fun dispose() {
        font.dispose()
    }

    companion object {
        val FADE_TIME = 0.3f
    }
}
