package de.nopefrogking.utils

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.Array
import de.nopefrogking.Assets
import de.nopefrogking.ui.levelProgressBarStyle

val baseFontFile = Gdx.files.internal("ui/GosmickSans.ttf")
val ICON_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ€$£"

/******* Fonts *******/
internal val defaultFontFreetype = Assets.fontManager.load {
    name = "defaultFontFreetype"
    file = baseFontFile
}
internal val messageFontFreetype = Assets.fontManager.load {
    name = "messageFontFreetype"
    file = baseFontFile
    size = (22 * scale).toInt()
    color = com.badlogic.gdx.graphics.Color.WHITE
    borderWidth = 3f
    borderColor = com.badlogic.gdx.graphics.Color.BLACK
}
internal val menuFontFreetype = Assets.fontManager.load {
    name = "menuFontFreetype"
    file = Gdx.files.internal("ui/GoodDog.otf")
    color = com.badlogic.gdx.graphics.Color.WHITE
    borderWidth = 3f * scale
    borderColor = com.badlogic.gdx.graphics.Color.BLACK
    size = (36 * scale).toInt()
}
internal val menuSmallFreetype = Assets.fontManager.load {
    name = "menuSmallFreetype"
    file = Gdx.files.internal("ui/GoodDog.otf")
    color = com.badlogic.gdx.graphics.Color.WHITE
    borderWidth = 3f * scale
    borderColor = com.badlogic.gdx.graphics.Color.BLACK
    size = (24 * scale).toInt()
}
internal val menuXSFreetype = Assets.fontManager.load {
    name = "menuXSFreetype"
    file = Gdx.files.internal("ui/GoodDog.otf")
    color = com.badlogic.gdx.graphics.Color.WHITE
    borderWidth = 3f * scale
    borderColor = com.badlogic.gdx.graphics.Color.BLACK
    size = (18 * scale).toInt()
}
internal val menuIconsFreetype = Assets.fontManager.load {
    name = "menuIconsFreetype"
    file = Gdx.files.internal("ui/MenuIcons.ttf")
    color = com.badlogic.gdx.graphics.Color.WHITE
    characters = ICON_CHARS
    borderWidth = 3f * scale
    borderColor = com.badlogic.gdx.graphics.Color.BLACK
    size = (60 * scale).toInt()
}
internal val menuIconsXSFreetype = Assets.fontManager.load {
    name = "menuIconsXSFreetype"
    file = Gdx.files.internal("ui/MenuIcons.ttf")
    color = com.badlogic.gdx.graphics.Color.WHITE
    characters = ICON_CHARS
    borderWidth = 1.5f * scale
    borderColor = com.badlogic.gdx.graphics.Color.BLACK
    size = (18 * scale).toInt()
}
internal val menuIconsSmallFreetype = Assets.fontManager.load {
    name = "menuIconsSmallFreetype"
    file = Gdx.files.internal("ui/MenuIcons.ttf")
    color = com.badlogic.gdx.graphics.Color.WHITE
    characters = ICON_CHARS
    borderWidth = 3f * scale
    borderColor = com.badlogic.gdx.graphics.Color.BLACK
    size = (36 * scale).toInt()
}
internal val menuIconsMediumFreetype = Assets.fontManager.load {
    name = "menuIconsMediumFreetype"
    file = Gdx.files.internal("ui/MenuIcons.ttf")
    color = com.badlogic.gdx.graphics.Color.WHITE
    borderWidth = 3f * scale
    characters = ICON_CHARS
    borderColor = com.badlogic.gdx.graphics.Color.BLACK
    size = (48 * scale).toInt()
}
internal val noTextFontFreetype = Assets.fontManager.load {
    name = "noTextFontFreetype"
    file = baseFontFile
    size = 0
}
internal val hudProgressIconFontFreetype = Assets.fontManager.load {
    name = "hudProgressIconFontFreetype"
    file = Gdx.files.internal("ui/MenuIcons.ttf")
    color = com.badlogic.gdx.graphics.Color.WHITE
    characters = ICON_CHARS
    borderWidth = 3f * scale
    borderColor = com.badlogic.gdx.graphics.Color.BLACK
    size = (22 * scale).toInt()
}
internal val hudProgressFontFreetype = Assets.fontManager.load {
    name = "hudProgressFontFreetype"
    file = Gdx.files.internal("ui/GoodDog.otf")
    color = com.badlogic.gdx.graphics.Color.WHITE
    borderWidth = 3f * scale
    borderColor = com.badlogic.gdx.graphics.Color.BLACK
    size = (22 * scale).toInt()
}
/******* End Fonts *******/

