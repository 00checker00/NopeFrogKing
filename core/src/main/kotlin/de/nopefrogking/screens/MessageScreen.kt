package de.nopefrogking.screens

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.viewport.ScreenViewport
import de.nopefrogking.Main
import de.nopefrogking.utils.DefaultSkin
import de.nopefrogking.utils.RoundedRectangleDrawable
import de.project.ice.screens.BaseScreenAdapter

class MessageScreen(game: Main, vararg messages: String, private val timeout: Float = 2.0f) : BaseScreenAdapter(game) {
    private val messages = Array(messages).apply { reverse() }

    private val stage = Stage()
    private val root = Table(DefaultSkin)
    private val messageLabel: Label

    private var remainingTime = timeout + FADE_TIME

    init {
        stage.viewport = ScreenViewport()

        stage.addActor(root)

        root.setFillParent(true)
        root.bottom()

        val background = object: Image(RoundedRectangleDrawable(Color(0f, 0f, 0f, 0.4f), true, 5f)) {
            override fun draw(batch: Batch?, parentAlpha: Float) {
                (drawable as? RoundedRectangleDrawable)?.alpha = parentAlpha
                super.draw(batch, parentAlpha)
            }
        }

        messageLabel = Label(null, DefaultSkin.message())

        val stack = Stack(background, Container(messageLabel).pad(5f))
        root.add(Container(stack).padBottom(200f))

        showNextMessage()
    }

    fun addMessages(vararg messages: String) {
        messages.forEach { this.messages.insert(0, it) }
    }

    private fun showNextMessage() {
        if (messages.size == 0) {
            game.removeScreen(this)
            return
        }

        messageLabel.setText(messages.pop())

        stage.addAction(Actions.sequence(Actions.alpha(0f), Actions.fadeIn(FADE_TIME)))
    }

    override val priority: Int
        get() = 90

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun update(delta: Float) {
        stage.act(delta)
        remainingTime -= delta
        if (remainingTime <= -FADE_TIME) {
            showNextMessage()
            remainingTime = timeout + FADE_TIME
        } else if (remainingTime <= 0) {
            stage.addAction(Actions.fadeOut(FADE_TIME))
        }
    }

    override fun render() {
        stage.viewport.apply()
        stage.draw()
    }

    override fun dispose() {
        stage.dispose()
    }

    companion object {
        val FADE_TIME = 0.3f
    }
}
