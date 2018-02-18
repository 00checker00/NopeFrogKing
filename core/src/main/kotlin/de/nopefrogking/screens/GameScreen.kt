package de.nopefrogking.screens

import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import com.badlogic.gdx.utils.ObjectFloatMap
import com.badlogic.gdx.utils.viewport.FitViewport
import de.nopefrogking.*
import de.nopefrogking.actors.*
import de.nopefrogking.utils.*
import de.project.ice.screens.BaseScreenAdapter
import ktx.actors.parallelTo
import ktx.actors.repeatForever
import ktx.actors.then
import ktx.app.clearScreen
import ktx.assets.pool
import ktx.collections.gdxArrayOf
import java.util.*
import kotlin.collections.HashMap
import kotlin.concurrent.thread
import com.badlogic.gdx.utils.Array as GdxArray




class GameScreen(game: Main) : BaseScreenAdapter(game), GameState {
    override var level = 1
        private set
    override var levelProgress = 0f
        private set
    override val isBossFight: Boolean
        get() = bossStarted
    override var score = 0f
        private set
    override val bonusPoints = GdxArray<Int>()
    override val messages = GdxArray<String>()
    override var isPaused = false
        private set
    override val Cooldowns = hashMapOf(Item.Flask to 0f, Item.Orb to 0f, Item.Storm to 0f, Item.Umbrella to 0f)

    private var levelTime = 0f

    private var random = Random()
    private val camera = OrthographicCamera()
    private val viewport = FitViewport(450f, 800f, camera)
    private val prince = Prince()
    private val princess = Princess()

    private var speedModifiers = HashMap<String, Float>()

    private var flaskActive = false

    private var orbActive = false

    private var umbrellaActive = false

    private val stonepool = pool {
        Stone().apply {
            type = random.nextInt(Stone.NUMBER_OF_TYPES)
            this@GameScreen.stage.addActor(this)
            stones.add(this)
        }
    }

    private val stones = GdxArray<Stone>()

    private var backgroundSpeed = BACKGROUND_SPEED_BASE
    private val backgrounds = gdxArrayOf(
            ParallaxBackground(BackgroundType.Bricks).apply { speed =  backgroundSpeed },
            ParallaxBackground(BackgroundType.Wall).apply { speed = backgroundSpeed; speedMultiplyer = 0.8f; isForeground = true  }
    )

    private val currentSpeed get() = speedModifiers.values.fold(backgroundSpeed, Float::times)


    private var stoneNextDist = 1500.0f

    private var stoneLargeGap = 0f
    private var stoneSmallGap = 0f
    private var stoneWaveLeft = 0

    private var distanceTravelled = 0f
    private var distanceComplete = 0f
    private var distanceBoss = 0f
    private var bossStarted = false
    private val bossSpeed = 100.0f
    private val bossHitDistance = 150.0f

    private var isBreakLevel = false

    private val music by Assets.getSound("music/game_bg.wav")
    private var loopMusicThread: Thread? = null

    private fun createLevelColor(): Color {
//        val hue = random.nextFloat() * 360f
//        val sat = random.nextFloat() * 30f + 50f
//        val valueBase = Math.min(80f, Math.max(50f, 80f-level))
//        val value = random.nextFloat() * 10f + valueBase
//
//        return hsvToColor(hue, sat, value)

        return PASTELL_COLORS[random.nextInt(PASTELL_COLORS.size)]
    }

    private val shader = SaturationShader()

    private val stage = Stage(viewport, SpriteBatch(1000, shader)).apply {
        val stage = this

        backgrounds.forEach {
            it.width = stage.width
            it.height = stage.height

            addActor(it)
        }


        addActor(princess.let {
            it.x = stage.width/2 - it.width/2
            it.y = stage.height - it.height
            it
        })

        addActor(prince.let {
            it.x = stage.width/2 - it.width/2
            it.y = stage.height*2 - it.height
            it
        })
    }

    var color: Color
        set(value) {
            stage.root.color = value
        }
        get() = stage.root.color

    override val inputProcessor: InputProcessor = object: DelegatingBlockingInputProcessor(stage) {
        override fun keyTyped(character: Char): Boolean {
            return super.keyTyped(character)
        }
    }

