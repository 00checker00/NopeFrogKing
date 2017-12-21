package de.nopefrogking.utils


import com.badlogic.gdx.InputProcessor

/**
 * InputProcessor which delegates all call to another InputProcessor
 * and sets all events to handled => They won't be processed any further
 * Usage: Subclass and selectively override Events your interested in
 */
open class DelegatingBlockingInputProcessor(private val processor: InputProcessor) : InputProcessor, DetachableInputProcessor {
    override var detached: Boolean = false

    override fun keyDown(keycode: Int): Boolean {
        if (!detached) processor.keyDown(keycode)
        return true
    }

    override fun keyUp(keycode: Int): Boolean {
        if (!detached) processor.keyUp(keycode)
        return true
    }

    override fun keyTyped(character: Char): Boolean {
        if (!detached) processor.keyTyped(character)
        return true
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        if (!detached) processor.touchDown(screenX, screenY, pointer, button)
        return true
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        if (!detached) processor.touchUp(screenX, screenY, pointer, button)
        return true
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        if (!detached) processor.touchDragged(screenX, screenY, pointer)
        return true
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        if (!detached) processor.mouseMoved(screenX, screenY)
        return true
    }

    override fun scrolled(amount: Int): Boolean {
        if (!detached) processor.scrolled(amount)
        return true
    }
}
