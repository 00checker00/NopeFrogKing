package de.nopefrogking.actors

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.ParticleEffect
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.scenes.scene2d.Actor
import de.nopefrogking.Assets
import de.nopefrogking.Sounds
import de.nopefrogking.utils.drawWithTexture
import com.badlogic.gdx.utils.Array as GdxArray

class Prince: Actor() {
    var t: Float = 0f
    var hitT: Float = 0f

    val fallAnimation: Animation<TextureRegion>? by Assets.getAnimation("Characters/Prince/Fall_Normal")
    val fallDamagedAnimation: Animation<TextureRegion>? by Assets.getAnimation("Characters/Prince/Fall_Damaged")
    val fallHitAnimation: Animation<TextureRegion>? by Assets.getAnimation("Characters/Prince/Fall_Normal_Hit")
    val fallDamagedHitAnimation: Animation<TextureRegion>? by Assets.getAnimation("Characters/Prince/Fall_Damaged_Hit")
    val electrifiedAnimation: Animation<TextureRegion>? by Assets.getAnimation("Characters/Prince/Electro")

    val effects = GdxArray<ParticleEffect>()

    val damaged: Boolean get() = hits >= hitsTillDamaged

    var hits: Int = 0
    var hitsTillDamaged: Int = 8

    private var electrified = false

    val hitbox = Rectangle()

    private var hit = false

    init {
        width = Width
        height = Height
    }

    fun hit() {
        hits++

        hit = true
        hitT = 0f

        Sounds.princ_get_hit().play(0.2f)
    }

    fun getElectrified() {
        t = 0f
        electrified = true
    }

    override fun act(delta: Float) {
        super.act(delta)
        t += delta
        hitT += delta

        hitbox.set(x + 10, y, width - 20, height)
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        if (electrified) {
            if (!(electrifiedAnimation?.isAnimationFinished(t) ?: true)) {
                electrifiedAnimation?.getKeyFrame(t, false)?.let {
                    drawWithTexture(batch, it, parentAlpha)
                    return
                }
            }
            electrified = false
        }
        if (hit) {
            val finished: Boolean
            if (damaged) {
                fallDamagedHitAnimation?.getKeyFrame(hitT, true)?.let {
                    drawWithTexture(batch, it, parentAlpha)
                }
                finished = fallDamagedHitAnimation?.isAnimationFinished(hitT) ?: true
            } else {
                fallHitAnimation?.getKeyFrame(hitT, true)?.let {
                    drawWithTexture(batch, it, parentAlpha)
                }
                finished = fallHitAnimation?.isAnimationFinished(hitT) ?: true
            }
            if (finished) {
                hit = false
            }
        } else {
            if (damaged) {
                fallDamagedAnimation?.getKeyFrame(t, true)?.let {
                    drawWithTexture(batch, it, parentAlpha)
                }
            } else {
                fallAnimation?.getKeyFrame(t, true)?.let {
                    drawWithTexture(batch, it, parentAlpha)
                }
            }
        }
    }

    companion object {
        val Width = 89f
        val Height = 131f
    }
}