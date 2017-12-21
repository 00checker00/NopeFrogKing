package de.nopefrogking.actors

import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import de.nopefrogking.utils.DefaultSkin
import de.nopefrogking.utils.FontIcon
import de.nopefrogking.utils.NamedAsset
import de.nopefrogking.utils.UIScale

class Bubble(val icon: NamedAsset<Drawable>): Stack() {
    init {
        add(Container(Image(icon(), Scaling.stretch)).apply {
            pad(7f * DefaultSkin.UIScale)
        })
        add(Image(DefaultSkin.ui_bubble(), Scaling.stretch))
    }

    override fun getPrefHeight(): Float = height
    override fun getMinHeight(): Float = height
    override fun getMaxHeight(): Float = height
    override fun getPrefWidth(): Float = width
    override fun getMinWidth(): Float = width
    override fun getMaxWidth(): Float = width
}