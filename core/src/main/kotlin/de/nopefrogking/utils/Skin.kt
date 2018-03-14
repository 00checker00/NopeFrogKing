package de.nopefrogking.utils

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.utils.ObjectMap
import de.nopefrogking.Assets
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import kotlin.reflect.KProperty


class FreetypeFontBuilder: FreeTypeFontGenerator.FreeTypeFontParameter() {
    var file: FileHandle? = null
}



class ScaledSkin<T: Skin>(val atlasTemplate: String, val creator: (TextureAtlas, Float)->T) {
    val skins = ObjectMap<String, T>()

    operator fun get(scale: Float): T {
        val atlasName = atlasTemplate.replace("{}", decimalFormat.format(scale))
        return skins.get(atlasName) orElse {
            if (!Gdx.files.internal(atlasName).exists()) {
                throw RuntimeException("Atlas $atlasName for skin at scale ${decimalFormat.format(scale)} not found")
            }
            val atlas = Assets.manager.get(atlasName, TextureAtlas::class.java)
            val start = System.currentTimeMillis()
            val skin = creator(atlas, scale).apply { UIScale = scale }
            val duration = System.currentTimeMillis() - start
            debug { "Creating skin took ${duration}ms" }
            skins.put(atlasName, skin)

            skin
        }
    }

    fun load(scale: Float) {
        val atlasName = atlasTemplate.replace("{}", decimalFormat.format(scale))
        if (!Assets.manager.isLoaded(atlasName)) {
            if (!Gdx.files.internal(atlasName).exists()) {
                throw RuntimeException("Atlas $atlasName for skin at scale ${decimalFormat.format(scale)} not found")
            }
            Assets.manager.load(atlasName, TextureAtlas::class.java)
        }
    }

    companion object {
        private val decimalFormat = DecimalFormat("0", DecimalFormatSymbols(Locale.ENGLISH)).apply {
            maximumFractionDigits = 2
        }
    }
}


interface NamedAsset<out T> {
    val name: String
    val asset: T

    operator fun invoke() = asset
}

data class NamedAssetImpl<out T> internal constructor(override val name: String, override val asset: T): NamedAsset<T>

class CachedAssetDelegateProvider<T>(val creator: (String)->Unit, val getter: (String)->T) {
    operator fun provideDelegate(thisRef: Any?, prop: KProperty<*>): CachedAssetDelegate<T> {
        creator(prop.name)
        return CachedAssetDelegate(prop.name, getter)
    }
}
class AssetDelegateProvider<T>(val creator: (String)->Unit, val getter: (String)->T) {
    operator fun provideDelegate(thisRef: Any?, prop: KProperty<*>): NamedAssetDelegate<T> {
        creator(prop.name)
        return NamedAssetDelegate(prop.name, getter)
    }

    fun cached() = CachedAssetDelegateProvider(creator, getter)
}
class NamedAssetDelegateProvider<T>(val name: String, val creator: (String)->Unit, val getter: (String)->T) {
    operator fun provideDelegate(thisRef: Any?, prop: KProperty<*>): NamedAssetDelegate<T> {
        creator(name)
        return NamedAssetDelegate(name, getter)
    }

    fun cached() = CachedNamedAssetDelegateProvider(name, creator, getter)
}
class CachedNamedAssetDelegateProvider<T>(val name: String, val creator: (String)->Unit, val getter: (String)->T) {
    operator fun provideDelegate(thisRef: Any?, prop: KProperty<*>): CachedAssetDelegate<T> {
        creator(name)
        return CachedAssetDelegate(name, getter)
    }
}
class NamedAssetDelegate<T>(val name: String, val getter: (String)->T) {
    operator fun getValue(thisRef: Any?, prop: KProperty<*>): NamedAsset<T> {
        return NamedAssetImpl(name, getter(name))
    }
}
class CachedAssetDelegate<T>(val name: String, val getter: (String)->T) {
    val value: NamedAsset<T> by lazy { NamedAssetImpl(name, getter(name)) }

    operator fun getValue(thisRef: Any?, prop: KProperty<*>): NamedAsset<T> {
        return value
    }
}

class ColorBuilder {
    private var color = Color.WHITE