class NopeFrogKingSkin(atlas: TextureAtlas, scale: Float): Skin(atlas) {
    /******* Drawables *******/
    val ui_button_normal by drawable().cached()
    val ui_button_pressed by drawable().cached()
    val ui_circle_normal by drawable().cached()
    val ui_circle_pressed by drawable().cached()
    val ui_circle_gold_normal by drawable().cached()
    val ui_circle_gold_pressed by drawable().cached()
    val ui_circle_green_normal by drawable().cached()
    val ui_circle_green_pressed by drawable().cached()
    val ui_input_normal by drawable().cached()
    val ui_input_pressed by drawable().cached()
    val ui_tab_icon_items by drawable().cached()
    val ui_tab_icon_gold by drawable().cached()
    val ui_tab_icon_gift by drawable().cached()
    val lvlbar_bg by drawable().cached()
    val lvlbar_fg_princess by drawable().cached()
    val lvlbar_bg_prince by drawable().cached()
    val lvlbar_handle_princess by drawable().cached()
    val lvlbar_fg_prince by drawable().cached()
    val lvlbar_handle_prince by drawable().cached()
    val ui_window by drawable().cached()
    val ui_window_small by drawable().cached()
    val ui_window_title by drawable().cached()
    val ui_window_tab_left by drawable().cached()
    val ui_window_tab_right by drawable().cached()
    val ui_window_sheet by drawable().cached()
    val ui_powerups_bg by drawable().cached()
    val ui_premium_badge by drawable().cached()
    val ui_premium_highlight by drawable().cached()
    val ui_bubble by drawable().cached()
    val logo by drawable().cached()
    val coin by drawable().cached()
    val intro_bg by drawable().cached()
    val intro_cloud by drawable().cached()
    val intro_princess by drawable().cached()
    val intro_well by drawable().cached()
    val intro_castle by drawable().cached()
    val ui_item_storm by drawable().cached()
    val ui_item_flask by drawable().cached()
    val ui_item_orb by drawable().cached()
    val ui_item_umbrella by drawable().cached()
    /******* End Drawables *******/

    /******* TextureRegions *******/
    val ui_item_cooldown by textureRegion().cached()
    val gift_open_front by textureRegion().cached()
    val gift_open by textureRegions().cached()
    /******* End TextureRegions *******/

    /******* Colors *******/
    val fontColor by color { hex = "#FFFFFF" }.cached()
    val fontBorderColor by color { hex = "#000000" }.cached()
    /******* End Colors *******/

    /******* Fonts *******/
    val defaultFont by put(defaultFontFreetype()).cached()
    val messageFont by put(messageFontFreetype()).cached()
    val menuFont by put(menuFontFreetype()).cached()
    val menuSmall by put(menuSmallFreetype()).cached()
    val menuXS by put(menuXSFreetype()).cached()
    val menuIcons by put(menuIconsFreetype()).cached()
    val menuIconsXS by put(menuIconsXSFreetype()).cached()
    val menuIconsSmall by put(menuIconsSmallFreetype()).cached()
    val menuIconsMedium by put(menuIconsMediumFreetype()).cached()
    val noTextFont by put(noTextFontFreetype()).cached()
    val hudProgressIconFont by put(hudProgressIconFontFreetype()).cached()
    val hudProgressFont by put(hudProgressFontFreetype()).cached()
    /******* End Fonts *******/

    /******* Labels *******/
    val defaultLabelStyle by labelStyle("default") {
        font = defaultFont()
    }.cached()
    val message by labelStyle {
        font = messageFont()
    }.cached()
    val bonusPointsLabel by labelStyle {
        font = hudProgressFont()
        fontColorHex ="#ffffff"
    }.cached()
    val gameMessage by labelStyle {
        font = menuFont()
        fontColorHex ="#ffffff"
    }.cached()
    val windowTitle by labelStyle {
        font = menuFont()
        background = ui_window_title()
        fontColorHex ="#ffffff"
    }.cached()
    val menu by labelStyle {
        font = menuSmall()
        fontColorHex ="#ffffff"
    }.cached()
    /******* End Labels *******/