    fun addBonusPoints(amount: Int) {
        bonusPoints.add(100)
        score += amount
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
    }

    fun activateItem(item: Item): Boolean {
        if (Cooldowns[item]?:0f > 0f)
            return false

        when(item) {
            Item.Flask -> {
                if (flaskActive) return false
                flaskActive = true

                Cooldowns[Item.Flask] = 1.0f

                stage.addAction(
                        (ModifySpeedAction(this, SPEED_MODIFIER_FLASK, FLASK_SPEED_FACTOR, 0.2f) parallelTo SaturationAction(1f, FLASK_DESATURATION_STRENGTH, 0.2f))
                                then Actions.delay(FLASK_DURATION - 0.4f)
                                then (SaturationAction(FLASK_DESATURATION_STRENGTH, 1f, 0.2f) parallelTo Actions.run {
                            if (flaskActive) {
                                flaskActive = false
                                stage.addAction(ModifySpeedAction(this, SPEED_MODIFIER_FLASK, 1f, 0.2f))
                            }
                        })
                )

                stage.addAction(object: TemporalAction(FLASK_DURATION) {
                    override fun update(percent: Float) {
                        Cooldowns[Item.Flask] = 1.0f - percent
                    }
                })
            }
            Item.Orb -> {
                if (orbActive) return false

                Cooldowns[Item.Orb] = 1.0f

                val rainbowAction = (Actions.color(Color.RED, 0.1f) then
                        Actions.color(Color.VIOLET, 0.1f) then
                        Actions.color(Color.BLUE, 0.1f) then
                        Actions.color(Color.CYAN, 0.1f) then
                        Actions.color(Color.GREEN, 0.1f) then
                        Actions.color(Color.YELLOW, 0.1f)).repeatForever()

                princess.addAction(rainbowAction)

                orbActive = true
                stage.addAction(Actions.delay(ORB_DURATION) then Actions.run {
                    orbActive = false
                    princess.removeAction(rainbowAction)
                    princess.color = Color.WHITE
                })

                stage.addAction(
                    ModifySpeedAction(this, SPEED_MODIFIER_ORB, ORB_SPEED_FACTOR, 0.2f)
                            then Actions.run { princess.drawStars = true }
                            then Actions.delay(ORB_DURATION - 0.4f)
                            then Actions.run {
                        if (orbActive) {
                            princess.drawStars = false
                            orbActive = false
                            stage.addAction(ModifySpeedAction(this, SPEED_MODIFIER_ORB, 1f, 0.2f))
                        }
                    }
                )

                stage.addAction(object: TemporalAction(ORB_DURATION) {
                    override fun update(percent: Float) {
                        Cooldowns[Item.Orb] = 1.0f - percent
                    }
                })

                val y = princess.y
                princess.addAction(Actions.moveTo(princess.x, stage.height/2 - princess.height/2, 0.5f, Interpolation.pow2)
                        then Actions.delay(ORB_DURATION - 1f)
                        then Actions.moveTo(princess.x, y, 0.5f, Interpolation.pow2)
                )

            }
            Item.Umbrella -> {
                Cooldowns[Item.Umbrella] = 1.0f

                umbrellaActive = true

                princess.openUmbrella {

                    princess.addAction(Actions.moveTo(princess.x, stage.height - princess.height, 4f) then Actions.run {
                        umbrellaActive = false
                        bossStarted = false

                        endBossFight()
                        nextLevel()

                        princess.closeUmbrella()
                    })


                }


            }
            Item.Storm -> {
                Cooldowns[Item.Storm] = 1.0f

                stage.addAction(object: TemporalAction(ORB_DURATION) {
                    override fun update(percent: Float) {
                        Cooldowns[Item.Storm] = 1.0f - percent
                    }
                })

                princess.doPirouette()

                val whirl = Whirl().apply { x = princess.x; y = princess.y }
                stage.addActor(whirl)


                whirl.addAction(Actions.moveTo(prince.x, stage.height, 1f))

                whirl.addAction(Actions.run {
                    if (whirl.y >= prince.y) {
                        prince.addAction(Actions.moveBy(0.0f, STORM_DISTANCE, STORM_DURATION, Interpolation.pow2))
                        prince.getElectrified()
                        whirl.remove()
                    }
                }.repeatForever())
            }
        }

        return true
    }

