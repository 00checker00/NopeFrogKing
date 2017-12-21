package de.nopefrogking

import com.badlogic.gdx.*
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.I18NBundle
import com.badlogic.gdx.utils.ObjectMap
import de.nopefrogking.screens.*
import de.nopefrogking.ui.Dialogs
import de.nopefrogking.utils.*
import ktx.app.clearScreen
import ktx.i18n.I18n
import org.funktionale.either.Disjunction.*
import java.util.*

open class Main : ApplicationAdapter() {
    @PublishedApi internal val screens = Array<BaseScreen>()
    internal val screensToAdd = Array<BaseScreen>()
    internal val screensToRemove = Array<Pair<BaseScreen, Boolean>>()
    internal val screenStack = ObjectMap<BaseScreen, Pair<BaseScreen, Boolean>>()
    protected var gameScreen: GameScreen? = null
    protected val inputProcessor = InputMultiplexer()

    protected val fixedTimeStep: Float = 1f / 30f
    protected val maxDeltaTime: Float = 1f
    private var timeSinceLastRender = 0f

    var blockInteraction = false
        set(value) {
            field = value
        }
    var blockSaving = false
    var currentSaveSlot: Int = 0; private set

    private val _state = StubGameState()
    val state: GameState = object : GameState by _state {}

    val gold: Int = 0

    override fun create() {
        Gdx.app.logLevel = Application.LOG_DEBUG

        I18n.defaultBundle = I18NBundle.createBundle(Gdx.files.internal("i18n/i18n"))

        IAP.onInit {
            debug { "IAP initialized" }
        }
        IAP.onPurchaseResult { when (it) {
            is Right -> info { "Purchase of ${it.get().offer.id} successfull" }
            is Left -> error(it.swap().get()) { "Purchase error" }
        }}
        IAP.init()

        Dialogs.init()

        init()
    }

    protected open fun init() {
        addScreen(FPSScreen(this))

        Gdx.input.inputProcessor = inputProcessor

        Thread {
            Gdx.app.postRunnable {
                LoadSkinAssets()


                Assets.preloadAssets(
                    *Gdx.files.internal("particles").list().map { ParticleEffect::class.java assetFrom it.path() with ParticleEffectLoader.ParticleEffectParameter().apply { this.atlasFile = Assets.atlasFile } }.toTypedArray(),
                    *Gdx.files.internal("sounds").list().map { Sound::class.java assetFrom it.path() }.toTypedArray(),
                    Music::class.java assetFrom "music/menu_bg.mp3",
                    Sound::class.java assetFrom "music/game_bg.wav"
                )

                val start = System.currentTimeMillis()
                addScreen(LoadingScreen(this, true) { loadingScreen ->
                    val duration = System.currentTimeMillis() - start
                    debug { "Loading skin assets took ${duration}ms" }

                    addScreen(MainMenuScreen(this))
                    PlatformHandler.Instance?.showAd()
                })
            }
        }.start()



        //startNewGame()
    }

    fun pauseGame() {
        gameScreen?.pauseGame()
    }

    fun resumeGame() {
        gameScreen?.resumeGame()
    }

    fun activateItem(item: Item) = gameScreen?.activateItem(item) ?: false

    fun showToastMessages(vararg messages: String) {
        val messageStrings = messages.map {
            try {
                I18n.defaultBundle?.get(it) ?: "$$$it$$"
            } catch (ex: MissingResourceException) {
                "$$$it$$"
            }
        }.toTypedArray()

        val messageScreen = screens.filter { it is MessageScreen }.firstOrNull() as MessageScreen?
        if (messageScreen != null)
            messageScreen.addMessages(*messageStrings)
        else
            addScreen(MessageScreen(this, *messageStrings))
    }

    override fun dispose() {
        for (screen in Array(screens)) {
            screen.hide()
            screen.dispose()
        }
        screens.clear()
        Assets.dispose()
    }



    override fun pause() {
        screens.forEach(BaseScreen::pause)
    }

    var init = false
    override fun resume() {
        debug { "Resume Called, reloading Assets!" }
        Assets.reloadAssets()
        addScreen(PreloaderScreen(this))
        screens.forEach(BaseScreen::resume)
    }

    override fun render() {
        timeSinceLastRender = Math.min(timeSinceLastRender + Gdx.graphics.rawDeltaTime, maxDeltaTime)
        while (timeSinceLastRender >= fixedTimeStep) {
            timeSinceLastRender -= fixedTimeStep
            update(fixedTimeStep)
        }
        if (timeSinceLastRender > 0) {
            update(timeSinceLastRender)
            timeSinceLastRender = 0f
        }

        clearScreen(0f, 0f, 0f)
        for (screen in screens) {
            screen.render()
        }
    }