    var r: Float get() = color.a; set(value) { color.set(  value, color.g, color.b, color.a) }
    var g: Float get() = color.r; set(value) { color.set(color.r,   value, color.b, color.a) }
    var b: Float get() = color.g; set(value) { color.set(color.r, color.g,   value, color.a) }
    var a: Float get() = color.b; set(value) { color.set(color.r, color.g, color.b,   value) }
    var hex: String get() = color.toString(); set(value) { color = Color.valueOf(value) }

    internal fun build(): Color = color
}

fun Skin.Color(name: String = "default", init: ColorBuilder.()->Unit): Color {
    val color = ColorBuilder().apply { init() }.build()
    add(name, color)
    return color
}
fun Skin.color(init: ColorBuilder.()->Unit) = AssetDelegateProvider({Color(it, init)}, {get(it, Color::class.java)})
fun Skin.color(name: String, init: ColorBuilder.()->Unit) = NamedAssetDelegateProvider(name, {Color(it, init)}, {get(it, Color::class.java)})

fun Skin.ButtonStyle(name: String = "default", init: Button.ButtonStyle.()->Unit): Button.ButtonStyle {
    val style = Button.ButtonStyle().apply { init() }
    add(name, style)
    return style
}
fun Skin.buttonStyle(init: Button.ButtonStyle.()->Unit) = AssetDelegateProvider({ButtonStyle(it, init) }, {get(it, Button.ButtonStyle::class.java)})
fun Skin.buttonStyle(name: String, init: Button.ButtonStyle.()->Unit) = NamedAssetDelegateProvider(name, {ButtonStyle(it, init) }, {get(it, Button.ButtonStyle::class.java)})

fun Skin.CheckBoxStyle(name: String = "default", init: CheckBox.CheckBoxStyle.()->Unit): CheckBox.CheckBoxStyle {
    val style = CheckBox.CheckBoxStyle().apply { init() }
    add(name, style)
    return style
}
fun Skin.checkBoxStyle(init: CheckBox.CheckBoxStyle.()->Unit) = AssetDelegateProvider({CheckBoxStyle(it, init)}, {get(it, CheckBox.CheckBoxStyle::class.java)})
fun Skin.checkBoxStyle(name: String, init: CheckBox.CheckBoxStyle.()->Unit) = NamedAssetDelegateProvider(name, {CheckBoxStyle(it, init)}, {get(it, CheckBox.CheckBoxStyle::class.java)})

fun Skin.ImageButtonStyle(name: String = "default", init: ImageButton.ImageButtonStyle.()->Unit): ImageButton.ImageButtonStyle {
    val style = ImageButton.ImageButtonStyle().apply { init() }
    add(name, style)
    return style
}
fun Skin.imageButtonStyle(init: ImageButton.ImageButtonStyle.()->Unit) = AssetDelegateProvider({ImageButtonStyle(it, init)}, {get(it, ImageButton.ImageButtonStyle::class.java)})
fun Skin.imageButtonStyle(name: String, init: ImageButton.ImageButtonStyle.()->Unit) = NamedAssetDelegateProvider(name, {ImageButtonStyle(it, init)}, {get(it, ImageButton.ImageButtonStyle::class.java)})

fun Skin.ImageTextButtonStyle(name: String = "default", init: ImageTextButton.ImageTextButtonStyle.()->Unit): ImageTextButton.ImageTextButtonStyle {
    val style = ImageTextButton.ImageTextButtonStyle().apply { init() }
    add(name, style)
    return style
}
fun Skin.imageTextButtonStyle(init: ImageTextButton.ImageTextButtonStyle.()->Unit) = AssetDelegateProvider({ImageTextButtonStyle(it, init)}, {get(it, ImageTextButton.ImageTextButtonStyle::class.java)})
fun Skin.imageTextButtonStyle(name: String, init: ImageTextButton.ImageTextButtonStyle.()->Unit) = NamedAssetDelegateProvider(name, {ImageTextButtonStyle(it, init)}, {get(it, ImageTextButton.ImageTextButtonStyle::class.java)})

