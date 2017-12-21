package de.nopefrogking.actors

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import de.nopefrogking.Assets
import de.nopefrogking.utils.drawWithTexture

class Whirl: Actor() {
    var t: Float = 0f

    val whirlAnimation: Animation<TextureRegion>? by Assets.getAnimation("Characters/Princess/Whirl")

    init {
        width = Width
        height = Height
    }

    override fun act(delta: Float) {
        super.act(delta)
        t += delta
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        whirlAnimation?.getKeyFrame(t, true)?.let {
            drawWithTexture(batch, it, parentAlpha)
        }
    }

    companion object {
        val Width = 92f
        val Height = 168f
    }
}