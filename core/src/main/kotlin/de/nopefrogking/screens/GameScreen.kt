package de.nopefrogking.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.Actor
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

internal open class TutorialTarget(
        open val x: Float,
        open val y: Float,
        open val radius: Float) {

    private class TutorialTargetActor(private val actor: Actor): TutorialTarget(0f, 0f, 0f) {
        override val x get() = actor.x + actor.width/2
        override val y get() = actor.y + actor.height/2
        override val radius get() = maxOf(actor.width, actor.height)
    }

    companion object {
        fun fromActor(actor: Actor): TutorialTarget = TutorialTargetActor(actor)
    }
}
internal data class TutorialStage(val progress: Float, val target: TutorialTarget, val page: TutorialScreen.Page) {
    var isDone = false
        private set

    fun done() {
        isDone = true
    }
}

class GameScreen(game: Main) : BaseScreenAdapter(game), GameState {
    override var level = 1
        private set
    override var levelProgress = 0f
        private set
    override var bossBegin = 0.7f
        private set
    override val isBossFight: Boolean
        get() = bossStarted
    override var score = 0f
        private set
    override val bonusPoints = GdxArray<Int>()
    override val bonusMoney = GdxArray<Long>()
    override val messages = GdxArray<String>()
    override var isPaused = false
        private set
    override var isRunning = true
    override val cooldowns = hashMapOf(Item.Flask to 0f, Item.Orb to 0f, Item.Storm to 0f, Item.Umbrella to 0f)
    override var state = GameState.State.Running
        private set

    private var levelTime = 0f

    private var random = Random()
    private val camera = OrthographicCamera()
    private val viewport = FitViewport(800f * SCREEN_RATION, 800f, camera)
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
            ParallaxBackground(BackgroundType.Bricks, this).apply { speed =  backgroundSpeed },
            ParallaxBackground(BackgroundType.Wall, this).apply { speed = backgroundSpeed; speedMultiplyer = 0.8f; isForeground = true  }
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

    var tutorial = true
    private val tutorials = ArrayList<TutorialStage>()
    private val nextTutorial: TutorialStage?
        get() = tutorials.firstOrNull { !it.isDone }

    private fun createLevelColor(): Color {
//        val hue = random.nextFloat() * 360f
//        val sat = random.nextFloat() * 30f + 50f
//        val valueBase = Math.min(80f, Math.max(50f, 80f-level))
//        val value = random.nextFloat() * 10f + valueBase
//
//        return hsvToColor(hue, sat, value)

        return PASTELL_COLORS[random.nextInt(PASTELL_COLORS.size)]
    }

