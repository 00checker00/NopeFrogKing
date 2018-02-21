package de.nopefrogking.screens

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import de.nopefrogking.*
import de.nopefrogking.GameState.State.*
import de.nopefrogking.utils.DefaultSkin
import de.nopefrogking.utils.FontIcon
import de.nopefrogking.utils.SafePreferences
import ktx.actors.alpha
import ktx.scene2d.image
import ktx.scene2d.label
import ktx.scene2d.stack
import ktx.scene2d.table

class GameOverScreen(game: Main) : WindowScreen(game,
        if (game.state.state == Running) "Pause" else "Game Over",
        if (game.state.state == Running) FontIcon.Pause else FontIcon.Highscore,
        game.state.state != Running,
        SheetStyle.Normal ) {

    private val lose_princ by Assets.getSound("music/lose_princ.mp3") {
        if (game.state.state == GameState.State.EndedPrinceKiss) {
            it?.play()?.also { id ->
                it.setLooping(id, true)
            }
        }
    }

    private val highscorePrince = DefaultSkin.highscore_prince
    private val highscoreStone = DefaultSkin.highscore_stone
    private val gameoverPrince = DefaultSkin.gameover_prince
    private val gameoverStone = DefaultSkin.gameover_stone

    private var effect: ParticleEffectPool.PooledEffect? = null

    private var color = Color.WHITE

    private val batch = SpriteBatch()

    private val newHighscore = SafePreferences { highscore < game.state.score }

    init {
        addButton(FontIcon.Home) {
            game.removeScreen(GameScreen::class.java, true)
            game.removeScreen(this, true)
            game.addScreen(MainMenuScreen(game))
        }

        addButton(FontIcon.Repeat) {
            game.removeScreen(this, true)
            game.startNewGame()
        }

        isClosable = false

        if (game.state.state == GameState.State.Running) {
            addButton(FontIcon.Play) {
                game.removeScreen(this, true)
                game.resumeGame()
            }
        } else {
            if (game.state.state == GameState.State.EndedPrinceKiss) {
                Sounds.kiss().play(0.5f)
            }
        }

        when {
            game.state.state != Running && newHighscore -> Pair(highscoreEffectPool?.obtain(), Color.YELLOW)
            game.state.state == EndedPrinceKiss -> Pair(losePrinceEffectPool?.obtain(), Color.RED)
            game.state.state == EndedStoneHit -> Pair(loseStoneEffectPool?.obtain(), Color.WHITE)
            else -> null
        }?.also { (effect, color) ->
            this@GameOverScreen.effect = effect
            this@GameOverScreen.color = color
            effect?.scaleEffect(scale)
        }

        if (newHighscore) {
            SafePreferences { highscore = game.state.score.toLong() }
        }
    }

    override fun createContentView(table: Table, tab: Int) {
        table.add(stack {
            if (game.state.state != Running) {
                image(when {
                    newHighscore && game.state.state == EndedPrinceKiss -> highscorePrince.name
                    newHighscore && game.state.state == EndedStoneHit -> highscoreStone.name
                    !newHighscore && game.state.state == EndedPrinceKiss -> gameoverPrince.name
                    else -> gameoverStone.name
                }, DefaultSkin).apply {
                    setScaling(Scaling.fit)
                    setAlign(Align.right)
                    alpha = 0.5f
                }
            }

            table {
                label("Level: ${game.state.level}", DefaultSkin.menu.name) {
                    it.expandX()
                    it.align(Align.left)
                    it.padBottom(5 * scale)
                }

                row()
                label("Highscore: ${SafePreferences { highscore }}", DefaultSkin.menu.name) {
                    if (newHighscore) color = Color(1.0f, 1.0f, 0.0f, 1.0f)
                    it.expandX()
                    it.align(Align.left)
                    it.padBottom(5 * scale)
                }
                row()
                label("Score: ${game.state.score.toInt()}", DefaultSkin.menu.name) {
                    it.expandX()
                    it.align(Align.left)
                    it.padBottom(5 * scale)
                }
                row()
                label("Stones passed: 0", DefaultSkin.menu.name)  {
                    it.expandX()
                    it.align(Align.left)
                    it.padBottom(5 * scale)
                }
                row()
                label("Stones destroyed: 0", DefaultSkin.menu.name) {
                    it.expandX()
                    it.align(Align.left)
                    it.padBottom(5 * scale)
                }
                row()
            }
        }).expand().fill()
    }

    override fun update(delta: Float) {
        super.update(delta)

        effect?.also {
            it.emitters.forEach { it.tint.colors = floatArrayOf(color.r, color.g, color.b) }
            it.setPosition(stage.width / 2f, stage.height / 2f)
            it.update(delta)
            if (it.isComplete) {
                freeEffect()
                effect = null
            }
        }
    }

    override fun render() {
        batch.begin()
        effect?.draw(batch)
        batch.end()
        super.render()
    }

    override fun createTab(tab: Int): TabDetails {
        return TabDetails(when (tab) {
            0 -> "Results"
            else -> "Quests"
        })
    }

    override fun createSheetView(table: Table) {
        table.add(ktx.scene2d.table {
            for (item in arrayOf(Item.Flask, Item.Orb, Item.Storm, Item.Umbrella)) {
                label(item.icon(), DefaultSkin.pauseIcons.name) {
                    setFontScale(0.8f)
                    it.expandX()
                    it.fill()
                    it.align(Align.right)
                }

                label(SafePreferences { this.item[item] }.toString(), DefaultSkin.menu.name) {
                    setFontScale(0.8f)
                    it.expand()
                    it.fill()
                    it.align(Align.left)
                }
            }
        }).expand().fill().pad(0.0f, 15*scale, 10*scale, 15*scale)
    }

    override fun dispose() {
        super.dispose()
        lose_princ?.stop()
        freeEffect()
    }

    fun freeEffect() {
        effect?.scaleEffect(1/scale)
        effect?.free()
        effect = null
    }

    companion object {
        internal val highscoreEffectPool: ParticleEffectPool? by Assets.getParticleEffect("particles/Highscore.p")
        internal val losePrinceEffectPool: ParticleEffectPool? by Assets.getParticleEffect("particles/LosePrince.p")
        internal val loseStoneEffectPool: ParticleEffectPool? by Assets.getParticleEffect("particles/LoseStone.p")
    }
}