    /******* TextButtons *******/
    val menuButton by textButtonStyle {
        font = menuFont()
        up = ui_button_normal()
        down = ui_button_pressed()
        checked = down
        unpressedOffsetY = 5f * scale
        pressedOffsetY = unpressedOffsetY - (5f * scale)
        checkedOffsetY = pressedOffsetY
        fontColorHex ="#ffffff"
        downFontColorHex ="#009900"
        checkedFontColor = downFontColor
    }.cached()
    val menuCircleToggle by textButtonStyle {
        font = menuIcons()
        up = ui_circle_normal()
        down = ui_circle_pressed()
        checked = down
        unpressedOffsetY = (5f * scale)
        pressedOffsetY = unpressedOffsetY - (5f * scale)
        checkedOffsetY = pressedOffsetY
        fontColorHex ="#ffffff"
        downFontColorHex ="#009900"
        checkedFontColor = downFontColor
    }.cached()
    val menuCircle by textButtonStyle {
        font = menuIcons()
        up = ui_circle_normal()
        down = ui_circle_pressed()
        checked = down
        unpressedOffsetY = (5f * scale)
        pressedOffsetY = unpressedOffsetY - (5f * scale)
        checkedOffsetY = pressedOffsetY
        fontColorHex ="#ffffff"
        downFontColorHex ="#009900"
        checkedFontColor = downFontColor
    }.cached()
    val menuCircleSmall by textButtonStyle {
        font = menuIconsSmall()
        up = ui_circle_normal()
        down = ui_circle_pressed()
        checked = down
        pressedOffsetY = -5f * scale
        checkedOffsetY = pressedOffsetY
        fontColorHex ="#ffffff"
        downFontColorHex ="#009900"
        checkedFontColor = downFontColor
    }.cached()
    val menuCircleXS by textButtonStyle {
        font = menuIconsXS()
        up = ui_circle_normal()
        down = ui_circle_pressed()
        checked = down
        pressedOffsetY = -5f * scale
        checkedOffsetY = pressedOffsetY
        fontColorHex ="#ffffff"
        downFontColorHex ="#009900"
        checkedFontColor = downFontColor
    }.cached()
    val menuCircleXSImage by imageTextButtonStyle {
        font = menuIconsXS()
        up = ui_circle_normal()
        down = ui_circle_pressed()
        checked = down
        pressedOffsetY = -5f * scale
        checkedOffsetY = pressedOffsetY
        fontColorHex ="#ffffff"
        downFontColorHex ="#009900"
        checkedFontColor = downFontColor
    }.cached()
    val menuCircleXSCoin by imageTextButtonStyle {
        font = menuIconsXS()
        up = coin()
        down = up
        checked = down
        fontColorHex ="#ffffff"
    }.cached()
    val menuCircleXSText by textButtonStyle {
        font = menuXS()
        up = ui_circle_normal()
        down = ui_circle_pressed()
        checked = down
        pressedOffsetY = -5f * scale
        checkedOffsetY = pressedOffsetY
        fontColorHex ="#ffffff"
        downFontColorHex ="#009900"
        checkedFontColor = downFontColor
    }.cached()
    val menuCircleMedium by textButtonStyle {
        font = menuIconsMedium()
        disabledFontColor = com.badlogic.gdx.graphics.Color(0.47f, 0.47f, 0.47f, 1f)
        up = ui_circle_normal()
        down = ui_circle_pressed()
        checked = down
        disabled = up
        pressedOffsetY = -5f * scale
        checkedOffsetY = pressedOffsetY
        fontColorHex ="#ffffff"
        downFontColorHex ="#009900"
        checkedFontColor = downFontColor
    }.cached()
    val menuCircleSmallText by textButtonStyle {
        font = menuSmall()
        up = ui_circle_normal()
        down = ui_circle_pressed()
        checked = down
        pressedOffsetY = -5f * scale
        checkedOffsetY = pressedOffsetY
        fontColorHex ="#ffffff"
        downFontColorHex ="#009900"
        checkedFontColor = downFontColor
    }.cached()
    val hudProgressText by textButtonStyle {
        font = hudProgressFont()
        fontColorHex ="#ffffff"
        pressedOffsetY = -5f * scale
        checkedOffsetY = pressedOffsetY
        downFontColorHex ="#009900"
        checkedFontColor = downFontColor
    }.cached()
    /******* End TextButtons *******/


