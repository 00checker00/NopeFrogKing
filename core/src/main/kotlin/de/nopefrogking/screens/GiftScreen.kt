package de.nopefrogking.screens

import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Cell
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.utils.Align
import de.nopefrogking.Main
import de.nopefrogking.Sounds
import de.nopefrogking.ui.IconBar
import de.nopefrogking.ui.IconBarOptions
import de.nopefrogking.ui.OpenGift
import de.nopefrogking.utils.DefaultSkin
import de.nopefrogking.utils.FontIcon
import de.nopefrogking.utils.SafePreferences
import de.nopefrogking.utils.addSpacer
import ktx.actors.alpha
import ktx.actors.parallelTo
import ktx.actors.then
import ktx.scene2d.table

class GiftScreen(game: Main) : WindowScreen(game, "Quest", FontIcon.Present, true) {
    private val leftButton: TextButton
    private val rightButton: TextButton


    private var giftCount: TextButton? = null

    init {
        leftButton = addButton(FontIcon.ArrowLeft) {
            visibleTab = 0
        }
        rightButton = addButton(FontIcon.ArrowRight) {
            visibleTab = 1
        }

        onClose { goBack() }

        isClosable = true
    }


    override fun createTab(tab: Int): TabDetails {
        return if (tab == 0) {
            TabDetails("Quests", DefaultSkin.ui_tab_icon_items)
        } else {
            TabDetails("Gifts", DefaultSkin.ui_tab_icon_gift)
        }
    }

    override fun onTabSwitched(tab: Int) {
        if (tab == 0) {
            leftButton.isDisabled = true
            rightButton.isDisabled = false
        } else {
            leftButton.isDisabled = false
            rightButton.isDisabled = true
        }
    }

    fun createGiftWidget(table: Table) {
        table.add(
            OpenGift(game).apply {

                onAllPopped {
                    addAction(Actions.fadeOut(0.5f)
                            then Actions.run {
                        table.clearChildren()
                        createGiftWidget(table)
                    })
                }

                if (SafePreferences { presents } > 0 ) {
                    setScale(2.0f)
                    addAction(Actions.scaleTo(1f, 1f, 0.2f)
                            parallelTo Actions.fadeIn(2.0f)
                    )
                } else {
                    touchable = Touchable.disabled
                    alpha = 0.5f
                }


            }).width(200f * scale).height(200f * scale).align(Align.center)
    }



    override fun createContentView(table: Table, tab: Int) {
        if (tab == 1) {
            table.apply {
                add(ktx.scene2d.stack {

                    table {
                       createGiftWidget(this)
                    }

                    table {
                        addSpacer().expand()

                        row()

                        table { cell ->
                            val buttonSize = 45.0f

                            val barOptions = IconBarOptions().apply {
                                barHeight = buttonSize
                                iconStyle = DefaultSkin.menuCircleGreenSmall
                            }
                            val bar = IconBar(DefaultSkin, FontIcon.Shop, barOptions).apply {
                                text = "Get more"
                            }

                            add(bar).expand().fill()

                            giftCount = TextButton("x99", DefaultSkin.menuCircleSmallText()).let {
                                add(it)
                                        .width(buttonSize * scale)
                                        .height(buttonSize * scale).row()

                                bar.connectToButtons(it) {
                                    switchToScreen(ShopScreen(game), true)
                                    Sounds.click().play()
                                }

                                it
                            }



                            cell.expandX().fill()
                        }
                    }
                }).expand().fill()
            }
        } else {
            val buttonType = ButtonType.CircleSmall
            val size = buttonType.width * scale

            table.addSpacer().expandY().row()
            val barNormalQuest =IconBar(DefaultSkin, FontIcon.Progress, IconBarOptions().apply {
                barHeight = buttonType.height
            })
            table.add(barNormalQuest).expandX().fillX()

            barNormalQuest.text = "fall 400m"

            val btnNormalQuest = TextButton(FontIcon.Present(), DefaultSkin.menuCircleGoldSmallIcon())
            table.add(btnNormalQuest)
                    .width(size)
                    .height(size)
            table.row()

            barNormalQuest.connectToButtons(btnNormalQuest) {

            }

            table.addSpacer().expandY().row()

            val barPremiumQuest = IconBar(DefaultSkin, FontIcon.Progress, IconBarOptions().apply {
                barHeight = buttonType.height
                barEndStyle = "ui_premium_end"
                barStyle = "ui_premium_bar"
                iconStyle = DefaultSkin.menuCircleGoldSmallIcon
            })
            table.add(barPremiumQuest).expandX().fillX()
            barPremiumQuest.text = "premium quest"

            val btnPremiumQuest = TextButton(FontIcon.Present(), DefaultSkin.menuCircleGoldSmallIcon())
            table.add(btnPremiumQuest)
                    .width(size)
                    .height(size)
            table.row()

            barPremiumQuest.connectToButtons(btnPremiumQuest) {

            }

            table.addSpacer().expandY().row()
        }
    }

    override fun update(delta: Float) {
        super.update(delta)

        giftCount?.setText("x" + SafePreferences { presents }.toString())
    }

}