fun Skin.LabelStyle(name: String = "default", init: Label.LabelStyle.()->Unit): Label.LabelStyle {
    val style = Label.LabelStyle().apply { init() }
    add(name, style)
    return style
}
fun Skin.labelStyle(init: Label.LabelStyle.()->Unit) = AssetDelegateProvider({LabelStyle(it, init)}, {get(it, Label.LabelStyle::class.java)})
fun Skin.labelStyle(name: String, init: Label.LabelStyle.()->Unit) = NamedAssetDelegateProvider(name, {LabelStyle(it, init)}, {get(it, Label.LabelStyle::class.java)})

fun Skin.ProgressBarStyle(name: String = "default", init: ProgressBar.ProgressBarStyle.()->Unit): ProgressBar.ProgressBarStyle {
    val style = ProgressBar.ProgressBarStyle().apply { init() }
    add(name, style)
    return style
}
fun Skin.progressBarStyle(init: ProgressBar.ProgressBarStyle.()->Unit) = AssetDelegateProvider({ProgressBarStyle(it, init)}, {get(it, ProgressBar.ProgressBarStyle::class.java)})
fun Skin.progressBarStyle(name: String, init: ProgressBar.ProgressBarStyle.()->Unit) = NamedAssetDelegateProvider(name, {ProgressBarStyle(it, init)}, {get(it, ProgressBar.ProgressBarStyle::class.java)})

fun Skin.ScrollPaneStyle(name: String = "default", init: ScrollPane.ScrollPaneStyle.()->Unit): ScrollPane.ScrollPaneStyle {
    val style = ScrollPane.ScrollPaneStyle().apply { init() }
    add(name, style)
    return style
}
fun Skin.scrollPaneStyle(init: ScrollPane.ScrollPaneStyle.()->Unit) = AssetDelegateProvider({ScrollPaneStyle(it, init)}, {get(it, ScrollPane.ScrollPaneStyle::class.java)})
fun Skin.scrollPaneStyle(name: String, init: ScrollPane.ScrollPaneStyle.()->Unit) = NamedAssetDelegateProvider(name, {ScrollPaneStyle(it, init)}, {get(it, ScrollPane.ScrollPaneStyle::class.java)})

fun Skin.SelectBoxStyle(name: String = "default", init: SelectBox.SelectBoxStyle.()->Unit): SelectBox.SelectBoxStyle {
    val style = SelectBox.SelectBoxStyle().apply { init() }
    add(name, style)
    return style
}
fun Skin.selectBoxStyle(init: SelectBox.SelectBoxStyle.()->Unit) = AssetDelegateProvider({SelectBoxStyle(it, init)}, {get(it, SelectBox.SelectBoxStyle::class.java)})
fun Skin.selectBoxStyle(name: String, init: SelectBox.SelectBoxStyle.()->Unit) = NamedAssetDelegateProvider(name, {SelectBoxStyle(it, init)}, {get(it, SelectBox.SelectBoxStyle::class.java)})

fun Skin.SliderStyle(name: String = "default", init: Slider.SliderStyle.()->Unit): Slider.SliderStyle {
    val style = Slider.SliderStyle().apply { init() }
    add(name, style)
    return style
}
fun Skin.sliderStyle(init: Slider.SliderStyle.()->Unit) = AssetDelegateProvider({SliderStyle(it, init)}, {get(it, Slider.SliderStyle::class.java)})
fun Skin.sliderStyle(name: String, init: Slider.SliderStyle.()->Unit) = NamedAssetDelegateProvider(name, {SliderStyle(it, init)}, {get(it, Slider.SliderStyle::class.java)})

fun Skin.SplitPaneStyle(name: String = "default", init: SplitPane.SplitPaneStyle.()->Unit): SplitPane.SplitPaneStyle {
    val style = SplitPane.SplitPaneStyle().apply { init() }
    add(name, style)
    return style
}
fun Skin.splitPaneStyle(init: SplitPane.SplitPaneStyle.()->Unit) = AssetDelegateProvider({SplitPaneStyle(it, init)}, {get(it, SplitPane.SplitPaneStyle::class.java)})
fun Skin.splitPaneStyle(name: String, init: SplitPane.SplitPaneStyle.()->Unit) = NamedAssetDelegateProvider(name, {SplitPaneStyle(it, init)}, {get(it, SplitPane.SplitPaneStyle::class.java)})

