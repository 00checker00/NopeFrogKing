package de.nopefrogking.ui

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.utils.Queue
import de.nopefrogking.Item
import de.nopefrogking.Main
import de.nopefrogking.actors.AnimationWidget
import de.nopefrogking.actors.Bubble
import de.nopefrogking.utils.*
import ktx.actors.alpha
import ktx.actors.onClick
import ktx.actors.parallelTo
import ktx.actors.then
import ktx.collections.toGdxList
import ktx.math.plus
import ktx.math.times

/**
 * Created by Marco Kirchner.
 */
class OpenGift(private val game: Main): AnimationWidget() {
    var clickCount = 0
    var gift_front = Image(DefaultSkin.gift_open_front())
    var open = false

    sealed class Reward(val icon: NamedAsset<Drawable>) {
        class Gold(val size: Size): Reward(size.asset) {
            enum class Size(val asset: NamedAsset<Drawable>, val amount: Long) {
                Small(DefaultSkin.gold_small, 10L),
                Medium(DefaultSkin.gold_medium, 20L),
                Big(DefaultSkin.gold_big, 30L),
                XL(DefaultSkin.gold_xl, 70L),
            }
        }
        class Item(val item: de.nopefrogking.Item): Reward(item.drawable)
    }

    init {
        animation = Animation(0.44f, DefaultSkin.gift_open())
        paused = true

        addActor(gift_front)

        onClick { _, _ ->
            clickCount++

            if (clickCount >= GIFT_OPEN_CLICK_COUNT) {
                if (open) return@onClick

                open = true
                paused = false

                val pos = Vector2(this@OpenGift.originX, this@OpenGift.originY) + ( BUBBLE_START.cpy() * DefaultSkin.UIScale )


                val rewards = arrayListOf(REWARDS_GOLD, REWARDS_GOLD, REWARDS_ITEM)
                        .sortedBy { random.nextBoolean() }
                        .toGdxList()
                BUBBLE_END.forEach {
                    val reward = rewards.removeFirst().getRandom()
                    val bubble = Bubble(reward.icon).apply {
                        width = 48 * DefaultSkin.UIScale
                        height = 48 * DefaultSkin.UIScale

                        x = pos.x
                        y = pos.y

                        alpha = 0f
                    }
                    addActor(bubble)

                    val end = pos.cpy() + ( it.cpy() * DefaultSkin.UIScale )

                    bubble.addAction(Actions.delay(1.8f)
                            then Actions.fadeIn(0.2f)
                            then Actions.moveTo(end.x, end.y, 1.5f, Interpolation.pow2))

                    when (reward) {
                        is Reward.Gold -> game.addMoney(reward.size.amount)
                        is Reward.Item -> SafePreferences { this.item[reward.item] += 1 }
                    }
                }
            } else {
                val rot = 30f * if (clickCount%2 == 0) -1 else +1

                showFrame(1)
                touchable = Touchable.disabled

                addAction(
                        (Actions.rotateTo(rot, 0.3f) parallelTo Actions.scaleTo(1.3f, 1.3f, 0.3f)) then
                                (Actions.rotateTo(0f, 0.3f) parallelTo Actions.scaleTo(1f, 1f, 0.3f)) then Actions.run {
                    showFrame(0)
                    touchable = Touchable.enabled
                })
            }
        }
    }

    override fun layout() {
        super.layout()
        originX = width/2
        originY = height/2

        gift_front.width = width
        gift_front.height = height
    }

    override fun act(delta: Float) {
        super.act(delta)

        if (animationFinished && !paused && !open) {
            gift_front.isVisible = true
            open = true
        }
    }

    companion object {
        val GIFT_OPEN_CLICK_COUNT = 5

        val BUBBLE_START = Vector2(-25f, -10f)

        val BUBBLE_END = arrayOf(Vector2(-80f, 35f), Vector2(0f, 50f), Vector2(80f, 35f))

        val REWARDS_ITEM = arrayOf(
                Reward.Item(Item.Flask),
                Reward.Item(Item.Flask),
                Reward.Item(Item.Flask),
                Reward.Item(Item.Flask),
                Reward.Item(Item.Storm),
                Reward.Item(Item.Storm),
                Reward.Item(Item.Storm),
                Reward.Item(Item.Storm),
                Reward.Item(Item.Orb),
                Reward.Item(Item.Umbrella)
        )

        val REWARDS_GOLD = arrayOf(
                Reward.Gold(Reward.Gold.Size.Small),
                Reward.Gold(Reward.Gold.Size.Small),
                Reward.Gold(Reward.Gold.Size.Small),
                Reward.Gold(Reward.Gold.Size.Small),
                Reward.Gold(Reward.Gold.Size.Medium),
                Reward.Gold(Reward.Gold.Size.Medium),
                Reward.Gold(Reward.Gold.Size.Medium),
                Reward.Gold(Reward.Gold.Size.Big),
                Reward.Gold(Reward.Gold.Size.Big),
                Reward.Gold(Reward.Gold.Size.XL)
        )
    }
}