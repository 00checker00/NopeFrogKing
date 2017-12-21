package de.nopefrogking.screens

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.viewport.ExtendViewport
import de.nopefrogking.Main
import de.nopefrogking.utils.DefaultSkin
import de.nopefrogking.utils.UIScale
import de.nopefrogking.utils.addSpacer
import de.project.ice.screens.BaseScreenAdapter
import ktx.actors.alpha
import ktx.actors.parallelTo
import ktx.actors.then
import ktx.collections.isNotEmpty
import ktx.scene2d.table


open class GameMessageScreen(game: Main) : BaseScreenAdapter(game) {
    private val stage = Stage()
    private val scale = DefaultSkin.UIScale
    private val root: Table
    private val messageLabel = Label("Hello", DefaultSkin.gameMessage())
    private var messageRunning = false

    init {
//        if (Config.RENDER_DEBUG) {
//            stage.setDebugAll(true)
//        }
        stage.viewport = ExtendViewport(450f * scale, 800f * scale, OrthographicCamera())

        root = table(DefaultSkin) {
            setFillParent(true)

            addSpacer().height(100 * scale).row()

            add(messageLabel).expandX().center().row()

            addSpacer().expand().row()
        }

        messageLabel.alpha = 0.0f

        stage.addActor(root)
    }

    override val priority: Int
        get() = 130


    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun update(delta: Float) {
        stage.act(delta)

        if (game.state.messages.isNotEmpty() && !messageRunning) {
            messageRunning = true

            val message = game.state.messages.first()
            game.state.messages.removeIndex(0)

            messageLabel.setText(message)

            messageLabel.addAction(
                    Actions.fadeOut(0f) then
                            (Actions.moveBy(0f, -30f * scale, 2.0f) parallelTo Actions.fadeIn(2.0f)) then
                                Actions.delay(1.0f) then
                            (Actions.moveBy(0f, -30f * scale, 2.0f) parallelTo Actions.fadeOut(1.0f)) then
                                Actions.run {
                                    messageLabel.alpha = 0f
                                    messageRunning = false
                                }
            )
        }
    }

    override fun render() {
        stage.viewport.apply()
        stage.draw()
    }

    override fun dispose() {
        stage.dispose()
    }
}