fun Skin.TextButtonStyle(name: String = "default", init: TextButton.TextButtonStyle.()->Unit): TextButton.TextButtonStyle {
    val style = TextButton.TextButtonStyle().apply { init() }
    add(name, style)
    return style
}
fun Skin.textButtonStyle(init: TextButton.TextButtonStyle.()->Unit) = AssetDelegateProvider({TextButtonStyle(it, init)}, {get(it, TextButton.TextButtonStyle::class.java)})
fun Skin.textButtonStyle(name: String, init: TextButton.TextButtonStyle.()->Unit) = NamedAssetDelegateProvider(name, {TextButtonStyle(it, init)}, {get(it, TextButton.TextButtonStyle::class.java)})

fun Skin.TextFieldStyle(name: String = "default", init: TextField.TextFieldStyle.()->Unit): TextField.TextFieldStyle {
    val style = TextField.TextFieldStyle().apply { init() }
    add(name, style)
    return style
}
fun Skin.textFieldStyle(init: TextField.TextFieldStyle.()->Unit) = AssetDelegateProvider({TextFieldStyle(it, init)}, {get(it, TextField.TextFieldStyle::class.java)})
fun Skin.textFieldStyle(name: String, init: TextField.TextFieldStyle.()->Unit) = NamedAssetDelegateProvider(name, {TextFieldStyle(it, init)}, {get(it, TextField.TextFieldStyle::class.java)})

fun Skin.TextTooltipStyle(name: String = "default", init: TextTooltip.TextTooltipStyle.()->Unit): TextTooltip.TextTooltipStyle {
    val style = TextTooltip.TextTooltipStyle().apply { init() }
    add(name, style)
    return style
}
fun Skin.textTooltipStyle(init: TextTooltip.TextTooltipStyle.()->Unit) = AssetDelegateProvider({TextTooltipStyle(it, init)}, {get(it, TextTooltip.TextTooltipStyle::class.java)})
fun Skin.textTooltipStyle(name: String, init: TextTooltip.TextTooltipStyle.()->Unit) = NamedAssetDelegateProvider(name, {TextTooltipStyle(it, init)}, {get(it, TextTooltip.TextTooltipStyle::class.java)})

fun Skin.TouchpadStyle(name: String = "default", init: Touchpad.TouchpadStyle.()->Unit): Touchpad.TouchpadStyle {
    val style = Touchpad.TouchpadStyle().apply { init() }
    add(name, style)
    return style
}
fun Skin.touchpadStyle(init: Touchpad.TouchpadStyle.()->Unit) = AssetDelegateProvider({TouchpadStyle(it, init)}, {get(it, Touchpad.TouchpadStyle::class.java)})
fun Skin.touchpadStyle(name: String, init: Touchpad.TouchpadStyle.()->Unit) = NamedAssetDelegateProvider(name, {TouchpadStyle(it, init)}, {get(it, Touchpad.TouchpadStyle::class.java)})

fun Skin.TreeStyle(name: String = "default", init: Tree.TreeStyle.()->Unit): Tree.TreeStyle {
    val style = Tree.TreeStyle().apply { init() }
    add(name, style)
    return style
}
fun Skin.treeStyle(init: Tree.TreeStyle.()->Unit) = AssetDelegateProvider({TreeStyle(it, init)}, {get(it, Tree.TreeStyle::class.java)})
fun Skin.treeStyle(name: String, init: Tree.TreeStyle.()->Unit) = NamedAssetDelegateProvider(name, {TreeStyle(it, init)}, {get(it, Tree.TreeStyle::class.java)})

fun Skin.WindowStyle(name: String = "default", init: Window.WindowStyle.()->Unit): Window.WindowStyle {
    val style = Window.WindowStyle().apply { init() }
    add(name, style)
    return style
}
fun Skin.windowStyle(init: Window.WindowStyle.()->Unit) = AssetDelegateProvider({WindowStyle(it, init)}, {get(it, Window.WindowStyle::class.java)})
fun Skin.windowStyle(name: String, init: Window.WindowStyle.()->Unit) = NamedAssetDelegateProvider(name, {WindowStyle(it, init)}, {get(it, Window.WindowStyle::class.java)})