    private fun createStone(): Stone {
        val stone = stonepool.obtain()
        stone.y = -Stone.Height
        val pad = (stage.width - stone.width - princess.width) / 2
        stone.x = pad + random.nextFloat() * (stage.width - 2 * pad - stone.width)
        stone.type = random.nextInt(Stone.NUMBER_OF_TYPES)

        if (!isBossFight && random.nextBoolean()) {
            stone.actionType = Stone.ActionType.Tap
        } else {
            stone.actionType = Stone.ActionType.Move
        }


        if (bossStarted) {
            val left = random.nextBoolean()
            if (left) {
                stone.x = 0f
            } else {
                stone.x = stage.width - stone.width
                stone.flipX = true
            }
        }

        stone.onDestroyed {
            addBonusPoints(100)
        }

        return stone
    }

    override fun update(delta: Float) {
        val currentSpeed = currentSpeed

        music
        if (isPaused) {
            return
        }

        stage.act(delta)

        score += delta * currentSpeed / 10f

        levelProgress = distanceTravelled / distanceComplete

        distanceTravelled += delta * currentSpeed

        if (!bossStarted && distanceTravelled >= distanceBoss && distanceTravelled <= distanceComplete) {
            bossStarted = true
            initiateBossFight()
        }

        if (bossStarted && distanceTravelled >= distanceComplete) {
            bossStarted = false
            endBossFight()
            nextLevel()
        }

        if (bossStarted) {
            prince.y -= delta * bossSpeed

            if (prince.y <= princess.y + princess.height) {
                if (umbrellaActive) {
                    prince.y = princess.y + princess.height
                } else {
                    Sounds.kiss().play(0.5f)
                    game.pauseGame()
                    game.addScreen(GameOverScreen(game))

                }
            }
        }

        for (stone in stones) {
            stone.moveBy(0f, delta * currentSpeed * 0.8f)

            if ( stone.y > stage.height) {
                removeStone(stone)
                continue
            }

            if (!stone.isDestroyed && !stone.isDisabled) {
                if (bossStarted) {
                    if (stone.actionType == Stone.ActionType.Move && stone.intersects(prince)) {
                        //info { "Stone collided with prince" }
                        stone.crush()
                        prince.addAction(Actions.moveBy(0.0f, bossHitDistance, 0.8f, Interpolation.pow2))
                        prince.hit()
                    }
                } else {
                    val bounds = Rectangle(princess.x, princess.y, princess.width, princess.height)
                    if (stone.y + stone.height >= princess.y && stone.hitbox.overlaps(bounds)) {
                        //info { "Stone collided with princess" }
                        stone.crush()
                        if (!orbActive) {
                            game.pauseGame()
                            game.addScreen(GameOverScreen(game))
                        } else {
                            addBonusPoints(100)
                        }
                    }
                }

//            if (stones[i].hitTestObject(princess)) {
//                dispatchEvent(new CollisionEvent(CollisionEvent.COLLISION, stones[i], princess));
//            }
            }
        }

        stoneLargeGap -= delta
        stoneSmallGap -= delta
        if (stoneLargeGap <= 0f) {
            if (stoneSmallGap <= 0f) {
                if (stoneWaveLeft > 0) {
                    stoneWaveLeft--
                    stoneSmallGap = random.nextFloat(GAP_SMALL_MIN, GAP_SMALL_MAX)

                    createStone()
                } else {
                    stoneLargeGap = GAP_LARGE
                    stoneWaveLeft = STONE_SPAWN_MIN_COUNT + random.nextInt(STONE_SPAWN_MAX_COUNT)
                }
            }
        }

//        stoneNextDist -= (backgroundSpeed * delta)
//
//        if (stoneNextDist <= 0) {
//            val stone = createStone()
//
//            if (bossStarted) {
//                stoneNextDist = random.nextFloat(200f, 400f)
//            } else {
//                stoneNextDist = random.nextFloat(100f, 300f)
//            }
//        }

        backgrounds.forEach { it.color = color; it.speed = currentSpeed }

        backgrounds.filter { it.isForeground }.forEach { it.color = Color.WHITE }

        stones.forEach { /*it.color = color;*/ it.toFront() }

        backgrounds.filter { it.isForeground }.forEach { it.toFront() }
    }

