package de.nopefrogking.utils


import com.badlogic.gdx.InputProcessor

/**
 * InputProcessor which just delegates all call to another InputProcessor
 * Usage: Subclass and selectively override Events your interested in
 */
open class DelegatingInputProcessor(protected var processor: InputProcessor) : InputProcessor, DetachableInputProcessor {
    override var detached: Boolean = false

    override fun keyDown(keycode: Int): Boolean = if (!detached) processor.keyDown(keycode) else false
    override fun keyUp(keycode: Int): Boolean = if (!detached) processor.keyUp(keycode) else false
    override fun keyTyped(character: Char): Boolean = if (!detached) processor.keyTyped(character) else false
    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean = if (!detached) processor.touchDown(screenX, screenY, pointer, button) else false
    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean = if (!detached) processor.touchUp(screenX, screenY, pointer, button) else false
    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean = if (!detached) processor.touchDragged(screenX, screenY, pointer) else false
    override fun mouseMoved(screenX: Int, screenY: Int): Boolean = if (!detached) processor.mouseMoved(screenX, screenY) else false
    override fun scrolled(amount: Int): Boolean = if (!detached) processor.scrolled(amount) else false
}