inline fun <reified T> Skin.Resource(name: String = "default", vararg constructorArgs: Any?, init: T.()->Unit) {
    val constructor = T::class.constructors.firstOrNull { it.parameters.count() == 0 }
            ?: throw IllegalStateException("Resource type must have a default constructor!")

    val instance = constructor.call(constructorArgs)
    Resource(instance, name, init)
}
inline fun <T> Skin.Resource(obj: T, name: String = "default", init: T.()->Unit) {
    add(name, obj.apply { init() })
}

fun Skin.drawable() = AssetDelegateProvider({}, {getDrawable(it)})
fun Skin.drawable(name: String) = NamedAssetDelegateProvider(name, {}, {getDrawable(it)})

fun Skin.textureRegion() = AssetDelegateProvider({}, {getRegion(it)})
fun Skin.textureRegion(name: String) = NamedAssetDelegateProvider(name, {}, {getRegion(it)})

fun Skin.textureRegions() = AssetDelegateProvider({}, { atlas?.findRegions(it) })
fun Skin.textureRegions(name: String) = NamedAssetDelegateProvider(name, {}, { atlas?.findRegions(it) })

fun Skin.animation(frameDuration: Float = 0.13f, playMode: Animation.PlayMode = Animation.PlayMode.NORMAL)
        = AssetDelegateProvider({}, { Animation<TextureRegion>(frameDuration, atlas?.findRegions(it), playMode) })

fun Skin.animation(name: String, frameDuration: Float = 0.13f, playMode: Animation.PlayMode = Animation.PlayMode.NORMAL)
        = NamedAssetDelegateProvider(name, {}, { Animation<TextureRegion>(frameDuration, atlas?.findRegions(it), playMode) })

fun Skin.getPatchDrawable(region: String) = NinePatchDrawable(getPatch(region))


fun <T> Skin.put(obj: T, clazz: Class<T>) = AssetDelegateProvider({add(it, obj)}, {get(it, clazz)})
inline fun <reified T> Skin.put(obj: T) = put(obj, T::class.java)

var TextButton.TextButtonStyle.fontColorHex: String
    get() = "#$fontColor"
    set(value) { fontColor = Color.valueOf(value)}
var TextButton.TextButtonStyle.downFontColorHex: String
    get() = "#$downFontColor"
    set(value) { downFontColor = Color.valueOf(value)}
var TextButton.TextButtonStyle.overFontColorHex: String
    get() = "#$overFontColor"
    set(value) { overFontColor = Color.valueOf(value)}
var TextButton.TextButtonStyle.checkedFontColorHex: String
    get() = "#$checkedFontColor"
    set(value) { checkedFontColor = Color.valueOf(value)}
var TextButton.TextButtonStyle.checkedOverFontColorHex: String
    get() = "#$checkedOverFontColor"
    set(value) { checkedOverFontColor = Color.valueOf(value)}
var TextButton.TextButtonStyle.disabledFontColorHex: String
    get() = "#$disabledFontColor"
    set(value) { disabledFontColor = Color.valueOf(value)}

var TextField.TextFieldStyle.fontColorHex: String
    get() = "#$fontColor"
    set(value) { fontColor = Color.valueOf(value) }
var TextField.TextFieldStyle.focusedFontColorHex: String
    get() = "#$focusedFontColor"
    set(value) { focusedFontColor = Color.valueOf(value) }
var TextField.TextFieldStyle.disabledFontColorHex: String
    get() = "#$disabledFontColor"
    set(value) { disabledFontColor = Color.valueOf(value) }
var TextField.TextFieldStyle.messageFontColorHex: String
    get() = "#$messageFontColor"
    set(value) { messageFontColor = Color.valueOf(value) }

var Label.LabelStyle.fontColorHex: String
    get() = "#$fontColor"
    set(value) { fontColor = Color.valueOf(value) }

var Window.WindowStyle.titleFontColorHex: String
    get() = "#$titleFontColor"
    set(value) { titleFontColor = Color.valueOf(value) }

var Skin.UIScale: Float
    get() = get("UIScale", Float::class.java)
    internal set(value) { add("UIScale", value, Float::class.java) }