    fun update(delta: Float) {
        if (screens.firstOrNull { it is PreloaderScreen } == null)
            Assets.manager.update()

        for (screen in Array(screens)) {
            screen.update(delta)
        }
        val addCopy = Array(screensToAdd)
        screensToAdd.clear()
        val removeCopy = Array(screensToRemove)
        screensToRemove.clear()

        for (screen in addCopy) {
            if (screens.contains(screen, true)) {
                continue
            }

            val index = (0..screens.size - 1).firstOrNull {
                screens.get(it).priority < screen.priority
            } ?: -1
            if (index == -1) {
                if (screens.size > 0) {
                    screens.peek().pause()
                }
                screens.add(screen)
                screen.show()
            } else {
                screens.insert(index, screen)
                screen.show()
            }
            screen.resize(Gdx.graphics.width, Gdx.graphics.height)
        }
        for (pair in removeCopy.filter { screens.contains(it.first, true) }) {
            if (pair === screens.peek()) {
                screens.pop()
                screens.peek().resume()
            } else {
                screens.removeValue(pair.first, true)
            }
            pair.first.hide()
            if (pair.second) {
                pair.first.dispose()
            }
            if (pair.first == gameScreen)
                gameScreen = null
        }
    }

    override fun resize(width: Int, height: Int) {
        for (screen in screens) {
            screen.resize(width, height)
        }
    }

    fun removeScreen(screen: BaseScreen, dispose: Boolean = true) {
        screensToRemove.add(Pair(screen, dispose))
    }

    fun removeScreen(screenClass: Class<out BaseScreen>, dispose: Boolean = true) {
        screens.forEach {
            if (screenClass == it.javaClass) {
                screensToRemove.add(Pair(it, dispose))
            }
        }
    }

    fun <T: BaseScreen> addScreen(screen: T): T {
        screensToAdd.add(screen)
        return screen
    }


    inline fun <reified T: BaseScreen> getScreen(): T? {
        return screens.find { T::class.java == it.javaClass } as? T
    }

    fun <T: BaseScreen> switchToScreen(curScreen: BaseScreen, nextScreen: T, disposeOnReturn: Boolean = true): T {
        if (screenStack.containsKey(nextScreen)) {
            throw RuntimeException("Trying to switch to screen of type ${nextScreen::class.java.simpleName}, " +
                    "but the screen is already in the screen stack, this won't work currently")
        }
        screenStack.put(nextScreen, Pair(curScreen, disposeOnReturn))
        removeScreen(curScreen, false)
        addScreen(nextScreen)
        return nextScreen
    }

    fun switchToLastScreen(curScreen: BaseScreen): Boolean {
        if (!screenStack.containsKey(curScreen)) return false

        val lastScreen = screenStack[curScreen]
        screenStack.remove(curScreen)
        removeScreen(curScreen, lastScreen.second)
        addScreen(lastScreen.first)

        return true
    }

    fun isScreenVisible(screen: BaseScreen): Boolean {
        return screens.contains(screen, true)
    }

    fun startNewGame() {
        if (gameScreen == null) {
            gameScreen = GameScreen(this).apply {
                addScreen(this)
                _state.delegate = this
            }
        } else {
            addScreen(gameScreen!!)
            gameScreen!!.restartGame()
        }
        resumeGame()
    }

    /**
     * Exit the game. Cleans up all the resources
     */
    fun exit() {
        Gdx.app.exit()
    }

    inner class InputMultiplexer : InputProcessor {
        override fun keyDown(keycode: Int): Boolean {
            if (keycode == Input.Keys.ENTER && Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT)) {
                if (Gdx.graphics.isFullscreen)
                    Gdx.graphics.setWindowedMode(1920, 1080)
                else for (mode in Gdx.graphics.getDisplayModes(Gdx.graphics.primaryMonitor)) {
                        if (mode.width == 1920 && mode.height == 1080) {
                            Gdx.graphics.setFullscreenMode(mode)
                            break
                        }
                    }
                return true
            }
            return (screens.size - 1 downTo 0).any {
                screens.get(it).inputProcessor.keyDown(keycode)
            }
        }

        override fun keyUp(keycode: Int): Boolean {
            return (screens.size - 1 downTo 0).any {
                screens.get(it).inputProcessor.keyUp(keycode)
            }
        }

        override fun keyTyped(character: Char): Boolean {
            return (screens.size - 1 downTo 0).any {
                screens.get(it).inputProcessor.keyTyped(character)
            }
        }

        override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
            return (screens.size - 1 downTo 0).any {
                screens.get(it).inputProcessor.touchDown(screenX, screenY, pointer, button)
            }
        }

        override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
            return (screens.size - 1 downTo 0).any {
                screens.get(it).inputProcessor.touchUp(screenX, screenY, pointer, button)
            }
        }

        override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
            return (screens.size - 1 downTo 0).any {
                screens.get(it).inputProcessor.touchDragged(screenX, screenY, pointer)
            }
        }

        override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
            return (screens.size - 1 downTo 0).any {
                screens.get(it).inputProcessor.mouseMoved(screenX, screenY)
            }
        }

        override fun scrolled(amount: Int): Boolean {
            return (screens.size - 1 downTo 0).any {
                screens.get(it).inputProcessor.scrolled(amount)
            }
        }
    }
}
