package de.nopefrogking.ui

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import de.nopefrogking.utils.FontIcon

class ScoreBar(skin: Skin) : IconBar(skin, FontIcon.Progress) {

    var score: Float = 0f
        set(value) {
            field = value
            text = String.format("%.0f", score)
        }
}
