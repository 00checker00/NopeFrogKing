package de.nopefrogking.screens

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.pay.Information
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.utils.Align
import de.nopefrogking.IAP
import de.nopefrogking.Item
import de.nopefrogking.Main
import de.nopefrogking.Sounds
import de.nopefrogking.ui.IconBar
import de.nopefrogking.ui.IconBarOptions
import de.nopefrogking.utils.DefaultSkin
import de.nopefrogking.utils.FontIcon
import de.nopefrogking.utils.addSpacer
import de.nopefrogking.utils.addSubIcon
import ktx.actors.repeatForever
import ktx.actors.then
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

class ShopScreen(game: Main) : WindowScreen(game, "Shop", FontIcon.Shop, true, true) {
    private val goldDisplay = Label("00000000", DefaultSkin.menu())
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
            return TabDetails("Items", DefaultSkin.ui_tab_icon_items)
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
//            if (!IAP.isAvailable) {
//                game.showToastMessages("IAP not available")
//                visibleTab = 0
//                return
//            }

            for (offer in arrayOf(IAP.Offer.GiftSmall, IAP.Offer.GiftMedium, IAP.Offer.GiftBig, IAP.Offer.GiftHuge)) {
                val info = when {
                    IAP.isAvailable -> IAP.getInfo(offer)
                    else -> Information.UNAVAILABLE
                }

                var title = info.localName ?: "Error"
                if (IAP.storeName == "GooglePlay") {
                    title = title.substringBeforeLast("(")
                }

                val buttonSize = 45.0f

                val barOptions = IconBarOptions().apply {
                    barHeight = buttonSize
                    iconStyle = DefaultSkin.menuCircleGoldSmallIcon
                }
                val bar = IconBar(DefaultSkin, offer.id, barOptions).apply {
                    text = title

                    contentButton.style = TextButton.TextButtonStyle(contentButton.style).apply {
                        this.fontColor = Color(1.0f, 1.0f, 0.0f, 1.0f)
                    }
                }

                table.add(bar).expand().fill()

                val amount:Double = (info.priceInCents?:0).toDouble() / 100.0
                val currency = Currency.getInstance(info.priceCurrencyCode ?: "EUR")
                val format = NumberFormat.getCurrencyInstance() as DecimalFormat
                format.maximumFractionDigits = currency.defaultFractionDigits
                format.currency = currency
                val symbol = format.decimalFormatSymbols.currencySymbol
                format.decimalFormatSymbols = format.decimalFormatSymbols.apply { currencySymbol = "" }
                val formattedAmount = format.format(amount)

                val price = TextButton(formattedAmount, ButtonType.CircleSmallText.style())

                table.add(price)
                        .width(buttonSize * scale)
                        .height(buttonSize * scale)
                        .row()


                val currencyBtn = price.addSubIcon(DefaultSkin.menuCircleXSImage(), symbol)

                table.addSpacer().expand().row()

                bar.connectToButtons(price, currencyBtn) {
                    IAP.purchase(offer)
                    Sounds.click().play()
                }
            }
        } else {
            for (item in Item.values()) {
                val buttonSize = 45.0f

                val barOptions = IconBarOptions().apply {
                    barHeight = buttonSize
                    iconStyle = DefaultSkin.menuCircleGreenSmall
                }
                val bar = IconBar(DefaultSkin, item.icon, barOptions).apply {
                    text = item.title()

                    iconButton.addSubIcon(DefaultSkin.menuCircleXSCoin())
                }

                table.add(bar).expand().fill()


                val price = TextButton("100", DefaultSkin.menuCircleGoldSmall())

                table.add(price)
                        .width(buttonSize * scale)
                        .height(buttonSize * scale).row()

                table.addSpacer().expand().row()

                bar.connectToButtons(price) {
                    switchToScreen(ShopConfirmScreen(game, item), true)
                    Sounds.click().play()
                }
            }
        }
    }

    override fun update(delta: Float) {
        goldDisplay.setText("${game.gold}")

        super.update(delta)
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

    override fun createTitleContent(table: Table) {
        val marker = table.addSpacer().align(Align.topRight)

        val badgeImage = DefaultSkin.ui_premium_badge()
        val badgeRatio = badgeImage.minWidth / badgeImage.minHeight
        val badgeWidth = ButtonType.Circle.width * scale * badgeRatio
        val badgeHeight = ButtonType.Circle.width * scale

        val premiumBadge = Image(badgeImage).apply {
            originX = badgeWidth/2
            originY = badgeHeight/2
            this.width = badgeWidth
            this.height = badgeHeight


            addAction(
                    (Actions.rotateBy(PREMIUM_BADGE_ROTATION, 3f) then (
                            Actions.rotateBy(-PREMIUM_BADGE_ROTATION*2, 3f)
                                    then Actions.rotateBy(PREMIUM_BADGE_ROTATION*2, 3f)
                            ).repeatForever())
            )

            addAction(
                    (Actions.scaleTo(PREMIUM_BADGE_SCALE, PREMIUM_BADGE_SCALE, 2.5f)
                            then Actions.scaleTo(1f, 1f, 2.5f)).repeatForever()
            )

            addAction(Actions.run {
                val pos = marker.actor.localToStageCoordinates(Vector2(0f, 0f))
                x = pos.x - badgeWidth
                y = pos.y - badgeHeight
            }.repeatForever())
        }
        stage.addActor(premiumBadge)

        val highlightImage = DefaultSkin.ui_premium_highlight()
        val highlightRatio = highlightImage.minWidth / highlightImage.minHeight
        val highlightWidth = ButtonType.CircleMedium.width * scale * highlightRatio
        val highlightHeight = ButtonType.CircleMedium.width * scale

        val highlight = Image(highlightImage).apply {
            originX = highlightWidth/2
            originY = highlightHeight/2
            this.width = highlightWidth
            this.height = highlightHeight
            scaleX = 0f
            scaleY = 0f

            addAction(
                    (Actions.delay(3f)
                            then Actions.parallel(
                            Actions.scaleTo(1f, 1f, 3f, Interpolation.elasticIn)
                                    then Actions.scaleTo(0f, 0f, 3f, Interpolation.elasticOut),
                            Actions.rotateBy(-60f, 4f, Interpolation.pow2)
                    )).repeatForever()
            )

            addAction(Actions.run {
                val pos = marker.actor.localToStageCoordinates(Vector2(0f, 0f))
                x = pos.x - badgeWidth/2 - highlightWidth/2 + badgeWidth/4
                y = pos.y - badgeHeight/2 - highlightHeight/2 + badgeHeight/3
            }.repeatForever())
        }
        stage.addActor(highlight)

    }

    companion object {
        private val PREMIUM_BADGE_ROTATION = 10f
        private val PREMIUM_BADGE_SCALE = 1.2f
    }
}