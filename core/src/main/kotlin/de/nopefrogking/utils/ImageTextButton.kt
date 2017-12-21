package de.nopefrogking.utils

import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton
import com.badlogic.gdx.scenes.scene2d.ui.Skin

fun ImageTextButton.setDrawable(skin: Skin, drawable: String) {
    style = ImageTextButton.ImageTextButtonStyle(style)
    style.imageUp = DefaultSkin.getDrawable(drawable)
    style.imageDown = style.imageUp
    style.imageChecked = style.imageUp
    style.imageCheckedOver = style.imageUp
    style.imageDisabled = style.imageUp
    style.imageOver = style.imageUp
    style.imageDisabled = style.imageUp
}