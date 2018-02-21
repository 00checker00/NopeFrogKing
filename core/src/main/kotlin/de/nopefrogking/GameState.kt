
package de.nopefrogking

import com.badlogic.gdx.utils.Array as GdxArray

interface GameState {
    val level: Int
    val levelProgress: Float
    val bossBegin: Float
    val isBossFight: Boolean
    val score: Float
    val bonusPoints: GdxArray<Int>
    val bonusMoney: GdxArray<Long>
    val messages: GdxArray<String>
    val isPaused: Boolean
    val isRunning: Boolean
    val cooldowns: Map<Item, Float>
    val state: State

    enum class State { Running, EndedPrinceKiss, EndedStoneHit }
}

class StubGameState(var delegate: GameState? = null): GameState {
    override val level: Int
        get() = delegate?.level ?: 1

    override val levelProgress: Float
        get() = delegate?.levelProgress ?: 0f

    override val bossBegin: Float
        get() = delegate?.bossBegin ?: 0.7f

    override val isBossFight: Boolean
        get() = delegate?.isBossFight ?: false

    override val isRunning: Boolean
        get() = delegate?.isRunning ?: false

    override val score: Float
        get() = delegate?.score ?: 0f

    override val bonusPoints: GdxArray<Int>
        get() = delegate?.bonusPoints ?: GdxArray()

    private val bonusMoneyField = GdxArray<Long>()
    override val bonusMoney: GdxArray<Long>
        get() = delegate?.bonusMoney ?: bonusMoneyField

    override val messages: GdxArray<String>
        get() = delegate?.messages ?: GdxArray()

    override val isPaused: Boolean
        get() = delegate?.isPaused ?: true

    override val cooldowns: Map<Item, Float>
        get() = delegate?.cooldowns ?: mapOf()

    override val state: GameState.State
        get() = delegate?.state ?: GameState.State.EndedStoneHit
}