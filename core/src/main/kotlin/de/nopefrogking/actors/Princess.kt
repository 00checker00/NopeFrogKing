package de.nopefrogking.actors

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import de.nopefrogking.Assets
import de.nopefrogking.utils.drawWithTexture

class Princess: Actor() {
    var t: Float = 0f

    private var pirouette = false
    private var umbrella = false

    private var umbrellaCallback: (()->Unit)? = null

    val fallAnimation: Animation<TextureRegion>? by Assets.getAnimation("Characters/Princess/Fall_Normal")
    val pirouetteAnimation: Animation<TextureRegion>? by Assets.getAnimation("Characters/Princess/Pirouette")
    val umbrellaAnimation: Animation<TextureRegion>? by Assets.getAnimation("Characters/Princess/Umbrella")

    var starsEffect: ParticleEffectPool.PooledEffect? = null

    var drawStars: Boolean = false
        set(value) {
            field = value
            starsEffect?.reset()
        }


    init {
        width = Width
        height = Height

        starsEffect = starsEffectPool?.obtain()
    }

    override fun act(delta: Float) {
        super.act(delta)
        t += delta

        if (drawStars) {
            starsEffect?.let { effect ->
                effect.emitters.forEach { it.tint.colors = floatArrayOf(color.r, color.g, color.b) }
                effect.setPosition(this@Princess.x + width / 2, this@Princess.y + height / 2)
                effect.update(delta)
            }
        }
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        if (drawStars)
            starsEffect?.draw(batch)

        if (umbrella) {
            umbrellaAnimation?.getKeyFrame(t, false)?.let {
                drawWithTexture(batch, it, parentAlpha)
            }
            if (umbrellaAnimation?.isAnimationFinished(t) ?: true) {
                umbrellaCallback?.invoke()
                umbrellaCallback = null
            }
            return
        }
        if (pirouette) {
            if (!(pirouetteAnimation?.isAnimationFinished(t) ?: true)) {
                pirouetteAnimation?.getKeyFrame(t, false)?.let {
                    drawWithTexture(batch, it, parentAlpha)
                    return
                }
            }
            pirouette = false
        }
        fallAnimation?.getKeyFrame(t, true)?.let {
            drawWithTexture(batch, it, parentAlpha)
        }
    }

    fun doPirouette() {
        pirouette = true
        t = 0f
    }

    fun openUmbrella(cb: (()->Unit)? = {closeUmbrella()}) {
        umbrella = true
        t = 0f

        umbrellaCallback = cb
    }

    fun closeUmbrella() {
        umbrella = false
    }

    companion object {
        val Width = 92f
        val Height = 168f

        internal val starsEffectPool: ParticleEffectPool? by Assets.getParticleEffect("particles/Stars.p")
    }
}