package de.nopefrogking.actors

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.scenes.scene2d.Action
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import de.nopefrogking.Assets
import de.nopefrogking.utils.drawWithTexture
import ktx.actors.repeatForever
import ktx.actors.then

class Princess: Actor() {
    var t: Float = 0f

    private var pirouette = false
    private var umbrella = false

    private var umbrellaCallback: (()->Unit)? = null

    val fallAnimation: Animation<TextureRegion>? by Assets.getAnimation("Characters/Princess/Fall_Normal")
    val pirouetteAnimation: Animation<TextureRegion>? by Assets.getAnimation("Characters/Princess/Pirouette")
    val umbrellaAnimation: Animation<TextureRegion>? by Assets.getAnimation("Characters/Princess/Umbrella")

    var starsEffect: ParticleEffectPool.PooledEffect? = null

    private var rainbowAction: Action? = null

    val hitbox = Rectangle()

    var drawStars: Boolean = false
        set(value) {
            if (value && rainbowAction == null) {
                rainbowAction = (Actions.color(Color.RED, 0.1f) then
                        Actions.color(Color.VIOLET, 0.1f) then
                        Actions.color(Color.BLUE, 0.1f) then
                        Actions.color(Color.CYAN, 0.1f) then
                        Actions.color(Color.GREEN, 0.1f) then
                        Actions.color(Color.YELLOW, 0.1f)).repeatForever()
                addAction(rainbowAction)
            } else if (!value && rainbowAction != null) {
                removeAction(rainbowAction)
                rainbowAction = null
            }
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

        hitbox.set(x + 15, y, width - 30, height)
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
        val Width = 154f
        val Height = 168f

        internal val starsEffectPool: ParticleEffectPool? by Assets.getParticleEffect("particles/Stars.p")
    }
}