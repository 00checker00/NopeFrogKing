package de.nopefrogking.ui

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Window


class Panel(skin: Skin, styleName: String = "default"): Window("", skin, styleName) {
    init {
        isMovable = false
        isResizable = false
    }
}