    private fun initTutorials() {
        tutorials.clear()
        tutorials.addAll(arrayOf(
                TutorialStage(0.05f, TutorialTarget.fromActor(princess), TutorialScreen.Page.Princess)
        ))
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

    override val inputProcessor: InputProcessor = DelegatingBlockingInputProcessor(stage)

    val disableActiveItem = HashMap<Item, (()->Unit)>()
    fun disableItems(vararg items: Item = Item.values()) {
        for(item in items) {
            disableActiveItem[item]?.invoke()
            disableActiveItem.remove(item)
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
        if (cooldowns.values.any { it > 0f })
            return false

        if (SafePreferences { this.item[item] <= 0 }) {
            return false
        }

        SafePreferences { this.item[item]-- }

        disableItems()

        when(item) {
            Item.Flask -> {
                if (flaskActive) return false
                flaskActive = true

                cooldowns[Item.Flask] = 1.0f

                disableActiveItem[Item.Flask] = {
                    if (flaskActive) {
                        stage.addAction(SaturationAction(FLASK_DESATURATION_STRENGTH, 1f, 0.2f))
                        Sounds.time_normal().play()
                        flaskActive = false
                        stage.addAction(ModifySpeedAction(this, SPEED_MODIFIER_FLASK, 1f, 0.2f))
                    }
                    cooldowns[Item.Flask] = 0f
                    flaskActive = false
                }

                Sounds.time_slow().play()
                stage.addAction((ModifySpeedAction(this, SPEED_MODIFIER_FLASK, FLASK_SPEED_FACTOR, 0.2f) parallelTo SaturationAction(1f, FLASK_DESATURATION_STRENGTH, 0.2f))
                                then Actions.delay(FLASK_DURATION - 0.4f)
                                then Actions.run {
                    disableItems(Item.Flask)
                })

                stage.addAction(object: TemporalAction(FLASK_DURATION) {
                    override fun update(percent: Float) {
                        if (flaskActive) {
                            cooldowns[Item.Flask] = 1.0f - percent
                        }
                    }
                })
            }
            Item.Orb -> {
                if (orbActive) return false

                cooldowns[Item.Orb] = 1.0f

                orbActive = true

                val cooldownAction = object: TemporalAction(ORB_DURATION) {
                    override fun update(percent: Float) {
                        if (orbActive) {
                            cooldowns[Item.Orb] = 1.0f - percent
                        }
                    }
                }
                stage.addAction(cooldownAction)

                disableActiveItem[Item.Orb] = {
                    Sounds.item_orb().stop()

                    if (orbActive) {
                        princess.drawStars = false
                        orbActive = false
                        stage.addAction(ModifySpeedAction(this, SPEED_MODIFIER_ORB, 1f, 0.2f))

                        princess.color = Color.WHITE
                    }

                    cooldowns[Item.Orb] = 0f
                    orbActive = false
                }

                Sounds.item_orb().play()

                stage.addAction(
                    ModifySpeedAction(this, SPEED_MODIFIER_ORB, ORB_SPEED_FACTOR, 0.2f)
                            then Actions.run { princess.drawStars = true }
                            then Actions.delay(ORB_DURATION - 0.4f)
                            then Actions.run {
                        disableItems(Item.Orb)
                    }
                )

                val y = princess.y
                princess.addAction(Actions.moveTo(princess.x, stage.height/2 - princess.height/2, 0.5f, Interpolation.pow2)
                        then Actions.delay(ORB_DURATION - 1f)
                        then Actions.run {
                    if (!isBossFight) {
                        princess.addAction(Actions.moveTo(princess.x, y, 0.5f, Interpolation.pow2))
                    }
                })
            }

            Item.Umbrella -> {
                cooldowns[Item.Umbrella] = 1.0f

                umbrellaActive = true

                princess.openUmbrella {
                    val action = Actions.moveTo(princess.x, stage.height - princess.height, 4f) then Actions.run {
                        if (umbrellaActive) {
                            umbrellaActive = false
                            bossStarted = false

                            endBossFight()
                            nextLevel()

                            princess.closeUmbrella()
                        }
                    }
                    princess.addAction(action)

                    disableActiveItem[Item.Umbrella] = {
                        umbrellaActive = false
                        princess.closeUmbrella()
                        princess.removeAction(action)
                    }
                }


            }
            Item.Storm -> {
                cooldowns[Item.Storm] = 1.0f

                princess.doPirouette()

                val whirl = Whirl().apply { x = princess.x; y = princess.y }
                stage.addActor(whirl)

                Sounds.item_storm_start().play()

                whirl.addAction(Actions.moveTo(prince.x, stage.height, 1f))

                disableActiveItem[Item.Storm] = {
                    whirl.remove()
                    cooldowns[Item.Storm] = 0f
                }

                whirl.addAction(Actions.run {
                    if (whirl.y >= prince.y) {
                        prince.addAction(Actions.moveBy(0.0f, STORM_DISTANCE, STORM_DURATION, Interpolation.pow2))
                        prince.getElectrified()
                        Sounds.item_storm_hit().play()
                        Sounds.item_storm_start().stop()
                        disableItems(Item.Storm)
                    }
                }.repeatForever())
            }
        }

        return true
    }

    private fun createStone(): Stone {
        val stone = stonepool.obtain()
        stone.y = -Stone.Height
        val padSmall = (stage.width - stone.width)
        val padLarge  = (stage.width - stone.width) - princess.width/2
        val pad = if (random.nextFloat() <= 0.3) padLarge else padSmall
        stone.x = pad + random.nextFloat() * (stage.width - 2 * pad - stone.width)
        stone.type = random.nextInt(Stone.NUMBER_OF_TYPES)

        if (random.nextFloat() <= BROKEN_STONE_CHANCE.get(isBossFight)) {
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

    private fun showTutorialMessage(tutorial: TutorialStage) {
        val target = tutorial.target
        stage.addAction(ModifySpeedAction(this, SPEED_MODIFIER_TUTORIAL, 0f, 0.5f))
        game.addScreen(CircleFadeScreen(game, 1f) {

            game.addScreen(TutorialScreen(game, tutorial.page) {
                stage.addAction(ModifySpeedAction(this, SPEED_MODIFIER_TUTORIAL, 1f, 0.5f))

            })
        }.apply {
            color = TUTORIAL_OVERLAY_COLOR
            center = camera.project(Vector3(target.x, target.y, 0f),
                    stage.viewport.screenX.toFloat(), stage.viewport.screenY.toFloat(), stage.viewport.screenWidth.toFloat(), stage.viewport.screenHeight.toFloat())
                    .let { Vector2(it.x, it.y) }
            minRadius = camera.project(Vector3(target.radius, target.radius, 0f)).x
            maxRadius = Gdx.graphics.width.toFloat()
        })
        tutorial.done()
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
                    state = GameState.State.EndedPrinceKiss
                    prince.isVisible = false
                    princess.isVisible = false
                    game.pauseGame()
                    game.addScreen(GameOverScreen(game))

                }
            }
        }

        stones.forEach { stone ->
            stone.speed = delta * currentSpeed * 0.8f

            if ( stone.y > stage.height) {
                removeStone(stone)
            } else {
                if (!stone.isDestroyed) {
                    if (bossStarted) {
                        if (stone.intersects(prince.hitbox)) {
                            //info { "Stone collided with prince" }
                            stone.crush()
                            prince.addAction(Actions.moveBy(0.0f, bossHitDistance, 0.8f, Interpolation.pow2))
                            prince.hit()
                        }
                    } else if (!stone.isDisabled) {
                        if (stone.y + stone.height >= princess.y && stone.hitbox.overlaps(princess.hitbox)) {
                            //info { "Stone collided with princess" }
                            stone.crush()
                            if (!orbActive) {
                                state = GameState.State.EndedStoneHit
                                prince.isVisible = false
                                princess.isVisible = false
                                game.pauseGame()
                                game.addScreen(GameOverScreen(game))
                            } else {
                                addBonusPoints(100)
                            }
                        }
                    }
                }
            }
        }

        stoneLargeGap -= delta
        stoneSmallGap -= delta
        if (stoneLargeGap <= 0f) {
            if (stoneSmallGap <= 0f) {
                if (stoneWaveLeft > 0) {
                    stoneWaveLeft--
                    stoneSmallGap = random.nextFloat(GAP_SMALL_MIN.get(isBossFight), GAP_SMALL_MAX.get(isBossFight))

                    createStone()
                } else {
                    stoneLargeGap = GAP_LARGE.get(isBossFight)
                    stoneWaveLeft = STONE_SPAWN_MIN_COUNT.get(isBossFight) + random.nextInt(STONE_SPAWN_MAX_COUNT.get(isBossFight))
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

    private fun nextLevel(changeBG: Boolean = true) {

        distanceTravelled = 0f
        bossStarted = false

        level++

        cooldowns.keys.forEach { cooldowns[it] = 0f }

        val newSpeed =  (1 + (BACKGROUND_SPEED_MAX-1) *  (1 - (1 / Math.pow(2.0, level * BACKGROUND_SPEED_QUOTIENT.toDouble()).toFloat())))

        stage.addAction(ModifySpeedAction(this, SPEED_MODIFIER_NEXT_LEVEL, newSpeed, 0.4f, Interpolation.pow2))

        levelTime = (0..level).fold(LEVEL_TIME) { cur, _ -> cur * LEVEL_TIME_INCREASE }
        distanceComplete = newSpeed * levelTime * BACKGROUND_SPEED_BASE
        bossBegin = random.nextFloat(BOSS_START_MIN, BOSS_START_MAX)
        distanceBoss = distanceComplete * bossBegin

        val newColor = createLevelColor()

        princess.drawStars = false

        prince.hits = 0

        disableItems()

        if (changeBG) {
            val oldBackground = backgrounds.last { !it.isForeground }
            val type = BackgroundType.values()
                    .filterNot { it == BackgroundType.Wall }
                    .filterNot { it == oldBackground.type }
                    .let { it[random.nextInt(it.size)] }

            val newBackground = ParallaxBackground(type, this).apply {
                width = this@GameScreen.stage.width
                height = this@GameScreen.stage.height
                color = newColor
                speed = newSpeed
                moving = false
                y = 0f
            }
            oldBackground.moving = false
            backgrounds.add(newBackground)
            stage.addActor(newBackground)

            newBackground.addAction(Actions.moveTo(0f, -this@GameScreen.stage.height, 0f)
                    then Actions.moveTo(0f, 0f, BACKGROUND_SWITCH_TIME)
                    then Actions.run {
                oldBackground.remove()
                backgrounds.removeValue(oldBackground, true)
                if (backgrounds.last { !it.isForeground } == newBackground) {
                    newBackground.moving = true
                }
            })
            oldBackground.addAction(Actions.moveTo(0f, stage.height, BACKGROUND_SWITCH_TIME))
            newBackground.toBack()
            oldBackground.toBack()
        }

        stoneNextDist = 1500f

        isBreakLevel = random.nextBoolean()

        messages.add("Level $level!")

        cooldowns[Item.Umbrella] = 0f

        distanceBoss

        stoneLargeGap = LEVEL_START_PEACE_PHASE
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
        music

        restartGame(false)
    }

    override fun hide() {
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

        disableItems()

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
        //clearScreen(color.r * 0.14f, color.g * 0.14f, color.b * 0.14f)
        clearScreen(0f, 0f, 0f)
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
                        music?.play()
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

    fun restartGame(changeBG: Boolean = true) {
        level = 0
        score = 0f
        levelProgress = 0f
        bonusPoints.clear()

        distanceTravelled = 0f

        stoneNextDist = 1500f

        prince.hits = 0

        state = GameState.State.Running

        prince.isVisible = true
        princess.isVisible = true

        bossStarted = false

        backgroundSpeed = BACKGROUND_SPEED_BASE

        clearStones()

        endBossFight(false)

        color = createLevelColor()

        stopMusic()
        startMusic()

        //messages.add("Move the Stones\n out of your way!")

        nextLevel(changeBG)

        if (tutorial) {
            initTutorials()
        }
    }

    override fun dispose() {
        super.dispose()
        disableItems()
        stopMusic()
    }

    override val priority: Int
        get() = 1000

    companion object {
        private val LEVEL_TIME = 30.0f
        private val LEVEL_TIME_INCREASE = 1.1f

        private val BOSS_START_FIRST = 0.3f
        private val BOSS_START_MIN = 0.5f
        private val BOSS_START_MAX = 0.8f

        private val FLASK_SPEED_FACTOR = 0.5f
        private val FLASK_DURATION = 5f
        private val FLASK_DESATURATION_STRENGTH = 0.0f

        private val BACKGROUND_SPEED_BASE = 300.0f
        private val BACKGROUND_SPEED_MAX = 4f
        private val BACKGROUND_SWITCH_TIME = 0.5f
        private val BACKGROUND_SPEED_QUOTIENT = 0.1f

        data class PhasedValue<out T>(private val freefall: T, private val boss: T = freefall) {
            fun get(isBoss: Boolean) = if(isBoss) boss else freefall
        }
        private val STONE_SPAWN_MIN_COUNT = PhasedValue(2, 5)
        private val STONE_SPAWN_MAX_COUNT = PhasedValue(8)

        private val GAP_SMALL_MIN = PhasedValue(0.5f)
        private val GAP_SMALL_MAX = PhasedValue(1.5f)

        private val BROKEN_STONE_CHANCE = PhasedValue(0.3, 0.1)

        private val LEVEL_START_PEACE_PHASE = 4f

        private val GAP_LARGE = PhasedValue(0f)

        private val ORB_DURATION = 5f
        private val ORB_SPEED_FACTOR = 2.0f

        private val STORM_DISTANCE = 250f
        private val STORM_DURATION = 0.8f

        private val SPEED_MODIFIER_NEXT_LEVEL = "NEXT_LEVEL"
        private val SPEED_MODIFIER_ORB = "ORB"
        private val SPEED_MODIFIER_FLASK = "FLASK"
        private val SPEED_MODIFIER_TUTORIAL = "TUTORIAL"

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

        private val TUTORIAL_OVERLAY_COLOR = Color(172/255f, 59/255f, 172/255f, 0.4f)

        val SCREEN_RATION = 450f/800f
    }

    internal class ModifySpeedAction(val gameScreen: GameScreen, val id: String, val end: Float, duration: Float, interpolation: Interpolation = Interpolation.linear): TemporalAction(duration, interpolation) {
        private val start = gameScreen.speedModifiers[id]?:1f

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

    internal class FadeSoundAction(val sound: Sound, val soundID: Long, val start: Float, val end: Float, duration: Float, interpolation: Interpolation = Interpolation.linear): TemporalAction(duration, interpolation) {
        override fun update(percent: Float) {
            val d = (start - end) * percent
            sound.setVolume(soundID, d)
        }

        companion object {
            fun fadeOut(sound: Sound, soundID: Long, duration: Float, interpolation: Interpolation = Interpolation.linear): FadeSoundAction {
                return FadeSoundAction(sound, soundID, 1f, 0f, duration, interpolation)
            }
            fun fadeIn(sound: Sound, soundID: Long, duration: Float, interpolation: Interpolation = Interpolation.linear): FadeSoundAction {
                return FadeSoundAction(sound, soundID, 0f, 1f, duration, interpolation)
            }
        }
    }
}