    private fun nextLevel() {

        distanceTravelled = 0f
        bossStarted = false

        level++

        val newSpeed =  (1 + (BACKGROUND_SPEED_MAX-1) *  (1 - (1 / Math.pow(2.0, level * 0.1).toFloat())))

        stage.addAction(ModifySpeedAction(this, SPEED_MODIFIER_NEXT_LEVEL, newSpeed, 0.4f, Interpolation.pow2))

        levelTime = (0..level).fold(LEVEL_TIME) { cur, _ -> cur * LEVEL_TIME_INCREASE }
        distanceComplete = newSpeed * levelTime * BACKGROUND_SPEED_BASE
        distanceBoss = distanceComplete * (1.0f - random.nextFloat(BOSS_START_MIN, BOSS_START_MAX))

        val newColor = createLevelColor()

        stage.addAction(Actions.color(newColor, 2.0f))

        prince.hits = 0

        val oldBackground = backgrounds.filter { !it.isForeground }.first()
        val type = when (oldBackground.type) {
            BackgroundType.Bricks -> BackgroundType.Stones
            else -> BackgroundType.Bricks
        }
        val newBackground = ParallaxBackground(type).apply {
            width = this@GameScreen.stage.width
            height = this@GameScreen.stage.height
            color = Color(oldBackground.color).apply { a = 0.0f }
            speed = newSpeed
        }
        stage.addActor(newBackground)
        newBackground.addAction(Actions.color(newColor, 2.0f) then Actions.run {
            backgrounds.add(newBackground)
            backgrounds.removeValue(oldBackground, true)
            oldBackground.remove()
        })
        newBackground.toBack()
        oldBackground.toBack()

        stoneNextDist = 1500f

        isBreakLevel = random.nextBoolean()

        messages.add("Level $level!")

        Cooldowns[Item.Umbrella] = 0f

        distanceBoss

        stoneLargeGap = GAP_LARGE
    }

    private fun removeStone(stone: Stone) {
        stone.remove()
        stones.removeValue(stone, true)
        //stonepool.free(stone)
    }

    private fun clearStones() {
        stones.forEach { it.remove(); stonepool.free(it) }
        stones.clear()
    }

    override fun show() {
        game.addScreen(HUDScreen(game))

        music

        restartGame()
    }

    override fun hide() {
        game.removeScreen(HUDScreen::class.java, true)
        stopMusic()
    }

    private fun endBossFight(animate: Boolean = true) {
        val duration = if (animate) 1.5f else 0f
        princess.addAction(
                Actions.moveTo(princess.x, stage.height - princess.height, duration, Interpolation.pow2)
        )

        prince.addAction(
                Actions.moveTo(prince.x,  stage.height*2 - prince.height, duration, Interpolation.pow2)
        )

        stones.forEach { it.addAction(Actions.moveTo(it.x, stage.height + it.y, 1.5f, Interpolation.pow2Out)); it.isDisabled = true }
    }

    private fun initiateBossFight(animate: Boolean = true) {
        messages.add("The Prince is getting closer!")
        messages.add("Don't let him reach you!")

        val duration = if (animate) 1.5f else 0f

        stoneNextDist = duration * currentSpeed

        princess.addAction(
                Actions.moveTo(princess.x, -princess.height/2, duration, Interpolation.pow2)
        )

        prince.addAction(
                Actions.moveTo(prince.x, stage.height - prince.height, duration, Interpolation.pow2)
        )

        stones.forEach { it.addAction(Actions.moveTo(it.x, stage.height + it.y, 1.5f, Interpolation.pow2Out)); it.isDisabled = true }
    }

    override fun render() {
        clearScreen(color.r * 0.14f, color.g * 0.14f, color.b * 0.14f)
        stage.viewport.apply()
        stage.draw()
    }

    override fun resume() {
        startMusic()
    }

    override fun pause() {
        stopMusic()
    }

    fun pauseGame() {
        isPaused = true
    }

    fun resumeGame() {
        isPaused = false
    }

