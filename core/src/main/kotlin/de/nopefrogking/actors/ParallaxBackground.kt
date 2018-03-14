package de.nopefrogking.actors

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Touchable
import de.nopefrogking.Assets
import de.nopefrogking.GameState
import de.nopefrogking.utils.DefaultSkin
import de.nopefrogking.utils.UIScale
import com.badlogic.gdx.utils.Array as GdxArray

enum class BackgroundType(internal val textureName: String) {
    Bricks("Bricks"),
    Stones("Stones"),
    Nuggets("Nuggets"),
    Scales("Scales"),
    Wall("Wall")
}

class ParallaxBackground(val type: BackgroundType, private val gameState: GameState): Actor() {
    var speed = 0f
    var speedMultiplyer = 1.0f

    var moving: Boolean = true

    var offset = 0f
    val texture by Assets.getRegion("Backgrounds/${type.textureName}")
    val textureBlur by Assets.getRegion("Backgrounds/${type.textureName}Blur")

    var isForeground: Boolean = false

    init {
        touchable = Touchable.disabled
    }

    override fun act(delta: Float) {
        super.act(delta)

        if (!moving) return

        offset -= speed * speedMultiplyer * delta

        texture?.let {
            val scale = this.width / it.regionWidth
            if (scale > 0.0f) {
                val textureHeight = it.regionHeight * scale
                offset %= textureHeight
                //debug { "Offset: $offset, textureHeight: $textureHeight" }
            }
        }
    }
    override fun draw(batch: Batch, parentAlpha: Float) {
        val oldColor = batch.color
        batch.color = color
        (if (gameState.isPaused || isForeground) texture else textureBlur)?.let {
            val scale = this.width / it.regionWidth
            val textureHeight = it.regionHeight * scale

            var y = this.y + this.height - textureHeight - offset
            while (y >= this.y - textureHeight) {
                batch.draw(it, this.x, y, this.width, textureHeight)
                y -= Math.max(textureHeight - Overlap * DefaultSkin.UIScale, 1f)
            }
        }
        batch.color = oldColor

    }

    companion object  {
        val Overlap = 0f
    }
}