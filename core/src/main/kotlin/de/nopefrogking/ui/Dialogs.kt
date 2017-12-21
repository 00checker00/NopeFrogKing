package de.nopefrogking.ui

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import de.tomgrill.gdxdialogs.core.GDXDialogs
import de.tomgrill.gdxdialogs.core.GDXDialogsSystem

interface GDXNumberPrompt {
    /**
     * Sets the title
     * @param title String value to set the title
     */
    var title: CharSequence

    /**
     * Shows the dialog. show() can only be called after build() has been called
     * else there might be strange behavior. Runs asynchronously on a different thread.
     */
    fun show(): GDXNumberPrompt

    /**
     * Dismisses the dialog. You can show the dialog again.
     */
    fun dismiss(): GDXNumberPrompt

    /**
     * This builds the button and prepares for usage.
     */
    fun build(): GDXNumberPrompt

    /**
     * Sets the message.
     * @param message The text to be displayed at the body of the dialog.
     */
    var message: CharSequence

    /**
     * Sets the default value for the input field.
     * @param inputTip Default value for the number input.
     */
    var value: Int

    /**
     * Sets the minimum value for the input field.
     * @param inputTip Minimum value for the number input.
     */
    var minValue: Int
    /**
     * Sets the maximum value for the input field.
     * @param inputTip Maximum value for the number input.
     */
    var maxValue: Int

    /**
     * Sets the label for the cancel button on the dialog.
     * @param label Text of the cancel button
     */
    var cancelText: CharSequence

    /**
     * Sets the label for the confirm button on the dialog.
     * @param label Text of the confirm button
     */
    var confirmText: CharSequence

    /**
     * Sets the [TextPromptListene

     * @param listener listener to be called when the event is triggered
     */
    fun setNumberPromptListener(listener: NumberPromptListener): GDXNumberPrompt
}

interface NumberPromptListener {
    fun cancel()
    fun confirm(text: Int)
}

class FallbackGDXNumberPrompt: GDXNumberPrompt {
    override var title: CharSequence = ""
    override var message : CharSequence = ""
    override var value: Int = 0
    override var minValue: Int = 0
    override var maxValue: Int = 0
    override var cancelText: CharSequence = ""
    override var confirmText: CharSequence = ""
    override fun show() = this
    override fun dismiss() = this
    override fun build() = this
    override fun setNumberPromptListener(listener: NumberPromptListener): GDXNumberPrompt = this
}

class NumberPromptAdapter: NumberPromptListener {
    override fun cancel() {}
    override fun confirm(text: Int) {}
}

object Dialogs {
    val dialogs: GDXDialogs by lazy { GDXDialogsSystem.install() }

    fun init() {
        when (Gdx.app.type) {
            Application.ApplicationType.Android -> dialogs.registerDialog("de.nopefrogking.ui.GDXNumberPrompt", "de.nopefrogking.android.GDXNumberPrompt")
            Application.ApplicationType.Desktop -> dialogs.registerDialog("de.nopefrogking.ui.GDXNumberPrompt", "de.nopefrogking.lwjgl3.GDXNumberPrompt")
            Application.ApplicationType.iOS     -> dialogs.registerDialog("de.nopefrogking.ui.GDXNumberPrompt", "de.nopefrogking.ios.GDXNumberPrompt")
            else                                -> dialogs.registerDialog("de.nopefrogking.ui.GDXNumberPrompt", "de.nopefrogking.ui.FallbackGDXNumberPrompt")
        }
    }

    inline fun <reified T> newDialog() = dialogs.newDialog(T::class.java)!!
}
