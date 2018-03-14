package de.nopefrogking.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.utils.viewport.ExtendViewport
import de.nopefrogking.I18N
import de.nopefrogking.Main
import de.nopefrogking.Sounds
import de.nopefrogking.Videos
import de.nopefrogking.utils.*
import de.project.ice.screens.BaseScreenAdapter
import ktx.actors.onChange
import ktx.actors.onClick
import ktx.actors.then
import java.util.*
import com.badlogic.gdx.utils.Array as GdxArray

open class MainMenuScreen(game: Main) : BaseScreenAdapter(game) {
    private val stage = Stage()
    private val root = Table(DefaultSkin)
    private val scale = DefaultSkin.UIScale
    private val menuLayout = VerticalGroup()
    private val buttons = HashMap<String, TextButton>()

    private val buttonGroups = HashMap<String, HorizontalGroup>()

    override val inputProcessor: DetachableInputProcessor = DelegatingBlockingInputProcessor(stage)

    init {
//        if (Config.RENDER_DEBUG) {
//            stage.setDebugAll(true)
//        }
        stage.viewport = ExtendViewport(450f * scale, 800f * scale, OrthographicCamera())

        stage.addActor(root)

        root.setFillParent(true)

        root.addSpacer().height(20f * scale).row()

        val logo = Image(DefaultSkin.logo())
        root.add(logo).width(300 * scale).height(300 * logo.height/logo.width * scale)

        root.row()
        root.addSpacer().expandY()

        root.row()
        menuLayout.fill()
        menuLayout.space(5f * scale)
        root.add(menuLayout)

        connectButtons(
                insertMenuButton(ButtonProperties(BUTTON_PLAY_ICON_ID, ButtonType.CircleToggle), FontIcon.Play(), BUTTON_PLAY_GROUP_ID),
                insertMenuButton(ButtonProperties(BUTTON_PLAY_TEXT_ID, ButtonType.Button), I18N.menu_play(), BUTTON_PLAY_GROUP_ID)) {
            val duration = 3f
//            game.addScreen(CircleFadeScreen(game, duration).apply { centerOffset.y = 50 * scale })
            inputProcessor.detached = true
//            stage.addAction(Actions.delay(duration) then Actions.run {
//                game.startNewGame()
//                game.removeScreen(this, true)
//                game.removeScreen(CircleFadeScreen::class.java, true)
//                inputProcessor.detached = false
//            })
            val rect = Rectangle().apply {
                height = Gdx.graphics.height.toFloat()
                width = Videos.intro.width.toFloat()/Videos.intro.height.toFloat() * height
                y = 0f
                x = Gdx.graphics.width/2f - width/2
            }
            PlatformHandler.Instance?.showVideo(Gdx.files.internal(Videos.intro.path), rect) {
                game.startNewGame()
                game.removeScreen(this, true)
                inputProcessor.detached = false
            }
            game.getScreen<MenuBackgroundScreen>()?.stopMusic()
        }

        connectButtons(
                insertMenuButton(ButtonProperties(BUTTON_SHOP_ICON_ID, ButtonType.CircleToggle), FontIcon.Shop(), BUTTON_SHOP_GROUP_ID),
                insertMenuButton(ButtonProperties(BUTTON_SHOP_TEXT_ID, ButtonType.Button), I18N.menu_shop(), BUTTON_SHOP_GROUP_ID)) {
            switchToScreen(ShopScreen(game))
        }

        connectButtons(
                insertMenuButton(ButtonProperties(BUTTON_CREDITS_TEXT_ID, ButtonType.Button), I18N.menu_credits(), BUTTON_CREDITS_GROUP_ID),
                insertMenuButton(ButtonProperties(BUTTON_CREDITS_ICON_ID, ButtonType.CircleToggle), FontIcon.Credits(), BUTTON_CREDITS_GROUP_ID)) {
            game.showToastMessages("Soon")
        }

        createButtonGroup(BUTTON_VARIOUS_GROUP_ID).apply { center() }
        insertMenuButton(ButtonProperties(BUTTON_DAILY_ID, ButtonType.CircleSmall), FontIcon.Present(), BUTTON_VARIOUS_GROUP_ID) {
            switchToScreen(GiftScreen(game))
        }
        insertMenuButton(ButtonProperties(BUTTON_HIGHSCORE_ID, ButtonType.CircleSmall), FontIcon.Highscore(), BUTTON_VARIOUS_GROUP_ID) {
            PlatformHandler.Instance?.displayLeaderBoard()
        }
        insertMenuButton(ButtonProperties(BUTTON_INFO_ID, ButtonType.CircleSmall), FontIcon.Info(), BUTTON_VARIOUS_GROUP_ID) {
            switchToScreen(TutorialScreen(game))
        }
        insertMenuButton(ButtonProperties(BUTTON_SETTINGS_ID, ButtonType.CircleSmall), FontIcon.Options(), BUTTON_VARIOUS_GROUP_ID) {
            game.showToastMessages("Soon")
        }

        root.row()
        root.addSpacer().height(100f * scale)

        game.addScreen(MenuBackgroundScreen(game))
    }

