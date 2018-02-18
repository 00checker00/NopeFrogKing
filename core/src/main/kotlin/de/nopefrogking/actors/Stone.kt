package de.nopefrogking.actors

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener
import com.badlogic.gdx.scenes.scene2d.utils.DragListener
import com.badlogic.gdx.utils.Pool
import de.nopefrogking.Assets
import de.nopefrogking.Sounds
import de.nopefrogking.utils.drawWithTexture
import ktx.actors.then
import com.badlogic.gdx.utils.Array as GdxArray

typealias DestroyListener = (Stone)->Unit
class Stone: Actor(), Pool.Poolable {
    var type: Int = -1
    var actionType: ActionType = ActionType.Tap
    val textures by Assets.getRegions(*(0..(NUMBER_OF_TYPES-1)).flatMap { arrayListOf("Stones/Stone${it+1}","Stones/Stone${it+1}Broken") }.toTypedArray())
    var isDestroyed = false; private set
    var crushEffect: ParticleEffectPool.PooledEffect? = null
    var poofEffect: ParticleEffectPool.PooledEffect? = null
    var destroyListeners = GdxArray<DestroyListener>()
    var flipX: Boolean = false
    var isDisabled: Boolean = false

    val hitbox = Rectangle()

    fun onDestroyed(listener: DestroyListener) {
        destroyListeners.add(listener)
    }

    init {
        width = Stone.Width
        height = Stone.Height

        this.addListener(object: DragListener() {
            var dx = 0f
            //var dy = 0f

            override fun dragStart(event: InputEvent, x: Float, y: Float, pointer: Int) {
                if (actionType != ActionType.Move) return
                dx = this@Stone.x - event.stageX
               // dy = this@Stone.y - event.stageY
            }

            override fun dragStop(event: InputEvent?, x: Float, y: Float, pointer: Int) {
                super.dragStop(event, x, y, pointer)
            }

            override fun drag(event: InputEvent, x: Float, y: Float, pointer: Int) {
                if (actionType != ActionType.Move) return
                this@Stone.x = event.stageX + dx
                //this@Stone.y = event.stageY + dy
            }
        })

        this.addListener(object: ActorGestureListener() {


            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                if (actionType != ActionType.Tap) return

                if (isDestroyed) return
                isDestroyed = true

                touchable = Touchable.disabled

                crush()
            }

            override fun fling(event: InputEvent, velocityX: Float, velocityY: Float, button: Int) {
                if (actionType != ActionType.Fling) return

                if (isDestroyed) return
                isDestroyed = true

                touchable = Touchable.disabled

                Sounds.valueOf("Scratch_0${(Math.random()*2).toInt()+1}")().play()

                destroyListeners.forEach { it.invoke(this@Stone) }

                val dir = if (velocityX > 0) 1 else -1
                this@Stone.addAction(
                        Actions.parallel(
                                Actions.moveBy(stage.width * dir, 0f, 0.5f, Interpolation.pow2),
                                Actions.fadeOut(0.5f, Interpolation.pow2)
                        ) then Actions.run { isVisible = false }
                )
            }


        })
    }

    fun crush() {
        isDestroyed = true

        poofEffect = poofEffectPool?.obtain()
        crushEffect = explosionEffectPool?.obtain()

        Sounds.valueOf("Crush_0${(Math.random()*7).toInt()+1}")().play()

        destroyListeners.forEach { it.invoke(this@Stone) }
    }

    override fun reset() {
        type = -1
        actionType = ActionType.Tap
        isDestroyed = false
        isVisible = true
        touchable = Touchable.enabled
        flipX = false

        poofEffect?.free()
        poofEffect = null

        crushEffect?.free()
        crushEffect = null

        isDisabled = false

        actions.clear()
    }

    override fun act(delta: Float) {
        super.act(delta)

        val effects = arrayOf(poofEffect, crushEffect).filterNotNull()
        if (effects.isNotEmpty()) {
            var allFinished = true
            for (effect in effects) {
                effect.emitters.forEach { it.tint.colors = floatArrayOf(color.r, color.g, color.b) }
                effect.setPosition(this@Stone.x + width / 2, this@Stone.y + height / 2)
                effect.update(delta)
                if (effect.isComplete) {
                    effect.free()
                } else {
                    allFinished = false
                }
            }

            if (poofEffect?.isComplete ?: false) poofEffect = null
            if (crushEffect?.isComplete ?: false) crushEffect = null

            if (allFinished) {
                isVisible = false
            }
        }

        hitbox.set(x + width * 0.25f, y, width * 0.5f, height)
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        val effects = arrayOf(poofEffect, crushEffect).filterNotNull()
        if (effects.isNotEmpty()) {
            effects.forEach { it.draw(batch) }
        } else {
            textures?.get(when (actionType) {
                ActionType.Tap -> "Stones/Stone${type+1}Broken"
                else -> "Stones/Stone${type+1}"
            })?.firstOrNull()?.let {
                drawWithTexture(batch, it, parentAlpha, flipX)
            }
        }
    }

    enum class ActionType {
        Move, Fling, Tap
    }

    companion object {
        val Width = 146f
        val Height = 89f

        val NUMBER_OF_TYPES = 3

        internal val explosionEffectPool: ParticleEffectPool? by Assets.getParticleEffect("particles/StoneExplosion.p")
        internal val poofEffectPool: ParticleEffectPool? by Assets.getParticleEffect("particles/StonePoof.p")
    }
}