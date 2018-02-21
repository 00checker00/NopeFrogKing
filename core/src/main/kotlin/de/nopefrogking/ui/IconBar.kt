package de.nopefrogking.ui

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.Pools
import de.nopefrogking.screens.ClickListener
import de.nopefrogking.utils.*
import ktx.actors.onChange
import ktx.actors.onClick
import ktx.scene2d.*

class IconBarOptions {
    var barHeight: Float = 40.0f
    var iconStyle: NamedAsset<ImageTextButton.ImageTextButtonStyle> = DefaultSkin.hudProgressIcon
    var textStyle: NamedAsset<TextButton.TextButtonStyle> = DefaultSkin.hudProgressText
    var barStyle = "ui_score_bar"
    var barEndStyle = "ui_score_end"
}

open class IconBar private constructor(
        skin: Skin,
        icon: FontIcon,
        iconDrawable: String?,
        val options: IconBarOptions = IconBarOptions()) : Table(skin) {

    private val clickListener: com.badlogic.gdx.scenes.scene2d.utils.ClickListener
    lateinit var contentButton: TextButton; private set
    lateinit var contentButtonCell: Cell<*>; private set
    lateinit var iconButton: ImageTextButton; private set
    lateinit var iconButtonCell: Cell<*>; private set
    private lateinit var bar: Image
    private lateinit var barEnd: Image
    private val isPressed get() = clickListener.isVisualPressed
    private val iconDrawable: String? = null

    constructor(skin: Skin, iconDrawable: String, options: IconBarOptions = IconBarOptions())
            : this(skin, FontIcon.Nothing, iconDrawable, options)

    constructor(skin: Skin, icon: FontIcon, options: IconBarOptions = IconBarOptions())
            : this(skin, icon, null, options)

    var programmaticChangeEvents = true

    var text: String = ""
        set(value) {
            field = value
            contentButton.setText(value)
        }

    var icon: FontIcon = icon
        set(value) {
            field = value
            iconText = value()
        }

    var iconText: String
        get() = iconButton.text.toString()
        set(value) {
            if (icon() != value)
                icon = FontIcon.Nothing
            iconButton.text = value
        }

    var _isChecked: Boolean = false
    var isChecked: Boolean
        get() = _isChecked
        set(value) { setChecked(value, programmaticChangeEvents) }

    init {
        val table = table(DefaultSkin) {
            stack { cell ->
                table(DefaultSkin) {
                    setFillParent(true)

                    addSpacer()
                            .width(options.barHeight * scale / 2)
                            .height(options.barHeight * scale)

                    bar = image(options.barStyle, DefaultSkin) { cell ->
                        cell.expandX()
                        cell.height(options.barHeight * scale)
                        cell.padRight(options.barHeight/2 * scale)
                        cell.fill()
                    }
                }
                table(DefaultSkin) {
                    setFillParent(true)

                    addSpacer()
                            .expandX()
                            .minWidth(options.barHeight/2 * scale)
                            .height(options.barHeight * scale)

                    barEnd = image(options.barEndStyle, DefaultSkin) { cell ->
                        val ratio = width / height
                        cell.height(options.barHeight * scale)
                        cell.width(options.barHeight * scale * ratio)
                        cell.right()
                    }
                }
                table(DefaultSkin) {
                    contentButton = textButton("0", options.textStyle.name, DefaultSkin) { cell ->
                        style.font.setFixedWidthGlyphs("1234567890")
                        cell.height(options.barHeight * scale)

                        cell.padLeft(options.barHeight * scale + 5 * scale)
                        cell.padRight(10 * scale)

                        contentButtonCell = cell
                    }
                }
                table(DefaultSkin) {
                    setFillParent(true)
                    val ratio = options.iconStyle().up.minWidth / options.iconStyle().up.minHeight
                    iconButton = iconDrawable?.let {
                        imageTextButton("", options.iconStyle.name, DefaultSkin) { cell ->
                            iconButtonCell = cell

                            cell.height(options.barHeight * scale)
                            cell.width(options.barHeight * scale * ratio)
                        }.apply {
                            setDrawable(skin, it)
                            imageCell.expand().fill()
                        }
                    } ?: imageTextButton(icon(), options.iconStyle.name, DefaultSkin) { cell ->
                            iconButtonCell = cell

                            cell.height(options.barHeight * scale)
                            cell.width(options.barHeight * scale * ratio)
                        }

                    addSpacer().expandX()
                }
                cell.fill().expand()
            }
        }
        add(table).expand().fill()

        clickListener = object: com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            var pressed = false

            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                setChecked(false, true)
                pressed = false
            }

            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                setChecked(true, true)
                pressed = true
                return true
            }

            override fun exit(event: InputEvent?, x: Float, y: Float, pointer: Int, toActor: Actor?) {
                setChecked(false, true)
            }

            override fun enter(event: InputEvent?, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
                setChecked(pressed, true)
            }
        }

        addListener(clickListener)
    }

    override fun act(delta: Float) {
        super.act(delta)
    }

    private fun setChecked(isChecked: Boolean, fireEvent: Boolean) {
        if (this._isChecked == isChecked) return
        this._isChecked = isChecked
        val postfix = if (isChecked) "_pressed" else ""
        bar.setDrawable(skin, "${options.barStyle}$postfix")
        barEnd.setDrawable(skin, "${options.barEndStyle}$postfix")
        iconButton.isChecked = isChecked
        contentButton.isChecked = isChecked
        if (fireEvent)
        {
            val changeEvent = Pools.obtain(ChangeListener.ChangeEvent::class.java)
            if (fire(changeEvent)) this.isChecked = !isChecked
            Pools.free(changeEvent)
        }
    }


    fun connectToButtons(vararg buttons: Button, listener: ClickListener? = null) {
        val bar = this

        buttons.forEach { button ->
            button.setProgrammaticChangeEvents(false)

            button.addListener(object: InputListener() {
                val buttons = buttons

                override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, mouseBtn: Int) {
                    buttons.filterNot { it == button }.forEach {
                        it.isChecked = false
                    }
                    bar.isChecked = false
                }

                override fun enter(event: InputEvent, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
                    buttons.filterNot { it == button }.forEach {
                        it.isChecked = button.isPressed
                    }
                    bar.isChecked = button.isPressed
                }

                override fun exit(event: InputEvent, x: Float, y: Float, pointer: Int, toActor: Actor?) {
                    buttons.filterNot { it == button }.forEach {
                        it.isChecked = false
                    }
                    bar.isChecked = false
                }

                override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, mouseBtn: Int): Boolean {
                    buttons.filterNot { it == button }.forEach {
                        it.isChecked = true
                    }
                    bar.isChecked = true
                    return true
                }
            })

            button.onChange { _, _ ->
                buttons.forEach {
                    it.isChecked = false
                }
            }

            button.onClick { _, _ -> listener?.invoke() }
        }

        bar.programmaticChangeEvents = false

        bar.addListener(object: InputListener() {
            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, mouseBtn: Int) {
                buttons.forEach {
                    it.isChecked = false
                }
            }

            override fun enter(event: InputEvent, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
                buttons.forEach {
                    it.isChecked = bar.isChecked
                }
            }

            override fun exit(event: InputEvent, x: Float, y: Float, pointer: Int, toActor: Actor?) {
                buttons.forEach {
                    it.isChecked = false
                }
            }

            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, mouseBtn: Int): Boolean {
                buttons.forEach {
                    it.isChecked = true
                }
                return true
            }
        })

        bar.onChange { _, _ ->
            buttons.forEach {
                it.isChecked = false
            }
        }

        bar.onClick { _, _ -> listener?.invoke() }
    }

}



