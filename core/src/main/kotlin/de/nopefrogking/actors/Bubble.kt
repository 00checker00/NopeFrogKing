package de.nopefrogking.actors

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Container
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.scenes.scene2d.utils.Layout
import com.badlogic.gdx.utils.Scaling
import de.nopefrogking.Assets
import de.nopefrogking.utils.DefaultSkin
import de.nopefrogking.utils.NamedAsset
import de.nopefrogking.utils.UIScale
import ktx.actors.onClick
import ktx.actors.parallelTo
import ktx.actors.then

class Bubble(val icon: NamedAsset<Drawable>, animated: Boolean = true, destroyable: Boolean = true): WidgetGroup() {
    val bubblePop by Assets.getAnimation("bubble_pop") {
        val content = Container(Image(icon(), Scaling.stretch)).apply {
            pad(7f * DefaultSkin.UIScale)
        }
        add(content)

        val bubble = AnimationWidget(it).apply {
            if (animated) {
                addAction(Actions.forever(
                        Actions.scaleTo(1f, SQUEEZE_STRENGTH, ANIMATION_DURATION)
                                then Actions.scaleTo(1f, 1f, ANIMATION_DURATION)
                                then Actions.scaleTo(SQUEEZE_STRENGTH, 1f, ANIMATION_DURATION)
                                then Actions.scaleTo(1f, 1f, ANIMATION_DURATION)
                ))
            }
            showFrame(0)
            originX = width/2
            originY = height/2
        }
        add(bubble)

        if (destroyable) {
            bubble.onClick { _, _ ->
                val label = Label("+", DefaultSkin.bonusPointsLabel()).apply {
                    width = prefWidth
                    height = prefHeight
                    val pos = content.localToStageCoordinates(Vector2(0f, 0f))
                    x = pos.x - width
                    y = pos.y + content.height/2 - height/2
                }
                stage.addActor(label)

                content.addAction(
                        (Actions.moveBy(0f, 200f, 1f) parallelTo Actions.fadeOut(0.5f))
                                then Actions.run { this@Bubble.remove() }
                )

                label.addAction(
                        (Actions.moveBy(0f, 200f, 1f) parallelTo Actions.fadeOut(0.5f))
                                then Actions.run { label.remove() }
                )

                bubble.showFrame(-1)
            }
        }
    }

    init {
        bubblePop
    }

    override fun getPrefHeight(): Float = height
    override fun getMinHeight(): Float = height
    override fun getMaxHeight(): Float = height
    override fun getPrefWidth(): Float = width
    override fun getMinWidth(): Float = width
    override fun getMaxWidth(): Float = width

    fun add(actor: Actor) {
        addActor(actor)
    }

    override fun layout() {
        children.forEach {
            it.setBounds(0f, 0f, width, height)
            if (it is Layout) it.validate()
        }
    }

    companion object {
        val ANIMATION_DURATION = 2f
        val SQUEEZE_STRENGTH = 0.9f
    }
}