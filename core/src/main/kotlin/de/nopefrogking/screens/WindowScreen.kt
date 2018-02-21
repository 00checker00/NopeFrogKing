package de.nopefrogking.screens

import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.ExtendViewport
import de.nopefrogking.Main
import de.nopefrogking.Sounds
import de.nopefrogking.utils.*
import de.project.ice.screens.BaseScreenAdapter
import ktx.actors.alpha
import ktx.actors.onChange
import ktx.actors.onClick

enum class SheetStyle { NoSheet, Gold, Normal }

abstract class WindowScreen(game: Main,
                            private val title: String,
                            private val icon: FontIcon = FontIcon.Nothing,
                            private val hasTabs: Boolean = false,
                            private val sheet: SheetStyle = SheetStyle.NoSheet,
                            private val windowContentStyle: Window.WindowStyle = DefaultSkin.windowContent()) : BaseScreenAdapter(game) {
    protected val stage = Stage()
    protected val scale = DefaultSkin.UIScale

    var visibleTab: Int = -1
    set(value) {
        val new = Math.min(1, Math.max(0, value))

        if (new != field) {
            tabContent.clearChildren()
            createContentView(tabContent, new)
            field = new

            if (new == 0) {
                tab1.apply {
                    image.isVisible = true
                    alpha = 1f
                }
                tab2.apply {
                    image.isVisible = false
                    alpha = 0.6f
                }
            } else {
                tab1.apply {
                    image.isVisible = false
                    alpha = 0.6f
                }
                tab2.apply {
                    image.isVisible = true
                    alpha = 1f
                }
            }

            onTabSwitched(new)
        }
    }

    private val root = Table(DefaultSkin)
    private var initialized: Boolean = false

    private var tab1 = createTab(0).let { tab -> setupTab(DefaultSkin.windowTabTopLeft(), tab) }
    private var tab2 = createTab(1).let { tab -> setupTab(DefaultSkin.windowTabTopRight(), tab) }


    private fun setupTab(style: ImageTextButton.ImageTextButtonStyle, details: TabDetails): ImageTextButton {
        return ImageTextButton(details.text, style).apply {
            if (details.icon != null)
                setDrawable(DefaultSkin, details.icon.name)

            labelCell
                    .expandX()
                    .padLeft(-labelCell.prefHeight)

            imageCell
                    .padTop(5*scale)
                    .padLeft(20*scale)
                    .width(labelCell.prefHeight)
                    .height(labelCell.prefHeight)
        }
    }

    private lateinit var tabContent: Table

    protected val content: Table get() = tabContent

    private val closeBtnType = ButtonType.CircleSmall
    private val closeBtn = TextButton(FontIcon.Exit(), closeBtnType.style())
    var isClosable: Boolean = true
        set(value) {
            field = value
            closeBtn.isVisible = value
        }

    private val buttonTable = Table(DefaultSkin)

    private var onClose: (()->Unit)? = null

    override val inputProcessor: InputProcessor = DelegatingBlockingInputProcessor(stage)

    init {
//        if (Config.RENDER_DEBUG) {
//            stage.setDebugAll(true)
//        }
        stage.viewport = ExtendViewport(450f * scale, 800f * scale, OrthographicCamera())

        stage.addActor(root)

        root.setFillParent(true)
    }

    open fun onTabSwitched(tab: Int) {

    }

    override fun show() {
        if (!initialized) {
            initialized = true

            root.addSpacer().height(20f * scale).row()

            root.addSpacer().expandY()
            root.row()

            root.add(Window("", DefaultSkin).apply {
                row()
                add(Table(skin).apply {
                    createTitleView(this)
                }).expandX().fillX()
                row()
                if (hasTabs) {
                    createTabs(this)
                    row()
                }
                add(Window("", windowContentStyle).apply {
                    skin = DefaultSkin
                    createCloseButton(this)
                    row()
                    tabContent = Table(DefaultSkin)
                    add(tabContent)
                            .expand()
                            .fill()
                            .padLeft(35 * scale)
                            .padRight(35 * scale)
                            .padTop(15 * scale)
                            .padBottom(15 * scale)
                    row()
                    addSpacer().expand()
                }).expand()
                        .fillX()
                        .width(WINDOW_WIDTH*scale)
                        .height(windowContentStyle.background.minHeight)
                row()
                if (sheet != SheetStyle.NoSheet) {
                    createSheet(this)
                    row()
                }
                add(buttonTable)
                row()
                addSpacer().expandY().row()
                clip = false
            })

            root.row()
            root.addSpacer().expandY()

            root.row()
            root.addSpacer().height(20f * scale)

            tab1.toFront()
            tab2.toBack()
        }

        closeBtn.onClick { _, _ -> onClose?.invoke(); Sounds.click().play() }

        visibleTab = 0
    }

    fun createTabs(table: Table) {
        table.add(tab1)
                .padRight(110*scale)
                .padBottom(-TAB_HEIGHT * scale)
                .width(TAB_WIDTH*scale)
                .height(TAB_HEIGHT*scale)
        table.row()

        table.add(tab2)
                .padLeft(110*scale)
                .width(TAB_WIDTH*scale)
                .height(TAB_HEIGHT*scale)
                .padBottom(-9*scale)

        tab1.onClick { _, _ -> visibleTab = 0 }
        tab2.onClick { _, _ -> visibleTab = 1 }
    }

    fun createSheet(table: Table) {
        val window = Window("", when (sheet) {
            SheetStyle.Gold -> DefaultSkin.windowSheetGold()
            else -> DefaultSkin.windowSheetNormal()
        })
        table.add(window)
                .expandX()
                .width(SHEET_WIDTH*scale)
                .height(SHEET_HEIGHT*scale)

        createSheetView(window)
    }

    fun createCloseButton(table: Table) {
        table.addSpacer().expandX()
        table.add(closeBtn)
                .pad(-closeBtnType.width/2 * scale - CLOSE_BTN_OFFSET * scale,
                        -closeBtnType.width/2 * scale - CLOSE_BTN_OFFSET * scale,
                        -closeBtnType.width/2 * scale + CLOSE_BTN_OFFSET * scale,
                        -closeBtnType.width/2 * scale + CLOSE_BTN_OFFSET * scale)
                .width(closeBtnType.width * scale)
                .height(closeBtnType.height * scale)
        table.clip = false
    }

    open fun createTitleView(table: Table) {
        val icon = TextButton(icon(), ButtonType.CircleSmall.style()).apply {
            touchable = Touchable.disabled
        }
        val title = Label(title, DefaultSkin.windowTitle()).apply {
            setAlignment(Align.center)
        }
        val backgroundDrawable = title.style.background
        table.add(icon)
                .width(TITLE_HEIGHT*scale)
                .height(TITLE_HEIGHT*scale)
                .padRight(-TITLE_HEIGHT*scale)
        table.add(title)
                .width(TITLE_WIDTH*scale)
                .height(TITLE_HEIGHT*scale)
        createTitleContent(table)
        table.row()
        table.addSpacer().expandX().padBottom(15*scale)
        icon.toFront()
    }

    open fun createTitleContent(table: Table) {

    }

    data class TabDetails(val text: String, val icon: NamedAsset<Drawable>? = null)

    open fun createTab(tab: Int): TabDetails {
        return TabDetails("Tab $tab")
    }

    open fun createContentView(table: Table, tab: Int) {

    }

    open fun createSheetView(table: Table) {

    }

    protected fun onClose(listener: (()->Unit)?) {
        onClose = listener
    }

    protected fun addButton(icon: FontIcon, click: ()->Unit): TextButton {
        val type = ButtonType.CircleMedium
        val button = TextButton(icon(), type.style())
        button.onClick { _, _ -> click.invoke(); Sounds.click().play() }
        button.onChange { _, _ -> button.isChecked = false }
        buttonTable
                .add(button)
                .width(type.width * scale)
                .height(type.height * scale)
                .pad(20 * scale, 20 * scale, 0f, 20 * scale)
        return button
    }


    override val priority: Int
        get() = 100


    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun update(delta: Float) {
        stage.act(delta)

        if (visibleTab == 0) {
            tab1.toFront()
            tab2.toBack()
        } else {
            tab2.toFront()
            tab1.toBack()
        }
    }

    override fun render() {
        stage.viewport.apply()
        stage.draw()
    }

    override fun dispose() {
    }

    companion object {
        private val TAB_WIDTH = 165f
        private val TAB_HEIGHT = 43f

        private val SHEET_WIDTH = 233f
        private val SHEET_HEIGHT = 39f

        private val WINDOW_WIDTH = 337f

        private val TITLE_WIDTH = 337f
        private val TITLE_HEIGHT = 75f

        private val CLOSE_BTN_OFFSET = 5f
    }
}
