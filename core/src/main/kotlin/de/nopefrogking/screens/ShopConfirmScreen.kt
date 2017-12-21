package de.nopefrogking.screens

import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.utils.Align
import de.nopefrogking.Item
import de.nopefrogking.Main
import de.nopefrogking.ui.Dialogs
import de.nopefrogking.ui.GDXNumberPrompt
import de.nopefrogking.ui.IconBar
import de.nopefrogking.ui.IconBarOptions
import de.nopefrogking.utils.DefaultSkin
import de.nopefrogking.utils.FontIcon
import de.nopefrogking.utils.addSpacer
import de.nopefrogking.utils.addSubIcon

class ShopConfirmScreen(game: Main, val item: Item) : WindowScreen(game, "Confirm Purchase", FontIcon.ExclamationMark, false, true, DefaultSkin.windowContentSmall()) {
    private val goldDisplay = Label("00000000", DefaultSkin.menu())
    private lateinit var count: ImageTextButton

    init {
        addButton(FontIcon.Confirm) {

        }

        isClosable = true

        onClose { goBack() }
    }

    override fun show() {
        goldDisplay.setText("${game.gold}")

        super.show()
    }

    override fun createContentView(table: Table, tab: Int) {
        table.addSpacer().expandY().colspan(2).row()


        val icon = TextButton(item.icon(), ButtonType.CircleSmall.style())
        table.add(icon)
                .width(ButtonType.CircleSmall.width * scale)
                .height(ButtonType.CircleSmall.height * scale)

        count = icon.addSubIcon(DefaultSkin.menuCircleGreenSmallText(), "+0")

        val bar = IconBar(DefaultSkin, FontIcon.Nothing, IconBarOptions().apply {
            this.iconStyle = DefaultSkin.menuInputSmall
            this.barHeight = ButtonType.CircleSmall.height
        })
        bar.iconText = "1x"
        table.add(bar).expandX().fillX().row()

        bar.connectToButtons(icon) {
            val dialog = Dialogs.newDialog<GDXNumberPrompt>().apply {
                title = "How much Nigga?"

                message = "Give us some Moneys!1!!!"

                minValue = 1
                value = 1
                maxValue = 99

                confirmText = "Ok"
                cancelText = "No thanks"

            }.build().show()
        }

        table.addSpacer().expandY().colspan(2).row()
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