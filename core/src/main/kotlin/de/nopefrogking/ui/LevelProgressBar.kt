package de.nopefrogking.ui

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import de.nopefrogking.utils.AssetDelegateProvider
import de.nopefrogking.utils.clamp
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.actor
import kotlin.properties.Delegates

class LevelProgressBar(style: LevelProgressBarStyle): WidgetGroup() {
    constructor(skin: Skin, styleName: String = "default"): this(skin.get(styleName, LevelProgressBarStyle::class.java))

    val handle = Image()
    var style: LevelProgressBarStyle by Delegates.observable(style) { _, _, value ->
        applyStyle()
    }

    var progress by clamp(0.0f, 1.0f) { 0.6f }
    var boss by clamp(0.0f, 1.0f) { 0.3f }

    init {
        addActor(handle)

        applyStyle()
    }

    private fun applyStyle() {
        handle.drawable = style.handle
        invalidateHierarchy()
    }

    override fun layout() {
        handle.width  = width * style.handleSize
        handle.height = handle.width
        handle.layout()
        handle.validate()
    }

    override fun act(delta: Float) {
        super.act(delta)

        handle.y = height * (1f-progress) - handle.height/2
        handle.x = (width - handle.width) / 2
    }

    override fun getPrefWidth(): Float = style.background?.minWidth ?: 0f
    override fun getPrefHeight(): Float = style.background?.minHeight ?: 0f

    override fun draw(batch: Batch, parentAlpha: Float) {

        style.background?.draw(batch, x, y, width, height)

        batch.flush()
        if (clipBegin(x, y, width, height * boss)) {
            style.boss?.draw(batch, x, y, width, height)
            batch.flush()
            clipEnd()
        }

        batch.flush()
        if (clipBegin(x, y + height * (1f-progress), width, height * progress)) {
            style.foreground?.draw(batch, x, y, width, height)
            batch.flush()
            clipEnd()
        }

        super.draw(batch, parentAlpha)
    }

    data class LevelProgressBarStyle(
            var background: Drawable? = null,
            var foreground: Drawable? = null,
            var boss: Drawable? = null,
            var handle: Drawable? = null,
            var handleSize: Float = 1.0f
    )
}

fun Skin.LevelProgressBarStyle(name: String = "default", init: LevelProgressBar.LevelProgressBarStyle.()->Unit): LevelProgressBar.LevelProgressBarStyle {
    val style = LevelProgressBar.LevelProgressBarStyle().apply { init() }
    add(name, style)
    return style
}
fun Skin.levelProgressBarStyle(init: LevelProgressBar.LevelProgressBarStyle.()->Unit) = AssetDelegateProvider({LevelProgressBarStyle(it, init)}, {get(it, LevelProgressBar.LevelProgressBarStyle::class.java)})
fun Skin.levelProgressBarStyle(name: String, init: LevelProgressBar.LevelProgressBarStyle.()->Unit) = AssetDelegateProvider({LevelProgressBarStyle(it, init)}, {get(it, LevelProgressBar.LevelProgressBarStyle::class.java)})


fun <S> KWidget<S>.levelProgressBar(skin: Skin = Scene2DSkin.defaultSkin, styleName: String = "default", init: (LevelProgressBar.(S) -> Unit)? = null) =
        actor(LevelProgressBar(skin, styleName)) {
            init?.invoke(this, it)
        }

fun <S> KWidget<S>.levelProgressBar(style: LevelProgressBar.LevelProgressBarStyle, init: (LevelProgressBar.(S) -> Unit)? = null) =
        actor(LevelProgressBar(style)) {
            init?.invoke(this, it)
        }
