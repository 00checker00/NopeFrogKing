package de.nopefrogking.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.utils.viewport.ExtendViewport
import de.nopefrogking.GameState
import de.nopefrogking.Item
import de.nopefrogking.Main
import de.nopefrogking.Sounds
import de.nopefrogking.ui.GoldBar
import de.nopefrogking.ui.LevelProgressBar
import de.nopefrogking.ui.ScoreBar
import de.nopefrogking.ui.levelProgressBar
import de.nopefrogking.utils.*
import de.project.ice.screens.BaseScreenAdapter
import ktx.actors.onClick
import ktx.actors.parallelTo
import ktx.actors.then
import ktx.collections.isNotEmpty
import ktx.scene2d.*


open class HUDScreen(game: Main) : BaseScreenAdapter(game) {
    private val stage = Stage()
    private val scale = DefaultSkin.UIScale
    private val root: Table

    private lateinit var princessBar: LevelProgressBar
    private lateinit var princeBar: LevelProgressBar

    private val score = ScoreBar(DefaultSkin)
    private val gold = GoldBar(DefaultSkin)
    private lateinit var bonusArea: WidgetGroup

    val scoreVisible get() = score.scaleY == 1f
    val goldVisible get() = gold.scaleY == 1f

    private lateinit var itemFlaskBtn: ImageTextButton
    private lateinit var itemOrbBtn: ImageTextButton
    private lateinit var itemUmbrellaBtn: ImageTextButton
    private lateinit var itemStormBtn: ImageTextButton

    private lateinit var topItemCooldown: Image
    private lateinit var topItemCooldownSprite: RadialSprite
    private lateinit var topItemCount: ImageTextButton
    private lateinit var bottomItemCooldown: Image
    private lateinit var bottomItemCooldownSprite: RadialSprite
    private lateinit var bottomItemCount: ImageTextButton

    private val hudWidgets = ArrayList<Actor>()

    private var showGoldFor = 0f

    var hudVisible: Boolean = true
        set(value) {
            if (value != field) {
                hudWidgets.forEach { it.isVisible = value }
            }
            field = value
        }

    override val inputProcessor: InputProcessor = DelegatingInputProcessor(stage)

    init {
        stage.viewport = ExtendViewport(450f * scale, 800f * scale, OrthographicCamera())

        val targetHeight = Gdx.graphics.width / GameScreen.SCREEN_RATION
        val verticalPadding = maxOf(0f, Gdx.graphics.height - targetHeight) / 2f

        root = table(DefaultSkin) {
            setFillParent(true)
            stack { cell ->
                princeBar = levelProgressBar(DefaultSkin.princeBar()) {
                    top()
                    left()
                    padLeft(20 * scale)
                    padTop(20 * scale)

                    isVisible = false
                }
                princessBar = levelProgressBar(DefaultSkin.princessBar()) {
                    top()
                    left()
                    padLeft(20 * scale)
                    padTop(20 * scale)
                }

                val ratio = princessBar.prefHeight/princessBar.prefWidth
                cell.width(40f * scale)
                cell.height(40f * ratio * scale)

                hudWidgets.add(this)
            }

            addSpacer().expandX()

            table(DefaultSkin) { cell ->
                cell.top()

                stack {
                    add(score)
                    add(gold.apply { scaleY = 0f })
                }


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

                hudWidgets.add(score)
            }
            row()

            textButton(FontIcon.Pause(), DefaultSkin.menuCircleXS.name, DefaultSkin) { cell ->
                cell.width(SIDEBAR_BTN_SIZE * scale)
                cell.height(SIDEBAR_BTN_SIZE * scale)

                onClick { _, _ ->
                    game.pauseGame()
                    game.addScreen(GameOverScreen(game))
                    Sounds.click().play()
                }

                disableToggle()

                hudWidgets.add(this)
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

                        topItemCount = addSubIcon(DefaultSkin.menuNoCircle() )
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

                        bottomItemCount = addSubIcon(DefaultSkin.menuNoCircle() , "")
                    }
                }

                hudWidgets.add(this)
            }

