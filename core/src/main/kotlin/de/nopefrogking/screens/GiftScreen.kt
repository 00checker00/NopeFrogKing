package de.nopefrogking.screens

import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import de.nopefrogking.Main
import de.nopefrogking.ui.IconBar
import de.nopefrogking.ui.IconBarOptions
import de.nopefrogking.ui.OpenGift
import de.nopefrogking.utils.DefaultSkin
import de.nopefrogking.utils.FontIcon
import de.nopefrogking.utils.addSpacer

class GiftScreen(game: Main) : WindowScreen(game, "Quest", FontIcon.Present, true, false) {
    private val leftButton: TextButton
    private val rightButton: TextButton

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
        if (tab == 0) {
            return TabDetails("Quest", DefaultSkin.ui_tab_icon_items)
        } else {
            return TabDetails("Gifts", DefaultSkin.ui_tab_icon_gift)
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

    override fun createContentView(table: Table, tab: Int) {
        if (tab == 1) {
            table.add(OpenGift()).expand().width(200f * scale).height(200f * scale)
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
    }

}