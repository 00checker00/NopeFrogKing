package de.project.ice.screens

import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.InputProcessor
import de.nopefrogking.Main
import de.nopefrogking.screens.BaseScreen

abstract class BaseScreenAdapter(override val game: Main) : BaseScreen {
    /**
     * {@inheritDoc}
     * @return The InputProcessor for this screen, base implementation returns an empty InputProcessor
     */
    public override val inputProcessor: InputProcessor = InputAdapter()

    /**
     * {@inheritDoc}
     * @return The display priority of the screen, base implementation returns 0
     */
    override val priority = 0

    override fun show() {
    }

    override fun update(delta: Float) {
    }

    override fun render() {
    }

    override fun resize(width: Int, height: Int) {
    }

    override fun pause() {
    }

    override fun resume() {
    }

    override fun hide() {
    }

    override fun dispose() {
    }
}