    /******* ImageTextButtons *******/
    val menuCircleGoldSmall by imageTextButtonStyle {
        font = menuSmall()
        up = ui_circle_gold_normal()
        down = ui_circle_gold_pressed()
        checked = down
        pressedOffsetY = -5f * scale
        checkedOffsetY = pressedOffsetY
        fontColorHex ="#ffffff"
        downFontColorHex ="#009900"
        checkedFontColor = downFontColor
    }.cached()
    val menuCircleGoldSmallIcon by imageTextButtonStyle {
        font = menuIconsSmall()
        up = ui_circle_gold_normal()
        down = ui_circle_gold_pressed()
        checked = down
        pressedOffsetY = -5f * scale
        checkedOffsetY = pressedOffsetY
        fontColorHex ="#ffffff"
        downFontColorHex ="#009900"
        checkedFontColor = downFontColor
    }.cached()
    val menuCircleGreenSmall by imageTextButtonStyle {
        font = menuIconsSmall()
        up = ui_circle_green_normal()
        down = ui_circle_green_pressed()
        checked = down
        pressedOffsetY = -5f * scale
        checkedOffsetY = pressedOffsetY
        fontColorHex ="#ffffff"
        downFontColorHex ="#009900"
        checkedFontColor = downFontColor
    }.cached()
    val menuCircleGreenXS by imageTextButtonStyle {
        font = menuIconsXS()
        up = ui_circle_green_normal()
        down = ui_circle_green_pressed()
        pressedOffsetY = -5f * scale
        fontColorHex ="#ffffff"
        downFontColorHex ="#ffffff"
    }.cached()
    val menuCircleYellowXS by imageTextButtonStyle {
        font = menuIconsXS()
        up = ui_circle_gold_normal()
        down = ui_circle_gold_pressed()
        pressedOffsetY = -5f * scale
        fontColorHex ="#ffffff"
        downFontColorHex ="#ffffff"
    }.cached()
    val menuInputSmall by imageTextButtonStyle {
        font = menuSmall()
        up = ui_input_normal()
        down = ui_input_pressed()
        checked = down
        pressedOffsetY = -5f * scale
        checkedOffsetY = pressedOffsetY
        fontColorHex ="#000000"
        downFontColorHex ="#000000"
        checkedFontColor = downFontColor
    }.cached()
    val menuCircleGreenSmallText by imageTextButtonStyle {
        font = menuSmall()
        up = ui_circle_green_normal()
        down = ui_circle_green_pressed()
        checked = down
        pressedOffsetY = -5f * scale
        checkedOffsetY = pressedOffsetY
        fontColorHex ="#ffffff"
        downFontColorHex ="#009900"
        checkedFontColor = downFontColor
    }.cached()
    val hudProgressIcon by imageTextButtonStyle {
        font = hudProgressIconFont()
        up = ui_circle_normal()
        down = ui_circle_pressed()
        checked = down
        fontColorHex ="#ffffff"
        pressedOffsetY = -5f * scale
        checkedOffsetY = pressedOffsetY
        downFontColorHex ="#009900"
        checkedFontColor = downFontColor
    }.cached()
    val windowTabTopLeft by imageTextButtonStyle {
        font = menuSmall()
        down = ui_window_tab_left()
        up = down
    }.cached()
    val windowTabTopRight by imageTextButtonStyle {
        font = menuSmall()
        down = ui_window_tab_right()
        up = down
    }.cached()
    /******* End ImageTextButtons *******/

    /******* LevelProgressBars *******/
    val princess by levelProgressBarStyle {
        background = lvlbar_bg()
        foreground = lvlbar_fg_princess()
        boss = lvlbar_bg_prince()
        handle = lvlbar_handle_princess()
        handleSize = 1.2f
    }.cached()
    val prince by levelProgressBarStyle {
        background = lvlbar_bg()
        foreground = lvlbar_fg_prince()
        boss = lvlbar_bg_prince()
        handle = lvlbar_handle_prince()
        handleSize = 1.2f
    }.cached()
    /******* End LevelProgressBars *******/

    /******* Windows *******/
    val defaultWindowStyle by windowStyle("default") {
        titleFont = noTextFont()
    }.cached()
    val windowContent by windowStyle {
        background = ui_window()
        titleFont = noTextFont()
    }.cached()
    val windowContentSmall by windowStyle {
        background = ui_window_small()
        titleFont = noTextFont()
    }.cached()
    val windowSheet by windowStyle {
        background = ui_window_sheet()
        titleFont = noTextFont()
    }.cached()

    /******* End Windows *******/

    override fun getRegions(regionName: String?): Array<TextureRegion> {
        return super.getRegions(regionName)
    }
}

private val scaledSkin = ScaledSkin("ui/{}x/skin.atlas", ::NopeFrogKingSkin)

val DefaultSkin: NopeFrogKingSkin by lazy { scaledSkin[scale] }

fun LoadSkinAssets() {
    scaledSkin.load(scale)
}