    private fun startMusic() {
        if (loopMusicThread == null) {
            loopMusicThread = thread {
                try {
                    while(true) {
                        music?.play(0.2f)
                        Thread.sleep(7000)
                    }
                } catch (_: InterruptedException) {}
            }
        }
    }

    private fun stopMusic() {
        music?.stop()
        loopMusicThread?.interrupt()
        loopMusicThread = null
    }

    fun restartGame() {
        level = 1
        score = 0f
        levelProgress = 0f
        bonusPoints.clear()

        distanceTravelled = 0f

        stoneNextDist = 1500f

        prince.hits = 0

        bossStarted = false

        backgroundSpeed = BACKGROUND_SPEED_BASE

        clearStones()

        endBossFight(false)

        color = createLevelColor()

        stopMusic()
        startMusic()

        //messages.add("Move the Stones\n out of your way!")

        nextLevel()
    }

    override fun dispose() {
        super.dispose()
        stopMusic()
    }

    override val priority: Int
        get() = 1000

    companion object {
        private val LEVEL_TIME = 30.0f
        private val LEVEL_TIME_INCREASE = 1.1f

        private val BOSS_START_FIRST = 0.3f
        private val BOSS_START_MIN = 0.2f
        private val BOSS_START_MAX = 0.5f

        private val FLASK_SPEED_FACTOR = 0.5f
        private val FLASK_DURATION = 5f
        private val FLASK_DESATURATION_STRENGTH = 0.0f

        private val BACKGROUND_SPEED_BASE = 300.0f
        private val BACKGROUND_SPEED_MAX = 2f



        private val STONE_SPAWN_MIN_COUNT = 2
        private val STONE_SPAWN_MAX_COUNT = 8

        private val GAP_SMALL_MIN = 0.5f
        private val GAP_SMALL_MAX = 1.5f

        private val GAP_LARGE = 4f

        private val ORB_DURATION = 5f
        private val ORB_SPEED_FACTOR = 2.0f

        private val STORM_DISTANCE = 250f
        private val STORM_DURATION = 0.8f

        private val SPEED_MODIFIER_NEXT_LEVEL = "NEXT_LEVEL"
        private val SPEED_MODIFIER_ORB = "ORB"
        private val SPEED_MODIFIER_FLASK = "FLASK"

        private val PASTELL_COLORS = arrayOf(
            Color(238/255f, 231/255f, 219/255f, 1f),
            Color(198/255f, 186/255f, 165/255f, 1f),
            Color(215/255f, 137/255f, 162/255f, 1f),
            Color(0/255f, 233/255f, 211/255f, 1f),
            Color(190/255f, 154/255f, 193/255f, 1f),
            Color(87/255f, 177/255f, 217/255f, 1f),
            Color(251/255f, 137/255f, 134/255f, 1f),
            Color(255/255f, 192/255f, 0/255f, 1f),
            Color(74/255f, 187/255f, 201/255f, 1f),
            Color(170/255f, 237/255f, 255/255f, 1f),
            Color(160/255f, 193/255f, 207/255f, 1f),
            Color(86/255f, 201/255f, 175/255f, 1f),
            Color(199/255f, 183/255f, 193/255f, 1f),
            Color(125/255f, 177/255f, 72/255f, 1f),
            Color(168/255f, 132/255f, 92/255f, 1f),
            Color(146/255f, 211/255f, 240/255f, 1f),
            Color(230/255f, 126/255f, 140/255f, 1f)
        )
    }

    internal class ModifySpeedAction(val gameScreen: GameScreen, val id: String, val end: Float, duration: Float, interpolation: Interpolation = Interpolation.linear): TemporalAction(duration, interpolation) {
        private val start = gameScreen.speedModifiers[id]?: 1f

        override fun update(percent: Float) {
            val d = (start - end) * percent
            gameScreen.speedModifiers[id] = start - d
        }
    }

    internal class SaturationAction(val start: Float, val end: Float, duration: Float, interpolation: Interpolation = Interpolation.linear): TemporalAction(duration, interpolation) {
        override fun update(percent: Float) {
            (target.stage.batch.shader as? SaturationShader)?.saturation = start + (end-start) * percent
        }

    }
}
