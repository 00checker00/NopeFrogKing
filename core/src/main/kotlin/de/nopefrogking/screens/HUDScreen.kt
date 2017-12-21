package de.nopefrogking.screens

import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.utils.viewport.ExtendViewport
import de.nopefrogking.Item
import de.nopefrogking.Main
import de.nopefrogking.Sounds
import de.nopefrogking.ui.LevelProgressBar
import de.nopefrogking.ui.ScoreBar
import de.nopefrogking.ui.levelProgressBar
import de.nopefrogking.utils.*
import de.project.ice.screens.BaseScreenAdapter
import ktx.actors.onClick
import ktx.actors.parallelTo
import ktx.actors.then
import ktx.scene2d.*


open class HUDScreen(game: Main) : BaseScreenAdapter(game) {
    private val stage = Stage()
    private val scale = DefaultSkin.UIScale
    private val root: Table

    private lateinit var princessBar: LevelProgressBar
    private lateinit var princeBar: LevelProgressBar
    private val score = ScoreBar(DefaultSkin)
    private lateinit var bonusArea: WidgetGroup

    private lateinit var itemFlaskBtn: ImageTextButton
    private lateinit var itemOrbBtn: ImageTextButton
    private lateinit var itemUmbrellaBtn: ImageTextButton
    private lateinit var itemStormBtn: ImageTextButton

    private lateinit var topItemCooldown: Image
    private lateinit var topItemCooldownSprite: RadialSprite
    private lateinit var bottomItemCooldown: Image
    private lateinit var bottomItemCooldownSprite: RadialSprite

    override val inputProcessor: InputProcessor = DelegatingInputProcessor(stage)

    init {
//        if (Config.RENDER_DEBUG) {
//            stage.setDebugAll(true)
//        }
        stage.viewport = ExtendViewport(450f * scale, 800f * scale, OrthographicCamera())

        root = table(DefaultSkin) {
            setFillParent(true)
            stack { cell ->
                princeBar = levelProgressBar(DefaultSkin.prince()) {
                    top()
                    left()
                    padLeft(20 * scale)
                    padTop(20 * scale)

                    isVisible = false
                }
                princessBar = levelProgressBar(DefaultSkin.princess()) {
                    top()
                    left()
                    padLeft(20 * scale)
                    padTop(20 * scale)
                }

                val ratio = princessBar.prefHeight/princessBar.prefWidth
                cell.width(40f * scale)
                cell.height(40f * ratio * scale)
            }

            addSpacer().expandX()

            table(DefaultSkin) { cell ->
                cell.top()

                add(score)

                row()
                bonusArea = object: WidgetGroup() {
                    override fun layout() {
                        children.forEach {
                            it.x = width - it.width - 15f * scale
                        }
                    }
                }
                add(bonusArea).apply {
                    expandX()
                    fillX()
                }
            }
            row()

            textButton(FontIcon.Pause(), DefaultSkin.menuCircleXS.name, DefaultSkin) { cell ->
                cell.width(SIDEBAR_BTN_SIZE * scale)
                cell.height(SIDEBAR_BTN_SIZE * scale)

                onClick { _, _ ->
                    game.pauseGame()
                    game.addScreen(PauseScreen(game))
                    Sounds.click().play()
                }

                disableToggle()
            }
            row()

            stack { cell ->
                val ratio = DefaultSkin.ui_powerups_bg().minWidth / DefaultSkin.ui_powerups_bg().minHeight
                cell.width(SIDEBAR_BTN_SIZE * scale)
                cell.height(SIDEBAR_BTN_SIZE * scale / ratio)

                image(DefaultSkin.ui_powerups_bg.name, DefaultSkin) {
                    it.width = SIDEBAR_BTN_SIZE * scale
                    it.height = SIDEBAR_BTN_SIZE * scale / ratio
                }

                table {
                    stack {
                        it.width(ITEM_CIRCLE_SIZE * scale)
                        it.height(ITEM_CIRCLE_SIZE * scale)
                        it.padBottom(5 * scale)

                        itemFlaskBtn = imageTextButton(FontIcon.Item_Flask(), DefaultSkin.menuCircleGreenXS.name, DefaultSkin) {
                            isTransform = true
                            originX = ITEM_CIRCLE_SIZE * scale * 0.5f
                        }
                        itemStormBtn = imageTextButton(FontIcon.Item_Storm(), DefaultSkin.menuCircleYellowXS.name, DefaultSkin) {
                            isTransform = true
                            originX = ITEM_CIRCLE_SIZE * scale * 0.5f
                            setScale(0f, 1f)
                        }
                        topItemCooldownSprite = RadialSprite(DefaultSkin.ui_item_cooldown()).apply {
                            setAngle(360f)
                            onClick { _, _-> game.activateItem(if (!game.state.isBossFight) Item.Flask else Item.Storm) }
                        }
                        topItemCooldown = image(DefaultSkin.ui_item_cooldown.name, DefaultSkin).apply {
                            drawable = topItemCooldownSprite
                        }
                    }

                    row()
                    stack {
                        it.width(ITEM_CIRCLE_SIZE * scale)
                        it.height(ITEM_CIRCLE_SIZE * scale)
                        it.padBottom(5 * scale)

                        itemOrbBtn = imageTextButton(FontIcon.Item_Orb(), DefaultSkin.menuCircleGreenXS.name, DefaultSkin) {
                            isTransform = true
                            originX = ITEM_CIRCLE_SIZE * scale * 0.5f
                        }
                        itemUmbrellaBtn = imageTextButton(FontIcon.Item_Umbrella(), DefaultSkin.menuCircleYellowXS.name, DefaultSkin) {
                            isTransform = true
                            originX = ITEM_CIRCLE_SIZE * scale * 0.5f
                            setScale(0f, 1f)
                        }
                        bottomItemCooldownSprite = RadialSprite(DefaultSkin.ui_item_cooldown()).apply {
                            setAngle(360f)
                            onClick { _, _-> game.activateItem(if (!game.state.isBossFight) Item.Orb else Item.Umbrella) }
                        }
                        bottomItemCooldown = image(DefaultSkin.ui_item_cooldown.name, DefaultSkin).apply {
                            drawable = bottomItemCooldownSprite
                        }
                    }
                }
            }



            addSpacer().fillX().row()
            addSpacer().fillY()
        }

        stage.addActor(root)

        score.onClick { _, _ ->
        }

    }

