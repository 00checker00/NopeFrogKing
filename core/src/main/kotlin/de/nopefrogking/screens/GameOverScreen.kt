package de.nopefrogking.screens

import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import de.nopefrogking.Main
import de.nopefrogking.utils.DefaultSkin
import de.nopefrogking.utils.FontIcon

class GameOverScreen(game: Main) : WindowScreen(game, "Game Over", FontIcon.Highscore, false, false) {

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

        isClosable = false
    }

    override fun createContentView(table: Table, tab: Int) {
        table.add(Label("Score: ${game.state.score.toInt()}", DefaultSkin.menu()))
    }

    override fun createSheetView(table: Table) {
        table
                .add(Label("00000000", DefaultSkin.menu()))
                .expand()
                .pad(0.0f, 15*scale, 10*scale, 15*scale)
                .align(Align.right)
    }
}