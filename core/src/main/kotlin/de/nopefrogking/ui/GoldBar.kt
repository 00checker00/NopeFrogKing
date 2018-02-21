package de.nopefrogking.ui

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import de.nopefrogking.utils.scale

class GoldBar(skin: Skin) : IconBar(skin, "coin", IconBarOptions().apply { barStyle = "ui_premium_bar"; barEndStyle = "ui_premium_end"  }) {
    var gold: Long = 0L
        set(value) {
            field = value
            text = value.toString()
        }

    init {
        isTransform = true

        originY = options.barHeight/2 * scale
    }
}