    override val priority: Int
        get() = 100


    protected fun createMenuButtonAfter(properties: ButtonProperties, text: String, idAfter: String, listener: ClickListener? = null): TextButton {

        return createButtonGroup(properties.id).let {

            val button = createButton(properties, text)
            if (listener != null) {
                button.onClick { _, _ -> listener() }
            }


            it.addActor(button)
            buttons.put(properties.id, button)

            menuLayout.addActorAfter(buttonGroups[idAfter], it)

            button
        }

    }

    protected fun insertMenuButton(properties: ButtonProperties, text: String, idGroup: String, listener: ClickListener? = null): TextButton {
        val group = buttonGroups[idGroup] ?: createButtonGroup(idGroup)

        return group.let {

            val button = createButton(properties, text)
            if (listener != null) {
                button.onClick { _, _ -> listener() }
            }

            it.addActor(button)
            buttons[properties.id] = button

            button
        }

    }

    protected fun createMenuButton(properties: ButtonProperties, text: String, listener: ClickListener? = null): TextButton {

        return createButtonGroup(properties.id).let {

            val button = createButton(properties, text)
            if (listener != null) {
                button.onClick { _, _ -> listener() }
            }


            it.addActor(button)
            buttons.put(properties.id, button)

            menuLayout.addActor(it)

            button
        }

    }

    private fun createButtonGroup(id: String): HorizontalGroup {

        return HorizontalGroup().apply {
            this.space(5.0f * scale)
            this.center()

            buttonGroups[id] = this

            menuLayout.addActor(this)
        }

    }

    private fun createButton(properties: ButtonProperties, text: String): TextButton {

        return object: TextButton(text, properties.type.style()){
            override fun getPrefWidth(): Float = properties.type.width * scale
            override fun getPrefHeight(): Float = properties.type.height * scale
        }.apply {
            onChange { _, _ -> isChecked = false }
            onClick { _, _ -> Sounds.click().play() }
        }
    }


    protected fun getButton(id: String): TextButton {
        return buttons[id]!!
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun update(delta: Float) {
        stage.act(delta)
    }

    override fun render() {
        stage.viewport.apply()
        stage.draw()
    }


    override fun dispose() {
        stage.dispose()
        game.removeScreen(MenuBackgroundScreen::class.java, true)
    }

    protected data class ButtonProperties(val id: String, val type: ButtonType<TextButton.TextButtonStyle> = ButtonType.Button)

    companion object {
        val BUTTON_PLAY_ICON_ID = "BTN_PLAY_ID_ICON_ID"
        val BUTTON_PLAY_TEXT_ID = "BTN_PLAY_ID_TEXT_ID"
        val BUTTON_PLAY_GROUP_ID = "BTN_PLAY_ID_GROUP_ID"
        val BUTTON_SHOP_ICON_ID = "BTN_SHOP_ICON_ID"
        val BUTTON_SHOP_TEXT_ID = "BTN_SHOP_TEXT_ID"
        val BUTTON_SHOP_GROUP_ID = "BTN_SHOP_GROUP_ID"
        val BUTTON_CREDITS_ICON_ID = "BTN_CREDITS_ICON_ID"
        val BUTTON_CREDITS_TEXT_ID = "BTN_CREDITS_TEXT_ID"
        val BUTTON_CREDITS_GROUP_ID = "BTN_CREDITS_GROUP_ID"
        val BUTTON_VARIOUS_GROUP_ID = "BUTTON_VARIOUS_GROUP_ID"
        val BUTTON_DAILY_ID = "BUTTON_DAILY_ID"
        val BUTTON_HIGHSCORE_ID = "BUTTON_HIGHSCORE_ID"
        val BUTTON_INFO_ID = "BUTTON_INFO_ID"
        val BUTTON_SETTINGS_ID = "BUTTON_SETTINGS_ID"
    }
}
