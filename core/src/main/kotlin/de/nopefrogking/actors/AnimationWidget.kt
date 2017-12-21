package de.nopefrogking.actors

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.ui.Widget
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import de.nopefrogking.utils.getKeyFrameAt
import kotlin.properties.Delegates


open class AnimationWidget(animation: Animation<TextureRegion>? = null): WidgetGroup() {

    private var time = 0f
    private var animationX = 0f
    private var animationY = 0f
    private var animationWidth = 0f
    private var animationHeight = 0f
    private var lockedToFrame = -1

    var animation: Animation<TextureRegion>? by Delegates.observable(animation) {  _, _, _ ->
        time = 0f; invalidate()
    }

    var paused: Boolean by Delegates.observable(false) { _, old, value ->
        if (old && !value) lockedToFrame = -1
        invalidate()
    }

    var scaling: Scaling by Delegates.observable(Scaling.stretch) { _, _, _ -> invalidate()}
    var align: Int by Delegates.observable(Align.center) { _, _, _ -> invalidate() }

    private val currentFrame: TextureRegion? get() = when {
        lockedToFrame > -1 -> animation?.getKeyFrameAt(lockedToFrame)
        else -> animation?.getKeyFrame(time)
    }

    override fun getPrefWidth(): Float {
        return currentFrame?.regionWidth?.toFloat() ?: 0f
    }

    override fun getPrefHeight(): Float {
        return currentFrame?.regionHeight?.toFloat() ?: 0f
    }

    val animationFinished: Boolean get() = animation?.isAnimationFinished(time) ?: false

    override fun act(delta: Float) {
        super.act(delta)
        if (!paused && lockedToFrame == -1)
            time += delta
    }

    override fun layout() {
        currentFrame?.let { frame ->
            val regionWidth = frame.regionWidth.toFloat()
            val regionHeight = frame.regionHeight.toFloat()
            val width = width
            val height = height

            val size = scaling.apply(regionWidth, regionHeight, width, height)
            animationWidth = size.x
            animationHeight = size.y

            if (align and Align.left != 0)
                animationX = 0f
            else if (align and Align.right != 0)
                animationX = width - animationWidth
            else
                animationX = width / 2 - animationWidth / 2

            if (align and Align.top != 0)
                animationY = height - animationHeight
            else if (align and Align.bottom != 0)
                animationY = 0f
            else
                animationY = height / 2 - animationHeight / 2
        }
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        validate()

        currentFrame?.let { frame ->
            batch.setColor(color.r, color.g, color.b, color.a * parentAlpha)
            batch.draw(frame, x + animationX, y + animationY, originX - animationX, originY - animationY, animationWidth, animationHeight, scaleX, scaleY, rotation)
        }

        super.draw(batch, parentAlpha)
    }

    fun showFrame(frame: Int) {
        lockedToFrame = frame
    }
}