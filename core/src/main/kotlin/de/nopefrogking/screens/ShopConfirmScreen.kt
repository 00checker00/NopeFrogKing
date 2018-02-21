package de.nopefrogking.screens

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.utils.Align
import de.nopefrogking.Item
import de.nopefrogking.Main
import de.nopefrogking.ui.*
import de.nopefrogking.utils.*
import ktx.actors.parallelTo
import ktx.actors.then

class ShopConfirmScreen(game: Main, val item: Item) : WindowScreen(game, "Confirm", FontIcon.ExclamationMark, false, SheetStyle.Gold, DefaultSkin.windowContentSmall()) {
    private val goldDisplay = Label("00000000", DefaultSkin.menu())
    private val icon = TextButton(item.icon(), ButtonType.CircleSmall.style())
    private val subIcon = icon.addSubIcon(DefaultSkin.menuCircleGreenSmallText(), "+")

    private var curGold = 0L
    private var numberToBuy by clamp(1, 99) { 1 }

    private var confirmButton: TextButton = addButton(FontIcon.Confirm) {
        if (game.gold >= item.price*numberToBuy) {
            stage.addActor(Label(bar.iconButton.text.toString(), bar.iconButton.label.style).apply {
                val pos = bar.iconButton.localToStageCoordinates(Vector2())
                x = pos.x
                y = pos.y
                width = bar.iconButton.width
                height = bar.iconButton.height

                setAlignment(Align.center)

                addAction(Actions.moveBy(0f, 100 * scale, 1f, Interpolation.pow2In)
                        parallelTo Actions.fadeOut(1f)
                        then Actions.removeActor()
                )
            })

            stage.addActor(Label(icon.text.toString(), icon.label.style).apply {
                val pos = icon.localToStageCoordinates(Vector2())
                x = pos.x
                y = pos.y
                width = icon.width
                height = icon.height

                setAlignment(Align.center)

                addAction(Actions.moveBy(0f, 100 * scale, 1f, Interpolation.pow2In)
                        parallelTo Actions.fadeOut(1f)
                        then Actions.removeActor()
                )
            })

            stage.addActor(Label("-${item.price * numberToBuy}", goldDisplay.style).apply {
                val pos = goldDisplay.localToStageCoordinates(Vector2())
                x = pos.x
                y = pos.y
                width = goldDisplay.width
                height = goldDisplay.height

                setAlignment(Align.center)

                addAction(Actions.moveBy(0f, 100 * scale, 1f, Interpolation.pow2In)
                        parallelTo Actions.fadeOut(1f)
                        then Actions.removeActor()
                )
            })


            stage.addActor(Label(subIcon.text.toString(), subIcon.label.style).apply {
                val pos = subIcon.localToStageCoordinates(Vector2())
                x = pos.x
                y = pos.y
                width = subIcon.width
                height = subIcon.height

                setAlignment(Align.center)

                addAction(Actions.moveBy(0f, 100 * scale, 1f, Interpolation.pow2In)
                        parallelTo Actions.fadeOut(1f)
                        then Actions.removeActor()
                )
            })


            SafePreferences { item[this@ShopConfirmScreen.item] += numberToBuy }

            game.gold -= item.price * numberToBuy
        }
    }

    private val bar = IconBar(DefaultSkin, FontIcon.Nothing, IconBarOptions().apply {
        this.iconStyle = DefaultSkin.menuInputSmall
        this.barHeight = ButtonType.CircleSmall.height
    })

    init {
        isClosable = true

        onClose { goBack() }
    }

    override fun show() {
        super.show()
        curGold = game.gold
    }

    override fun update(delta: Float) {
        super.update(delta)

        if (game.gold < curGold)
            curGold--
        else if (game.gold > curGold)
            curGold++

        goldDisplay.setText("$curGold")
        if (game.gold >= numberToBuy * item.price) {
            bar.contentButton.label.color = Color.WHITE
            confirmButton.isDisabled = false
        } else {
            bar.contentButton.label.color = Color.RED
            confirmButton.isDisabled = true
        }

//        superIcon.text = when (item) {
//            Item.Flask -> SafePreferences { itemFlask }
//            Item.Orb -> SafePreferences { itemOrb }
//            Item.Umbrella -> SafePreferences { itemUmbrella }
//            Item.Storm -> SafePreferences { itemStorm }
//        }.toString()
    }

    override fun createContentView(table: Table, tab: Int) {
        table.addSpacer().expandY().colspan(2).row()

        table.add(icon)
                .width(ButtonType.CircleSmall.width * scale)
                .height(ButtonType.CircleSmall.height * scale)

        table.add(bar).expandX().fillX().row()

        bar.contentButton.labelCell.expandX()
        bar.contentButtonCell.expand().fill()
        bar.contentButton.label.setAlignment(Align.right)
        bar.contentButton.add(Image(DefaultSkin.coin()))
                .width(ButtonType.CircleXS.width * scale)
                .height(ButtonType.CircleXS.height * scale)
                .padLeft(5 * scale)

        bar.connectToButtons(icon) {
            val dialog = Dialogs.newDialog<GDXNumberPrompt>().apply {
                title = "How much Nigga?"

                message = "Give us some Moneys!1!!!"

                minValue = 1
                value = 1
                maxValue = 99

                confirmText = "Ok"
                cancelText = "No thanks"

                setNumberPromptListener(object: NumberPromptListener {
                    override fun cancel() {

                    }

                    override fun confirm(text: Int) {
                        updateNumberToBuy(text)
                    }

                })

            }.build().show()
        }

        table.addSpacer().expandY().colspan(2).row()

        updateNumberToBuy(1)
    }

    fun updateNumberToBuy(count: Int) {
        numberToBuy = count
        bar.iconText = "${numberToBuy}x"
        bar.text = "${item.price*numberToBuy}"
    }

    override fun createSheetView(table: Table) {
        table
                .add(goldDisplay)
                .expand()
                .pad(0.0f, 15*scale, 10*scale, 5*scale)
                .align(Align.right)

        table.add(Image(DefaultSkin.coin()))
                .pad(0.0f, 0f, 10*scale, 15*scale)
                .width(goldDisplay.height)
                .height(goldDisplay.height)
    }
}