            addSpacer().fillX().row()
            addSpacer().fillY()

            pad(verticalPadding, 0f, verticalPadding, 0f)
        }

        stage.addActor(root)

        score.onClick { _, _ ->
            showGoldFor = SHOW_GOLD_DURATION
        }

        switchToGold()
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

    fun switchToGold() {
        score.addAction(Actions.scaleTo(1f, 0f, ITEM_SWITCH_TIME) then Actions.run {
            gold.addAction(Actions.scaleTo(1f, 1f, ITEM_SWITCH_TIME))
        })
    }

    fun switchToScore() {
        gold.addAction(Actions.scaleTo(1f, 0f, ITEM_SWITCH_TIME) then Actions.run {
            score.addAction(Actions.scaleTo(1f, 1f, ITEM_SWITCH_TIME))
        })
    }

    override val priority: Int
        get() = 100


    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun update(delta: Float) {
        hudVisible = game.state.state == GameState.State.Running && !game.state.isPaused

        princeBar.progress = game.state.levelProgress
        princessBar.progress = game.state.levelProgress
        princeBar.boss = game.state.bossBegin
        princessBar.boss = game.state.bossBegin
        score.score = game.state.score
        gold.gold = game.gold

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

        game.state.bonusMoney.forEach {
            val label = Label("+$it", DefaultSkin.bonusPointsLabel().apply { fontColor = Color.YELLOW }).apply {
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
        if (game.state.bonusMoney.isNotEmpty()) showGoldBar()
        game.state.bonusMoney.clear()

        val cooldown = 360f - (game.state.cooldowns.values.max() ?: 0f) * 360

        if (game.state.isBossFight) {
            if (!princeBar.isVisible)
                switchToPrince()

            topItemCooldownSprite.setAngle(if (SafePreferences { item[Item.Storm] } == 0) 0f else cooldown )
            bottomItemCooldownSprite.setAngle(if (SafePreferences { item[Item.Umbrella] } == 0) 0f else cooldown)

            SafePreferences {
                topItemCount.text = "${item[Item.Storm]}"
                bottomItemCount.text = "${item[Item.Umbrella]}"
            }
        } else {
            if (!princessBar.isVisible)
                switchToPrincess()

            topItemCooldownSprite.setAngle(if (SafePreferences { item[Item.Flask] } == 0) 0f else cooldown)
            bottomItemCooldownSprite.setAngle(if (SafePreferences { item[Item.Orb] } == 0) 0f else cooldown)

            SafePreferences {
                topItemCount.text = "${item[Item.Flask]}"
                bottomItemCount.text = "${item[Item.Orb]}"
            }
        }

        if (showGoldFor > 0) {
            showGoldFor -= delta
        }

        if (scoreVisible && ((game.state.isPaused && game.state.isRunning) || showGoldFor > 0)) {
            switchToGold()
        } else if (goldVisible && (showGoldFor <= 0f && (!game.state.isPaused || !game.state.isRunning)) ) {
            switchToScore()
        }

        stage.act(delta)
    }

    fun showGoldBar() {
        showGoldFor = 2f
    }

    override fun show() {
        game.addScreen(GameMessageScreen(game))
    }

    override fun hide() {
        game.removeScreen(GameMessageScreen::class.java, true)
    }

    override fun render() {
        stage.viewport.apply()
        stage.draw()
    }

    override fun dispose() {
    }

    companion object {
        private val SIDEBAR_BTN_SIZE = 50
        private val ITEM_CIRCLE_SIZE = SIDEBAR_BTN_SIZE * 0.8f
        private val ITEM_SWITCH_TIME = 0.2f
        private val SHOW_GOLD_DURATION = 2f
    }
}
