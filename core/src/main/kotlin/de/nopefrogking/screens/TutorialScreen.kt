package de.nopefrogking.screens

import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import de.nopefrogking.I18N
import de.nopefrogking.Main
import de.nopefrogking.utils.DefaultSkin
import de.nopefrogking.utils.FontIcon
import de.nopefrogking.utils.NamedAsset
import kotlin.properties.Delegates

class TutorialScreen(game: Main, val singlePage: Page? = null, val onCloseCB: (()->Unit)? = null): WindowScreen(game, "Tutorial", FontIcon.Info, false, if (singlePage == null) SheetStyle.Normal else SheetStyle.NoSheet) {
    private val numPages = Page.values().size
    private var currentPage: Int by Delegates.vetoable(0) { _, _, new ->
        when {
            new < 0 || new >= numPages -> false
            else -> { onTabSwitched(new); true }
        }
    }
    private val leftButton: TextButton?
    private val rightButton: TextButton?
    private val okButton: TextButton?
    private val pageNumberDisplay = Label("", DefaultSkin.menu())
    private val pageTitleDisplay = Label("", DefaultSkin.menu())
    override val priority = 0

    init {
        onClose { goBack() }

        if (singlePage == null) {
            isClosable = true
            leftButton = addButton(FontIcon.ArrowLeft) {
                currentPage--
            }
            rightButton = addButton(FontIcon.ArrowRight) {
                currentPage++
            }
            okButton = null
        } else {
            isClosable = false
            leftButton = null
            rightButton = null
            okButton = addButton(FontIcon.Confirm) {
                game.removeScreen(this, true)
            }
        }
    }

    override fun onTabSwitched(tab: Int) {
        when (tab) {
            0 -> {
                leftButton?.isDisabled = true
                rightButton?.isDisabled = false
            }
            numPages-1 -> {
                leftButton?.isDisabled = false
                rightButton?.isDisabled = true
            }
            else -> {
                leftButton?.isDisabled = false
                rightButton?.isDisabled = false
            }
        }

        pageNumberDisplay.setText("${tab+1}/$numPages")
        pageTitleDisplay.setText(Page.values()[tab].title())

        content.clear()
        createContentView(content, tab)
    }

    override fun dispose() {
        onCloseCB?.invoke()
        super.dispose()
    }

    override fun createSheetView(table: Table) {
        table.add(pageTitleDisplay)
                .expand()
                .pad(0.0f, 15*scale, 10*scale, 5*scale)
                .align(Align.left)

        table.add(pageNumberDisplay)
                .pad(0.0f, 0.0f, 10*scale, 5*scale)
    }

    override fun createContentView(table: Table, tab: Int) {
        val page = singlePage ?: Page.values()[tab]
        table.add(ScrollPane(Table(DefaultSkin).apply {
            page.icon?.also {
                add(Image(it, Scaling.fit, Align.center))
                        .width(128*scale)
                        .height(128*scale)
                        .row()
            }
            add(Label(page.text(), DefaultSkin.menu()).apply {
                setWrap(true)
            }).expand().fill()
        }, DefaultSkin.menuScrollPane()).apply {
            setFadeScrollBars(false)
            setScrollbarsOnTop(false)
            variableSizeKnobs = false
        }).expand().fill()
    }

    enum class Page(val title: I18N, val text: I18N, private val iconAsset: NamedAsset<Drawable>?) {
        Princess(I18N.tutorial_1_title, I18N.tutorial_1_text, DefaultSkin.intro_princess),
        Stone(I18N.tutorial_2_title, I18N.tutorial_2_text, DefaultSkin.stone),
        BrokenStone(I18N.tutorial_3_title, I18N.tutorial_3_text, DefaultSkin.stone_broken),
        Prince(I18N.tutorial_4_title, I18N.tutorial_4_text, DefaultSkin.prince),
        Level(I18N.tutorial_5_title, I18N.tutorial_5_text, DefaultSkin.lvlbar_icon),
        ItemFlask(I18N.tutorial_6_title, I18N.tutorial_6_text, DefaultSkin.ui_item_flask),
        ItemOrb(I18N.tutorial_7_title, I18N.tutorial_7_text, DefaultSkin.ui_item_orb),
        ItemStorm(I18N.tutorial_8_title, I18N.tutorial_8_text, DefaultSkin.ui_item_storm),
        ItemUmbrella(I18N.tutorial_9_title, I18N.tutorial_9_text, DefaultSkin.ui_item_umbrella),
        ItemGift(I18N.tutorial_10_title, I18N.tutorial_10_text, DefaultSkin.gift_icon),
        ;

        val icon: Drawable? get() = iconAsset?.asset
    }
}