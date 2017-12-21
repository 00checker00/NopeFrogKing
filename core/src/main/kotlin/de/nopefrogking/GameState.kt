
package de.nopefrogking

import com.badlogic.gdx.utils.Array as GdxArray

interface GameState {
    val level: Int
    val levelProgress: Float
    val isBossFight: Boolean
    val score: Float
    val bonusPoints: GdxArray<Int>
    val messages: GdxArray<String>
    val isPaused: Boolean
    val Cooldowns: Map<Item, Float>
}

class StubGameState(var delegate: GameState? = null): GameState {
    override val level: Int
        get() = delegate?.level ?: 1

    override val levelProgress: Float
        get() = delegate?.levelProgress ?: 0f

    override val isBossFight: Boolean
        get() = delegate?.isBossFight ?: false

    override val score: Float
        get() = delegate?.score ?: 0f

    override val bonusPoints: GdxArray<Int>
        get() = delegate?.bonusPoints ?: GdxArray<Int>()

    override val messages: GdxArray<String>
        get() = delegate?.messages ?: GdxArray<String>()

    override val isPaused: Boolean
        get() = delegate?.isPaused ?: false

    override val Cooldowns: Map<Item, Float>
        get() = delegate?.Cooldowns ?: mapOf()
}