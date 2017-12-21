package de.nopefrogking.utils

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import de.nopefrogking.screens.ButtonType
import de.nopefrogking.screens.ClickListener
import ktx.actors.onChange
import ktx.actors.onClick

fun connectButtons(vararg buttons: Button, listener: ClickListener? = null) {
    buttons.forEach { button ->
        button.setProgrammaticChangeEvents(false)

        button.addListener(object: InputListener() {
            val buttons = buttons

            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, mouseBtn: Int) {
                buttons.filterNot { it == button }.forEach {
                    it.isChecked = false
                }
            }

            override fun enter(event: InputEvent, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
                buttons.filterNot { it == button }.forEach {
                    it.isChecked = button.isPressed
                }
            }

            override fun exit(event: InputEvent, x: Float, y: Float, pointer: Int, toActor: Actor?) {
                buttons.filterNot { it == button }.forEach {
                    it.isChecked = false
                }
            }

            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, mouseBtn: Int): Boolean {
                buttons.filterNot { it == button }.forEach {
                    it.isChecked = true
                }
                return true
            }
        })

        button.onChange { _, _ ->
            buttons.forEach {
                it.isChecked = false
            }
        }

        button.onClick { _, _ -> listener?.invoke() }
    }
}

fun Button.disableToggle() {
    onChange { _, _ -> isChecked = false }
}

fun Button.addSubIcon(style: ImageTextButton.ImageTextButtonStyle, icon: FontIcon = FontIcon.Nothing, drawable: Drawable? = null, offset: Float = 5.0f * scale, size: Float = ButtonType.CircleXSText.width * scale)
    = addSubIcon(style, icon(), drawable, offset)

fun Button.addSubIcon(style: ImageTextButton.ImageTextButtonStyle, text: String, drawable: Drawable? = null, offset: Float = 5.0f * scale, size: Float = ButtonType.CircleXSText.width * scale): ImageTextButton {
    val btn = ImageTextButton(text, style)
    if (drawable != null)
        btn.image.drawable = drawable

    add(btn)
            .width(size)
            .height(size)
            .bottom()
            .padLeft(-ButtonType.CircleXSText.height * scale + offset)
            .padBottom(-offset)
            .padRight(-offset)
            .row()
    return btn
}

fun Button.addSuperIcon(style: ImageTextButton.ImageTextButtonStyle, icon: FontIcon = FontIcon.Nothing, drawable: Drawable? = null, offset: Float = 5.0f * scale, size: Float = ButtonType.CircleXSText.width * scale)
        = addSuperIcon(style, icon(), drawable, offset)

fun Button.addSuperIcon(style: ImageTextButton.ImageTextButtonStyle, text: String, drawable: Drawable? = null, offset: Float = 5.0f * scale, size: Float = ButtonType.CircleXSText.width * scale): ImageTextButton {
    val btn = ImageTextButton(text, style)
    if (drawable != null)
        btn.image.drawable = drawable

    add(btn)
            .width(size)
            .height(size)
            .top()
            .padLeft(-ButtonType.CircleXSText.height * scale + offset)
            .padTop(-offset)
            .padRight(-offset)
            .row()
    return btn
}