    fun switchToPrince() {
        princeBar.isVisible = true
        princessBar.isVisible = false

        itemFlaskBtn.addAction(Actions.scaleTo(0f, 1f, ITEM_SWITCH_TIME) then Actions.run {
            itemStormBtn.addAction(Actions.scaleTo(1f, 1f, ITEM_SWITCH_TIME))
        })

        itemOrbBtn.addAction(Actions.scaleTo(0f, 1f, ITEM_SWITCH_TIME) then Actions.run {
            itemUmbrellaBtn.addAction(Actions.scaleTo(1f, 1f, ITEM_SWITCH_TIME))
        })
    }

    fun switchToPrincess() {
        princeBar.isVisible = false
        princessBar.isVisible = true

        itemStormBtn.addAction(Actions.scaleTo(0f, 1f, ITEM_SWITCH_TIME) then Actions.run {
            itemFlaskBtn.addAction(Actions.scaleTo(1f, 1f, ITEM_SWITCH_TIME))
        })

        itemUmbrellaBtn.addAction(Actions.scaleTo(0f, 1f, ITEM_SWITCH_TIME) then Actions.run {
            itemOrbBtn.addAction(Actions.scaleTo(1f, 1f, ITEM_SWITCH_TIME))
        })
    }

    override val priority: Int
        get() = 150


    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    var test = 0f
    override fun update(delta: Float) {
        princeBar.progress = game.state.levelProgress
        princessBar.progress = game.state.levelProgress
        score.score = game.state.score

        game.state.bonusPoints.forEach {
            val label = Label("+$it", DefaultSkin.bonusPointsLabel()).apply {
                y = (bonusArea.children.minBy { it.y }?.y ?: 0f) - height
                addAction(
                        (Actions.alpha(0f, 0f) parallelTo Actions.moveBy(0f, 3*scale, 0f))
                                then (Actions.fadeIn(0.5f) parallelTo Actions.moveBy(0f, -3*scale, 0.2f))
                                then (Actions.fadeOut(0.5f) parallelTo Actions.moveBy(0f, -y, 1f))
                                then Actions.run { remove() }
                )
            }
            bonusArea.addActor(label)
        }
        game.state.bonusPoints.clear()

        if (game.state.isBossFight) {
            if (!princeBar.isVisible)
                switchToPrince()

            topItemCooldownSprite.setAngle(360f - (game.state.Cooldowns.getOrDefault(Item.Storm, 0f) * 360))
            bottomItemCooldownSprite.setAngle(360f - (game.state.Cooldowns.getOrDefault(Item.Umbrella, 0f) * 360))
        } else {
            if (!princessBar.isVisible)
                switchToPrincess()

            topItemCooldownSprite.setAngle(360f - (game.state.Cooldowns.getOrDefault(Item.Flask, 0f) * 360))
            bottomItemCooldownSprite.setAngle(360f - (game.state.Cooldowns.getOrDefault(Item.Orb, 0f) * 360))
        }

        stage.act(delta)
    }


    override fun show() {
        game.addScreen(GameMessageScreen(game))
    }

    override fun hide() {
        game.removeScreen(GameMessageScreen::class.java, true)
    }

    override fun render() {
        if (!game.state.isPaused) {
            stage.viewport.apply()
            stage.draw()
        }
    }

    override fun dispose() {
    }

    companion object {
        private val SIDEBAR_BTN_SIZE = 50
        private val ITEM_CIRCLE_SIZE = SIDEBAR_BTN_SIZE * 0.8f
        private val ITEM_SWITCH_TIME = 0.2f
    }
}
