package de.nopefrogking.screens

import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import de.nopefrogking.Main
import de.nopefrogking.utils.DefaultSkin
import de.nopefrogking.utils.NamedAsset


typealias ClickListener = ()->Unit

class ButtonType<out T>(val style: NamedAsset<T>, val width: Float, val height: Float) {
    companion object {
        val Button: ButtonType<TextButton.TextButtonStyle>
                = ButtonType(DefaultSkin.menuButton, 150f, 150f * 0.54f)

        val CircleToggle: ButtonType<TextButton.TextButtonStyle>
                = ButtonType(DefaultSkin.menuCircleToggle, Button.height, Button.height)

        val Circle: ButtonType<TextButton.TextButtonStyle>
                = ButtonType(DefaultSkin.menuCircle, Button.height, Button.height)

        val CircleMedium: ButtonType<TextButton.TextButtonStyle>
                = ButtonType(DefaultSkin.menuCircleMedium, CircleToggle.width*0.80f, CircleToggle.width*0.80f)

        val CircleSmall: ButtonType<TextButton.TextButtonStyle>
                = ButtonType(DefaultSkin.menuCircleSmall, CircleToggle.width*0.65f, CircleToggle.width*0.65f)

        val CircleSmallText: ButtonType<TextButton.TextButtonStyle>
                = ButtonType(DefaultSkin.menuCircleSmallText, CircleToggle.width*0.65f, CircleToggle.width*0.65f)

        val CircleXSText: ButtonType<TextButton.TextButtonStyle>
                = ButtonType(DefaultSkin.menuCircleXSText, CircleToggle.width*0.30f, CircleToggle.width*0.30f)

        val CircleXS: ButtonType<TextButton.TextButtonStyle>
                = ButtonType(DefaultSkin.menuCircleXS, CircleToggle.width*0.30f, CircleToggle.width*0.30f)

    }

}

/**
 *
 *
 * Represents one of many application screens, such as a main menu, a settings menu, the game screen and so on.
 *
 *
 *
 * Note that [.dispose] is not called automatically.
 *

 * @see Game
 */
interface BaseScreen {
    /**
     * The priority of the screen is used to calculate the order in which the screens are drawn.
     * 0 meaning it's the topmost screen, Integer.MAX_VALUE will be behind every other screen.
     * When two screens have the same priority, the one added first will be behind the one added later

     * @return The display priority of the screen
     */
    val priority: Int

    /**
     * Get the game to which this screen belongs

     * @return the game to which this screen belongs
     */
    val game: Main

    /**
     * Called when this screen becomes the current screen for a [Game].
     */
    fun show()

    /**
     * Called when the screen should update itself.

     * @param delta The time in seconds since the last render.
     */
    fun update(delta: Float)

    /**
     * Called when the screen should render itself.
     */
    fun render()

    /**
     * @see ApplicationListener.resize
     */
    fun resize(width: Int, height: Int)

    /**
     * @see ApplicationListener.pause
     */
    fun pause()

    /**
     * @see ApplicationListener.resume
     */
    fun resume()

    /**
     * Called when this screen is no longer the current screen for a [Game].
     */
    fun hide()

    /**
     * Called when this screen should release all resources.
     */
    fun dispose()

    /**
     * Goes back to the last screen in this stack
     * Returns false if this is the last screen in the stack
     * @return true if successful
     */
    fun goBack() = game.switchToLastScreen(this)

    /**
     * Switches to the given screen, putting the current screen on the backstack
     * @param disposeOnReturn wether the next screen should be disposed automatically, when we
     * return to the current screen
     * @return the next screen for method chaining
     */
    fun switchToScreen(screen: BaseScreen, disposeOnReturn: Boolean = true)
            = game.switchToScreen(this, screen, disposeOnReturn)

    /**
     * Get the InputProcessor for this screen
     * @return The InputProcessor for this screen
     */
    public val inputProcessor: InputProcessor
}
