package de.nopefrogking.ui

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor

class LoadingBar(internal var animation: Animation<out TextureRegion>) : Actor() {
    internal var reg = animation.getKeyFrame(0f)
    internal var stateTime: Float = 0.toFloat()

    override fun act(delta: Float) {
        stateTime += delta
        reg = animation.getKeyFrame(stateTime)
    }

    fun draw(batch: SpriteBatch, parentAlpha: Float) {
        batch.draw(reg, x, y)
    }
}
