package de.nopefrogking.screens

import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import de.nopefrogking.Main
import de.nopefrogking.utils.DefaultSkin
import de.nopefrogking.utils.FontIcon


class PauseScreen(game: Main): WindowScreen(game, "Pause", FontIcon.Pause, false, false) {

    init {
        addButton(FontIcon.Home) {
            game.removeScreen(GameScreen::class.java, true)
            game.removeScreen(this, true)
            game.addScreen(MainMenuScreen(game))
        }

        addButton(FontIcon.Repeat) {
            game.removeScreen(this, true)
            game.startNewGame()
        }

        addButton(FontIcon.Play) {
            game.removeScreen(this, true)
            game.resumeGame()
        }

        isClosable = false
    }

    override fun createContentView(table: Table, tab: Int) {
        table.add(Label("Score: ${game.state.score.toInt()}", DefaultSkin.menu()))
    }
}